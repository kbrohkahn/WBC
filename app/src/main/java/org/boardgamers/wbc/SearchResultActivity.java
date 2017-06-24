package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by Kevin
 */
public class SearchResultActivity extends AppCompatActivity {
	//private final String TAG="Search Activity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_results);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		String query = intent.getStringExtra("query_title");
		int id = intent.getIntExtra("query_id", -1);

		if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_SEARCH)) {
			query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
		}

		if (query != null || id > -1) {
			setTitle(query);
			SearchListFragment fragment =
					(SearchListFragment) getSupportFragmentManager().findFragmentById(R.id.searchFragment);
			fragment.loadEvents(query, id);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_search, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
			@Override
			public boolean onSuggestionSelect(int position) {
				return false;
			}

			@Override
			public boolean onSuggestionClick(int position) {
				searchView.clearFocus();
				Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
				int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
				String title = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
				startSearchActivity(id, title);
				return true;
			}
		});


		return true;

	}

	private void startSearchActivity(int id, String title) {
		Intent intent = new Intent(this, SearchResultActivity.class);
		intent.putExtra("query_title", title);
		intent.putExtra("query_id", id);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}

		return true;
	}
}
