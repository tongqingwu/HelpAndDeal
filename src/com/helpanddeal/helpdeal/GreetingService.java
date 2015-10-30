package com.helpanddeal.helpdeal;

import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GreetingService extends Service{
	
	private static final String TAG = "GreetingService";
	
	private static String url = "http://121.199.47.183/";
    private static final String TAG_MESSAGE = "message";
    
    private SharedPreferences sp ;
    
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("GreetingService", ">>>>>>>>>>>>>>enter onCreate()");
        
        // do something
        //register device if needed
        sp = this.getSharedPreferences("sp", MODE_WORLD_READABLE);
        if (sp.getInt("cid", -1) == -1) {
        	Log.d("greetingservice", "new RegisterDevice().execute() called");
        	new RegisterDevice().execute();
        }
        
        //Tony add to start python
        Process p = null;
        Runtime runtime = Runtime.getRuntime();
        OutputStreamWriter osw = null;
        
        try {
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
        
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
   	    Log.d(TAG, "onStartCommand called");
        super.onStart(intent, startId);
        return START_STICKY;
    } 
    
    private void registerDevice() {  
        // Building Parameters
    	String url = "http://121.199.47.183/tony/deviceRegister.php";
    	//android.util.Log.d("greetingservice","tfhn63"+"registerDevice()called");
    	TelephonyManager mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    	String deviceId = mTm.getDeviceId();    
        //String imsi = mTm.getSubscriberId();    
        String model = android.os.Build.MODEL; //  
        String mdn =  mTm.getSimSerialNumber();//
        String type = android.os.Build.USER;
        String sw = android.os.Build.VERSION.CODENAME + "  " + android.os.Build.VERSION.INCREMENTAL;
        android.util.Log.d("greetingservice","tfhn63"+deviceId + model + mdn + type + sw);
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceId", deviceId));
        params.add(new BasicNameValuePair("model", model));
        params.add(new BasicNameValuePair("mdn", mdn));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("sw", sw));
        
        // getting JSON Object
        // Note that create product url accepts POST method
       try{
            JSONObject json = new JSONParser().makeHttpRequest(url,"POST", params);   
            android.util.Log.d("greetingservice","tfhn63 "+json.toString());
            int code = Integer.valueOf(json.getString("code"));
            if (code == 0) {//register succeed
            	String message = json.getString("message").trim();
            	Editor editor = sp.edit();
            	editor.putInt("cid", Integer.valueOf(message));
            	editor.commit();
            	//cid = Integer.valueOf(message);
            	//android.util.Log.d("greetingservice","tfhn63 code=0"+cid);
            } else {
            	android.util.Log.d("greetingservice","tfhn63 code !=0"+cid);
            }
            
       }catch(Exception e){
           e.printStackTrace(); 
           Log.d("greetingservice", "execption");
           //return "failed device";          
       }
        // check for success tag      
    }
    
    class RegisterDevice extends AsyncTask<String, String,String> {
    	protected String doInBackground(String...strings ) {    
            // check for success tag
            registerDevice();
            return null;
        }
    }
    

}
