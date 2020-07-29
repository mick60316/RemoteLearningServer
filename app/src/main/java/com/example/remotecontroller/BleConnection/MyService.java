package com.example.remotecontroller.BleConnection;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.remotecontroller.Constant;
import com.example.remotecontroller.MainActivity;
import com.example.remotecontroller.R;

import java.util.List;
import java.util.UUID;


public class MyService extends Service {
	private static final String TAG = Service.class.getSimpleName();
	private static final int SETUP_REQCODE = 100;
	private static final int CLOSE_REQCODE = 200;
	private static final int NOTIFICATION_ID = 543;
	private static final String CHANNEL_ID = "alive channel";

	private boolean isServiceOn = false;
	private NotificationManager notificationManager;
	private GattServer mGattServer;

	private String connClientState = "disconnected";


	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mGattServer = new GattServer((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE), this);

		// 註冊 Receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.SEND);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		if (!isServiceOn){ //首次啟動
			startForegroundService();
			isServiceOn = true;
			Log.e(TAG, "startAdvertising");
			mGattServer.startAdvertising();
		} else{
			stateChanged(connClientState);
		}

		if (intent.getAction() == Constant.CMD_SHUTDOWN) { //點擊狀態列的結束按鈕
			onDestroy();
		}

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(receiver);
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
		mGattServer.closeGattServer();
		sendIntentBroadcast(Constant.CMD_SHUTDOWN,"",null);
		stopForegroundService();
	}


	private void startForegroundService() {
		Intent setupIntent = new Intent(this, MainActivity.class);
		PendingIntent setupPendingIntent = PendingIntent.getActivity(this, SETUP_REQCODE, setupIntent, 0);

		Intent closeIntent = new Intent(this, MyService.class);
		closeIntent.setAction(Constant.CMD_SHUTDOWN);
		PendingIntent closePendingIntent = PendingIntent.getService(this, CLOSE_REQCODE, closeIntent, 0);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "RemoteLearning", NotificationManager.IMPORTANCE_HIGH);
			notificationChannel.setDescription("keep it alive");
			notificationManager.createNotificationChannel(notificationChannel);

			Notification.Action closeAction = new Notification.Action.Builder(Icon.createWithResource(getApplicationContext(), R.drawable.ic_launcher_background), "CLOSE", closePendingIntent).build();
			Notification.Action setupAction = new Notification.Action.Builder(Icon.createWithResource(getApplicationContext(), R.drawable.ic_launcher_background), "SETUP", setupPendingIntent).build();

			Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
					.setContentTitle(getString(R.string.app_name))
					.setContentText("Service is still alive...")
					.setSmallIcon(R.drawable.ic_launcher_foreground)
					.addAction(setupAction)
					.addAction(closeAction);

			Notification notification = builder.build();
			notificationManager.notify(NOTIFICATION_ID, notification);
			startForeground(NOTIFICATION_ID, notification);
		}else {
			Notification notification = new NotificationCompat.Builder(this)
					.setContentTitle(getResources().getString(R.string.app_name))
					.setContentText("Service is still alive...")
					.addAction(R.drawable.ic_launcher_background, "SETUP", setupPendingIntent)
					.addAction(R.drawable.ic_launcher_background, "CLOSE", closePendingIntent).build();

			startForeground(NOTIFICATION_ID, notification);
		}
	}


	private void stopForegroundService() {
		notificationManager.cancel(NOTIFICATION_ID);
		isServiceOn = false;
		stopForeground(true);
		stopSelf();
	}




	/**
	 * 喚醒 Activity
	 */
	public void triggerActivity() {
		Log.e("Mike","triggerActivity");
		if (!isForeground(this.getPackageName())) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Log.e(TAG,"action: UI get session and index");
			String msg = mGattServer.session + "," + mGattServer.index;
			intent.putExtra("hello","world");
			intent.putExtra("Session", mGattServer.session);
			intent.putExtra("ImageIndex", mGattServer.index);
			//intent.putExtra("getSessionFromServer", msg);
			startActivity(intent);
			Log.e("Mike","triggerActivity");
//			mGattServer.sendNotifyCharacterChanged( "mode,0,in".getBytes());
		}
	}
	public void triggerActivityToHome() {
		Log.e("Mike","triggerActivityToHome");
		if (isForeground(this.getPackageName())) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			Log.e("Mike","triggerActivityToHome");
//			mGattServer.sendNotifyCharacterChanged("mode,1,out".getBytes());
		}
	}
	public boolean isForeground(String myPackage) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		return componentInfo.getPackageName().equals(myPackage);
	}

	/**
	 * 通知 Activity 連線狀態改變
	 * @param state
	 */
	public void stateChanged(String state) {
		connClientState = state;
		sendIntentBroadcast(Constant.STATE, Constant.CONN, connClientState.getBytes());
	}

	/**
	 * 通知 Activity client 傳來訊息
	 * @param data
	 */
	public void clientDataToUI(byte[] data) {
		sendIntentBroadcast(Constant.RECEIVE, Constant.RECEIVE_MSG, data);
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Constant.SEND)){
				Log.e(TAG,"action: server is sending data");
				byte[] msg = intent.getByteArrayExtra(Constant.SEND_MSG);
				mGattServer.sendNotifyCharacterChanged(msg);
//			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
//				int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
//				switch (blueState) {
//					case BluetoothAdapter.STATE_TURNING_ON:
//						Log.e(TAG,"STATE_TURNING_ON");
//						break;
//					case BluetoothAdapter.STATE_ON:
//						Log.e(TAG,"STATE_ON");
//						mGattServer = new GattServer((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE), MyService.this);
//						break;
//					case BluetoothAdapter.STATE_TURNING_OFF:
//						Log.e(TAG,"STATE_TURNING_OFF");
//						mGattServer.closeGattServer();
//						break;
//					case BluetoothAdapter.STATE_OFF:
//						Log.e(TAG,"STATE_OFF");
//						mGattServer = null;
//						break;
//				}
			}
		}
	};

	private void sendIntentBroadcast(String action, String key, byte[] val){
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra(key, val);

		sendBroadcast(intent);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
}















