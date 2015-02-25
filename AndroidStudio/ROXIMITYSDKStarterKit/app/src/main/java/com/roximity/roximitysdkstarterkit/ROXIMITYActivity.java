package com.roximity.roximitysdkstarterkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.roximity.sdk.ROXIMITYEngine;
import com.roximity.sdk.ROXIMITYEngineListener;
import com.roximity.sdk.external.ROXConsts;
import com.roximity.sdk.messages.MessageParcel;
import com.roximity.system.classes.ROXIMITYStatusCallback;
import com.roximity.system.exceptions.ROXIMITYEngineNotRunningException;

import org.json.JSONException;

import java.util.HashMap;


public class ROXIMITYActivity extends ActionBarActivity implements ROXIMITYEngineListener {

    private static final String TAG = "ROXIMITYSDKStarterKit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roximity);

        // This is merely for example purposes. START_LOCATION_DEACTIVATED already has a default of false
        HashMap<String, Object> engineOptions = new HashMap<String, Object>();
        engineOptions.put(ROXConsts.ENGINE_OPTIONS_START_LOCATION_DEACTIVATED, false);

        try {
            ROXIMITYEngine.startEngineWithOptions(this.getApplicationContext(), R.drawable.ic_launcher, engineOptions, this, null);
            createBroadcastRecievers();

        } catch (Exception e) {
            Log.e(TAG, "Unable to start ROXIMITY Engine");
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_roximity, menu);
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

    @Override
    public void onResume(){
        super.onResume();

        Log.i(TAG, "ROXIMITY Engine running: " + ROXIMITYEngine.isROXIMITYEngineRunning());

        if (getIntent().hasExtra(ROXConsts.EXTRA_MESSAGE_DATA)){
            Log.d(TAG, "Activity launched with ROXIMITY intent containing message data: " + this.getIntent().getStringExtra(ROXConsts.EXTRA_MESSAGE_DATA));
        }

    }

    public void setRoximityAlias(String alias) {

        try {
            ROXIMITYEngine.setAlias(alias, new ROXIMITYStatusCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, message);
                    Toast.makeText(ROXIMITYActivity.this, message, Toast.LENGTH_LONG).show();;
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, message);
                    Toast.makeText(ROXIMITYActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (ROXIMITYEngineNotRunningException e) {
            Log.w(TAG, "ROXIMITY Engine is not running");
        }
    }

    @Override
    public void onROXIMITYEngineStarted() {
        Log.i(TAG, "ROXIMITY Engine has started");
    }

    @Override
    public void onROXIMITYEngineStopped() {
        Log.w(TAG, "ROXIMITY Engine has stopped");
    }

    private void createBroadcastRecievers(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ROXConsts.MESSAGE_FIRED);
        intentFilter.addAction(ROXConsts.BEACON_RANGE_UPDATE);
        intentFilter.addAction(ROXConsts.WEBHOOK_POSTED);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ROXConsts.MESSAGE_FIRED)) {
                MessageParcel messageParcel = (MessageParcel)intent.getParcelableExtra(ROXConsts.EXTRA_MESSAGE_PARCEL);
                handleMessageFired(messageParcel);
            } else if (intent.getAction().equals(ROXConsts.BEACON_RANGE_UPDATE)){
                String rangeJson = intent.getStringExtra(ROXConsts.EXTRA_RANGE_DATA);
                handleBeaconRangeUpdate(rangeJson);
            } else if (intent.getAction().equals(ROXConsts.WEBHOOK_POSTED)){
                String webhookJson = intent.getStringExtra(ROXConsts.EXTRA_BROADCAST_JSON);
                handleWebhookPosted(webhookJson);
            }
        }
    };


    public void handleMessageFired(MessageParcel messageParcel) {
        try {
            Log.i(TAG, "Message fired:" + messageParcel.show(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void handleWebhookPosted(String webhookJson){
        Log.d(TAG, "Webhook posted: " + webhookJson);
    }


    public void handleBeaconRangeUpdate(String rangeUpdate) {
        Log.i(TAG, "Received a beacon range update:" + rangeUpdate);
    }

}
