package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.database.LocalDatabase;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.model.Reserve;

public class RecorderDetailsActivity extends ActionBarActivity implements OnClickListener {

    EditText reserveName;
    EditText userName;
    EditText phoneNumber;
    EditText email;
    EditText osGridReference;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_details);
        //
        reserveName = (EditText) findViewById(R.id.Reserve_Name);
        userName = (EditText) findViewById(R.id.Name);
        phoneNumber = (EditText) findViewById(R.id.Phone);
        email = (EditText) findViewById(R.id.Email);
        osGridReference = (EditText) findViewById(R.id.Reserve_Grid_Reference);
        description = (EditText) findViewById(R.id.Reserve_Description);

        Button nextButton = (Button) findViewById(R.id.nextButton1);
        nextButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent i;

        int id = view.getId();
        if (id == R.id.nextButton1) {
            if (isValidReserveInput()) {
                Reserve reserve = createReserve();
                i = new Intent(getApplicationContext(), SpeciesList.class);
                i.putExtra("RESERVE", reserve);
                startActivity(i);
                finish();
            }
        }
    }

    /** Create a new reserve from data in the form and insert it into the database. */
    private Reserve createReserve() {
        // Create reserve from data in the form.
        Reserve reserve = new Reserve();
        reserve.reserveName = reserveName.getText().toString();
        reserve.userName = userName.getText().toString();
        reserve.phoneNumber = phoneNumber.getText().toString();
        reserve.email = email.getText().toString();
        reserve.gridReference = osGridReference.getText().toString();
        reserve.description = description.getText().toString();

        // Insert reserve into database.
        LocalDatabase database = new LocalDatabase(this);
        reserve.id = database.insertReserve(reserve);
        database.close();

        return reserve;

    }


    /* Validates input and sets error messages if invalid*/
    public boolean isValidReserveInput() {
        boolean validFlag = true;

        if (reserveName.getText().length() < 3) {
            validFlag = false;
            reserveName.setError("Please enter a name or 3 or more characters");
        }
        if (userName.getText().length() < 3) {
            validFlag = false;
            userName.setError("Please enter a name or 3 or more characters");
        }
        if (!phoneNumber.getText().toString().matches("[0-9]{11}")) {
            validFlag = false;
            phoneNumber.setError("Please enter an 11 digit phonenumber");
        }
        if (email.getText().length() < 3 || !email.getText().toString().matches("[\\d\\w._-]+@[\\d\\w]+\\.\\w+(.\\w+)?")) {
            validFlag = false;
            email.setError("Please enter an email, eg user@example.com");
        }
        if (!osGridReference.getText().toString().matches("[A-Z]{2}(\\d\\d)+")) {
            validFlag = false;
            osGridReference.setError("Please enter a valid OS grid reference");
        }

        return validFlag;


    }
}