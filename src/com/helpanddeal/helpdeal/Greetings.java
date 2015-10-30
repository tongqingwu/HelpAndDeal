/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import java.util.Calendar;

/**
 * The Greetings provider supplies info about Greeting Clock settings
 */
public class Greetings {
	
    // This string is used when passing an Greeting object through an intent.
    public static final String GREETING_INTENT_EXTRA = "intent.extra.greeting";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";

    final static int INVALID_GREETING_ID = -1;

    /**
     * Creates a new Greeting and fills in the given greeting's id.
     */
    public static void addGreeting(Context context, Greeting greeting) {
        ContentValues values = createContentValues(greeting);
        Uri uri = context.getContentResolver().insert(
        		Greeting.Columns.CONTENT_URI, values);
    }

    /**
     * Removes an existing Greeting.  If this greeting is snoozing, disables
     * snooze.  Sets next alert.
     */
    public static void deleteGreeting(Context context, int greetingId) {
        if (greetingId == INVALID_GREETING_ID) return;

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Greeting.Columns.CONTENT_URI, greetingId);
        contentResolver.delete(uri, "", null);
    }

    /**
     * Queries all greetings
     * @return cursor over all greetings
     */
    public static Cursor getGreetingsCursor(ContentResolver contentResolver) {
        return contentResolver.query(
        		Greeting.Columns.CONTENT_URI, Greeting.Columns.GREETING_QUERY_COLUMNS,
                null, null, Greeting.Columns.DEFAULT_SORT_ORDER);
    }

    // Private method to get a more limited set of greetings from the database.
    private static Cursor getFilteredGreetingsCursor(
            ContentResolver contentResolver) {
        return contentResolver.query(Greeting.Columns.CONTENT_URI,
        		Greeting.Columns.GREETING_QUERY_COLUMNS, Greeting.Columns.WHERE_ENABLED,
                null, null);
    }

    private static ContentValues createContentValues(Greeting greeting) {
        ContentValues values = new ContentValues(8);

        values.put(Greeting.Columns.ENABLED, greeting.enabled ? 1 : 0);
        values.put(Greeting.Columns.HOUR, greeting.hour);
        values.put(Greeting.Columns.MINUTES, greeting.minutes);
        values.put(Greeting.Columns.COUNT, greeting.count);
        values.put(Greeting.Columns.CONTENT, greeting.content);
        values.put(Greeting.Columns.PACE, greeting.pace);
        values.put(Greeting.Columns.LOCATION, greeting.location);

        return values;
    }

    /**
     * Return an Greeting object representing the greeting id in the database.
     * Returns null if no greeting exists.
     */
    public static Greeting getGreeting(ContentResolver contentResolver, int greetingId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Greeting.Columns.CONTENT_URI, greetingId),
                Greeting.Columns.GREETING_QUERY_COLUMNS,
                null, null, null);
        Greeting greeting = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
            	greeting = new Greeting(cursor);
            }
            cursor.close();
        }
        return greeting;
    }


    /**
     * A convenience method to set an greeting in the Greetings
     * content provider.
     */
    public static void setGreeting(Context context, Greeting greeting) {
        ContentValues values = createContentValues(greeting);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(
                ContentUris.withAppendedId(Greeting.Columns.CONTENT_URI, greeting.id),
                values, null, null);
    }

    /**
     * A convenience method to enable or disable an greeting.
     *
     * @param id             corresponds to the _id column
     * @param enabled        corresponds to the ENABLED column
     */

    public static void enableGreeting(
            final Context context, final int id, boolean enabled) {
        enableGreetingInternal(context, id, enabled);
        //setNextAlert(context);
    }

    private static void enableGreetingInternal(final Context context,
            final int id, boolean enabled) {
        enableGreetingInternal(context, getGreeting(context.getContentResolver(), id),
                enabled);
    }

    private static void enableGreetingInternal(final Context context,
            final Greeting greeting, boolean enabled) {
        if (greeting == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Greeting.Columns.ENABLED, enabled ? 1 : 0);

        resolver.update(ContentUris.withAppendedId(
        		Greeting.Columns.CONTENT_URI, greeting.id), values, null, null);
    }

    /**
     * Given an greeting in hours and minutes, return a calendar time 
     */
    static Calendar calculateGreeting(int hour, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    static String formatTime(final Context context, int hour, int minute
                             ) {
        Calendar c = calculateGreeting(hour, minute);
        return formatTime(context, c);
    	//return "";
    }

    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
}
