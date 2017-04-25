package com.smartphoneappdev.wcd.connecfourt;

import android.provider.BaseColumns;

/**
 * Created by James on 19/04/2015.
 * Similar construct to the contract I used for an app last year, much simpler with only one table.
 */

public final class DatabaseContract {
    public static final String DATABASE_NAME = "GAME_HISTORY";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {
    }

    /* Inner classes that define the tables/contents */
    public static abstract class Statistics implements BaseColumns {
        public static final String TABLE_NAME = "Statistics";
        public static final String COLUMN_NAME_GAME_NUMBER = "game_number";
        public static final String COLUMN_NAME_TIME_TAKEN = "time_taken";
        public static final String COLUMN_NAME_PLAYER_START = "player_start";
        public static final String COLUMN_NAME_GAME_WINNER = "game_winner";

    }

}
