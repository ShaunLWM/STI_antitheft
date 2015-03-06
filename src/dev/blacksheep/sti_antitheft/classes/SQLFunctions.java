package dev.blacksheep.sti_antitheft.classes;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLFunctions {
	public static final String TAG = "AIRemote";
	public static final String GLOBAL_ROWID = "_id";
	public static final String GLOBAL_DEFINDEX = "defindex";
	private static final String FILES_PATH = "fPath";
	private static final String FILES_PASSWORD = "fPass";
	private static final String DATABASE_NAME = "schema";
	private static final String TABLE_FILES = "files";

	private static final int DATABASE_VERSION = 1;

	private DbHelper ourHelper;
	private final Context ourContext;
	private SQLiteDatabase ourDatabase;

	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_FILES + " (" + GLOBAL_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FILES_PATH + " TEXT NOT NULL, " + FILES_PASSWORD + " TEXT NOT NULL);");
			Log.e(TAG, "Created DB");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
			onCreate(db);
		}

	}

	public SQLFunctions(Context c) {
		ourContext = c;
	}

	public SQLFunctions open() throws SQLException {
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		return null;
	}

	public void close() {
		if (ourHelper != null) {
			ourHelper.close();
		} else {
			Log.e(TAG, "You did not open your database. Null error");
		}
	}

	public long unixTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public boolean longerThanTwoHours(String pTime) {
		int prevTime = Integer.parseInt(pTime);
		int currentTime = (int) (System.currentTimeMillis() / 1000L);
		int seconds = currentTime - prevTime;
		int how_many;
		if (seconds > 3600 && seconds < 86400) {
			how_many = (int) seconds / 3600;
			if (how_many >= 2) { // 2 hours
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public String getLastRowId() {
		String sql = "SELECT * FROM " + TABLE_FILES + " ORDER BY " + GLOBAL_ROWID + " DESC LIMIT 1";
		Cursor cursor = ourDatabase.rawQuery(sql, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex(GLOBAL_ROWID));
				cursor.close();
				Log.e("LATEST SQL ROW", id);
				return id;
			}
		}
		cursor.close();
		return "";
	}

	public String getPasswordOfFiles(String filePath) {
		String sql = "SELECT * FROM " + TABLE_FILES + " WHERE " + FILES_PATH + " = ?";
		Cursor cursor = ourDatabase.rawQuery(sql, new String[] { filePath });
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				String pass = cursor.getString(cursor.getColumnIndex(FILES_PASSWORD));
				cursor.close();
				Log.e("FILE PASS", pass);
				return pass;
			}
		}
		cursor.close();
		return "";
	}

	public ArrayList<String> loadFileList() {
		ArrayList<String> map = new ArrayList<String>();
		Cursor cursor = ourDatabase.rawQuery("SELECT * FROM " + TABLE_FILES, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				while (cursor.isAfterLast() == false) {
					map.add(cursor.getString(cursor.getColumnIndex(FILES_PATH)));
					cursor.moveToNext();
				}
			}
		}
		cursor.close();
		return map;
	}

	public void insertFilePath(String filePath) {
		ContentValues cv = new ContentValues();
		String sql = "SELECT * FROM " + TABLE_FILES + " WHERE " + FILES_PATH + " = ?";
		Cursor cursor = ourDatabase.rawQuery(sql, new String[] { filePath });
		if (cursor.moveToFirst()) {
			// exist
		} else {
			cv.put(FILES_PATH, filePath);
			cv.put(FILES_PASSWORD, "");
			try {
				ourDatabase.insert(TABLE_FILES, null, cv);
			} catch (Exception e) {
				Log.e(TAG, "Error creating market entry", e);
			}
		}
		cursor.close();
	}

	public void updatePassword(String filePath, String password) {
		ContentValues cv = new ContentValues();
		String sql = "SELECT * FROM " + TABLE_FILES + " WHERE " + FILES_PATH + " = ?";
		Cursor cursor = ourDatabase.rawQuery(sql, new String[] { filePath });
		if (cursor.moveToFirst()) {
			cv.put(FILES_PASSWORD, password);
			String whereClause = FILES_PATH + "=?";
			String[] whereArgs = new String[] { filePath };
			try {
				ourDatabase.update(TABLE_FILES, cv, whereClause, whereArgs);
			} catch (Exception e) {
				Log.e(TAG, "Error updating files password entry", e);
			}
		}
		cursor.close();
	}

	public void deleteFilePath(String filePath) {
		String whereClause = FILES_PATH + "=?";
		String[] whereArgs = new String[] { String.valueOf(filePath) };
		ourDatabase.delete(TABLE_FILES, whereClause, whereArgs);
	}

}
