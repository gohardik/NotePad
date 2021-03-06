package com.nononsenseapps.notepad.database;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class TaskList extends DAO {

	// SQL convention says Table name should be "singular"
	public static final String TABLE_NAME = "tasklist";
	public static final Uri URI = Uri.withAppendedPath(
			Uri.parse(MyContentProvider.SCHEME + MyContentProvider.AUTHORITY),
			TABLE_NAME);

	public static Uri getUri(final long id) {
		return Uri.withAppendedPath(URI, Long.toString(id));
	}

	public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.nononsenseapps.list";

	public static final int BASEURICODE = 101;
	public static final int BASEITEMCODE = 102;
	// Legacy support, these also need to use legacy projections
		public static final int LEGACYBASEURICODE = 111;
		public static final int LEGACYBASEITEMCODE = 112;
		public static final int LEGACYVISIBLEURICODE = 113;
		public static final int LEGACYVISIBLEITEMCODE = 114;

	/**
	 * TaskList URIs start at 101, up to 199
	 */
	public static void addMatcherUris(UriMatcher sURIMatcher) {
		sURIMatcher
				.addURI(MyContentProvider.AUTHORITY, TABLE_NAME, BASEURICODE);
		sURIMatcher.addURI(MyContentProvider.AUTHORITY, TABLE_NAME + "/#",
				BASEITEMCODE);
		
		// Legacy URIs
				sURIMatcher.addURI(MyContentProvider.AUTHORITY,
						LegacyDBHelper.NotePad.Lists.LISTS, LEGACYBASEURICODE);
				sURIMatcher.addURI(MyContentProvider.AUTHORITY,
						LegacyDBHelper.NotePad.Lists.LISTS + "/#",
						LEGACYBASEITEMCODE);
				sURIMatcher.addURI(MyContentProvider.AUTHORITY,
						LegacyDBHelper.NotePad.Lists.VISIBLE_LISTS,
						LEGACYVISIBLEURICODE);
				sURIMatcher.addURI(MyContentProvider.AUTHORITY,
						LegacyDBHelper.NotePad.Lists.VISIBLE_LISTS + "/#",
						LEGACYVISIBLEITEMCODE);
	}

	public static class Columns implements BaseColumns {

		private Columns() {
		}

		public static final String TITLE = "title";
		public static final String UPDATED = "updated";
		public static final String LISTTYPE = "tasktype";
		public static final String SORTING = "sorting";

		// public static final String GTASKACCOUNT = "gtaskaccount";
		// public static final String GTASKID = "gtaskid";
		//
		// // Future proofing
		// public static final String DROPBOXACCOUNT = "dropboxaccount";
		// public static final String DROPBOXID = "dropboxid";

		public static final String[] FIELDS = { _ID, TITLE, UPDATED, LISTTYPE,
				SORTING };
		// GTASKACCOUNT, GTASKID, DROPBOXACCOUNT, DROPBOXID };
		public static final String[] SHALLOWFIELDS = { _ID, TITLE, UPDATED };
	}

	public static final String CREATE_TABLE = new StringBuilder("CREATE TABLE ")
			.append(TABLE_NAME).append("(").append(Columns._ID)
			.append(" INTEGER PRIMARY KEY,").append(Columns.TITLE)
			.append(" TEXT NOT NULL DEFAULT '',").append(Columns.UPDATED)
			.append(" INTEGER,").append(Columns.LISTTYPE)
			.append(" TEXT DEFAULT NULL,").append(Columns.SORTING)
			.append(" TEXT DEFAULT NULL")
			// GTask fields
			// .append(Columns.GTASKACCOUNT).append(" TEXT,").append(Columns.GTASKID).append(" TEXT,")
			// Dropbox fields
			// .append(Columns.DROPBOXACCOUNT).append(" TEXT,").append(Columns.DROPBOXID).append(" TEXT")
			.append(")").toString();

	public String title = "";

	// milliseconds since 1970-01-01 UTC
	public Long updated = null;
	
	// Null, use global prefs
	public String listtype = null;
	public String sorting = null;

	// Sync stuff
	// public String gtaskaccount = null;
	// public String gtaskid = null;
	// public String dropboxaccount = null;
	// public String dropboxid = null;

	public TaskList() {
	}

	public TaskList(final Cursor c) {
		this._id = c.getLong(0);
		this.title = c.getString(1);
		this.updated = c.getLong(2);
		this.listtype = c.getString(3);
		this.sorting = c.getString(4);
		// sync stuff
//		gtaskaccount = c.getString(3);
//		gtaskid = c.getString(4);
//		dropboxaccount = c.getString(5);
//		dropboxid = c.getString(6);
	}

	public TaskList(final Uri uri, final ContentValues values) {
		this(Long.parseLong(uri.getLastPathSegment()), values);
	}

	public TaskList(final long id, final ContentValues values) {
		this(values);
		this._id = id;
	}

	public TaskList(final ContentValues values) {
		title = values.getAsString(Columns.TITLE);
		updated = values.getAsLong(Columns.UPDATED);
		listtype = values.getAsString(Columns.LISTTYPE);
		sorting = values.getAsString(Columns.SORTING);
		
//		gtaskaccount = values.getAsString(Columns.GTASKACCOUNT);
//		gtaskid = values.getAsString(Columns.GTASKID);
//		dropboxaccount = values.getAsString(Columns.DROPBOXACCOUNT);
//		dropboxid = values.getAsString(Columns.DROPBOXID);
	}

	public ContentValues getContent() {
		final ContentValues values = new ContentValues();
		// Note that ID is NOT included here
		values.put(Columns.TITLE, title);
		values.put(Columns.UPDATED, updated);
		values.put(Columns.LISTTYPE, listtype);
		values.put(Columns.SORTING, sorting);
		
//		values.put(Columns.GTASKACCOUNT, gtaskaccount);
//		values.put(Columns.GTASKID, gtaskid);
//		values.put(Columns.DROPBOXACCOUNT, dropboxaccount);
//		values.put(Columns.DROPBOXID, dropboxid);

		return values;
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}
	
	@Override
	public int save(final Context context) {
		return save(context, Calendar.getInstance().getTimeInMillis());
	}

	public int save(final Context context, final long updateTime) {
		int result = 0;
		updated = updateTime;
		if (_id < 1) {
			final Uri uri = context.getContentResolver().insert(getBaseUri(),
					getContent());
			if (uri != null) {
				_id = Long.parseLong(uri.getLastPathSegment());
				result++;
			}
		}
		else {
			result += context.getContentResolver().update(getUri(),
					getContent(), null, null);
		}
		return result;
	}
}
