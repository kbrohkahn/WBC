package org.boardgamers.wbc;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MapActivity extends AppCompatActivity {
  //private final String TAG="Map Activity";

  private final int[] upstairsIDs=
      {R.drawable.room_conestoga_1, R.drawable.room_conestoga_2, R.drawable.room_conestoga_3,
          R.drawable.room_good_spirits_bar, R.drawable.room_heritage, R.drawable.room_lampeter,
          R.drawable.room_laurel_grove, R.drawable.room_open_gaming_pavilion,
          R.drawable.room_showroom, R.drawable.room_vistas_cd, R.drawable.room_wheatland};
  private final int[] downstairsIDs=
      {R.drawable.room_ballroom_b_corridor, R.drawable.room_ballroom_a,
          R.drawable.room_ballrooms_a_and_b, R.drawable.room_ballroom_b, R.drawable.room_cornwall,
          R.drawable.room_hopewell, R.drawable.room_kinderhook, R.drawable.room_limerock,
          R.drawable.room_marietta, R.drawable.room_new_holland, R.drawable.room_paradise,
          R.drawable.room_strasburg, R.drawable.room_terrace, R.drawable.room_terrace,
          R.drawable.room_terrace, R.drawable.room_terrace, R.drawable.room_terrace,
          R.drawable.room_terrace, R.drawable.room_terrace, R.drawable.room_terrace};

  private ImageView upstairsIV;
  private ImageView downstairsIV;
  private int roomID;
  private Handler handler=new Handler();
  private boolean on;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.map);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    String selectedRoom=getIntent().getStringExtra("room");
    if (selectedRoom==null) {
      selectedRoom="";
    }

    upstairsIV=(ImageView) findViewById(R.id.map_upstairs_overlay);
    downstairsIV=(ImageView) findViewById(R.id.map_downstairs_overlay);

    roomID=-1;
    String[] roomsDownstairs=getResources().getStringArray(R.array.rooms_downstairs);
    for (int i=0; i<roomsDownstairs.length; i++) {
      if (roomsDownstairs[i].equalsIgnoreCase(selectedRoom)) {
        roomID=i;
        break;
      }
    }

    String[] roomsUpstairs=getResources().getStringArray(R.array.rooms_upstairs);
    for (int i=0; i<roomsUpstairs.length; i++) {
      if (roomsUpstairs[i].equalsIgnoreCase(selectedRoom)) {
        roomID=50+i;
        break;
      }
    }

    if (roomID>-1) {
      setTitle(selectedRoom);

      if (roomID>=50) {
        upstairsIV.setImageResource(upstairsIDs[roomID-50]);
      } else {
        downstairsIV.setImageResource(downstairsIDs[roomID]);
      }
      on=true;
    } else {
      setTitle(getResources().getString(R.string.map));
    }
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

  private final Runnable runnable=new Runnable() {
    @Override
    public void run() {
      setRoom();
    }
  };

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
