package com.example.mingle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

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
	  public static final String COLUMN_PIC_PATH_1 = "path_1";
	  public static final String COLUMN_PIC_PATH_2 = "path_3";
	  public static final String COLUMN_PIC_PATH_3 = "path_2";
	  public static final String COLUMN_TIMESTAMP = "ts";
	  public static final String COLUMN_IS_ME = "is_me";
	  public static final String COLUMN_MSG = "msg";
	  public static final String COLUMN_DIST = "dist";

	  private static final String DATABASE_NAME = "minglelocal.db";
	  private static final int DATABASE_VERSION = 1;
	  
	  private MingleApplication app;
	  private static DatabaseHelper sInstance = null;
	  
	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_UIDLIST + "(" + COLUMN_UID
	      + " text not null, " + COLUMN_NUM
	      + " int not null, " + COLUMN_COMM
	      + " text not null, " + COLUMN_DIST
	      + " int not null);";
	  private static final String MYUID_CREATE = "create table "
	      + TABLE_MYUID + "(" + COLUMN_MYUID
	      + " text not null, "+ COLUMN_SEX
	      + " text not null, " + COLUMN_NUM
	      + " int not null, " + COLUMN_COMM
	      + " text not null, " + COLUMN_LOC_LAT
	      + " float not null, " + COLUMN_LOC_LONG
	      + " float not null, " + COLUMN_DIST_LIM
	      + " int not null, " + COLUMN_PIC_PATH_1
	      + " text not null, " + COLUMN_PIC_PATH_2
	      + " text not null, " + COLUMN_PIC_PATH_3
	      + " text not null);";
	 
	  
	  public static DatabaseHelper getInstance(Context context, MingleApplication app) {
		  if(sInstance == null)
			  sInstance = new DatabaseHelper(context.getApplicationContext(), app);
		  
		  return sInstance;
	  }
	  
	  /* Database constructor*/
	  public DatabaseHelper(Context context, MingleApplication app) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    this.app = app;
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
	  public boolean insertMessages(String uid, boolean is_me, String msg, String msg_ts) {
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  String strIsMe = "N";
		  if(is_me) strIsMe = "Y";
		  values.put(DatabaseHelper.COLUMN_IS_ME, strIsMe);
		  values.put(DatabaseHelper.COLUMN_MSG, msg);
		  values.put(DatabaseHelper.COLUMN_TIMESTAMP, msg_ts);
		  db.insert("\""+uid+"\"",null,values);
		  
		  db.close();
		  return true;
	  }
	  
	  // Insert UID of other users in database
	  public boolean insertNewUID(String uid, int num, String comm, int dist){
		  SQLiteDatabase db = this.getWritableDatabase();
		  ContentValues values = new ContentValues();
		  values.put(DatabaseHelper.COLUMN_UID, uid);
		  values.put(DatabaseHelper.COLUMN_NUM, num);
		  values.put(DatabaseHelper.COLUMN_COMM, comm);
		  values.put(DatabaseHelper.COLUMN_DIST, dist);
		  db.insert(DatabaseHelper.TABLE_UIDLIST, null, values);
		  
		  String createUIDTableQuery = "create table " + "\"" + uid + "\""
				  + "(" + COLUMN_IS_ME + " text not null, " + COLUMN_MSG + " text not null, " 
				  + COLUMN_TIMESTAMP + " text not null);";
		  db.execSQL(createUIDTableQuery);
		  
		  db.close();
		  //app.getMsgUidList().add(uid);
		  return true;
	  }
	  
	  // My UID
	  public boolean setMyInfo(JSONObject userData){
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		  
		  ContentValues values = new ContentValues();
		  try {
		  values.put(DatabaseHelper.COLUMN_MYUID, userData.getString("UID"));
		  values.put(DatabaseHelper.COLUMN_SEX, userData.getString("SEX"));
		  values.put(DatabaseHelper.COLUMN_NUM, userData.getInt("NUM"));
		  values.put(DatabaseHelper.COLUMN_COMM, userData.getString("COMM"));
		  values.put(DatabaseHelper.COLUMN_LOC_LAT, userData.getDouble("LOC_LAT"));
		  values.put(DatabaseHelper.COLUMN_LOC_LONG, userData.getDouble("LOC_LONG"));
		  values.put(DatabaseHelper.COLUMN_DIST_LIM, userData.getDouble("DIST_LIM"));
		  values.put(DatabaseHelper.COLUMN_PIC_PATH_1, userData.getString("PIC_PATH_1"));
		  values.put(DatabaseHelper.COLUMN_PIC_PATH_2, userData.getString("PIC_PATH_2"));
		  values.put(DatabaseHelper.COLUMN_PIC_PATH_3, userData.getString("PIC_PATH_3"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  db.delete(DatabaseHelper.TABLE_MYUID,null,null);
		  db.insert(DatabaseHelper.TABLE_MYUID,null,values);
		  System.out.println(isFirst());
		  db.close();
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
	  
	  public ArrayList<ContentValues> getUserList(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] uid_columns={DatabaseHelper.COLUMN_UID, DatabaseHelper.COLUMN_NUM, DatabaseHelper.COLUMN_COMM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_UIDLIST,uid_columns,null,null,null,null,null);
		  ArrayList<ContentValues> userList = new ArrayList<ContentValues>();
		  if(cursor!=null){
			  while(cursor.moveToNext()){
				  ContentValues tempContent = new ContentValues();
				  tempContent.put("UID", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_UID)));
				  tempContent.put("NUM", cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUM)));
				  tempContent.put("COMM",cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COMM)));
				  tempContent.put("DIST", cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DIST)));
				  userList.add(tempContent);
			  }
		  }
		  db.close();
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
				  if(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ME)).equals("N")) tempIsMe = false;
				  
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
		  
		  String return_val = cursor.getString(0);
		  db.close();
		  return return_val;
	  }
	  
	  public String getMySex(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_SEX};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  String return_val = cursor.getString(0);
		  db.close();
		  return return_val;
	  }
	  
	  public int getMyNum(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_NUM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  int return_val = cursor.getInt(0);
		  db.close();
		  return return_val;
	  }
	  
	  public String getMyComm(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_COMM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  String return_val = cursor.getString(0);
		  db.close();
		  return return_val;
	  }
	  
	  public float getMyLocLat(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_LOC_LAT};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  float return_val = cursor.getFloat(0);
		  db.close();
		  return return_val;
	  }
	  
	  public float getMyLocLong(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_LOC_LONG};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  float return_val = cursor.getFloat(0);
		  db.close();
		  return return_val;
	  }
	  
	  public int getMyDistLim(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_DIST_LIM};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  
		  int return_val = cursor.getInt(0);
		  db.close();
		  return return_val;
	  }
	  
	  public ArrayList<String> getPhotoPaths(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_PIC_PATH_1, DatabaseHelper.COLUMN_PIC_PATH_2, DatabaseHelper.COLUMN_PIC_PATH_3};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  cursor.moveToFirst();
		  ArrayList<String> return_array = new ArrayList<String>();
		  
		  for(int i = 0 ; i < 3; i++) {
			  return_array.add(cursor.getString(0));
			  if(!cursor.isLast()) cursor.moveToNext();
		  }
		  
		  db.close();
		  return return_array;
	  }
	  // returns false if there is already a row inside MYUID table, true otherwise
	  // i.e. using first time -> true, else -> false
	  public boolean isFirst(){
		  SQLiteDatabase db = this.getReadableDatabase();
		  String[] myuid_columns={DatabaseHelper.COLUMN_MYUID};
		  Cursor cursor = db.query(DatabaseHelper.TABLE_MYUID,myuid_columns,null,null,null,null,null);
		  
		  int count = cursor.getCount();
		  db.close();
		  
		  if(count == 0) return true;
		  return false;
	  }
	  
	  public JSONObject getUserData() { 
		 
		  JSONObject user_obj = new JSONObject();
		  try {
			
			  user_obj.put("UID", getMyUID());
			  user_obj.put("COMM", getMyComm());
			  user_obj.put("NUM", getMyNum());
			  user_obj.put("SEX", getMySex());
			  user_obj.put("LOC_LAT", getMyLocLat());
			  user_obj.put("LOC_LONG", getMyLocLong());
			  user_obj.put("DIST_LIM", getMyDistLim());
			  ArrayList<String> photo_array = getPhotoPaths();
			  user_obj.put("PIC_PATH_1", photo_array.get(0));
			  user_obj.put("PIC_PATH_2", photo_array.get(1));
			  user_obj.put("PIC_PATH_3", photo_array.get(2));
		  } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  return user_obj;
	  }
	  
	  public void deleteAll(){
		  SQLiteDatabase db = this.getWritableDatabase();
		  db.execSQL("DROP TABLE " + TABLE_UIDLIST);
		  db.execSQL("DROP TABLE " + TABLE_MYUID);
		  for(int i = 0; i < app.getChoiceList().size(); i++) {
			  Log.i("sktag", "DROP TABLE IF EXISTS \"" + app.getChoiceList().get(i) + "\"");
			  db.execSQL("DROP TABLE IF EXISTS \"" + app.getChoiceList().get(i) + "\"");
		  }
		  
		  db.execSQL(DATABASE_CREATE);
		  db.execSQL(MYUID_CREATE);
		  
		  db.close();
	  }
}
