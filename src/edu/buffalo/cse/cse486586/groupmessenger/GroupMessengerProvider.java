package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {
	
	private SQLiteDatabase database;
	MyDBHelper DBHelper;
	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final String table_name= "MESSAGE";
	
	@Override
    public boolean onCreate() {
		DBHelper = new MyDBHelper(getContext());
		  return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues valuesToInsert) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that I used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

    	Log.v("TABLE NAME ", table_name);
    	
    	SQLiteDatabase database = DBHelper.getWritableDatabase();
    	
    	long value = database.insert(table_name, null, valuesToInsert);
        Log.v("insert-!!", valuesToInsert.toString());
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         * 
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         * 
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

    	  database = DBHelper.getWritableDatabase();
    	  Log.v("SELECTION", selection);
    	  String query_statement= "SELECT * FROM "+table_name+" WHERE key=?";
    	  String[] sel=new String[1];
    	  sel[0]=selection;
    	  Cursor cursor = database.rawQuery(query_statement,sel);
    
    	  cursor.moveToFirst();
    	  Log.v("query", selection); 
    	  return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
  
}
