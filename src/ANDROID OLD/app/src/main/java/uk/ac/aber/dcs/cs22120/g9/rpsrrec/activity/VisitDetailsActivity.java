package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;

public class VisitDetailsActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Inflate layout XML.
    setContentView(R.layout.activity_visit_details);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate menu XML
    getMenuInflater().inflate(R.menu.activity_visit_details, menu);
    return true;
  }
}
