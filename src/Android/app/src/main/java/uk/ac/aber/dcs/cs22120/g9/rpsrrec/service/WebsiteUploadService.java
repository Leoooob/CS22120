package uk.ac.aber.dcs.cs22120.g9.rpsrrec.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Recording;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;

public class WebsiteUploadService extends IntentService {
    /** Status code returned when the upload completes successfully. */
    public static final int RESULT_OK = 0x00;
    /** Status code returned when the upload fails. */
    public static final int RESULT_FAIL = 0x01;
    /** Action ID of the broadcast sent when the detection has completed. */
    public static final String ACTION_DONE = "uk.ac.aber.dcs.cs22120.g9.rpsrrec.UPLOAD_DONE";
    /** Time to wait for the HTTP requests to complete. */
    private static final int REQUEST_TIMEOUT = 30;
    /** Base URL of the website. */
    private static final String WEBSITE_URL_BASE = "http://users.aber.ac.uk/pus1/cs22120";
    /** Endpoint used to add reserves to the database. */
    private static final String WEBSITE_RESERVE_ENDPOINT = "/api_reserve.php";
    /** Endpoint used to add recordings to the database. */
    private static final String WEBSITE_RECORDING_ENDPOINT = "/api_recording.php";
    /** JPEG media type. */
    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
    /** HTTP client. */
    private final OkHttpClient client = new OkHttpClient();

    /** Sets service name, used for debugging. */
    public WebsiteUploadService() {
        super("uk.ac.aber.dcs.cs22120.g9.rpsrrec.WebsiteUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get reserve from Intent.
        Reserve reserve = intent.getParcelableExtra("RESERVE");
        // Create status broadcast Intent.
        Intent broadcastIntent = new Intent(ACTION_DONE);


        // Set HTTP timeouts.
        client.setConnectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        client.setReadTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        // Try uploading reserve to the website.
        long reserveID;
        try {
            reserveID = uploadReserve(reserve);
        } catch (IOException e) {
            // Upload error.
            sendBroadcast(broadcastIntent.putExtra("RESULT_CODE", RESULT_FAIL));
            return;
        }

        // FIXME: Uncomment this.

        // Retrieve recordings for given reserve from the database.
        LocalDatabase localDatabase = new LocalDatabase(this);
        List<Recording> recordings = localDatabase.getAllRecordingsForReserve(reserve);
        localDatabase.close();

        // Upload each recording to the website.
        for (Recording recording : recordings) {
            try {
                uploadRecording(recording, reserveID);
            } catch (IOException e) {
                // Upload error.
                sendBroadcast(broadcastIntent.putExtra("RESULT_CODE", RESULT_FAIL));
                return;
            }
        }


        // Upload completed successfully, send the status broadcast to SpeciesList activity.
        sendBroadcast(broadcastIntent.putExtra("RESULT_CODE", RESULT_OK));
    }

    /**
     * Upload reserve data to the website.
     *
     * @param reserve Reserve to upload
     * @return Database ID of the newly created reserve.
     */
    private long uploadReserve(Reserve reserve) throws IOException {
        // Build request body.
        RequestBody requestBody = new FormEncodingBuilder()
                .add("reserve_name", reserve.reserveName)
                .add("user_name", reserve.userName)
                .add("phone_number", reserve.phoneNumber)
                .add("email", reserve.email)
                .add("grid_reference", reserve.gridReference)
                .add("description", reserve.description)
                .build();

        // Build the request.
        Request request = new Request.Builder()
                .url(WEBSITE_URL_BASE + WEBSITE_RESERVE_ENDPOINT)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("HTTP request failed.");

        return (Long.valueOf(response.body().string()));
    }

    private void uploadRecording(Recording recording, long reserveId) throws IOException {
        // Build request body.
        RequestBody requestBody = new FormEncodingBuilder()
                .add("reserve_id", String.valueOf(reserveId))
                .add("species_name", recording.species.nameSpecies)
                .add("species_common_name", recording.species.nameCommon)
                .add("species_authority", recording.species.authority)
                .add("location_lat", String.format(Locale.US, "%.7f", recording.locationLat))
                .add("location_lon", String.format(Locale.US, "%.7f", recording.locationLon))
                .add("abundance", recording.abundance.toChar())
                .add("date", String.valueOf(recording.date.getTime()/1000))
                .add("comment", recording.comment)
                .build();

        // Build the request.
        Request request = new Request.Builder()
                .url(WEBSITE_URL_BASE + WEBSITE_RECORDING_ENDPOINT)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("HTTP request failed.");

        // TODO: Remove this
        String responseBody = response.body().string();
        Log.w("WebsiteUploadService", responseBody);
    }

    /**
     * Read an image file into an URI.
     *
     * @param uri Content URI to the image.
     * @return Byte array
     */
    private byte[] readImageIntoByteArray(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        is.close();
        return bytes;
    }


    /**
     * Make a RequestBody that will stream an image to the server without eating a lot of memory.
     * @param uri Content URI to the image.
     * @return RequestBody to use in the Multipart Request.
     */
    private RequestBody imageURIToRequestBody(final Uri uri) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(getContentResolver().getType(uri));
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                InputStream is = getContentResolver().openInputStream(uri);
                // Pipe InputStream into OutputStream.
                int size;
                byte[] buffer = new byte[1024];

                while ((size = is.read(buffer)) > -1) {
                    sink.write(buffer, 0, size);
                }
                is.close();
            }
        };
    }
}
