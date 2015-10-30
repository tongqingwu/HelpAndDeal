/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import java.util.Calendar;

public final class Greeting implements Parcelable {

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<Greeting> CREATOR
            = new Parcelable.Creator<Greeting>() {
                public Greeting createFromParcel(Parcel p) {
                    return new Greeting(p);
                }

                public Greeting[] newArray(int size) {
                    return new Greeting[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(count);
        p.writeString(content);
        p.writeInt(pace);
        p.writeString(location);
    }
    //////////////////////////////
    // end Parcelable apis
    //////////////////////////////

    //////////////////////////////
    // Column definitions
    //////////////////////////////
    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.helpanddeal.helpdeal/greeting");

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";
        
        public static final String COUNT = "count";
        
        public static final String CONTENT = "content";
        
        public static final String PACE = "pace";
        
        public static final String LOCATION = "location";

        
        /**
         * True if alarm is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        static final String[] GREETING_QUERY_COLUMNS = {
            _ID, HOUR, MINUTES, COUNT, CONTENT, PACE, LOCATION,
            ENABLED };

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int GREETING_ID_INDEX = 0;
        public static final int GREETING_HOUR_INDEX = 1;
        public static final int GREETING_MINUTES_INDEX = 2;
        public static final int GREETING_COUNT_INDEX = 3;
        public static final int GREETING_CONTENT_INDEX = 4;
        public static final int GREETING_PACE_INDEX = 5;
        public static final int GREETING_LOCATION_INDEX = 6;
        public static final int GREETING_ENABLED_INDEX = 7;
       
    }
    //////////////////////////////
    // End column definitions
    //////////////////////////////

    // Public fields
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes; 
    public int        count;
    public String     content;
    public int        pace;
    public String     location;

    public Greeting(Cursor c) {
        id = c.getInt(Columns.GREETING_ID_INDEX);
        enabled = c.getInt(Columns.GREETING_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.GREETING_HOUR_INDEX);
        minutes = c.getInt(Columns.GREETING_MINUTES_INDEX);
        count = c.getInt(Columns.GREETING_COUNT_INDEX);
        content = c.getString(Columns.GREETING_CONTENT_INDEX);
        pace = c.getInt(Columns.GREETING_PACE_INDEX);
        location = c.getString(Columns.GREETING_LOCATION_INDEX);
        
    }

    public Greeting(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        count = p.readInt();
        content = p.readString();
        pace = p.readInt();
        location = p.readString();
        
    }

    // Creates a default greeting at the current time.
    public Greeting() {
        id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        count = 0;
        content = "";
        pace = 0;
        location = "";
        
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Greeting)) return false;
        final Greeting other = (Greeting) o;
        return id == other.id;
    }
}
