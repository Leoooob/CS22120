package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Recording;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.service.WebsiteUploadService;

public class SpeciesList extends ActionBarActivity {
    private Reserve reserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_list);

        // Get reserve from the Intent.
        reserve = getIntent().getParcelableExtra("RESERVE");

        // Initialise the recording list view.
        ListView recordingList = (ListView) findViewById(R.id.recordingList);
        final RecordingAdapter recordingAdapter = new RecordingAdapter();
        recordingList.setOnItemClickListener(recordingAdapter);
        recordingList.setAdapter(recordingAdapter);

        Button addRecording = (Button) findViewById(R.id.action_addRecording);
        addRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpeciesList.this, SpeciesDetails.class);
                intent.putExtra("RESERVE", reserve);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.species_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_deleteReserve:

                //if delete reserve button is clicked gives confirmation dialog
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to delete this reserve?").setCancelable(false)

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                deleteReserve();
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

            case R.id.action_uploadReserve:
                uploadReserve();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Upload reserve to the database. */
    public void uploadReserve() {
        // Show progress dialog during the upload process.
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.dialog_message_uploadingReserveToWebsite));
        dialog.show();

        // Register broadcast receiver to retrieve status from the website upload service.
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get result code of the website upload.
                int resultCode = intent.getIntExtra("RESULT_CODE", -1);
                if (resultCode == WebsiteUploadService.RESULT_OK) {
                    // Remove reserve and related recordings from local storage.
                    LocalDatabase localDatabase = new LocalDatabase(SpeciesList.this);
                    localDatabase.deleteReserve(reserve);
                    localDatabase.close();
                    // Show upload notification.
                    Toast.makeText(getApplicationContext(), "Reserve uploaded to the website successfully.", Toast.LENGTH_LONG).show();
                    // Close the activity.
                    SpeciesList.this.finish();
                } else {
                    Toast.makeText(SpeciesList.this, "Error uploading data to the website.", Toast.LENGTH_LONG).show();
                }

                // Unregister the broadcast receiver.
                unregisterReceiver(this);
                // Dismiss progress dialog.
                dialog.dismiss();
            }
        }, new IntentFilter(WebsiteUploadService.ACTION_DONE));

        // Start the website upload service in the background.
        Intent serviceIntent = new Intent(this, WebsiteUploadService.class);
        serviceIntent.putExtra("RESERVE", reserve);
        startService(serviceIntent);
    }

    /** Remove currently displayed reserve from the database. */
    private void deleteReserve() {
        // Delete record from database.
        LocalDatabase localDatabase = new LocalDatabase(this);
        localDatabase.deleteReserve(reserve);
        localDatabase.close();
        // Close the activity.
        finish();
    }

    private class RecordingAdapter extends BaseAdapter implements LoaderManager.LoaderCallbacks<List<Recording>>, AdapterView.OnItemClickListener {
        /** ID of the recording loader. */
        private static final int LOADER_ID_RECORDINGS = 0x01;
        /** List of recordings loaded from the database. */
        private List<Recording> recordings;

        public RecordingAdapter() {
            getSupportLoaderManager().initLoader(LOADER_ID_RECORDINGS, null, this);
        }

        @Override
        public int getCount() {
            if (recordings != null) {
                return recordings.size();
            }
            return 0;
        }

        @Override
        public Recording getItem(int i) {
            return recordings.get(i);
        }

        @Override
        public long getItemId(int i) {
            return recordings.get(i).id;
        }

        @Override
        public View getView(int position, View recycledView, ViewGroup container) {
            // Recycle view, if possible.
            View view = recycledView;

            if (view == null) {
                // Inflate a new instance of this view.
                view = LayoutInflater.from(SpeciesList.this).inflate(R.layout.listitem_recording, container, false);
            }

            // Get the recording for the current list item.
            Recording recording = getItem(position);
            // Populate views with content.
            TextView speciesName = (TextView) view.findViewById(R.id.species_name);
            speciesName.setText(recording.species.nameSpecies);
            TextView recordingDate = (TextView) view.findViewById(R.id.recording_date);
            recordingDate.setText(Recording.DATE_FORMAT.format(recording.date));
            TextView recordingAbundance = (TextView) view.findViewById(R.id.recording_abundance);
            recordingAbundance.setText(recording.abundance.toChar());
            TextView recordingComment = (TextView) view.findViewById(R.id.recording_comment);
            recordingComment.setText(recording.comment);

            return view;
        }

        @Override
        public Loader<List<Recording>> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ID_RECORDINGS) {
                // Initialize the database loader.
                return new LocalDatabase.RecordingLoader(SpeciesList.this, SpeciesList.this.reserve);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Recording>> loader, List<Recording> data) {
            this.recordings = data;
            notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<Recording>> loader) {
            this.recordings = null;
            notifyDataSetInvalidated();
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(SpeciesList.this, SpeciesDetails.class);
            intent.putExtra("RESERVE", reserve);
            intent.putExtra("RECORDING", getItem(i));
            startActivity(intent);
        }
    }
}
