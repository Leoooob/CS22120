package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;

public class MainActivity extends ActionBarActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_reserves);

        // Enable location updates.
        isGPSOn();
        enableLocationUpdates();

        // Initialise the reserve list view.
        ListView reserveList = (ListView) findViewById(R.id.reserveList);
        final ReserveAdapter reserveAdapter = new ReserveAdapter();
        reserveList.setOnItemClickListener(reserveAdapter);
        reserveList.setAdapter(reserveAdapter);

        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent i;

        int id = view.getId();
        if (id == R.id.createButton) {
            i = new Intent(getApplicationContext(), RecorderDetailsActivity.class);
            startActivity(i);
        }
    }

    public void isGPSOn() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enable high accuracy (GPS) location service").setCancelable(false)
                    .setPositiveButton("Location settings", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void enableLocationUpdates() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private class ReserveAdapter extends BaseAdapter implements LoaderManager.LoaderCallbacks<List<Reserve>>, AdapterView.OnItemClickListener {
        /** Database loader ID. */
        private static final int LOADER_ID_RESERVES = 0x00;
        /** List of reserves loaded from the database. */
        private List<Reserve> reserves;

        public ReserveAdapter() {
            getSupportLoaderManager().initLoader(LOADER_ID_RESERVES, null, this);
        }

        @Override
        public int getCount() {
            if (reserves != null) {
                return reserves.size();
            }
            return 0;
        }

        @Override
        public Reserve getItem(int i) {
            return reserves.get(i);
        }

        @Override
        public long getItemId(int i) {
            return reserves.get(i).id;
        }

        @Override
        public View getView(int position, View recycledView, ViewGroup container) {
            // Recycle the view, if possible.
            View view = recycledView;

            if (view == null) {
                // Create a new instance of the view.
                view = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, container, false);
            }

            // Get the data for the current list item.
            Reserve reserve = getItem(position);
            // Populate views with content.
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            text1.setText(reserve.reserveName);

            return view;
        }

        @Override
        public Loader<List<Reserve>> onCreateLoader(int id, Bundle bundle) {
            if (id == LOADER_ID_RESERVES) {
                // Initialize the database loader.
                return new LocalDatabase.ReserveLoader(MainActivity.this);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Reserve>> listLoader, List<Reserve> reserves) {
            this.reserves = reserves;
            notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<Reserve>> listLoader) {
            this.reserves = null;
            notifyDataSetInvalidated();
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(MainActivity.this, SpeciesList.class);
            intent.putExtra("RESERVE", getItem(i));
            startActivity(intent);
        }
    }
}
