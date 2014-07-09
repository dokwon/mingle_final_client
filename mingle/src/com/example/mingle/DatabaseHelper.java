package com.example.mingle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper  {
	
	  public static final String TABLE_UIDLIST = "uidlist";
	  public static final String COLUMN_UID = "_uid";
	  public static final String TABLE_MYUID = "myuid";
	  public static final String COLUMN_MYUID = "my_uid";
	  public static final String COLUMN_TIMESTAMP = "ts";
	  public static final String COLUMN_SENDUID = "send_uid";
	  public static final String COLUMN_MSG = "msg";

	  private static final String DATABASE_NAME = "minglelocal.db";
	  private static final int DATABASE_VERSION = 1;
	  public HashMap uid_list;
	  
	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_UIDLIST + "(" + COLUMN_UID
	      + " text not null);";
	  private static final String MYUID_CREATE = "create table "
	      + TABLE_MYUID + "(" + COLUMN_MYUID
	      + " text not null);";
	 
	  
	  /* Database constructor*/
	  public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	    database.execSQL(MYUID_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		  Log.w(DatabaseHelper.class.getName(),
	      "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
		  db.execSQL("DROP TABLE IF EXISTS " + TABLE_UIDLIST);
		  db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYUID);
		  onCreate(db);
	  }
	  
	  // Insert messages to database
	  public boolean insertMessages(String uid, String send_uid, String msg, String msg_ts) {
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_SENDUID, send_uid);
		  values.put(DatabaseHelper.COLUMN_MSG, msg);
		  values.put(DatabaseHelper.COLUMN_TIMESTAMP, msg_ts);
		  db.insert(uid,null,values);
		  return true;
	  }
	  
	  // Insert UID of other users in database
	  public boolean insertNewUID(String uid){
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_UID,uid);
		  db.insert(DatabaseHelper.TABLE_UIDLIST,null,values);
		  
		  String createUIDTableQuery = "create table " + uid 
				  + "(" + COLUMN_SENDUID + " text not null, " + COLUMN_MSG + " text not null, " 
				  + COLUMN_TIMESTAMP + " text not null);";
		  db.execSQL(createUIDTableQuery);
		  return true;
	  }
	  
	  // My UID
	  public boolean setMyUID(String uid){
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_MYUID,uid);
		  
		  db.delete(DatabaseHelper.TABLE_MYUID,null,null);
		  db.insert(DatabaseHelper.TABLE_MYUID,null,values);
		  return true;
	  }
	  
	
	  
	  private ArrayList<String> getData(Cursor c, String key) {
		  ArrayList<String> datas = new ArrayList<String>();
		  
		  if (c != null ) {
			    if  (c.moveToFirst()) {
			        do {
			            String frag = c.getString(c.getColumnIndex(key));
			            datas.add(frag);
			        }while (c.moveToNext());
			    }
			}
			c.close();
		  return datas;
	  }
	  
	  public ArrayList<String> getUIDList(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] uid_columns={DatabaseHelper.COLUMN_UID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_UIDLIST,uid_columns,null,null,null,null,null);
		  
		  return getData(cursor, COLUMN_UID);
	  }
	  
	  public Cursor getMsgList(String uid){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] msg_columns={DatabaseHelper.COLUMN_SENDUID, DatabaseHelper.COLUMN_MSG, DatabaseHelper.COLUMN_TIMESTAMP};
		  Cursor cursor = db.query(uid,msg_columns,null,null,null,null,null);
		  return cursor;
	  }
	  
	  // returns the previously used MYUID
	  // use this ONLY IF you can assert that this app is used once before
	  public String getMyUID(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_MYUID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  return cursor.getString(0);
	  }
	  
	  // returns false if there is already a row inside MYUID table, true otherwise
	  // i.e. using first time -> true, else -> false
	  public boolean isFirst(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_MYUID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  if(cursor.getCount()==0) return true;
		  return false;
	  }

}
