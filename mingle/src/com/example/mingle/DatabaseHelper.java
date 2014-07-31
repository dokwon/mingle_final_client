package com.example.mingle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper  {
	
	  public static final String TABLE_UIDLIST = "uidlist";
	  public static final String COLUMN_UID = "_uid";
	  public static final String TABLE_MYUID = "myuid";
	  public static final String COLUMN_MYUID = "my_uid";
	  public static final String COLUMN_SEX = "sex";
	  public static final String COLUMN_NUM = "num";
	  public static final String COLUMN_COMM = "comm";
	  public static final String COLUMN_LOC_LAT = "loc_lat";
	  public static final String COLUMN_LOC_LONG = "loc_long";
	  public static final String COLUMN_DIST_LIM = "dist_lim";
	  public static final String COLUMN_TIMESTAMP = "ts";
	  public static final String COLUMN_IS_ME = "is_me";
	  public static final String COLUMN_MSG = "msg";

	  private static final String DATABASE_NAME = "minglelocal.db";
	  private static final int DATABASE_VERSION = 1;
	  public HashMap uid_list;
	  
	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_UIDLIST + "(" + COLUMN_UID
	      + " text not null, " + COLUMN_NUM
	      + " int not null, " + COLUMN_COMM
	      + " text not null, " + COLUMN_LOC_LAT
	      + " float not null, " + COLUMN_LOC_LONG
	      + " float not null, " + COLUMN_DIST_LIM
	      + " int not null);";
	  private static final String MYUID_CREATE = "create table "
	      + TABLE_MYUID + "(" + COLUMN_MYUID
	      + " text not null, "+ COLUMN_SEX
	      + " text not null, " + COLUMN_NUM
	      + " int not null, " + COLUMN_COMM
	      + " text not null, " + COLUMN_LOC_LAT
	      + " float not null, " + COLUMN_LOC_LONG
	      + " float not null, " + COLUMN_DIST_LIM
	      + " int not null);";
	 
	  
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
	  
	  public String getRidOfQuotes(String quotedString){
		  return quotedString.substring(1,quotedString.length()-1);
	  }
	  
	  // Insert messages to database
	  public boolean insertMessages(String uid, boolean is_me, String msg, String msg_ts) {
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  String strIsMe = "N";
		  if(is_me) strIsMe = "Y";
		  values.put(DatabaseHelper.COLUMN_IS_ME, strIsMe);
		  values.put(DatabaseHelper.COLUMN_MSG, msg);
		  values.put(DatabaseHelper.COLUMN_TIMESTAMP, msg_ts);
		  db.insert("\""+uid+"\"",null,values);
		  return true;
	  }
	  
	  // Insert UID of other users in database
	  public boolean insertNewUID(String uid, int num, String comm, float loc_lat, float loc_long, int dist_lim){
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_UID,"\""+uid+"\"");
		  values.put(DatabaseHelper.COLUMN_NUM,num);
		  values.put(DatabaseHelper.COLUMN_COMM,comm);
		  values.put(DatabaseHelper.COLUMN_LOC_LAT,loc_lat);
		  values.put(DatabaseHelper.COLUMN_LOC_LONG,loc_long);
		  values.put(DatabaseHelper.COLUMN_DIST_LIM,dist_lim);
		  db.insert(DatabaseHelper.TABLE_UIDLIST,null,values);
		  
		  String createUIDTableQuery = "create table " + "\"" + uid + "\""
				  + "(" + COLUMN_IS_ME + " text not null, " + COLUMN_MSG + " text not null, " 
				  + COLUMN_TIMESTAMP + " text not null);";
		  db.execSQL(createUIDTableQuery);
		  return true;
	  }
	  
	  // My UID
	  public boolean setMyInfo(String uid, String sex, int num, String comm, float loc_lat, float loc_long, int dist_lim){
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_MYUID,"\""+uid+"\"");
		  values.put(DatabaseHelper.COLUMN_SEX, sex);
		  values.put(DatabaseHelper.COLUMN_NUM, num);
		  values.put(DatabaseHelper.COLUMN_COMM, comm);
		  values.put(DatabaseHelper.COLUMN_LOC_LAT, loc_lat);
		  values.put(DatabaseHelper.COLUMN_LOC_LONG, loc_long);
		  values.put(DatabaseHelper.COLUMN_DIST_LIM, dist_lim);
		  
		  db.delete(DatabaseHelper.TABLE_MYUID,null,null);
		  db.insert(DatabaseHelper.TABLE_MYUID,null,values);
		  System.out.println(isFirst());
		  return true;
	  }
	  
	
	  
	  private ArrayList<String> getData(Cursor c, String key) {
		  ArrayList<String> datas = new ArrayList<String>();
		  
		  if (c != null ) {
			    if  (c.moveToFirst()) {
			        do {
			            String frag = c.getString(c.getColumnIndex(key));
			            datas.add(getRidOfQuotes(frag));
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
	  
	  public ArrayList<ContentValues> getUserList(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] uid_columns={DatabaseHelper.COLUMN_UID, DatabaseHelper.COLUMN_NUM, DatabaseHelper.COLUMN_COMM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_UIDLIST,uid_columns,null,null,null,null,null);
		  ArrayList<ContentValues> userList = new ArrayList<ContentValues>();
		  if(cursor!=null){
			  while(cursor.moveToNext()){
				  ContentValues tempContent = new ContentValues();
				  tempContent.put("UID",getRidOfQuotes(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_UID))));
				  tempContent.put("NUM", cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUM)));
				  tempContent.put("COMM",cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COMM)));
				  userList.add(tempContent);
			  }
		  }
		  return userList;
	  }
	  

	  public ArrayList<Message> getMsgList(String uid) {
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] msg_columns={DatabaseHelper.COLUMN_IS_ME, DatabaseHelper.COLUMN_MSG, DatabaseHelper.COLUMN_TIMESTAMP};
		  Cursor cursor = db.query("\""+uid+"\"",msg_columns,null,null,null,null,null);
		  ArrayList<Message> msgList = new ArrayList<Message>();
		  
		  if(cursor!=null){
			  while(cursor.moveToNext()){
				  boolean tempIsMe = true;
				  if(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ME))=="N") tempIsMe=false;
				  Message newMsg = new Message(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MSG)),-1,
						  cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP)),1,tempIsMe);
				  msgList.add(newMsg);
			  }
		  }
		  cursor.close();
		  return msgList;
	  }
	  
	  // returns the previously used MYUID
	  // use this ONLY IF you can assert that this app is used once before
	  public String getMyUID(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_MYUID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return getRidOfQuotes(cursor.getString(0));
	  }
	  
	  public String getMySex(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_SEX};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getString(0);
	  }
	  
	  public int getMyNum(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_NUM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getInt(0);
	  }
	  
	  public String getMyComm(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_COMM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getString(0);
	  }
	  
	  public float getMyLocLat(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_LOC_LAT};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getFloat(0);
	  }
	  
	  public float getMyLocLong(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_LOC_LONG};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getFloat(0);
	  }
	  
	  public int getMyDistLim(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_DIST_LIM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  return cursor.getInt(0);
	  }
	  // returns false if there is already a row inside MYUID table, true otherwise
	  // i.e. using first time -> true, else -> false
	  public boolean isFirst(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_MYUID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  
		  if(cursor.getCount() == 0) return true;
		  return false;
	  }

}
