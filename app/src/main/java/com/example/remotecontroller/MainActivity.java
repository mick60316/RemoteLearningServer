package com.example.remotecontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.remotecontroller.BleConnection.MyService;

public class MainActivity extends Activity {
	private TextView connState;
	private TextView responseMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ble permission
		checkSelfPermission();
		// UI
		linkUserInterface();
		// 註冊 Receiver
		registerBoardcast();
		setActivityInfo();



	}


	private void registerBoardcast ()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.CMD_SHUTDOWN);
		filter.addAction(Constant.RECEIVE);
		filter.addAction(Constant.STATE);
		registerReceiver(receiver, filter);

	}

	private void linkUserInterface()
	{
		responseMsg = findViewById(R.id.text_receive_text);
		connState = findViewById(R.id.text_conn_state);

	}



	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(MainActivity.this, MyService.class);
		// start service
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else {
			startService(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}


	private void checkSelfPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
						final AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle("This app needs background location access");
						builder.setMessage("Please grant location access so this app can detect beacons in the background.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@TargetApi(23)
							@Override
							public void onDismiss(DialogInterface dialog) {
								requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
										100);
							}

						});
						builder.show();
					}
				}
			} else {
				if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
							200);
				}
			}
		}
	}


	// btn onclick listener
	public void btnSendBytes(View view) {
		//test
		String testStr = "Hello world";
		sendMessageToClient(testStr.getBytes());
	}


	/**
	 *  Broadcast
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Constant.CMD_SHUTDOWN)){
				finishAndRemoveTask();
			} else if(action.equals(Constant.RECEIVE)){
				String data = new String(intent.getByteArrayExtra(Constant.RECEIVE_MSG));
				responseMsg.setText(data);
			} else if (action.equals(Constant.STATE)){
				String state = new String(intent.getByteArrayExtra(Constant.CONN));
				connState.setText(state);
			}
		}
	};


	private void sendMessageToClient(byte[] message){
		Intent intent = new Intent();
		intent.setAction(Constant.SEND);
		intent.putExtra(Constant.SEND_MSG, message);

		sendBroadcast(intent);
	}
	@SuppressLint("SourceLockedOrientationActivity")
	private void setActivityInfo()// Lock screen orientation and full screen
	{
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE);
	}


	public void onClick (View view)
	{
		switch (view.getId())
		{
			case R.id.btn_video1:
				break;
			case R.id.btn_video2:
				break;
			case R.id.btn_video3:
				break;
			case R.id.btn_video4:
				break;
			case R.id.btn_video5:
				break;


		}

	}


}
