package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Recording;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Species;

public class SpeciesDetails extends ActionBarActivity {
    private static final int OPEN_GALLERY_GENERAL = 0x00;
    private static final int OPEN_GALLERY_SPECIMEN = 0x01;

    private Reserve reserve;
    private Recording recording;

    private AutoCompleteTextView speciesLatinName;
    private EditText speciesCommonName;
    private EditText speciesAuthority;
    private Spinner recordingAbundance;
    private TextView recordingLatitude;
    private TextView recordingLongitude;
    private TextView recordingDate;
    private EditText recordingComment;
    private ImageButton takeGeneralPhotoButton;
    private ImageButton deleteGeneralPhotoButton;
    private ImageButton takeSpecimenPhotoButton;
    private ImageButton deleteSpecimenPhotoButton;
    private ImageButton launchCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_details);

        // Get views from XML
        speciesLatinName = (AutoCompleteTextView) findViewById(R.id.species_latinName);
        speciesCommonName = (EditText) findViewById(R.id.species_commonName);
        speciesAuthority = (EditText) findViewById(R.id.species_authority);
        recordingAbundance = (Spinner) findViewById(R.id.recording_abundance);
        recordingLatitude = (TextView) findViewById(R.id.recording_locationLatitude);
        recordingLongitude = (TextView) findViewById(R.id.recording_locationLongitude);
        recordingDate = (TextView) findViewById(R.id.recording_date);
        recordingComment = (EditText) findViewById(R.id.recording_comment);
        takeGeneralPhotoButton = (ImageButton) findViewById(R.id.action_takeGeneralPhoto);
        takeGeneralPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), OPEN_GALLERY_GENERAL);
            }
        });
        deleteGeneralPhotoButton = (ImageButton) findViewById(R.id.action_deleteGeneralPhoto);
        deleteGeneralPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording.generalPhotoUrl = null;
                takeGeneralPhotoButton.setImageResource(R.drawable.ic_camera_roll_black_48dp);
            }
        });
        takeSpecimenPhotoButton = (ImageButton) findViewById(R.id.action_takeSpecimenPhoto);
        takeSpecimenPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), OPEN_GALLERY_SPECIMEN);
            }
        });
        deleteSpecimenPhotoButton = (ImageButton) findViewById(R.id.action_deleteSpecimenPhoto);
        deleteSpecimenPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording.specimenPhotoUrl = null;
                takeSpecimenPhotoButton.setImageResource(R.drawable.ic_camera_roll_black_48dp);
            }
        });
        launchCameraButton = (ImageButton) findViewById(R.id.action_launchCamera);
        launchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
            }
        });

        // Set up Abundance dropdown.
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.DAFOR_Array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        recordingAbundance.setAdapter(spinnerAdapter);

        // Set up Species common name suggestions.
        speciesLatinName.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_dropdown_item, getSpeciesNames()));
        speciesLatinName.setThreshold(1);
        speciesLatinName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get selected species name from adapter.
                String name = (String) adapterView.getItemAtPosition(i);
                // Get species from the database.
                LocalDatabase localDatabase = new LocalDatabase(SpeciesDetails.this);
                Species species = localDatabase.getSpeciesWithName(name);
                localDatabase.close();

                if (species != null) {
                    // Fill out rest of the species information.
                    speciesCommonName = (EditText) findViewById(R.id.species_commonName);
                    speciesCommonName.setText(species.nameCommon);
                    speciesAuthority.setText(species.authority);
                }
            }
        });

        // Get reserve from the Intent.
        Intent intent = getIntent();
        reserve = intent.getParcelableExtra("RESERVE");

        // Initialise a new recording, if not editing an existing one.
        if (intent.hasExtra("RECORDING")) {
            editExistingRecording();
        } else {
            initializeNewRecording();
        }


        // Set up the Save button.
        Button saveButton = (Button) findViewById(R.id.action_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidSpecies()) {
                    reserveFromFormData();
                    // Insert the recording into the database.
                    LocalDatabase localDatabase = new LocalDatabase(SpeciesDetails.this);
                    long speciesId = localDatabase.upsertSpecies(recording.species);
                    recording.species.id = speciesId;
                    if (!getIntent().hasExtra("RECORDING")) { // Insert a new recording.
                        localDatabase.insertRecording(recording);
                    } else { // Update an existing recording.
                        localDatabase.updateRecording(recording);
                    }
                    SpeciesDetails.this.finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.species_details, menu);

        if (!getIntent().hasExtra("RECORDING")) {
            // Creating a new recording, don't show the delete button as it doesn't exist in the database yet.
            menu.findItem(R.id.action_deleteRecording).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_deleteRecording:

                //if delete reserve button is clicked gives confirmation dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to delete this recording?").setCancelable(false)

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                deleteRecording();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case OPEN_GALLERY_GENERAL:
                    takeGeneralPhotoButton.setImageBitmap(resizeBitmap(takeGeneralPhotoButton.getWidth(),takeGeneralPhotoButton.getHeight(),data.getData()));
                    recording.generalPhotoUrl = data.getData().toString();
                    break;
                case OPEN_GALLERY_SPECIMEN:
                    takeSpecimenPhotoButton.setImageBitmap(resizeBitmap(takeSpecimenPhotoButton.getWidth(),takeSpecimenPhotoButton.getHeight(),data.getData()));
                    recording.specimenPhotoUrl = data.getData().toString();
                    break;
            }

        }
    }

    private void deleteRecording() {
        LocalDatabase localDatabase = new LocalDatabase(this);
        localDatabase.deleteRecording(recording);
        localDatabase.close();
        // Close the activity.
        finish();
    }

    private void initializeNewRecording() {
        // Create a new recording instance.
        recording = new Recording();
        recording.reserve = reserve;
        recording.species = new Species();
        // Set recording date.
        recording.date = new Date();
        updateDateDisplay();

        Location location = getGPSLocation();
        if (location != null) {
            recording.locationLat = location.getLatitude();
            recording.locationLon = location.getLongitude();
        } else {
            Toast.makeText(this, "Could not retrieve GPS location.", Toast.LENGTH_LONG).show();
            recording.locationLat = 0.00;
            recording.locationLon = 0.00;
        }
        recordingLatitude.setText(String.format(Locale.UK, "%.7f", recording.locationLat));
        recordingLongitude.setText(String.format(Locale.UK, "%.7f", recording.locationLon));
    }

    /**
     * Update the reserve and species with data from the form.
     */
    private void reserveFromFormData() {
        recording.species.nameCommon = speciesCommonName.getText().toString();
        recording.species.nameSpecies = speciesLatinName.getText().toString();
        recording.species.authority = speciesAuthority.getText().toString();

        switch (recordingAbundance.getSelectedItemPosition()) {
            case 0:
                recording.abundance = Recording.Abundance.DOMINANT;
                break;
            case 1:
                recording.abundance = Recording.Abundance.ABUNDANT;
                break;
            case 2:
                recording.abundance = Recording.Abundance.FREQUENT;
                break;
            case 3:
                recording.abundance = Recording.Abundance.OCCASIONAL;
                break;
            case 4:
                recording.abundance = Recording.Abundance.RARE;
                break;
        }

        recording.comment = recordingComment.getText().toString();
    }

    //validates species name, if invalid, provides error text
    private boolean isValidSpecies() {
        boolean isValid = true;

        if (speciesLatinName.getText().length() < 3) {
            isValid = false;
            speciesLatinName.setError("Please enter a species name at least 3 characters long.");
        }

        return isValid;
    }

    //returns the current gps location
    private Location getGPSLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void editExistingRecording() {
        recording = getIntent().getParcelableExtra("RECORDING");
        // Populate fields with existing data.

        // Species latin name.
        speciesLatinName.setText(recording.species.nameSpecies);
        // Species common name.
        speciesCommonName.setText(recording.species.nameCommon);
        // Species authority.
        speciesAuthority.setText(recording.species.authority);
        // Location latitude.
        recordingLatitude.setText(String.format(Locale.UK, "%.7f", recording.locationLat));
        // Location longitude.
        recordingLongitude.setText(String.format(Locale.UK, "%.7f", recording.locationLon));
        // Abundance.
        switch (recording.abundance) {
            case DOMINANT:
                recordingAbundance.setSelection(0);
                break;
            case ABUNDANT:
                recordingAbundance.setSelection(1);
                break;
            case FREQUENT:
                recordingAbundance.setSelection(2);
                break;
            case OCCASIONAL:
                recordingAbundance.setSelection(3);
                break;
            case RARE:
                recordingAbundance.setSelection(4);
                break;
        }
        // Date.
        updateDateDisplay();
        // Comment.
        recordingComment.setText(recording.comment);

        //if a general photoURL is present, displays a scaled thumbnail of the asociated image
        if (recording.generalPhotoUrl != null && !TextUtils.isEmpty(recording.generalPhotoUrl)) {
            Uri uri = Uri.parse(recording.generalPhotoUrl);
            takeGeneralPhotoButton.setImageBitmap(resizeBitmap(takeGeneralPhotoButton.getWidth(),takeGeneralPhotoButton.getHeight(),uri));

        }
        //if a specimen photoURL is present, displays a scaled thumbnail of the asociated image
        if (recording.specimenPhotoUrl != null && !TextUtils.isEmpty(recording.specimenPhotoUrl)) {
            Uri uri = Uri.parse(recording.specimenPhotoUrl);
            takeSpecimenPhotoButton.setImageBitmap(resizeBitmap(takeSpecimenPhotoButton.getWidth(),takeSpecimenPhotoButton.getHeight(),uri));

        }
    }

    private void updateDateDisplay() {
        recordingDate.setText(Recording.DATE_FORMAT.format(recording.date));
    }

    private String[] getSpeciesNames() {
        LocalDatabase localDatabase = new LocalDatabase(this);
        String[] names = localDatabase.getAllSpeciesNames();
        localDatabase.close();
        return names;
    }


    //takes a uri and returns a bitmap scaled to the target dimensions
    public Bitmap resizeBitmap(int targetW, int targetH, Uri uri) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        String photoPath = getRealPathFromURI(uri);
        BitmapFactory.decodeFile(photoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }


    //converts a URI to a path to a file on the sdcard
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

}
