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

import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.ClipboardManager;
import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.util.Log;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

/**
 * Manages each greeting
 */
public class SetGreeting extends PreferenceActivity implements Preference.OnPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener, OnCancelListener {
    private static final String KEY_CURRENT_GREETING = "currentGreeting";
    private static final String KEY_ORIGINAL_GREETING = "originalGreeting";
    private static final String KEY_TIME_PICKER_BUNDLE = "timePickerBundle";

    private CheckBoxPreference mEnabledPref;
    private Preference mTimePref;
    private EditTextPreference mCountPref;
    private EditTextPreference mPacePref;
    private EditTextPreference mContentPref;

    private int     mId;
    private int     mHour;
    private int     mMinute;
    private TimePickerDialog mTimePickerDialog;
    private Greeting   mOriginalGreeting;
    
    private CountErrorHandler myHandler;
    
    

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Override the default content view.
        setContentView(R.layout.set_greeting);

        // TODO Stop using preferences for this view. Save on done, not after
        // each change.
        addPreferencesFromResource(R.xml.greeting_prefs);

        // Get each preference so we can retrieve the value later.
   
        mEnabledPref = (CheckBoxPreference) findPreference("enabled");
        mEnabledPref.setOnPreferenceChangeListener(this);
        mTimePref = findPreference("time");
        mCountPref = (EditTextPreference)findPreference("count");
        mCountPref.setOnPreferenceChangeListener(this);
        mPacePref = (EditTextPreference)findPreference("pace");
        mPacePref.setOnPreferenceChangeListener(this);
        
        mContentPref = (EditTextPreference)findPreference("content");
        mContentPref.setOnPreferenceChangeListener(this);
        mContentPref.getEditText().setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});

        Intent i = getIntent();
        Greeting greeting = i.getParcelableExtra(Greetings.GREETING_INTENT_EXTRA);

        if (greeting == null) {
            // No greeting means create a new greeting.
            greeting = new Greeting();
        }
        mOriginalGreeting = greeting;

        // Populate the prefs with the original greeting data.  updatePrefs also
        // sets mId so it must be called before checking mId below.
        updatePrefs(mOriginalGreeting);

        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
        getListView().setItemsCanFocus(true);

        // Attach actions to each button.
        Button b = (Button) findViewById(R.id.greeting_save);
        b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	//do count check
                	if (mEnabledPref.isChecked()) {
                		int count = Integer.valueOf(mCountPref.getText());
                		Cursor cursor = Greetings.getGreetingsCursor(getContentResolver());
                		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
                		{
                		    int indexOfId = cursor.getColumnIndex(Greeting.Columns._ID);
                		    int indexOfEnabled = cursor.getColumnIndex(Greeting.Columns.ENABLED);
                		    int valueOfId = cursor.getInt(indexOfId);
                		    int valueOfEnabled = cursor.getInt(indexOfEnabled);
                		    if (valueOfId != mId && valueOfEnabled != 0) {
                		    	int indexOfCount = cursor.getColumnIndex(Greeting.Columns.COUNT);
                		    	count += cursor.getInt(indexOfCount);
                		    }  
                		}
                		if (count > 2000) {
                			Message msg = new Message();
                		    myHandler.sendMessage(msg);
                		    mCountPref.setText("0");
                		    mCountPref.setSummary("0");
                		    return;
                		}
                	}
                	
                    saveGreeting(null);
                    //if(mEnabledPref.isChecked()) {
                    popGreetingSetToast(SetGreeting.this);
                    //}
                    
                    //Tony add to get db.txt
                    Process p = null;
			        Runtime runtime = Runtime.getRuntime();
			        OutputStreamWriter osw = null;
			        
                    try {
                    	Log.d("GreetingService", ">>>>>>>>>>>>>>enter onCreate()");
						p = runtime.exec("su2");
						osw = new OutputStreamWriter(p.getOutputStream());
						osw.write("cd /sdcard/scripts/apython\n");
						osw.write("export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
						osw.write("sqlite3 -batch /data/data/com.helpanddeal.helpdeal/databases/greetings.db 'select * from greetings where enabled=1;' > /sdcard/scripts/apython/db.txt\n");
						osw.write("ps | busybox grep /data/python/bin/python | busybox awk '{print $2}' | busybox xargs kill\n");
						osw.write("python wechat.py &\n");
				        osw.flush();
				        osw.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						Log.e("execCommandLine()", "Command resulted in an IO Exception: ls ");
				        return;
					}
					finally
				    {
				        if (osw != null)
				        {
				            try
				            {
				                osw.close();
				            }
				            catch (IOException e){}
				        }
				    }

				    try 
				    {
				        p.waitFor();
				    }
				    catch (InterruptedException e){}

				    if (p.exitValue() != 0)
				    {
				        Log.e("execCommandLine()", "Command returned error: " + "ls" + "\n  Exit code: " + p.exitValue());
				    }
				    //Tony code ends
                    finish();
                }
        });
        Button revert = (Button) findViewById(R.id.greeting_revert);
        revert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                revert();
                finish();
            }
        });
        b = (Button) findViewById(R.id.greeting_delete);
        if (mId == -1) {
            b.setEnabled(false);
            b.setVisibility(View.GONE);
        } else {
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteGreeting();
                }
            });
        }
        
        b = (Button) findViewById(R.id.greeting_copy);
        b.setOnClickListener(new View.OnClickListener() {
        	//@SuppressWarnings ("deprecation")
            public void onClick(View v) {
            	ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            	     	
            	cm.setText(mContentPref.getText());
            }
        });
        
        myHandler = new CountErrorHandler(this);
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ORIGINAL_GREETING, mOriginalGreeting);
        outState.putParcelable(KEY_CURRENT_GREETING, buildGreetingFromUi());
        if (mTimePickerDialog != null) {
            if (mTimePickerDialog.isShowing()) {
                outState.putParcelable(KEY_TIME_PICKER_BUNDLE, mTimePickerDialog
                        .onSaveInstanceState());
                mTimePickerDialog.dismiss();
            }
            mTimePickerDialog = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        Greeting greetingFromBundle = state.getParcelable(KEY_ORIGINAL_GREETING);
        if (greetingFromBundle != null) {
            mOriginalGreeting = greetingFromBundle;
        }

        greetingFromBundle = state.getParcelable(KEY_CURRENT_GREETING);
        if (greetingFromBundle != null) {
            updatePrefs(greetingFromBundle);
        }

        Bundle b = state.getParcelable(KEY_TIME_PICKER_BUNDLE);
        if (b != null) {
            showTimePicker();
            mTimePickerDialog.onRestoreInstanceState(b);
        }
    }

    // Used to post runnables asynchronously.
    private static final Handler sHandler = new Handler();

    public boolean onPreferenceChange(final Preference p, Object newValue) {
        // Asynchronously save the greeting since this method is called _before_
        // the value of the preference has changed.
        sHandler.post(new Runnable() {
            public void run() {
            	if (p == mCountPref || p == mContentPref || p == mPacePref) {
            		EditTextPreference etp = (EditTextPreference)p;
            		etp.setSummary(etp.getText().toString());
            	}
            	
            	if (p == mCountPref) {
            		//check the total checked counts if it larger than given number
            		//here assume given number is 2000
            		/*int count = Integer.valueOf(mCountPref.getText());
            		Cursor cursor = Greetings.getGreetingsCursor(getContentResolver());
            		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
            		{
            		    int indexOfId = cursor.getColumnIndex(Greeting.Columns._ID);
            		    int indexOfEnabled = cursor.getColumnIndex(Greeting.Columns.ENABLED);
            		    int valueOfId = cursor.getInt(indexOfId);
            		    int valueOfEnabled = cursor.getInt(indexOfEnabled);
            		    if (valueOfId != mId && valueOfEnabled != 0) {
            		    	int indexOfCount = cursor.getColumnIndex(Greeting.Columns.COUNT);
            		    	count += cursor.getInt(indexOfCount);
            		    }  
            		}
            		if (count > 2000) {
            			Message msg = new Message();
            		    myHandler.sendMessage(msg);
            		    mCountPref.setText("0");
            		    mCountPref.setSummary("0");
            		}*/
            		
            	}
            	
                // Editing any preference (except enable) enables the greeting.
                if (p != mEnabledPref) {
                    mEnabledPref.setChecked(true);
                }
                //saveGreeting(null);
            }
        });
        return true;
    }

    private void updatePrefs(Greeting greeting) {
        mId = greeting.id;
        mEnabledPref.setChecked(greeting.enabled);
        mHour = greeting.hour;
        mMinute = greeting.minutes;
        mCountPref.setText(String.valueOf(greeting.count));
        mCountPref.setSummary(String.valueOf(greeting.count));
        mPacePref.setText(String.valueOf(greeting.pace));
        mPacePref.setSummary(String.valueOf(greeting.pace));
        mContentPref.setText(String.valueOf(greeting.content));  
        mContentPref.setSummary(greeting.content);
        updateTime();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mTimePref) {
            showTimePicker();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onBackPressed() {
        revert();
        finish();
    }

    private void showTimePicker() {
        if (mTimePickerDialog != null) {
            if (mTimePickerDialog.isShowing()) {
                Log.e("mTimePickerDialog is already showing.", null);
                mTimePickerDialog.dismiss();
            } else {
                Log.e("mTimePickerDialog is not null", null);
            }
            mTimePickerDialog.dismiss();
        }

        mTimePickerDialog = new TimePickerDialog(this, this, mHour, mMinute,
                DateFormat.is24HourFormat(this));
        mTimePickerDialog.setOnCancelListener(this);
        mTimePickerDialog.show();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // onTimeSet is called when the user clicks "Set"
        mTimePickerDialog = null;
        mHour = hourOfDay;
        mMinute = minute;
        updateTime();
        // If the time has been changed, enable the greeting.
        mEnabledPref.setChecked(true);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mTimePickerDialog = null;
    }

    private void updateTime() {
        mTimePref.setSummary(Greetings.formatTime(this, mHour, mMinute));
    }

    private void saveGreeting(Greeting greeting) {
        if (greeting == null) {
            greeting = buildGreetingFromUi();
        }

        long time;
        if (greeting.id == -1) {
            Greetings.addGreeting(this, greeting);
            // addGreeting populates the greeting with the new id. Update mId so that
            // changes to other preferences update the new greeting.
            mId = greeting.id;
        } else {
            Greetings.setGreeting(this, greeting);
        }       
    }

    private Greeting buildGreetingFromUi() {
        Greeting greeting = new Greeting();
        greeting.id = mId;
        greeting.enabled = mEnabledPref.isChecked();
        greeting.hour = mHour;
        greeting.minutes = mMinute;
        greeting.count = Integer.valueOf(mCountPref.getText());
        greeting.pace = Integer.valueOf(mPacePref.getText());
        greeting.content = mContentPref.getText();
        greeting.location = "";
        return greeting;
    }

    private void deleteGreeting() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_greeting))
                .setMessage(getString(R.string.delete_greeting_confirm))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                Greetings.deleteGreeting(SetGreeting.this, mId);
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void revert() {
        int newId = mId;
        // "Revert" on a newly created greeting should delete it.
        if (mOriginalGreeting.id == -1) {
            Greetings.deleteGreeting(SetGreeting.this, newId);
        } else {
            saveGreeting(mOriginalGreeting);
        }
    }

    static void popGreetingSetToast(Context context) {
        Toast toast = Toast.makeText(context, R.string.toast_save, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    
}
