package uk.ac.aber.dcs.cs22120.g9.rpsrrec.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.ac.aber.dcs.cs22120.g9.rpsrrec.R;

// FIXME: Prototype implementation!
public class VisitListAdapter extends BaseAdapter {

  /** Android context. */
  private final Context context;
  /** DateFormat used to create human readable dates. */
  private final DateFormat dateFormat;

  // TODO: Remove and implement me!
  private static final String[] PLACES = {"Biology Botany Gardens", "Magic of Life Butterfly House"};
  // TODO: Remove and implement me!
  private static final long[] TIMESTAMPS = {1414764000000L, 1413640800000L};

  /**
   * Create a new list adapter displaying a list of places visited.
   *
   * @param context Android context.
   */
  public VisitListAdapter(Context context) {
    this.context = context;
    dateFormat = android.text.format.DateFormat.getLongDateFormat(context.getApplicationContext());
  }

  @Override
  public int getCount() {
    // TODO: Implement me.
    return PLACES.length;
  }

  @Override
  public Pair<String, Long> getItem(int position) {
    // TODO: Implement me.
    return new Pair<String, Long>(PLACES[position], TIMESTAMPS[position]);
  }

  @Override
  public long getItemId(int position) {
    // TODO: Return database IDs?
    return position;
  }

  @Override
  public View getView(int position, View recycleView, ViewGroup parent) {
    Pair<String, Long> item = getItem(position);
    View view = recycleView;

    // Create a new view, if an existing view object could not be recycled.
    if (view == null) {
      view = LayoutInflater.from(context).inflate(R.layout.list_item_visit, parent, false);
    }

    // TODO: Might want to add more details here, such as name and e-mail address of the person who created the visit.
    TextView text1 = (TextView) view.findViewById(R.id.title);
    text1.setText(item.first);

    TextView text2 = (TextView) view.findViewById(R.id.summary);
    text2.setText(dateFormat.format(new Date(item.second)));

    return view;
  }
}
