package com.kuxhausen.huemore;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;

public class SerializedEditorActivity extends NetworkManagedSherlockFragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnCheckedChangeListener {

	Context context;
	Gson gson = new Gson();

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

	private SeekBar brightnessBar;
	private CheckBox brightnessCheckBox;
	private TextView brightnessDescripterTextView;
	private Spinner groupSpinner, moodSpinner;
	private SimpleCursorAdapter groupDataSource, moodDataSource;

	private GroupMoodBrightness priorGMB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;
		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(GROUPS_LOADER, null, this);
		lm.initLoader(MOODS_LOADER, null, this);

		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				preview();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		
		brightnessDescripterTextView = (TextView)this.findViewById(R.id.brightnessDescripterTextView);
		
		brightnessCheckBox = (CheckBox)this.findViewById(R.id.includeBrightnessCheckBox);
		brightnessCheckBox.setOnCheckedChangeListener(this);
		
		groupSpinner = (Spinner) this.findViewById(R.id.groupSpinner);
		String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
		groupDataSource = new SimpleCursorAdapter(this, layout, null, gColumns,
				new int[] { android.R.id.text1 }, 0);
		groupDataSource
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupDataSource);

		moodSpinner = (Spinner) this.findViewById(R.id.moodSpinner);
		String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
		moodDataSource = new SimpleCursorAdapter(this, layout, null, mColumns,
				new int[] { android.R.id.text1 }, 0);
		moodDataSource
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moodSpinner.setAdapter(moodDataSource);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			this.startActivity(new Intent(this,MainFragment.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void preview() {
		
		String groupName = ((TextView) groupSpinner.getSelectedView()).getText().toString();
		Group g = Group.loadFromDatabase(groupName, context);
		
		String moodName = ((TextView) moodSpinner.getSelectedView()).getText().toString();
		Mood m = Utils.getMoodFromDatabase(moodName, this);
		
		Integer brightness = null;
		if(brightnessBar.getVisibility()==View.VISIBLE)
			brightness = brightnessBar.getProgress();
		
		this.getService().getMoodPlayer().playMood(g, m, moodName, brightness);
	}

	public String getSerializedByNamePreview() {
		GroupMoodBrightness gmb = new GroupMoodBrightness();
		gmb.group = ((TextView) groupSpinner.getSelectedView()).getText()
				.toString();
		gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText()
				.toString();
		if(brightnessBar.getVisibility()==View.VISIBLE)
			gmb.brightness = brightnessBar.getProgress();
		
		String preview = gmb.group + " \u2192 " + gmb.mood;
		if(brightnessBar.getVisibility()==View.VISIBLE)
			preview+=" @ "+ ((gmb.brightness * 100) / 255) + "%";
		return preview;
	}

	public void setSerializedByName(String s) {
		priorGMB = gson.fromJson(s, GroupMoodBrightness.class);

	}

	public String getSerializedByName() {
		GroupMoodBrightness gmb = new GroupMoodBrightness();
		gmb.group = ((TextView) groupSpinner.getSelectedView()).getText()
				.toString();
		gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText()
				.toString();
		if(brightnessBar.getVisibility()==View.VISIBLE)
			gmb.brightness = brightnessBar.getProgress();
		return gson.toJson(gmb);
	}

	public String getSerializedByValue() {
		String url = "lampshade.io/nfc?";

		
		Group g = Group.loadFromDatabase(((TextView) groupSpinner.getSelectedView()).getText().toString(), context);
		
		Mood m = Utils.getMoodFromDatabase( ((TextView) moodSpinner.getSelectedView())
				.getText().toString(), this);
		
		Integer brightness = null;
		if(brightnessBar.getVisibility()==View.VISIBLE)
			brightness = brightnessBar.getProgress();
		
		String data = HueUrlEncoder.encode(m, g, brightness, context);
		return url + data;
	}

	/**
	 * Callback that's invoked when the system has initialized the Loader and is
	 * ready to start the query. This usually happens when initLoader() is
	 * called. The loaderID argument contains the ID value passed to the
	 * initLoader() call.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
		/*
		 * Takes action based on the ID of the Loader that's being created
		 */
		switch (loaderID) {
		case GROUPS_LOADER:
			// Returns a new CursorLoader
			String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
			return new CursorLoader(this, // Parent activity context
					DatabaseDefinitions.GroupColumns.GROUPS_URI, // Table
					gColumns, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		case MOODS_LOADER:
			// Returns a new CursorLoader
			String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
			return new CursorLoader(this, // Parent activity context
					DatabaseDefinitions.MoodColumns.MOODS_URI, // Table
					mColumns, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		default:
			// An invalid id was passed in
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		/*
		 * Moves the query results into the adapter, causing the ListView
		 * fronting this adapter to re-display
		 */
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(cursor);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(cursor);
			}
			break;
		}

		if (priorGMB != null) {

			// apply prior state
			int moodPos = 0;
			for (int i = 0; i < moodDataSource.getCount(); i++) {
				if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(
						priorGMB.mood))
					moodPos = i;
			}
			moodSpinner.setSelection(moodPos);

			int groupPos = 0;
			for (int i = 0; i < groupDataSource.getCount(); i++) {
				if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(
						priorGMB.group))
					groupPos = i;
			}
			groupSpinner.setSelection(groupPos);
			if(priorGMB.brightness!=null)
				brightnessBar.setProgress(priorGMB.brightness);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(null);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(null);
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			brightnessBar.setVisibility(View.VISIBLE);
			brightnessDescripterTextView.setVisibility(View.VISIBLE);
		} else {
			brightnessBar.setVisibility(View.INVISIBLE);
			brightnessDescripterTextView.setVisibility(View.INVISIBLE);
		}
	}
}