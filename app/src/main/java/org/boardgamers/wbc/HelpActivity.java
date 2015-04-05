package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends Activity {

  private final String TAG="Help Activity";
  private final View.OnClickListener headerListener=new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      selectHeader(v.getId());
    }
  };
  private TextView[] aboutTexts;
  private TextView[] aboutHeaders;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    // enable home button for navigation drawer
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    String[] headerStrings=getResources().getStringArray(R.array.help_headers);
    String[] textStrings=getResources().getStringArray(R.array.help_texts);

    LinearLayout layout=(LinearLayout) findViewById(R.id.help_layout);
    layout.removeAllViews();

    int count=headerStrings.length;
    aboutHeaders=new TextView[count];
    aboutTexts=new TextView[count];
    int headerPadding=(int) getResources().getDimension(R.dimen.text_margin_small);
    int textPadding=(int) getResources().getDimension(R.dimen.default_margin);
    int bottomPadding=(int) getResources().getDimension(R.dimen.text_margin_large);
    TextView tempTV;
    for (int i=0; i<count; i++) {
      tempTV=new TextView(this);
      tempTV.setId(i);
      tempTV.setText(headerStrings[i]);
      tempTV.setOnClickListener(headerListener);
      tempTV.setGravity(Gravity.CENTER);
      tempTV.setPadding(headerPadding, headerPadding, headerPadding, headerPadding);
      tempTV.setTextColor(getResources().getColor(R.color.white));
      tempTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
      tempTV.setBackgroundResource(R.drawable.group_collapsed);
      tempTV.setTypeface(null, Typeface.BOLD_ITALIC);
      aboutHeaders[i]=tempTV;
      layout.addView(tempTV);

      tempTV=new TextView(this);
      tempTV.setText(textStrings[i]);
      tempTV.setLineSpacing(4, 1);
      tempTV.setPadding(textPadding, 0, textPadding, bottomPadding);
      tempTV.setVisibility(View.GONE);
      tempTV.setGravity(Gravity.LEFT);
      Linkify.addLinks(tempTV, Linkify.WEB_URLS);
      aboutTexts[i]=tempTV;
      layout.addView(tempTV);
    }
  }

  public void selectHeader(int index) {
    boolean vis=aboutTexts[index].isShown();
    if (vis) {
      aboutHeaders[index].setBackgroundResource(R.drawable.group_collapsed);
      aboutTexts[index].setVisibility(View.GONE);
    } else {
      aboutHeaders[index].setBackgroundResource(R.drawable.group_expanded);
      aboutTexts[index].setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.close, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==R.id.menu_close) {
      finish();
      return true;
    } else if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
