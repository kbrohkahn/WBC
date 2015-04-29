package org.boardgamers.wbc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class SplashScreen extends Activity {
  //private final String TAG="Splash";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(true);

    setContentView(R.layout.splash);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    int result=dbHelper.startInitialLoad();

    if (result>0) {
      startMainActivity(result);
    } else if (result==-1) {
      showToast("ERROR: Could not parse schedule file,"+"contact dev@boardgamers.org for help.");
    } else if (result==-2) {
      showToast("ERROR: Could not find schedule file,"+"contact dev@boardgamers.org for help.");
    } else if (result==-3) {
      showToast("ERROR: Could not open schedule file,"+"contact dev@boardgamers.org for help.");
    }

  }

  public void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  public void startMainActivity(int result) {
    Intent intent=new Intent(this, MainActivity.class);
    // TODO changes
    // intent.putExtra("allChanges", allChanges);

    intent.putExtra("totalEvents", result);
    startActivity(intent);
    finish();
  }
}
