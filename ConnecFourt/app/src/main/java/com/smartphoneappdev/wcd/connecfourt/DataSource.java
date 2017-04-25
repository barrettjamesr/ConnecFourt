package com.smartphoneappdev.wcd.connecfourt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by JRB on 5/11/2016.
 * SQL codes for creating and accessing the DB
 */

public class DataSource {
    // Database fields
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context appContext;

    private static final String CommaSep = ", ";
    private static final String OpenBrac = " (";
    private static final String CloseBrac = ")";
    private static final String SemiColon = "; ";
    private static final String All = "* ";
    private static final String SELECT_ = "SELECT ";
    private static final String DELETE_ = "DELETE ";
    private static final String FROM_ = " FROM ";
    private static final String WHERE_ = " WHERE ";
    private static final String AS_ = " AS ";
    private static final String MAX_ = "MAX(";
    private static final String TOP_ = "TOP(";
    private static final String SUM_ = "SUM(";
    private static final String COUNT_ = "COUNT(";
    private static final String EQUALS_ = " = ";
    private static final String SORT_ = " ORDER BY ";
    private static final String ASC = " ASC ";
    private static final String DESC = " DESC ";
    private static final String GROUP_ = " GROUP BY ";

    public DataSource (Context context) {
        dbHelper = new DbHelper(context);
        appContext = context;

    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    //Get a specified field form a specified table.
    public String getField (Integer pk_number, String table_name, String column_name) {
        Cursor c = dbHelper.getReadableDatabase().rawQuery(SELECT_
                        + column_name
                        + FROM_ + table_name
                        + WHERE_ + DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER + EQUALS_ + pk_number
                , null );
        if (c != null){
            c.moveToFirst();
            String str = String.valueOf(c.getInt(0));
            if(str.equals("0"))
                str = c.getString(0);
            c.close();
            return str;
        } else {
            return "";
        }
    }

    //Write a row to the DB to save the details of the game that just finished
    public void saveGame (int pk_number, java.util.Date start_time, int winner, int player_start) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        java.util.Date date = new java.util.Date();
        long diff = (date.getTime() - start_time.getTime())/1000;

        values.put(DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER, pk_number);
        values.put(DatabaseContract.Statistics.COLUMN_NAME_TIME_TAKEN, diff);
        values.put(DatabaseContract.Statistics.COLUMN_NAME_GAME_WINNER, winner);
        values.put(DatabaseContract.Statistics.COLUMN_NAME_PLAYER_START, player_start);

        long insert_id = database.insert(DatabaseContract.Statistics.TABLE_NAME, null, values);
    }

    //Get how many entries are in the DB
    public int LastEntry (String table_name, String column_name) {
        Cursor c = dbHelper.getReadableDatabase().rawQuery(SELECT_ + MAX_
                        + column_name + CloseBrac + AS_ + column_name + FROM_
                        + table_name
                , null );

        if (c != null){
            c.moveToFirst();
            int IntI = c.getInt(c.getColumnIndex(column_name));
            c.close();
            return IntI;
        } else {
            return 0;
        }
    }

    //Get the list of all winners in games played
    public List<Integer> getWinners() {
        Cursor c = dbHelper.getReadableDatabase().rawQuery(SELECT_ + DatabaseContract.Statistics.COLUMN_NAME_GAME_WINNER
                + FROM_ + DatabaseContract.Statistics.TABLE_NAME
                + SORT_ + DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER + ASC , null );

        if (c != null){
            c.moveToFirst();
            List<Integer> games = new ArrayList<Integer>();
            while(!c.isAfterLast()) {
                games.add(c.getInt(0));
                c.moveToNext();
            }
            c.close();
            return games;
        } else {
            return null;
        }
    }

    //Delete all games from the DB to reset the stats counter
    public void resetStats (String table_name) {
        dbHelper.getWritableDatabase().execSQL(DELETE_
                         + FROM_ + table_name);
        dbHelper.close();
    }

}



