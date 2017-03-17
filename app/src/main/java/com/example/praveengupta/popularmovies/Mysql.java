package com.example.praveengupta.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Praveen Gupta on 7/26/2016.
 */
public class Mysql extends SQLiteOpenHelper {
    public Mysql(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE FAVOURITES " +
                "(ID INTEGER PRIMARY KEY, VIDEO VARCHAR(1000), REVIEW VARCHAR(20000), MOVIE VARCHAR(1000));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE FAVOURITES;");
        onCreate(sqLiteDatabase);
    }
}
