package org.boardgamers.wbc;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MapFragment extends Fragment {
  private final String TAG = "Map Activity";

  final private int[] upstairsIDs = {R.drawable.room_conestoga_1,
      R.drawable.room_conestoga_2, R.drawable.room_conestoga_3,
      R.drawable.room_good_spirits_bar, R.drawable.room_heritage,
      R.drawable.room_lampeter, R.drawable.room_laurel_grove,
      R.drawable.room_open_gaming_pavilion, R.drawable.room_showroom,
      R.drawable.room_vistas_cd, R.drawable.room_wheatland};
  final private int[] downstairsIDs = {R.drawable.room_ballroom_b_corridor,
      R.drawable.room_ballroom_a, R.drawable.room_ballrooms_a_and_b,
      R.drawable.room_ballroom_b, R.drawable.room_cornwall,
      R.drawable.room_hopewell, R.drawable.room_kinderhook,
      R.drawable.room_limerock, R.drawable.room_marietta,
      R.drawable.room_new_holland, R.drawable.room_paradise,
      R.drawable.room_strasburg, R.drawable.room_terrace,
      R.drawable.room_terrace, R.drawable.room_terrace,
      R.drawable.room_terrace, R.drawable.room_terrace,
      R.drawable.room_terrace, R.drawable.room_terrace};
  public String roomString;
  Runnable runnable = new Runnable() {
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.map, container);


    upstairsIV = (ImageView) view.findViewById(R.id.map_upstairs_overlay);
    downstairsIV = (ImageView) view.findViewById(R.id.map_downstairs_overlay);


    roomID = -1;
    String[] roomsDownstairs = getResources().getStringArray(R.array.rooms_downstairs);
    for (int i = 0; i < roomsDownstairs.length; i++) {
      if (roomsDownstairs[i].equalsIgnoreCase(roomString)) {
        roomID = i;
        break;
      }
    }

    String[] roomsUpstairs = getResources().getStringArray(R.array.rooms_upstairs);
    for (int i = 0; i < roomsUpstairs.length; i++) {
      if (roomsUpstairs[i].equalsIgnoreCase(roomString)) {
        roomID = 50 + i;
        break;
      }
    }

    if (roomID > -1) {
      getActivity().setTitle(roomString);
      handler = new Handler();

      if (roomID >= 50)
        upstairsIV.setImageResource(upstairsIDs[roomID - 50]);
      else
        downstairsIV.setImageResource(downstairsIDs[roomID]);
      on = true;
    } else
      getActivity().setTitle("Hotel Map");

    return view;
  }

  @Override
  public void onResume() {
    if (roomID > -1) {
      refreshRoom();
    }

    super.onResume();
  }

  public void setRoom() {
    if (roomID >= 50)
      upstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);
    else
      downstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);

    on = !on;
    refreshRoom();

  }

  public void refreshRoom() {
    handler.postDelayed(runnable, 500);
  }

  @Override
  public void onPause() {
    if (roomID > -1)
      handler.removeCallbacks(runnable);

    super.onPause();
  }
}
