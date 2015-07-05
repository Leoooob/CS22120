package uk.ac.aber.dcs.cs22120.g9.rpsrrec.test;

import android.content.Intent;
import android.provider.MediaStore;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;
import android.util.Log;
import java.util.Locale;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity.SpeciesDetails;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Recording;

public class SpeciesDetailsTest extends ActivityUnitTestCase<SpeciesDetails> {
    SpeciesDetails activity;
    private Recording recording;
    private static final String TAG = "GPS Test";


    public SpeciesDetailsTest() {
        super(SpeciesDetails.class);
    }


    @MediumTest
    public void testGps(){
    recording = new Recording();
        double lat = 52;
        double lng = 43;
        recording.locationLat  = lat;
        recording.locationLon = lng;
        assertEquals(lat, recording.locationLat);
        assertEquals(lng, recording.locationLon);

    }

}
