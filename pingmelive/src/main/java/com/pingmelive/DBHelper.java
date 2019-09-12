package com.pingmelive;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;


import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "errorData";
    private static final int DATABASE_VERSION = 1;

    private static final String DATA = "DATA";
    private static final String DATA_ID = "DATA_ID";
    private static final String DATA_DEVICE_INFO = "DATA_DEVICE_INFO";
    private static final String DATA_ERROR_INFO = "DATA_ERROR_INFO";
    private static final String DATA_ERROR_TRACE = "DATA_ERROR_TRACE";
    private static final String DATA_DATE_TIME = "DATA_DATE_TIME";

    private static DBHelper mInstance = null;
    Context context;

    public static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
        }
        return mInstance;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String DATA_TABLE =
                "CREATE TABLE IF NOT EXISTS " + DATA + "(" +
                        DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DATA_DEVICE_INFO + " TEXT, " +
                        DATA_ERROR_INFO + " TEXT, " +
                        DATA_ERROR_TRACE + " TEXT, " +
                        DATA_DATE_TIME + " TEXT)";

        try {
            db.execSQL(DATA_TABLE);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqldb, int oldVersion, int newVersion) {
        Log.e("TAG","Database on upgrade got called - - ");

        try {

            onCreate(sqldb);

            if(newVersion>oldVersion)
            {


            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void updateField(SQLiteDatabase sqldb,String tableName,String field_name)
    {
        if(!existsColumnInTable(sqldb,tableName,field_name)) {
            sqldb.execSQL("ALTER TABLE "+tableName+" ADD COLUMN "+field_name+" TEXT");
        }
        else {
            Log.e("Database : ","Feild "+field_name+" already exists not updatin.");
        }
    }

    private boolean existsColumnInTable(SQLiteDatabase sqldb, String inTable, String columnToCheck) {

        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = sqldb.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            Log.d("existsColumnInTable", "When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
            return false;
        } finally {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
        }
    }

    public ArrayList<pingModel> getPendingEvents()
    {
        ArrayList<pingModel> pingModels = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String filterData = "select * from "+DATA;

        Cursor cursor = db.rawQuery(filterData,null);

        if (cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    pingModel pingModel = new pingModel();

                    pingModel.setData_id(cursor.getInt(cursor.getColumnIndex(DATA_ID)));
                    pingModel.setData_device_info(cursor.getString(cursor.getColumnIndex(DATA_DEVICE_INFO)));
                    pingModel.setData_error_info(cursor.getString(cursor.getColumnIndex(DATA_ERROR_INFO)));
                    pingModel.setData_error_trace(cursor.getString(cursor.getColumnIndex(DATA_ERROR_TRACE)));
                    pingModel.setData_date_time(cursor.getString(cursor.getColumnIndex(DATA_DATE_TIME)));

                    pingModels.add(pingModel);
                } while (cursor.moveToNext());
            }
        }


        return pingModels;
    }

    public void addEvent(pingModel pingModel) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DATA_DEVICE_INFO,getDeviceInfo());
        values.put(DATA_ERROR_INFO,pingModel.getData_error_info());
        values.put(DATA_ERROR_TRACE,pingModel.getData_error_trace());
        values.put(DATA_DATE_TIME,getDatetime("HH:mm:ss dd-MM-yyyy",0));

        db.insert(DATA,null,values);
        Log.e("pingMeLive","New Error Event Added - "+pingModel.getData_error_info());
        sendData();
    }

    public void removeEvent(int id) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM "+DATA+" WHERE "+DATA_ID+" = '"+id+"'");
        // close db connection
        //db.close();
    }

    public void sendData()
    {
        Log.e("pingMeLive","Sending error log to server (if any)");
        startSerivceNEW(new Intent(context,sendLogToServer.class));
    }

    public void startSerivceNEW(Intent i)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

    public String getDeviceInfo(){

        JSONObject device_info = new JSONObject();
        try
        {
            device_info.put("Device Name",""+ Build.MANUFACTURER);
            device_info.put("Device Model",""+Build.MODEL);
            device_info.put("Android Version",""+ Build.VERSION.RELEASE);
            device_info.put("Android Version Code",""+Build.VERSION.SDK_INT);
            device_info.put("App Version Code",""+BuildConfig.VERSION_CODE);
            device_info.put("App Version Name",""+BuildConfig.VERSION_NAME);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return device_info.toString();
    }

    public static String getTodayYestFromMilli(long msgTimeMillis) {

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(msgTimeMillis);
        // get Currunt time
        Calendar now = Calendar.getInstance();

        final String strDateFormate = "dd-MM-yyyy";

        if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)
                &&
                ((now.get(Calendar.MONTH) == messageTime.get(Calendar.MONTH)))
                &&
                ((now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)))
        ) {

            return "Today";

        } else if (
                ((now.get(Calendar.DATE) - messageTime.get(Calendar.DATE)) == 1)
                        &&
                        ((now.get(Calendar.MONTH) == messageTime.get(Calendar.MONTH)))
                        &&
                        ((now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)))
        ) {
            return "Yesterday";
        } else {
            return ""+ DateFormat.format(strDateFormate, messageTime);
        }
    }

    public static String getDatetime(String dateFormat,long milliSeconds) {

        if(milliSeconds<=0)
        {
            milliSeconds = System.currentTimeMillis();
        }
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
