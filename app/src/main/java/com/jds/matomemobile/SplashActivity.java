package com.jds.matomemobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

//import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jds.matomemobile.net.NetConnection;
import com.jds.matomemobile.plugin.DBHelper;
import com.jds.matomemobile.plugin.WebData;

//import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends Activity {

    // --Commented out by Inspection (5/8/15, 9:39 AM):public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private final static int PLAY_SERVICE_RESOLUTION_REQUEST = 9000;

    private final DBHelper dbHelper = new DBHelper(this);

    private final String SENDER_ID = "708415891296";
    private String REG_ID_USER = "";

    // --Commented out by Inspection (5/8/15, 9:39 AM):WebData webData = new WebData();

    private GoogleCloudMessaging gcm;
    // --Commented out by Inspection (5/8/15, 9:40 AM):AtomicInteger msgID = new AtomicInteger();
    private Context context;

    private String regid;

    private final Handler handler = new Handler();

    private final NetConnection conn = new NetConnection();
    // --Commented out by Inspection (5/8/15, 9:40 AM):SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);

        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        dbHelper.createDatabase();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //ImageView image = (ImageView) findViewById(R.id.matome_splash_sreen);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        //handler.postDelayed(runnable, 3000);

        context = getApplicationContext();
        if(conn.isOnline(cm)){
            //there is internet service available

            if(checkPlayServices()){
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationID(context);

                //if no registration id detected, start to register this device in background
                if(regid.isEmpty()){
                    registerInBackground();
                } else {
                    REG_ID_USER = getRegistrationID(getApplicationContext());
                    Log.d("message", REG_ID_USER);
                    GoToMainMenu();
                }
            }
        } else {
            //there is no internet service available

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Matome need an active internet connection. Please enable Wi-Fi or your packet data")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GoToMainMenu();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(SplashActivity.this, ManagerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
            finish();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            } else {
                Log.i(String.valueOf(R.string.TAG), "This devices is not supported");
                finish();
            }
            return false;
        }
        return true;
    }

    private static int getAppVersion(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getRegistrationID(Context context){
        final SharedPreferences prefs = getGCMPreferences();
        String registrationID = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationID != null && registrationID.isEmpty()){
            Log.i(String.valueOf(R.string.TAG), "Registration not found.");
            return "";
        }


        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if(registeredVersion != currentVersion){
            Log.i(String.valueOf(R.string.TAG), "App version changed.");
            return "";
        }

        return registrationID;
    }

    private SharedPreferences getGCMPreferences(){
        return getSharedPreferences(SplashActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if(gcm == null){
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = regid;

                    //place sendRegistrationID to our server at here

                    //store registration ID to device here
                } catch (IOException e) {
                    msg = "error";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg){

                if(msg.equals("error")) {

                    Log.d("GCM Error: ", "Service not available");

                } else {
                    REG_ID_USER = msg + "\n";
                    Log.i(String.valueOf(R.string.TAG), "user_id: " + REG_ID_USER);

                    sendRegistrationToBackend();
                }
            }
        }.execute();
    }

    private void sendRegistrationToBackend() {
        //this code to send parameter and registration data into server.

        final String reg_id = REG_ID_USER;
        final String device_name = Build.MODEL;
        final String device_id = Build.DEVICE;
        final String salt = WebData.MATOME_SERVER_TOKEN;

        //Toast.makeText(getApplicationContext(), device_name, Toast.LENGTH_SHORT).show();

        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {

                try {
                    String urlString = WebData.MATOME_GCM_SERVER_REGISTRATION;
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("reg_id", reg_id)
                        .appendQueryParameter("device_name", device_name)
                        .appendQueryParameter("device_id", device_id)
                        .appendQueryParameter("salt", salt);

                    String query = builder.build().getEncodedQuery();

                    OutputStream outputStream = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    outputStream.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null){
                        sb.append(line).append("\n");
                    }

                    Log.d("matome", sb.toString());

                    return reg_id;
                } catch (MalformedURLException e) {
                    return "null";
                } catch (IOException e) {
                    return "null";
                }
            }

            @Override
            protected void onPostExecute(String message) {

                Log.d("matome", "Inserting new registration ID to device");
                Log.d("matome", message);

                //noinspection StringEquality
                if(message != "null") {
                    storeRegistrationID(getApplicationContext(), message);
                }

                GoToMainMenu();

            }
        }.execute();
    }

    //this function to store registration data into local storage.
    private void storeRegistrationID(Context context, String regID){
        final SharedPreferences pref = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(String.valueOf(R.string.TAG), "Saving regid on app version " + appVersion);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PROPERTY_REG_ID, regID);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void GoToMainMenu(){
        handler.postDelayed(runnable, 3000);
    }

}
