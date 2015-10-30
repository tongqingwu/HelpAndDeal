package com.helpanddeal.helpdeal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
/**
 * HelpAndDeal application.
 */
public class HelpAndDealActivity extends Activity implements OnItemClickListener {

    static final String PREFERENCES = "HelpAndDealActivity";

    /** This must be false for production.  If true, turns on logging,
        test code, etc. */
    static final boolean DEBUG = false;
    
    private CountErrorHandler handler = new CountErrorHandler(this);

    private LayoutInflater mFactory;
    private ListView mGreetingsList;
    private Cursor mCursor;

    private void updateGreeting(boolean enabled,
            Greeting greeting) {
        Greetings.enableGreeting(this, greeting.id, enabled);
        if (enabled) {
            SetGreeting.popGreetingSetToast(this);
        }
    }

    private class GreetingAdapter extends CursorAdapter {
        public GreetingAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.greeting_time, parent, false);

            DigitalClock digitalClock =
                    (DigitalClock) ret.findViewById(R.id.digitalClock);
            digitalClock.setLive(false);
            return ret;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Greeting greeting = new Greeting(cursor);

            View indicator = view.findViewById(R.id.indicator);

            // Set the initial state of the clock "checkbox"
            final CheckBox clockOnOff =
                    (CheckBox) indicator.findViewById(R.id.clock_onoff);
            clockOnOff.setChecked(greeting.enabled);

            // Clicking outside the "checkbox" should also change the state.
            indicator.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                	//check total counts
                	if (!clockOnOff.isChecked()) {
                		int count = greeting.count;
                		Cursor cursor = Greetings.getGreetingsCursor(getContentResolver());
                		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
                		{
                		    int indexOfEnabled = cursor.getColumnIndex(Greeting.Columns.ENABLED);
                		    int valueOfEnabled = cursor.getInt(indexOfEnabled);
                		    if (valueOfEnabled != 0) {
                		    	int indexOfCount = cursor.getColumnIndex(Greeting.Columns.COUNT);
                		    	count += cursor.getInt(indexOfCount);
                		    }  
                		}
                		if (count > 2000) {
                			handler.sendMessage(new Message());
                			return;
                		}
                	}
                    clockOnOff.toggle();
                    updateGreeting(clockOnOff.isChecked(), greeting);
                    
                }
            });

            DigitalClock digitalClock =
                    (DigitalClock) view.findViewById(R.id.digitalClock);

            // set the greeting text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, greeting.hour);
            c.set(Calendar.MINUTE, greeting.minutes);
            digitalClock.updateTime(c);
            
            TextView count =
                    (TextView) digitalClock.findViewById(R.id.count);
            
            count.setText(String.valueOf(greeting.count));
            
            TextView content = (TextView) digitalClock.findViewById(R.id.content);
            content.setText(greeting.content);
            TextView pace = (TextView) digitalClock.findViewById(R.id.pace);
            pace.setText(String.valueOf(greeting.pace));
            
          
        }
    };

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_greeting: {
                // Confirm that the greeting will be deleted.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_greeting))
                        .setMessage(getString(R.string.delete_greeting_confirm))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d,
                                            int w) {
                                        Greetings.deleteGreeting(HelpAndDealActivity.this, id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }

            case R.id.enable_greeting: {
                final Cursor c = (Cursor) mGreetingsList.getAdapter()
                        .getItem(info.position);
                final Greeting greeting = new Greeting(c);
                Greetings.enableGreeting(this, greeting.id, !greeting.enabled);
                if (!greeting.enabled) {
                    SetGreeting.popGreetingSetToast(this);
                }
                return true;
            }

            case R.id.edit_greeting: {
                final Cursor c = (Cursor) mGreetingsList.getAdapter()
                        .getItem(info.position);
                final Greeting greeting = new Greeting(c);
                Intent intent = new Intent(this, SetGreeting.class);
                intent.putExtra(Greetings.GREETING_INTENT_EXTRA, greeting);
                startActivity(intent);
                return true;
            }

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mFactory = LayoutInflater.from(this);
        mCursor = Greetings.getGreetingsCursor(getContentResolver());
        

        updateLayout();
    }

    private void updateLayout() {
        setContentView(R.layout.greeting_clock);
        mGreetingsList = (ListView) findViewById(R.id.greeting_list);
        GreetingAdapter adapter = new GreetingAdapter(this, mCursor);
        mGreetingsList.setAdapter(adapter);
        mGreetingsList.setVerticalScrollBarEnabled(true);
        mGreetingsList.setOnItemClickListener(this);
        mGreetingsList.setOnCreateContextMenuListener(this);

        View addGreeting = findViewById(R.id.add_greeting);
        addGreeting.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    addNewGreeting();
                }
            });
        // Make the entire view selected when focused.
        addGreeting.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setSelected(hasFocus);
                }
        });
        
    }

    private void addNewGreeting() {
        startActivity(new Intent(this, SetGreeting.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
        if (mCursor != null) {
            mCursor.close();
        }
    }
    
    /*public void registerDevice() {
        //String name = inputName.getText().toString();
        //String email = inputEmail.getText().toString();
        //String description = inputDesc.getText().toString();
    	
        // Building Parameters
    	String url = "http://121.199.47.183/tony/deviceRegister.php";
    	TelephonyManager mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	String deviceId = mTm.getDeviceId();    
        //String imsi = mTm.getSubscriberId();    
        String model = android.os.Build.MODEL; // 手机型号 
        String mdn = mTm.getDeviceSoftwareVersion()+ mTm.getPhoneType()+ mTm.getSimSerialNumber();//手机号码
        String type = android.os.Build.USER;
        String sw = android.os.Build.VERSION.CODENAME + "  " + android.os.Build.VERSION.INCREMENTAL;
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceId", deviceId));
        params.add(new BasicNameValuePair("model", model));
        params.add(new BasicNameValuePair("mdn", mdn));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("sw", sw));
        
        // getting JSON Object
        // Note that create product url accepts POST method
       try{
           JSONObject json = new JSONParser().makeHttpRequest(url,
                "POST", params);
            
            int code = json.getInt("code");
            if (code == 0) {//register succeed
            	String message = json.getString("message").trim();
            	int cid = Integer.valueOf(message);
            }
            
       }catch(Exception e){
           e.printStackTrace(); 
           //return "failed device";          
       }
        // check for success tag      
    }*/

    @Override
    public void onItemClick(AdapterView parent, View v, int pos, long id) {
    	//new registerDevice().execute();
        final Cursor c = (Cursor) mGreetingsList.getAdapter()
                .getItem(pos);
        final Greeting greeting = new Greeting(c);
        Intent intent = new Intent(this, SetGreeting.class);
        intent.putExtra(Greetings.GREETING_INTENT_EXTRA, greeting);
        startActivity(intent);
    }
    
    
    /*class registerDevice extends AsyncTask<String, String,String> {
    	protected String doInBackground(String...strings ) {
            
            // check for success tag
            registerDevice();
            return null;
     
        }
    }*/
}
