package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MapActivity extends Activity {
  private final String TAG="Map Activity";

  protected int[] upstairsIDs;
  protected int[] downstairsIDs;
  private final Runnable runnable=new Runnable() {
    @Override
    public void run() {
      setRoom();
    }
  };
  private ImageView upstairsIV;
  private ImageView downstairsIV;
  private int roomID;
  private Handler handler;
  private boolean on;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.map);

    // enable home button for navigation drawer
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    upstairsIV=(ImageView) findViewById(R.id.map_upstairs_overlay);
    downstairsIV=(ImageView) findViewById(R.id.map_downstairs_overlay);

    roomID=-1;
    String[] roomsDownstairs=getResources().getStringArray(R.array.rooms_downstairs);
    for (int i=0; i<roomsDownstairs.length; i++) {
      if (roomsDownstairs[i].equalsIgnoreCase(MainActivity.SELECTED_ROOM)) {
        roomID=i;
        break;
      }
    }

    String[] roomsUpstairs=getResources().getStringArray(R.array.rooms_upstairs);
    for (int i=0; i<roomsUpstairs.length; i++) {
      if (roomsUpstairs[i].equalsIgnoreCase(MainActivity.SELECTED_ROOM)) {
        roomID=50+i;
        break;
      }
    }

    if (roomID>-1) {
      loadResources();
      setTitle(MainActivity.SELECTED_ROOM);
      handler=new Handler();

      if (roomID>=50) {
        upstairsIV.setImageResource(upstairsIDs[roomID-50]);
      } else {
        downstairsIV.setImageResource(downstairsIDs[roomID]);
      }
      on=true;
    } else {
      setTitle(getResources().getString(R.string.map));
    }

    MainActivity.SELECTED_ROOM="";
  }

  public void loadResources() {
    upstairsIDs=new int[11];
    upstairsIDs[0]=R.drawable.room_conestoga_1;
    upstairsIDs[1]=R.drawable.room_conestoga_2;
    upstairsIDs[2]=R.drawable.room_conestoga_3;
    upstairsIDs[3]=R.drawable.room_good_spirits_bar;
    upstairsIDs[4]=R.drawable.room_heritage;
    upstairsIDs[5]=R.drawable.room_lampeter;
    upstairsIDs[6]=R.drawable.room_laurel_grove;
    upstairsIDs[7]=R.drawable.room_open_gaming_pavilion;
    upstairsIDs[8]=R.drawable.room_showroom;
    upstairsIDs[9]=R.drawable.room_vistas_cd;
    upstairsIDs[10]=R.drawable.room_wheatland;

    downstairsIDs=new int[20];
    downstairsIDs[0]=R.drawable.room_ballroom_b_corridor;
    downstairsIDs[1]=R.drawable.room_ballroom_a;
    downstairsIDs[2]=R.drawable.room_ballrooms_a_and_b;
    downstairsIDs[3]=R.drawable.room_ballroom_b;
    downstairsIDs[4]=R.drawable.room_cornwall;
    downstairsIDs[5]=R.drawable.room_hopewell;
    downstairsIDs[6]=R.drawable.room_kinderhook;
    downstairsIDs[7]=R.drawable.room_limerock;
    downstairsIDs[8]=R.drawable.room_marietta;
    downstairsIDs[9]=R.drawable.room_new_holland;
    downstairsIDs[10]=R.drawable.room_paradise;
    downstairsIDs[11]=R.drawable.room_strasburg;
    downstairsIDs[12]=R.drawable.room_terrace;
    downstairsIDs[13]=R.drawable.room_terrace;
    downstairsIDs[14]=R.drawable.room_terrace;
    downstairsIDs[15]=R.drawable.room_terrace;
    downstairsIDs[16]=R.drawable.room_terrace;
    downstairsIDs[17]=R.drawable.room_terrace;
    downstairsIDs[18]=R.drawable.room_terrace;
    downstairsIDs[19]=R.drawable.room_terrace;

  }

  @Override
  public void onResume() {
    if (roomID>-1) {
      refreshRoom();
    }

    super.onResume();
  }

  private void setRoom() {
    if (roomID>=50) {
      upstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);
    } else {
      downstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);
    }

    on=!on;
    refreshRoom();

  }

  private void refreshRoom() {
    handler.postDelayed(runnable, 500);
  }

  @Override
  public void onPause() {
    if (roomID>-1) {
      handler.removeCallbacks(runnable);
    }

    super.onPause();
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
