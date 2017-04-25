package com.smartphoneappdev.wcd.connecfourt;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by James on 19/04/2015.
 * Helper to set up the database and tables
 * Probably overkill for this assignment but I like to stick to best-practises.
 */

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GAME_HISTORY.db";

    private static final String CommaSep = ", ";
    private static final String OpenBrac = " (";
    private static final String CloseBrac = ")";
    private static final String SemiColon = "; ";
    private static final String IntType = " INTEGER";
    private static final String TextType = " TEXT";
    private static final String NotNull = " NOT NULL";
    private static final String PKey = " PRIMARY KEY";
    private static final String CreateTable = "CREATE TABLE IF NOT EXISTS ";

    private static final String SQL_CREATE_STATISTICS_TABLE = CreateTable + DatabaseContract.Statistics.TABLE_NAME
            + OpenBrac + DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER + IntType + NotNull
            + CommaSep + DatabaseContract.Statistics.COLUMN_NAME_TIME_TAKEN + IntType
            + CommaSep + DatabaseContract.Statistics.COLUMN_NAME_PLAYER_START + IntType + NotNull
            + CommaSep + DatabaseContract.Statistics.COLUMN_NAME_GAME_WINNER + IntType + NotNull
            + CommaSep + PKey + OpenBrac + DatabaseContract.Statistics.COLUMN_NAME_GAME_NUMBER + CloseBrac
            + CloseBrac + SemiColon;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATISTICS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Work out what to do on upgrades!!
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}