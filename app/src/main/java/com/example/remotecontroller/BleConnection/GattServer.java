package com.example.remotecontroller.BleConnection;

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
import android.util.Log;

import java.util.UUID;

public class GattServer {
	private static final String TAG = GattServer.class.getSimpleName();
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothGattServer bluetoothGattServer;
	private BluetoothDevice device;
	BluetoothGattCharacteristic characteristicRead;
	BluetoothGattCharacteristic characteristicWrite;
	MyService myService;

	public static UUID UUID_SERVER = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
	public static UUID UUID_CHAR_READ = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb"); // didn't use this channel
	public static UUID UUID_CHAR_WRITE = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");


	public GattServer(BluetoothManager bluetoothManager, MyService myService) {
		this.bluetoothManager = bluetoothManager;
		this.myService = myService;
		bluetoothAdapter = bluetoothManager.getAdapter();
	}

	public boolean enableBluetooth() {
		if (bluetoothAdapter == null) return false;
		if (!bluetoothAdapter.isEnabled()) {
			return bluetoothAdapter.enable();
		} else {
			return true;
		}
	}

	public void startAdvertising(String name) {
		AdvertiseSettings settings = new AdvertiseSettings.Builder()
				.setConnectable(true)
				.setTimeout(0)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
				.build();
		AdvertiseData advertiseData = new AdvertiseData.Builder()
				.setIncludeDeviceName(true)
				.setIncludeTxPowerLevel(true)
				.build();
		bluetoothAdapter.setName(name);

		BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
		bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback);
	}

	private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
		@Override
		public void onStartSuccess(AdvertiseSettings settingsInEffect) {
			super.onStartSuccess(settingsInEffect);
			Log.e(TAG, "Start Ble Success" + settingsInEffect.toString());
			addService();
		}
	};

	private void addService() {
		BluetoothGattService gattService = new BluetoothGattService(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY);

		characteristicRead = new BluetoothGattCharacteristic(UUID_CHAR_READ,
				BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ);

		characteristicWrite = new BluetoothGattCharacteristic(UUID_CHAR_WRITE,
				BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ
						| BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

		gattService.addCharacteristic(characteristicRead);
		gattService.addCharacteristic(characteristicWrite);

		bluetoothGattServer = bluetoothManager.openGattServer(myService, gattServerCallback);

		bluetoothGattServer.addService(gattService);
	}

	private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			super.onConnectionStateChange(device, status, newState);
			GattServer.this.device = device;
			String state = "";
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				state = "connected";
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				state = "disconnected";
				GattServer.this.device = null;
			}
			Log.e(TAG, "onConnectionStateChange device=" + device.toString() + " status=" + status + " newState=" + state);
			myService.stateChanged(state);
		}

		// receive msg
		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
			super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
			String data = new String(value).trim();
			Log.e(TAG, "receiving data: " + data);
			if (data.contentEquals("btm,in")) {
				myService.triggerActivity();
			}
			myService.clientDataToUI(value);
		}
	};

	// send msg
	public void sendNotifyCharacterChanged(byte[] value) {
		if (GattServer.this.device != null) {
			Log.e(TAG, device.getAddress());
			characteristicWrite.setValue(value);
			bluetoothGattServer.notifyCharacteristicChanged(device, characteristicWrite, false);
		} else {
			Log.e(TAG, "disconnected...?");
		}
	}
}
