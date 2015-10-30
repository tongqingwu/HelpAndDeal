/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helpanddeal.helpdeal;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Helper class for opening the database from multiple providers.  Also provides
 * some common functionality.
 */
class GreetingDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "greetings.db";
    private static final int DATABASE_VERSION = 2;//crashed once, change it to 1, tony

    public GreetingDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE greetings (" +
                   "_id INTEGER PRIMARY KEY," +
                   "hour INTEGER, " +
                   "minutes INTEGER, " +
                   "count INTEGER, " +
                   "content TEXT, " +
                   "pace INTEGER," +
                   "location TEXT," +
                   "enabled INTEGER);"                  
                   );

        // insert default alarms
        String insertMe = "INSERT INTO greetings " +
                "(hour, minutes, count, content, pace, location, enabled) VALUES "; 
        db.execSQL(insertMe + "(8, 30, 0, '', 30, '', 0);");
        db.execSQL(insertMe + "(9, 00, 0, '', 30, '', 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
            int currentVersion) {
        if (Log.LOGV) Log.v(
                "Upgrading greetings database from version " +
                oldVersion + " to " + currentVersion +
                ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS greetings");
        onCreate(db);
    }

    Uri commonInsert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert("greetings", null, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }
        if (Log.LOGV) Log.v("Added greeting rowId = " + rowId);

        return ContentUris.withAppendedId(Greeting.Columns.CONTENT_URI, rowId);
    }
}
