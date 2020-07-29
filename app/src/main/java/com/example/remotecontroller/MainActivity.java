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
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.example.remotecontroller.BleConnection.MyService;
import com.example.remotecontroller.Component.CustomVideoView;
import com.example.remotecontroller.Component.LightSensor;
import com.example.remotecontroller.Component.MenuBar;
import com.example.remotecontroller.Component.PaintingView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	private String TAG ="Mike Remote Learning Bot";

	private TextView connState;
	private TextView responseMsg;
	private LinearLayout bleDebugLayout,btnDebugLayout;
	private boolean isDebugMode =false;

	private CustomVideoView customVideoView;
	private MenuBar menubar;
	private ImageView imageView;
	private PaintingView paintingView;
	private Button screenshowButton,s4NextButton;
	private LightSensor lightSensor;
	private int imageIndex= 0;
	private  int currentSession=0;

	private boolean isNextLock =false;





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		checkSelfPermission();
		linkUserInterface();
		registerBoardcast();
		setActivityInfo();
		changeSession(ExtraTools.S1);
		//int session=  Integer.valueOf( intent.getStringExtra("Session"));
		//int imageIndex =Integer.valueOf( intent.getStringExtra("ImageIndex"));
		//Log.e(TAG,"Session "+session +" "+imageIndex);



		//changeSession(ExtraTools.S1);
	}


	private void registerBoardcast ()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.CMD_SHUTDOWN);
		filter.addAction(Constant.RECEIVE);
//		filter.addAction("getSessionFromServer");
		filter.addAction(Constant.STATE);
		registerReceiver(receiver, filter);

	}

	private void linkUserInterface()
	{
		responseMsg = findViewById(R.id.text_receive_text);
		connState = findViewById(R.id.text_conn_state);
		imageView=findViewById( R.id.imageview);
		screenshowButton =findViewById(R.id.btn_screenshot);
		paintingView =findViewById(R.id.paintingView);
		s4NextButton=findViewById(R.id.btn_s4next);
		lightSensor =new LightSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE),(ImageView) findViewById(R.id.light_mask));
		lightSensor.setEnable(false);
		customVideoView =new CustomVideoView((VideoView)findViewById(R.id.videoview),imageView );
		menubar =new MenuBar((RelativeLayout) findViewById(R.id.manubar),imageView);
		bleDebugLayout =findViewById(R.id.ble_debug_layout);
		btnDebugLayout=findViewById(R.id.btn_debug_layout);

	}


	@Override
	protected void onResume() {
		lightSensor.registerListener();
		Log.e(TAG,"OnResume");



		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		lightSensor.unregisterListener();
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Intent intent2 = intent;

		if (intent2 ==null)Log.e(TAG,"Intent  is null");
		String SessionStr= intent2.getStringExtra("Session");

		if (SessionStr ==null)Log.e(TAG,"SessionStr is null;");

		String SessionImageIndex =intent2.getStringExtra("ImageIndex");
		int intentSession = -1;
		if (SessionStr != null) {
			intentSession = Integer.valueOf(SessionStr)-1;
			changeSession(intentSession );
			Log.e(TAG,"Session "+intentSession);
		}
		else changeSession(ExtraTools.S1);

		if (SessionImageIndex !=null)
		{
			int imageIndex = Integer.valueOf(SessionImageIndex);
			if (intentSession ==ExtraTools.S4)
			{
				customVideoView.setSessionImage(imageIndex);

			}
			else if (intentSession ==ExtraTools.S1 && imageIndex ==1)
			{
				imageView.setVisibility(View.VISIBLE);
				menubar.setVisibility(View.VISIBLE);
				paintingView.setVisibility(View.INVISIBLE);
				screenshowButton.setVisibility(View.INVISIBLE);
				//imageView.setImageResource(R.mipmap.control_pad_calendar);
				s4NextButton.setVisibility(View.INVISIBLE);
				menubar.setToTranslatePage();

			}

			Log.e(TAG,"imageIndex "+imageIndex);


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
			} else if(action.equals(Constant.RECEIVE)) {
				String data = new String(intent.getByteArrayExtra(Constant.RECEIVE_MSG));
				Log.i(TAG,"data "+data);

				bleMessageProcess(data);

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
				changeSession(ExtraTools.S1);
				break;
			case R.id.btn_video2:
				break;
			case R.id.btn_video3:
				changeSession(ExtraTools.S3);
				break;
			case R.id.btn_video4:
				changeSession(ExtraTools.S4);
				//showManuBar();
				break;
			case R.id.btn_video5:
				Log.e(TAG,"BTN5 "+currentSession +" "+imageIndex);
				changeSession(currentSession);
				if (currentSession == ExtraTools.S4) {

					customVideoView.setSessionImage(imageIndex);
					//changeSession(session);

				} else {

					//changeSession(session);
				}
				//sendMessageToClient("mode,0".getBytes());
				//imageView.setVisibility(View.VISIBLE);
				//customVideoView.Init();
				//hideManuBar();
				break;
			case R.id.btn_play:
			case R.id.btn_screenshot:
			case R.id.btn_s4next:
				if (!isNextLock) {

					sendMessageToClient("next,".getBytes());
					customVideoView.nextClick();
					isNextLock =true;
					Timer timer =new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							isNextLock =false;
						}
					},500);
				}
				break;
			case R.id.btn_debug:
				isDebugMode =!isDebugMode;
				int visablity =isDebugMode?View.VISIBLE:View.INVISIBLE;
				bleDebugLayout.setVisibility(visablity);
				btnDebugLayout.setVisibility(visablity);

				break;
			default:
				break;
		}

	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);


	}

	private void  changeSession (int Session)
	{
		Log.e(TAG,"Change Session" +Session);
		customVideoView.changeSession(Session);
		currentSession =Session;
		switch (Session) {
			case ExtraTools.S3:
					menubar.init();
					screenshowButton.setVisibility(View.VISIBLE);
					paintingView.setVisibility(View.VISIBLE);
					customVideoView.changeSession(ExtraTools.S3);
					if(paintingView != null) paintingView.clearCanvas();
					s4NextButton.setVisibility(View.INVISIBLE);
				break;
			case ExtraTools.S1:case ExtraTools.S2: case ExtraTools.S5:
					imageView.setVisibility(View.VISIBLE);
					menubar.setVisibility(View.INVISIBLE);
					paintingView.setVisibility(View.INVISIBLE);
					screenshowButton.setVisibility(View.INVISIBLE);
					imageView.setImageResource(R.mipmap.control_pad_calendar);
					s4NextButton.setVisibility(View.INVISIBLE);
				break;
			case ExtraTools.S4:
					imageView.setVisibility(View.VISIBLE);
					menubar.setVisibility(View.INVISIBLE);
					paintingView.setVisibility(View.INVISIBLE);
					screenshowButton.setVisibility(View.INVISIBLE);
					imageView.setImageResource(R.mipmap.control_pad_calendar);
					s4NextButton.setVisibility(View.VISIBLE);
				break;

//			imageView.setVisibility(View.VISIBLE);
//			menubar.setVisibility(View.VISIBLE);
//			paintingView.setVisibility(View.INVISIBLE);
//			screenshowButton.setVisibility(View.INVISIBLE);
//			//imageView.setImageResource(R.mipmap.control_pad_calendar);
//			s4NextButton.setVisibility(View.INVISIBLE);
//			menubar.setToTranslatePage();

			default:
				break;

		}

	}
	private void bleMessageProcess (String message)
	{
		Log.i(TAG,message);
		String [] dataSplit =message.split(",");
		if (dataSplit[0].equals("session"))
		{
			int session =Integer.valueOf(dataSplit[1])-1;

			Log.e("TAG","Mike"+session );
			changeSession(session);
			if (dataSplit.length==3 && session==ExtraTools.S4)
			{
				int imageIndex=Integer.valueOf(dataSplit[2])-1;
				if (imageIndex <0)imageIndex =3;
				customVideoView.setSessionImage(imageIndex);
				//changeSession(session);

			}
			else {

				//changeSession(session);
			}

		}
		else if (dataSplit[0].equals("next"))
		{
			Log.e(TAG,"next Click");
			customVideoView.nextClick();
		}
		else if (dataSplit[0].equals("mode"))
		{
			//changeSession(Integer.valueOf(dataSplit[1])-1);

		}
		else if (dataSplit[0].equals("audible"))
		{
			imageView.setVisibility(View.VISIBLE);
			menubar.setVisibility(View.VISIBLE);
			paintingView.setVisibility(View.INVISIBLE);
			screenshowButton.setVisibility(View.INVISIBLE);
			//imageView.setImageResource(R.mipmap.control_pad_calendar);
			s4NextButton.setVisibility(View.INVISIBLE);
			menubar.setToTranslatePage();
		}
//		else if (dataSplit[0].equals("btm"))
//		{
//			if (dataSplit[1].equals("in")) {
//
//
//				int session = Integer.valueOf(dataSplit[2]) -1;
//				Log.e(TAG,"btm in session "+session);
//
//				changeSession(session);
//				if (session == ExtraTools.S4) {
//
//					int imageIndex = Integer.valueOf(dataSplit[3]) ;
//					Log.e(TAG,"in"+session+" "+imageIndex);
//					if (imageIndex < 0) imageIndex = 3;
//					this.imageIndex=imageIndex;
//					customVideoView.setSessionImage(imageIndex);
//					//changeSession(session);
//
//				} else {
//
//					//changeSession(session);
//				}
//			}
//
//		}

	}








}
