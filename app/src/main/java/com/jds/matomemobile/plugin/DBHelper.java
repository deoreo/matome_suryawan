package com.jds.matomemobile.plugin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Andhika Kurniawanto on 4/14/15.
 */

public class DBHelper extends SQLiteOpenHelper {

    private SQLiteDatabase customDB;
    private final static String tablename = "storage";
    private final String[] columns = {"id", "title","content", "link", "thumbnail", "status"};
    private final Context context;
    private static final String DB_NAME = "notifications.sqlite";
    private final static String DB_PATH = "/data/data/com.jds.matomemobile/";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;

    }

    public void createDatabase() {
        boolean dbExist = checkDatabase();

        if(dbExist) {
            Log.v("DB Exist", "database exist");
        } else {
            this.getReadableDatabase();
            try {
                this.close();
                copyDatabase();
                Log.v("DB Created: ", "Database successfully created");
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    //check database first, not create new database when there is one available
    private boolean checkDatabase(){

        boolean checkDB = false;
        try {
            String dbPath = DB_PATH + DB_NAME;
            File dbFile = new File(dbPath);
            checkDB = dbFile.exists();
        } catch (SQLiteException e) {

        }
        return checkDB;
    }

    private void copyDatabase() throws IOException {
        String output = DB_PATH + DB_NAME;
        Log.d("message", output);

        OutputStream outputStream = new FileOutputStream(output);
        InputStream inputStream = context.getAssets().open(DB_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.flush();
        outputStream.close();

    }

    public void db_delete() {
        File file = new File(DB_PATH + DB_NAME);
        if(file.exists()){
            file.delete();
            Log.v("DB_DELETE", "Database deleted");
        }
    }

    public void updateContent(String table, ContentValues values, String where, String[] where_parameter) throws SQLException {
        if(checkDatabase()) {
            if(!customDB.isOpen()) {
                openDatabase();
            }
            customDB.update(table, values, where + " = ?", where_parameter);
        }
    }

    public void openDatabase() {

        if(checkDatabase()) {
            String dbPath = DB_PATH + DB_NAME;
            customDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

            Log.d("database: ", customDB.getPath().toString());
        }
    }

    public void insertData(String table, ContentValues values) throws SQLException {
        if(checkDatabase()) {
            if (!customDB.isOpen()) {
                openDatabase();
            }
            customDB.insert(table,null, values);
        }
    }

    public int countUnreadData() throws SQLException{

        Cursor cursor;
        int count = 0;
        if(checkDatabase()) {
            if (!customDB.isOpen()) {
                openDatabase();
            }

            cursor = customDB.query(tablename, columns, "status = 0", null, null, null, null);

            count = cursor.getCount();

            cursor.moveToFirst();

            cursor.close();

        }

        return count;
    }

    public int countData() throws SQLException{

        Cursor cursor;
        //ArrayList<ArrayList<Object>> dataArray = new ArrayList<ArrayList<Object>>();
        int count = 0;
        if(checkDatabase()) {
            if (!customDB.isOpen()) {
                openDatabase();
            }

            cursor = customDB.query(tablename, columns, null, null, null, null, null);

            count = cursor.getCount();

            cursor.moveToFirst();

            cursor.close();
/*
            if(!cursor.isAfterLast()){
                do {
                    ArrayList<Object> datalist = new ArrayList<Object>();
                    datalist.add(cursor.getInt(0));
                    datalist.add(cursor.getString(1));
                    datalist.add(cursor.getString(2));
                    datalist.add(cursor.getString(3));
                    datalist.add(cursor.getInt(4));

                    dataArray.add(datalist);
                }
                while (cursor.moveToNext());
            }
*/
        }

        return count;
    }

    public ArrayList<ArrayList<Object>> getAllData() throws SQLException{

        Cursor cursor;
        ArrayList<ArrayList<Object>> dataArray = new ArrayList<ArrayList<Object>>();

        if(checkDatabase()) {
            if (!customDB.isOpen()) {
                openDatabase();
            }


            cursor = customDB.query(tablename, columns, null, null, null, null, null);

            cursor.moveToFirst();

            if(!cursor.isAfterLast()){
                do {
                    ArrayList<Object> datalist = new ArrayList<Object>();
                    datalist.add(cursor.getInt(0));
                    datalist.add(cursor.getString(1));
                    datalist.add(cursor.getString(2));
                    datalist.add(cursor.getString(3));
                    datalist.add(cursor.getInt(4));
                    datalist.add(cursor.getInt(5));

                    dataArray.add(datalist);
                }
                while (cursor.moveToNext());
            }

            cursor.close();

        }

        return dataArray;
    }


    public synchronized void closeDatabase() {
        if(customDB != null) {
            customDB.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if(newVersion > oldVersion){
            Log.v("Database Upgrade", "Database Version is higher, deleted database");
            try {
                copyDatabase();
            } catch (IOException e) {
                Log.d("data", e.getMessage().toString());
            }

        }
    }
}
