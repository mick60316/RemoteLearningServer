package com.example.remotecontroller.BleConnection;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import com.example.remotecontroller.Constant;
import com.example.remotecontroller.MainActivity;
import com.example.remotecontroller.R;


public class MyService extends Service {
	private static final String TAG = Service.class.getSimpleName();
	private static final int SETUP_REQCODE = 100;
	private static final int CLOSE_REQCODE = 200;
	private static final int NOTIFICATION_ID = 543;
	private static final String CHANNEL_ID = "alive channel";

	private boolean isServiceOn = false;
	private boolean isForegroundOn = false;
	private NotificationManager notificationManager;
	private GattServer mGattServer;


	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mGattServer = new GattServer((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE), this);

		// 註冊 Receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.SEND);
		registerReceiver(receiver, filter);


	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		if (!isServiceOn) { //首次啟動
			startForegroundService();
			isServiceOn = true;
			mGattServer.startAdvertising("BleServer");
		}

		if (intent.hasExtra(Constant.CMD)) { //點擊狀態列的結束按鈕
			String cmd = intent.getStringExtra(Constant.CMD);
			if (cmd.contentEquals(Constant.CMD_SHUTDOWN)) {
				onDestroy();
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		sendIntentBroadcast(Constant.CMD_SHUTDOWN, "", null);
		stopForegroundService();
	}


	private void startForegroundService() {
		if (isForegroundOn) return;
		isForegroundOn = true;

		Intent setupIntent = new Intent(this, MainActivity.class);
		PendingIntent setupPendingIntent = PendingIntent.getActivity(this, SETUP_REQCODE, setupIntent, 0);

		Intent closeIntent = new Intent(this, MyService.class);
		closeIntent.putExtra(Constant.CMD, Constant.CMD_SHUTDOWN);
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
		} else {
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
		isForegroundOn = false;
		stopForeground(true);
		stopSelf();
	}


	public void triggerActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void stateChanged(String state) {
		sendIntentBroadcast(Constant.STATE, Constant.CONN, state.getBytes());
	}

	public void clientDataToUI(byte[] data) {
		sendIntentBroadcast(Constant.RECEIVE, Constant.RECEIVE_MSG, data);
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.SEND)) {
				Log.e(TAG, "action: server is sending data");
				byte[] msg = intent.getByteArrayExtra(Constant.SEND_MSG);
				mGattServer.sendNotifyCharacterChanged(msg);
			}
		}
	};

	private void sendIntentBroadcast(String action, String key, byte[] val) {
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




