package com.example.hiitclips;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.UUID;

public class TreadmillManager {
    private static final String LOG_TAG = "HIIT_CLIPS";

    private final Application application;
    private final BluetoothLeScanner scanner;
    private BluetoothGatt bluetoothGatt;

    // Whether the bluetooth scanner is currently scanning.
    private boolean isScanning = false;

    // Used to schedule disabling the scanner to reduce unnecessary battery usage.
    private final Handler handler = new Handler(Looper.getMainLooper());

    // The maximum time in milliseconds that the bluetooth scanner can scan for once started.
    private static final long SCAN_PERIOD = 10000;

    private static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID    = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_DESCRIPTOR_UUID   = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public TreadmillManager(Application application, BluetoothAdapter adapter) {
        Log.d(LOG_TAG, "Initializing treadmill manager.");
        // Use the application context for better safety with background tasks.
        this.application = application;
        this.scanner = adapter.getBluetoothLeScanner();
    }

    /**
     * The callback definition for the bluetooth scanner.
     */
    private final ScanCallback bluetoothScanCallback = new ScanCallback() {
        /**
         * The method that will execute when the bluetooth scanner identifies a bluetooth device.
         * @param callbackType Determines how this callback was triggered. Could be one of {@link
         *     ScanSettings#CALLBACK_TYPE_ALL_MATCHES}, {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH}
         *     or {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
         * @param result A Bluetooth LE scan result.
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(LOG_TAG, "Bluetooth scan result retrieved.");
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();

            if (deviceName == null) {
                Log.d(LOG_TAG, "Device with null name found.");

            } else if (deviceName.equals("HORIZON_T303 454E")) {
                Log.d(LOG_TAG, "Horizon T303 treadmill found.");
                stopScan();
                initiateConnection(device);

            } else {
                Log.d(LOG_TAG, "Unrecognized device found: " + deviceName);
            }
        }
    };

    /**
     * The callback definition for the bluetooth GATT connection.
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /**
         * The method that will execute whenever the bluetooth GATT connection state changes.
         * @param gatt GATT client
         * @param status Status of the connect or disconnect operation. {@link
         *     BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of {@link
         *     BluetoothProfile#STATE_DISCONNECTED} or {@link BluetoothProfile#STATE_CONNECTED}
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(LOG_TAG, "Connected to treadmill GATT server. Discovering services.");
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(LOG_TAG, "Disconnected from treadmill GATT server.");
            }
        }

        /**
         * The method that will execute when services are discovered via the bluetooth GATT connection.
         * @param gatt GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device has been explored
         *     successfully.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(LOG_TAG, "Treadmill GATT services discovered.");
                enableNotifications(gatt);
            }
        }

        /**
         * The method that will execute when a bluetooth descriptor is written.
         * @param gatt GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was written to the associated remote device.
         * @param status The result of the write operation {@link BluetoothGatt#GATT_SUCCESS} if the
         *     operation succeeds.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            String uuid = descriptor.getUuid().toString();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(LOG_TAG, "Successfully wrote to descriptor: " + uuid);
            } else {
                Log.e(LOG_TAG, "Failed to write to descriptor: " + uuid);
                Log.e(LOG_TAG, "Failed descriptor status code: " + status);
            }
        }

        /**
         * The method that will execute when a notification is retrieved from the treadmill.
         * @param gatt GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result of a remote
         *     notification event.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            Log.d(LOG_TAG, "Treadmill Notification: " + HexHelper.bytesToHex(data));
        }
    };

    /**
     * Triggers the bluetooth scanner to start scanning for devices.
     */
    public void startScan() {
        if (isScanning) return;
        Log.d(LOG_TAG, "Starting bluetooth scan.");

        // Schedule the disabling of the scanner to save battery.
        handler.postDelayed(this::stopScan, SCAN_PERIOD);

        // Start scanning.
        isScanning = true;
        scanner.startScan(bluetoothScanCallback);
    }

    /**
     * Triggers the bluetooth scanner to stop scanning for devices.
     */
    public void stopScan() {
        if (!isScanning) return;
        Log.d(LOG_TAG, "Stopping bluetooth scan.");

        // Stop scanning.
        isScanning = false;
        scanner.stopScan(bluetoothScanCallback);

        // Remove the callback from the handler so it doesn't fire again in case the scanner was stopped manually.
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Establishes a bluetooth GATT (Generic Attribute Profile) connection.
     * @param device The bluetooth device to establish the GATT connection with.
     */
    private void initiateConnection(BluetoothDevice device) {
        Log.d(LOG_TAG, "Connecting to treadmill.");
        // TODO: This can fail and that case needs to be handled in the onConnectionStateChange callback method.
        device.connectGatt(application, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    /**
     * Subscribes the application to notifications from the treadmill regarding its status.
     * The treadmill will block any attempts to control it unless notifications are subscribed to.
     * @param gatt The bluetooth GATT object to subscribe to notifications from.
     */
    private void enableNotifications(BluetoothGatt gatt) {
        Log.d(LOG_TAG, "Retrieving Treadmill Logic service.");
        BluetoothGattService service = gatt.getService(SERVICE_UUID);
        if (service == null) {
            Log.d(LOG_TAG, "Failed to retrieve Treadmill Logic service.");
            return;
        }

        Log.d(LOG_TAG, "Retrieving Data Characteristic from Treadmill Logic service.");
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);
        if (characteristic == null) {
            Log.d(LOG_TAG, "Failed to retrieve the Data Characteristic.");
            return;
        }

        Log.d(LOG_TAG, "Retrieving the CCCD descriptor.");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD_DESCRIPTOR_UUID);
        if (descriptor == null) {
            Log.d(LOG_TAG, "Failed to retrieve the CCCD descriptor.");
            return;
        }

        Log.d(LOG_TAG, "Enabling local notification listener.");
        gatt.setCharacteristicNotification(characteristic, true);

        Log.d(LOG_TAG, "Instructing treadmill to start pushing notifications.");
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    /**
     * Sends a speed change command to the connected treadmill.
     * @param speed The target speed to set on the connected treadmill.
     */
    private void setSpeed(double speed) {
        // TODO: Actually send the speed change command.
        Log.d(LOG_TAG, "Setting treadmill to speed: " + speed);
    }

}