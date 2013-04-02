package com.kuxhausen.huemore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.HueState;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
				+ " TEXT," + MoodColumns.PRECEDENCE + " INTEGER,"
				+ MoodColumns.STATE + " TEXT" + ");");

		db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
				+ GroupColumns.GROUP + " TEXT," + GroupColumns.PRECEDENCE
				+ " INTEGER," + GroupColumns.BULB + " INTEGER" + ");");
	}

	public void initialPopulate() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		Gson gson = new Gson();
		HueState hs = new HueState();

		cv.put(MoodColumns.MOOD,  ((char)8)+"OFF");
		hs.on = false;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Energize");
		hs.ct = (1000000 / 6000);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Concentrate");
		hs.ct = (1000000 / 3600);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.put(MoodColumns.MOOD, "Reading");
		hs.ct = (1000000 / 2700);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Relax");
		hs.ct = (1000000 / 2500);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Red");
		Double[] redPair = { 0.6472, 0.3316 };
		hs.xy = redPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Orange");
		Double[] orangePair = { 0.5663, 0.3978 };
		hs.xy = orangePair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Pink");
		Double[] pinkPair = { 0.3385, 0.1680 };
		hs.xy = pinkPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Green");
		Double[] greenPair = { 0.4020, 0.5041 };
		hs.xy = greenPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Blue");
		Double[] bluePair = { 0.1721, 0.0500 };
		hs.xy = bluePair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Romantic");
		hs.xy = pinkPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = redPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Rainbow");
		hs.xy = orangePair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = pinkPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = greenPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = bluePair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = redPair;
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		for (int i = 0; i < PreferencesKeys.ALWAYS_FREE_BULBS; i++) {
			cv.clear();
			cv.put(GroupColumns.GROUP, ((char)8)+"ALL");
			cv.put(GroupColumns.BULB, i);
			cv.put(GroupColumns.PRECEDENCE, i);
			db.insert(GroupColumns.TABLE_NAME, null, cv);
		}
	}

	public void addBulbs(int first, int last) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		for (int i = first - 1; i < last; i++) {
			cv.clear();
			cv.put(GroupColumns.GROUP,  ((char)8)+"ALL");
			cv.put(GroupColumns.BULB, i);
			cv.put(GroupColumns.PRECEDENCE, i);
			db.insert(GroupColumns.TABLE_NAME, null, cv);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}
}
