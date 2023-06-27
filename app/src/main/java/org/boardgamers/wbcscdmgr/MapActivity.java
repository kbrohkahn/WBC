package org.boardgamers.wbcscdmgr;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MapActivity extends AppCompatActivity {
	//private final String TAG="Map Activity";

	private ImageView skiLodgeIV;
	private ImageView conventionCenterIV;
	private int roomID;
	private final Handler handler = new Handler();
	private boolean on;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		String selectedRoom = getIntent().getStringExtra("room");
		if (selectedRoom == null) {
			selectedRoom = "";
		}

		skiLodgeIV = findViewById(R.id.ski_lodge_overlay);
		conventionCenterIV = findViewById(R.id.convention_center_overlay);

		roomID = -1;
		String[] roomsDownstairs = getResources().getStringArray(R.array.rooms_ski_lodge);
		for (int i = 0; i < roomsDownstairs.length; i++) {
			if (roomsDownstairs[i].equalsIgnoreCase(selectedRoom)) {
				roomID = i;
				break;
			}
		}

		String[] roomsUpstairs = getResources().getStringArray(R.array.rooms_convention_center);
		for (int i = 0; i < roomsUpstairs.length; i++) {
			if (roomsUpstairs[i].equalsIgnoreCase(selectedRoom)) {
				roomID = 50 + i;
				break;
			}
		}

		if (roomID > -1) {
			setTitle(selectedRoom);

			if (roomID >= 50) {
				skiLodgeIV.setImageResource(Constants.conventionCenterIDs[roomID - 50]);
			} else {
				conventionCenterIV.setImageResource(Constants.skiLodgeIDs[roomID]);
			}
			on = true;
		} else {
			setTitle(getResources().getString(R.string.map));
		}
	}

	@Override
	public void onResume() {
		if (roomID > -1) {
			refreshRoom();
		}

		super.onResume();
	}

	private void setRoom() {
		if (roomID >= 50) {
			skiLodgeIV.setVisibility(on ? View.GONE : View.VISIBLE);
		} else {
			conventionCenterIV.setVisibility(on ? View.GONE : View.VISIBLE);
		}

		on = !on;
		refreshRoom();

	}

	private void refreshRoom() {
		handler.postDelayed(runnable, 500);
	}

	@Override
	public void onPause() {
		if (roomID > -1) {
			handler.removeCallbacks(runnable);
		}

		super.onPause();
	}

	private final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			setRoom();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
