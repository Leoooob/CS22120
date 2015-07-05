package uk.ac.aber.dcs.cs22120.g9.rpsrrec.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;
import uk.ac.aber.dcs.cs22120.g9.rpsrrec.adapter.VisitListAdapter;

/**
 * When the RPSRrec software starts on an Android device, the user will be given the opportunity to start a new
 * visit recording. The user will then be prompted for details (see FR2) and species recording may the start. When
 * recording is complete, it should be possible to record at a different site.
 */
public class VisitListActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set activity title.
    setTitle(getString(R.string.activity_visit_list));

    // Inflate layout XML.
    setContentView(R.layout.activity_visit_list);

    // Set up the list adapter.
    ListView list = (ListView) findViewById(R.id.list);
    list.setAdapter(new VisitListAdapter(this));
    list.setOnItemClickListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate menu XML.
    getMenuInflater().inflate(R.menu.activity_visit_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // Action bar back button.
        onBackPressed();
        return true;
      case R.id.action_new:
        // Open VisitDetailsActivity.
        startActivity(new Intent(this, VisitDetailsActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    // Open VisitDetailsActivity.
    startActivity(new Intent(this, VisitDetailsActivity.class));
  }
}
