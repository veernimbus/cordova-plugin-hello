package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.lang.Exception;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;

public class BLPPlugin extends CordovaPlugin {

	private final static String TAG = BLPPlugin.class.getSimpleName();
	String latitude = "";
	String longitude = "";
	String provider = "";
	boolean isServiceRunning = false;	

	@Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

		if(action.equals("makeRequestToServer")){
			try{
				Log.d(TAG, "Trying to start a service");
				
				if(!isServiceRunning){
					
					//Start Location Service
					Context context = cordova.getActivity().getApplicationContext();
					context.startService(new Intent(context, LocationService.class));
				
					//Register For Location Intent Receiveing Broadcast
					IntentFilter filter = new IntentFilter("Location Service Active");
					context.registerReceiver(new Receiver(), filter);
					isServiceRunning = true;
					
					//Trying to setup alarm manager
					Intent myIntent = new Intent("MR_LOCATION_SERVICE_ALARM");
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context,  0, myIntent, 0);

					AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 4 * 60 * 1000 , pendingIntent);		
				}				
																										        																										
				callbackContext.success("Request Made Successfully");
				return true;
			}
			catch(Exception e){
				Log.d(TAG, "Trouble calling activity");
				e.printStackTrace();
				return false;
			}
		}
		
		else {            
            return false;
        }
    }

	public void makeRequestToServer(){
		//Initialising HTTP Client
		HttpClient httpClient = new DefaultHttpClient();						
				
	    //Test Request
		HttpPost httpPost = new HttpPost("https://abc.com");

		//Creating the Form/Post parameters
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
		nameValuePair.add(new BasicNameValuePair("BLPPlugin", "Success"));
		nameValuePair.add(new BasicNameValuePair("Latitude", this.latitude));
		nameValuePair.add(new BasicNameValuePair("Longitude", this.longitude));
		nameValuePair.add(new BasicNameValuePair("LocationProvider", this.provider));
		
			//Binding parameters list to http request
			try { httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));} 
			catch (UnsupportedEncodingException e) {e.printStackTrace();}

			//Executing the HTTP Post Request
			try {
				  HttpResponse response = httpClient.execute(httpPost);				  	  
				} 
			catch (ClientProtocolException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}						
	}
	
	private class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			latitude = arg1.getExtras().getString("Latitude");
			longitude = arg1.getExtras().getString("Longitude");
			provider = arg1.getExtras().getString("Provider");			
			Log.d(TAG, "Latitude from BroadcastReceiver : " + latitude);
			Log.d(TAG, "Longitude from BroadcastReceiver : " + longitude);
			Log.d(TAG, "Provider from BroadcastReceiver : " + provider);
			
			try{
				Thread thread = new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							//Make Request To Server
							makeRequestToServer();	
						} catch (Exception e) {
							Log.e(TAG, "Error Calling makeRequestToServer() method inside", e);				
						}
					}
				});

				thread.start(); 					
			}
			catch(Exception e){				
				Log.e(TAG, "Error Calling makeRequestToServer() method Outside", e);				
			}
		}
}		
	
}
