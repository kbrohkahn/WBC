package org.boardgamers.wbc;

import android.content.Intent;

import com.kbrohkahn.conventionlibrary.CL_SplashScreen;

public class SplashScreen extends CL_SplashScreen {

  public void startMainActivity() {
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }
}
