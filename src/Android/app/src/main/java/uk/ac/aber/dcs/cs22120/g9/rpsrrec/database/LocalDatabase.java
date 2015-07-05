package uk.ac.aber.dcs.cs22120.g9.rpsrrec.database;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Recording;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Species;


/** Local recording database. */
public class LocalDatabase extends SQLiteOpenHelper {
    /** Database update notification action. */
    public static final String BROADCAST_UPDATE = "uk.ac.aber.dcs.cs22120.g9.rpsrrec.DATABASE_UPDATED";
    /** Filename of the underlying SQLite database. */
    private static final String DATABASE_NAME = "local_database.db";
    /** Database schema version. */
    private static final int SCHEMA_VERSION = 1;
    /** Reserve table name. */
    private static final String RESERVE_TABLE = "reserves";
    /** Species table name. */
    private static final String SPECIES_TABLE = "species";
    /** Recordings table name. */
    private static final String RECORDINGS_TABLE = "recordings";
    /** Android context. */
    private final Context context;

    public LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        this.context = context;
    }

    /**
     * Insert a new reserve into the database.
     *
     * @param reserve Reserve to insert.
     * @return Database ID of the newly created reserve.
     */
    public long insertReserve(Reserve reserve) {
        // Insert data into the database.
        SQLiteDatabase db = getWritableDatabase();

        // Convert reserve into ContentValues.
        ContentValues contentValues = new ContentValues();
        contentValues.put("reserve_name", reserve.reserveName);
        contentValues.put("user_name", reserve.userName);
        contentValues.put("phone_number", reserve.phoneNumber);
        contentValues.put("email", reserve.email);
        contentValues.put("grid_reference", reserve.gridReference);
        contentValues.put("description", reserve.description);

        // Insert ContentValues into the database.
        final long id = db.insert(RESERVE_TABLE, null, contentValues);
        db.close();
        sendUpdateNotification();
        return id;
    }

    /** Delete given reserve from the database. */
    public void deleteReserve(Reserve reserve) {
        // Get writable instance of the database.
        SQLiteDatabase db = getWritableDatabase();
        // Remove record from the database.
        db.delete(RESERVE_TABLE, "id = ?", new String[]{String.valueOf(reserve.id)});
        db.close();
        sendUpdateNotification();
    }


    /**
     * Insert species into the database, if it doesn't already exist.
     * Return the ID of the newly created or existing species.
     */
    public long upsertSpecies(Species species) {
        // Query the database for existing species.
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(SPECIES_TABLE, new String[]{"id"}, "species_name = ?", new String[]{species.nameSpecies}, null, null, null, "1");

        if (c.getCount() > 0) {
            c.moveToNext();
            long id = c.getLong(c.getColumnIndex("id"));

            // Update existing species.
            ContentValues contentValues = new ContentValues();
            contentValues.put("common_name", species.nameCommon);
            contentValues.put("species_name", species.nameSpecies);
            contentValues.put("authority", species.authority);
            db.update(SPECIES_TABLE, contentValues, "id = ?", new String[]{String.valueOf(id)});

            c.close();
            db.close();
            return id;
        } else {
            // Insert the species into the database.
            ContentValues contentValues = new ContentValues();
            contentValues.put("common_name", species.nameCommon);
            contentValues.put("species_name", species.nameSpecies);
            contentValues.put("authority", species.authority);

            long id = db.insert(SPECIES_TABLE, null, contentValues);
            c.close();
            db.close();
            sendUpdateNotification();
            return id;
        }
    }

    /** Get array of Species names from the database. */
    public String[] getAllSpeciesNames() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(SPECIES_TABLE, new String[]{"species_name"}, null, null, null, null, "id");

        List<String> names = new ArrayList<String>();
        while (c.moveToNext()) {
            names.add(c.getString(c.getColumnIndex("species_name")));
        }

        c.close();
        db.close();

        return names.toArray(new String[names.size()]);
    }

    /** Get a Species with the given name from the database. */
    public Species getSpeciesWithName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(SPECIES_TABLE, null, "species_name = ?", new String[]{name}, null, null, null, "1");

        Species species = null;
        if (c.moveToNext()) {
            species = cursorToSpecies(c);
        }

        c.close();
        db.close();
        return species;
    }

    /** Insert a recording into the database and return its ID. */
    public long insertRecording(Recording recording) {
        // Insert a new recording into the database.
        SQLiteDatabase db = getWritableDatabase();

        // Convert recording into ContentValues.
        ContentValues contentValues = new ContentValues();
        contentValues.put("reserve_id", recording.reserve.id);
        contentValues.put("species_id", recording.species.id);
        contentValues.put("location_lat", recording.locationLat);
        contentValues.put("location_lon", recording.locationLon);
        contentValues.put("abundance", recording.abundance.toChar());
        contentValues.put("date", recording.date.getTime());
        contentValues.put("comment", recording.comment);
        contentValues.put("general_photo_url", recording.generalPhotoUrl);
        contentValues.put("specimen_photo_url", recording.specimenPhotoUrl);

        // Insert the recording into the database.
        final long id = db.insert(RECORDINGS_TABLE, null, contentValues);
        db.close();
        sendUpdateNotification();
        return id;
    }

    /** Update an existing recording. */
    public void updateRecording(Recording recording) {
        SQLiteDatabase db = getWritableDatabase();

        // Convert recording into ContentValues.
        ContentValues contentValues = new ContentValues();
        contentValues.put("species_id", recording.species.id);
        contentValues.put("abundance", recording.abundance.toChar());
        contentValues.put("comment", recording.comment);
        contentValues.put("general_photo_url", recording.generalPhotoUrl);
        contentValues.put("specimen_photo_url", recording.specimenPhotoUrl);

        // Update the database record.
        db.update(RECORDINGS_TABLE, contentValues, "id = ?", new String[]{String.valueOf(recording.id)});
        db.close();
        sendUpdateNotification();
    }

    /** Delete a recording from the database. */
    public void deleteRecording(Recording recording) {
        SQLiteDatabase db = getWritableDatabase();
        // Remove record from the database.
        db.delete(RECORDINGS_TABLE, "id = ?", new String[]{String.valueOf(recording.id)});
        db.close();
        sendUpdateNotification();
    }

    /**
     * Get all recordings for the given reserve.
     */
    public List<Recording> getAllRecordingsForReserve(Reserve reserve) {
        // Query the database for recordings.
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(RECORDINGS_TABLE, null, "reserve_id = ?", new String[]{String.valueOf(reserve.id)}, null, null, "id");

        final List<Recording> recordings = new ArrayList<Recording>();
        while (c.moveToNext()) {
            recordings.add(cursorToRecording(c, reserve));
        }

        c.close();
        db.close();

        return recordings;
    }

    /**
     * Get species object with the given ID.
     */
    public Species getSpeciesWithId(long id) {
        // Query the database for a specie.
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(SPECIES_TABLE, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null, "1");

        Species species = null;
        if (c.moveToNext()) {
            species = cursorToSpecies(c);
        }

        c.close();
        db.close();

        return species;
    }

    /** Get a list of all reserves from the database. */
    public List<Reserve> getAllReserves() {
        // Query the database.
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(RESERVE_TABLE, null, null, null, null, null, "id");

        // Convert database cursor to list.
        final List<Reserve> reserves = new ArrayList<Reserve>();
        while (c.moveToNext()) {
            reserves.add(cursorToReserve(c));
        }

        c.close();
        db.close();

        return reserves;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create reserve table.
        db.execSQL(String.format(Locale.US, "CREATE TABLE %s (id INTEGER PRIMARY KEY AUTOINCREMENT, reserve_name TEXT NOT NULL, user_name TEXT NOT NULL, phone_number TEXT NOT NULL, email TEXT NOT NULL, grid_reference TEXT NOT NULL, description TEXT);", RESERVE_TABLE));
        // Create species table.
        db.execSQL(String.format(Locale.US, "CREATE TABLE %s (id INTEGER PRIMARY KEY AUTOINCREMENT, species_name TEXT NOT NULL, common_name TEXT, authority TEXT);", SPECIES_TABLE));
        // TODO: Populate species table.
        populateSpecies(db);
        // Create recordings table.
        db.execSQL(String.format(Locale.US, "CREATE TABLE %s (id INTEGER PRIMARY KEY AUTOINCREMENT, reserve_id INTEGER NOT NULL REFERENCES reserves(id) ON DELETE CASCADE, species_id INTEGER NOT NULL REFERENCES species(id) ON DELETE CASCADE, location_lat REAL NOT NULL, location_lon REAL NOT NULL, abundance TEXT NOT NULL, date INTEGER NOT NULL, comment TEXT, general_photo_url TEXT, specimen_photo_url TEXT);", RECORDINGS_TABLE));
    }

    /** Populate the species table with BSBIList2007 data. */
    private void populateSpecies(SQLiteDatabase db) {
        try {
            // Pre-populate the database with species names from the BSBIList2007 data set.
            BufferedReader in = new BufferedReader(new InputStreamReader(context.getAssets().open("BSBIList2007.csv")));
            String line;

            while ((line = in.readLine()) != null) {
                // Parse data from file.
                String[] s = line.split(";", -1);
                ContentValues contentValues = new ContentValues();
                contentValues.put("species_name", s[0]);
                contentValues.put("authority", s[1]);
                contentValues.put("common_name", s[2]);
                // Insert species into the database.
                db.insert(SPECIES_TABLE, null, contentValues);
            }
            // Close the file.
            in.close();
        } catch (IOException ignored) {
            // Too bad.
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing.
    }

    /** Convert a cursor item into a Species. */
    private Species cursorToSpecies(Cursor c) {
        Species species = new Species();
        species.id = c.getLong(c.getColumnIndex("id"));
        species.nameCommon = c.getString(c.getColumnIndex("common_name"));
        species.nameSpecies = c.getString(c.getColumnIndex("species_name"));
        species.authority = c.getString(c.getColumnIndex("authority"));
        return species;
    }

    /** Convert a cursor item into a Recording. */
    private Recording cursorToRecording(Cursor c, Reserve reserve) {
        Recording recording = new Recording();
        recording.id = c.getLong(c.getColumnIndex("id"));
        recording.reserve = reserve;
        recording.species = getSpeciesWithId(c.getLong(c.getColumnIndex("species_id")));
        recording.locationLat = c.getDouble(c.getColumnIndex("location_lat"));
        recording.locationLon = c.getDouble(c.getColumnIndex("location_lon"));
        recording.abundance = Recording.Abundance.fromChar(c.getString(c.getColumnIndex("abundance")));
        recording.date = new Date(c.getLong(c.getColumnIndex("date")));
        recording.comment = c.getString(c.getColumnIndex("comment"));
        recording.generalPhotoUrl = c.getString(c.getColumnIndex("general_photo_url"));
        recording.specimenPhotoUrl = c.getString(c.getColumnIndex("specimen_photo_url"));
        return recording;
    }

    /** Convert a cursor item into a Reserve. */
    private Reserve cursorToReserve(Cursor c) {
        Reserve reserve = new Reserve();
        reserve.id = c.getLong(c.getColumnIndex("id"));
        reserve.reserveName = c.getString(c.getColumnIndex("reserve_name"));
        reserve.userName = c.getString(c.getColumnIndex("user_name"));
        reserve.phoneNumber = c.getString(c.getColumnIndex("phone_number"));
        reserve.email = c.getString(c.getColumnIndex("email"));
        reserve.gridReference = c.getString(c.getColumnIndex("grid_reference"));
        reserve.description = c.getString(c.getColumnIndex("description"));
        return reserve;
    }

    /** Notify observers that the data in the database has changed. */
    private void sendUpdateNotification() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_UPDATE));
    }

    public static class ReserveLoader extends AsyncTaskLoader<List<Reserve>> {
        /** Database access helper. */
        private final LocalDatabase db;
        /** Cached result. */
        private List<Reserve> reserves;
        /** Broadcast receiver receiving change notifications. */
        private final BroadcastReceiver databaseChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ReserveLoader.this.onContentChanged();
            }
        };

        public ReserveLoader(Context context) {
            super(context);
            // Create database instance.
            this.db = new LocalDatabase(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            // If there is a cached result available, deliver it immediately.
            if (reserves != null) {
                deliverResult(reserves);
            }

            // Register broadcast receiver handling database change notifications.
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(databaseChangedBroadcastReceiver,
                    new IntentFilter(LocalDatabase.BROADCAST_UPDATE));

            if (takeContentChanged() || reserves == null) {
                forceLoad();
            }
        }

        @Override
        protected void onReset() {
            super.onReset();
            // Unregister broadcast receiver handling database change notifications.
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(databaseChangedBroadcastReceiver);
        }

        @Override
        public List<Reserve> loadInBackground() {
            return db.getAllReserves();
        }
    }

    /** Asynchronously load all recordings for given reserve in the background. */
    public static class RecordingLoader extends AsyncTaskLoader<List<Recording>> {
        /** Database access helper. */
        private final LocalDatabase db;
        /** Reserve to load recordings from. */
        private final Reserve reserve;
        /** Cached result. */
        private List<Recording> recordings;
        /** Broadcast receiver receiving change notifications. */
        private final BroadcastReceiver databaseChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RecordingLoader.this.onContentChanged();
            }
        };

        public RecordingLoader(Context context, Reserve reserve) {
            super(context);
            // Create database instance.
            this.db = new LocalDatabase(context);
            this.reserve = reserve;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            // If there is a cached result available, deliver it immediately.
            if (recordings != null) {
                deliverResult(recordings);
            }

            // Register broadcast receiver handling database change notifications.
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(databaseChangedBroadcastReceiver,
                    new IntentFilter(LocalDatabase.BROADCAST_UPDATE));

            if (takeContentChanged() || recordings == null) {
                forceLoad();
            }
        }

        @Override
        protected void onReset() {
            super.onReset();
            // Unregister broadcast receiver handling database change notifications.
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(databaseChangedBroadcastReceiver);
        }

        @Override
        public List<Recording> loadInBackground() {
            return db.getAllRecordingsForReserve(reserve);
        }
    }
}
