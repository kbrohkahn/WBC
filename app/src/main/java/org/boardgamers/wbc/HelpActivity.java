package org.boardgamers.wbc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {
  //private final String TAG="Help Activity";

  private TextView[] aboutTexts;
  private TextView[] aboutHeaders;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    String[] headerStrings=getResources().getStringArray(R.array.help_headers);
    String[] textStrings=getResources().getStringArray(R.array.help_texts);

    LinearLayout layout=(LinearLayout) findViewById(R.id.help_layout);
    layout.removeAllViews();

    int count=headerStrings.length;
    aboutHeaders=new TextView[count];
    aboutTexts=new TextView[count];
    TextView textView;
    LayoutInflater inflater=getLayoutInflater();
    for (int i=0; i<count; i++) {
      textView=(TextView) inflater.inflate(R.layout.help_header, layout, false);
      textView.setId(i);
      textView.setText(headerStrings[i]);
      textView.setClickable(true);
      aboutHeaders[i]=textView;
      layout.addView(textView);

      textView=(TextView) inflater.inflate(R.layout.help_text, layout, false);
      textView.setText(textStrings[i]);
      aboutTexts[i]=textView;
      layout.addView(textView);
    }
  }

  public void headerListener(View view) {
    int index=view.getId();
    if (aboutTexts[index].isShown()) {
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
    inflater.inflate(R.menu.menu_close, menu);
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
