package org.boardgamers.wbc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class Map extends Activity {
	// private final String TAG="Map Dialog";
	final private int[] upstairsIDs= { R.drawable.room_conestoga_1,
	    R.drawable.room_conestoga_2, R.drawable.room_conestoga_3,
	    R.drawable.room_good_spirits_bar, R.drawable.room_heritage,
	    R.drawable.room_lampeter, R.drawable.room_laurel_grove,
	    R.drawable.room_open_gaming_pavilion, R.drawable.room_showroom,
	    R.drawable.room_vistas_cd, R.drawable.room_wheatland };
	final private int[] downstairsIDs= { R.drawable.room_ballroom_b_corridor,
	    R.drawable.room_ballroom_a, R.drawable.room_ballrooms_a_and_b,
	    R.drawable.room_ballroom_b, R.drawable.room_cornwall,
	    R.drawable.room_hopewell, R.drawable.room_kinderhook,
	    R.drawable.room_limerock, R.drawable.room_marietta,
	    R.drawable.room_new_holland, R.drawable.room_paradise,
	    R.drawable.room_strasburg, R.drawable.room_terrace,
	    R.drawable.room_terrace, R.drawable.room_terrace,
	    R.drawable.room_terrace, R.drawable.room_terrace,
	    R.drawable.room_terrace, R.drawable.room_terrace };

	public String[] roomsUpstairs;
	public String[] roomsDownstairs;

	private ImageView upstairsIV;
	private ImageView downstairsIV;

	int roomID;
	private Handler handler;

	private boolean on;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		upstairsIV=(ImageView) findViewById(R.id.map_upstairs_overlay);
		downstairsIV=(ImageView) findViewById(R.id.map_downstairs_overlay);

		String room=getIntent().getStringExtra("room");

		roomID=-1;
		roomsDownstairs=getResources().getStringArray(R.array.rooms_downstairs);
		for (int i=0; i<roomsDownstairs.length; i++) {
			if (roomsDownstairs[i].equalsIgnoreCase(room)) {
				roomID=i;
				break;
			}
		}

		roomsUpstairs=getResources().getStringArray(R.array.rooms_upstairs);
		for (int i=0; i<roomsUpstairs.length; i++) {
			if (roomsUpstairs[i].equalsIgnoreCase(room)) {
				roomID=50+i;
				break;
			}
		}

		if (roomID>-1) {
			setTitle(room);
			handler=new Handler();

			if (roomID>=50)
				upstairsIV.setImageResource(upstairsIDs[roomID-50]);
			else
				downstairsIV.setImageResource(downstairsIDs[roomID]);
			on=true;
		} else
			setTitle("Hotel Map");
	}

	@Override
	protected void onResume() {
		if (roomID>-1) {
			refreshRoom();
		}

		super.onResume();
	}

	public void setRoom() {
		if (roomID>=50)
			upstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);
		else
			downstairsIV.setVisibility(on ? View.GONE : View.VISIBLE);

		on=!on;
		refreshRoom();

	}

	public void refreshRoom() {
		handler.postDelayed(runnable, 500);
	}

	Runnable runnable=new Runnable() {
		@Override
		public void run() {
			setRoom();
		}
	};

	@Override
	protected void onPause() {
		if (roomID>-1)
			handler.removeCallbacks(runnable);

		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
