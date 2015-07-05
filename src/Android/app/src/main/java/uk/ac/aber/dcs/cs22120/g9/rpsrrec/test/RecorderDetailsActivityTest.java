package uk.ac.aber.dcs.cs22120.g9.rpsrrec.test;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity.MainActivity;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity.RecorderDetailsActivity;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;

import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.content.Context;

public class RecorderDetailsActivityTest extends ActivityUnitTestCase<RecorderDetailsActivity>{

    RecorderDetailsActivity Activity;

    public RecorderDetailsActivityTest() {
        super(RecorderDetailsActivity.class);
    }

    void testCreateReserve(){
        Reserve reserve = new Reserve();
        reserve.reserveName = "jack";
        reserve.userName = "1234";
        reserve.phoneNumber = "12345678910";
        reserve.email = "d@a.com";
        reserve.gridReference = "SW123456";
        reserve.description = "gjgjg";

        // Insert reserve into database.
        //this.context = context;
        LocalDatabase database = new LocalDatabase(getActivity());
        reserve.id = database.insertReserve(reserve);
        SQLiteDatabase db = database.getReadableDatabase();
        database.close();


        assertEquals(reserve.reserveName, "jack");




    }

}
