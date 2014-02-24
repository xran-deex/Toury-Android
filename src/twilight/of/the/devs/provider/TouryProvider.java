package twilight.of.the.devs.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class TouryProvider extends ContentProvider {
	
	public static class TouryProviderMetaData {

		public static final String AUTHORITY = "twilight.of.the.devs.provider.TouryProvider";
		
		public static final String DATABASE_NAME = "toury.db";
		public static final int DATABASE_VERSION = 1;
		public static final String MARKERS_TABLE_NAME = "markers";
		
		private TouryProviderMetaData() {}
		
		public final static class MarkersTableMetaData implements BaseColumns {
			
			private MarkersTableMetaData(){}
			
			public static final String TABLE_NAME = "markers";
			
			public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/markers");
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.toury.marker";
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.toury.marker";
			
			//Column names
			public static final String DEFAULT_SORT_ORDER = "_id DESC";
			public static final String DESCRIPTION = "description";
			public static final String LATITUDE = "latitude";
			public static final String LONGITUDE = "longitude";
			public static final String DIRECTION = "direction";
			public static final String RADIUS = "radius";
			public static final String TITLE = "title";
			
		}
		
	}
	
	private static final String TAG = TouryProvider.class.getName();
	
	private static HashMap<String, String> sMarkersProjectionMap;
	
	static {
		sMarkersProjectionMap = new HashMap<String, String>();
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData._ID, TouryProviderMetaData.MarkersTableMetaData._ID);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.TITLE, TouryProviderMetaData.MarkersTableMetaData.TITLE);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.RADIUS, TouryProviderMetaData.MarkersTableMetaData.RADIUS);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.LATITUDE, TouryProviderMetaData.MarkersTableMetaData.LATITUDE);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.LONGITUDE, TouryProviderMetaData.MarkersTableMetaData.LONGITUDE);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.DIRECTION, TouryProviderMetaData.MarkersTableMetaData.DIRECTION);
		sMarkersProjectionMap.put(TouryProviderMetaData.MarkersTableMetaData.DESCRIPTION, TouryProviderMetaData.MarkersTableMetaData.DESCRIPTION);
	}
	
	private static final UriMatcher sUriMatcher;
	private static final int INCOMING_ITEM_COLLECTION_URI_INDICATOR = 1;
	private static final int INCOMING_SINGLE_ITEM_URI_INDICATOR = 2;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(TouryProviderMetaData.AUTHORITY, "markers", INCOMING_ITEM_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(TouryProviderMetaData.AUTHORITY, "markers/#", INCOMING_SINGLE_ITEM_URI_INDICATOR);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, TouryProviderMetaData.DATABASE_NAME, null, TouryProviderMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME + " ("
					+ TouryProviderMetaData.MarkersTableMetaData._ID + " INTEGER PRIMARY KEY, "
					+ TouryProviderMetaData.MarkersTableMetaData.TITLE + " TEXT, "
					+ TouryProviderMetaData.MarkersTableMetaData.DESCRIPTION + " TEXT, "
					+ TouryProviderMetaData.MarkersTableMetaData.RADIUS + " REAL, "
					+ TouryProviderMetaData.MarkersTableMetaData.DIRECTION + " REAL, "
					+ TouryProviderMetaData.MarkersTableMetaData.LONGITUDE + " REAL, "
					+ TouryProviderMetaData.MarkersTableMetaData.LATITUDE + " REAL"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME);
			onCreate(db);
		}
	}
	
	private DatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)){
		case INCOMING_ITEM_COLLECTION_URI_INDICATOR:
			count = db.delete(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME, where, whereArgs);
			break;
		case INCOMING_SINGLE_ITEM_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.delete(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME, TouryProviderMetaData.MarkersTableMetaData._ID + "=" + rowId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case INCOMING_ITEM_COLLECTION_URI_INDICATOR:
			return TouryProviderMetaData.MarkersTableMetaData.CONTENT_TYPE;
		case INCOMING_SINGLE_ITEM_URI_INDICATOR:
			return TouryProviderMetaData.MarkersTableMetaData.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if(sUriMatcher.match(uri) != INCOMING_ITEM_COLLECTION_URI_INDICATOR) throw new IllegalArgumentException("Unknown URI " + uri);
		
		ContentValues values;
		if(initialValues != null){
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME, TouryProviderMetaData.MarkersTableMetaData.TITLE, values);
		if(rowId > 0){
			Uri insertedBookUri = ContentUris.withAppendedId(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(insertedBookUri, null);
			return insertedBookUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(sUriMatcher.match(uri)){
		case INCOMING_ITEM_COLLECTION_URI_INDICATOR:
			qb.setTables(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME);
			qb.setProjectionMap(sMarkersProjectionMap);
			break;
		case INCOMING_SINGLE_ITEM_URI_INDICATOR:
			qb.setTables(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME);
			qb.setProjectionMap(sMarkersProjectionMap);
			qb.appendWhere(TouryProviderMetaData.MarkersTableMetaData._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		if(TextUtils.isEmpty(sortOrder)){
			orderBy = TouryProviderMetaData.MarkersTableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		//int i = c.getCount();
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch(sUriMatcher.match(uri)){
		case INCOMING_ITEM_COLLECTION_URI_INDICATOR:
			count = db.update(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME, values, where, whereArgs);
			break;
		case INCOMING_SINGLE_ITEM_URI_INDICATOR:
			String rowId = uri.getPathSegments().get(1);
			count = db.update(TouryProviderMetaData.MarkersTableMetaData.TABLE_NAME, values, TouryProviderMetaData.MarkersTableMetaData._ID + "=" + rowId
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
