package com.example.hiitclips;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class TreadmillManager {
    private static final String LOG_TAG = "HIIT_CLIPS";

    private final BluetoothLeScanner scanner;

    // Whether the bluetooth scanner is currently scanning.
    private boolean isScanning = false;

    // Used to schedule disabling the scanner to reduce unnecessary battery usage.
    private final Handler handler = new Handler(Looper.getMainLooper());

    // The maximum time in milliseconds that the bluetooth scanner can scan for once started.
    private static final long SCAN_PERIOD = 10000;

    public TreadmillManager(BluetoothAdapter adapter) {
        Log.d(LOG_TAG, "Initializing treadmill manager.");
        this.scanner = adapter.getBluetoothLeScanner();
    }

    /**
     * The callback method that will execute when the bluetooth scanner identifies a bluetooth device.
     */
    private final ScanCallback bluetoothScanCallback = new ScanCallback() {
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
        // TODO: Actually establish the connection.
        Log.d(LOG_TAG, "Treadmill found! Connecting to: " + device.getName());
    }

    /**
     * Sends a speed change command to the connected treadmill.
     * @param speed The target speed to set on teh connected treadmill.
     */
    private void setSpeed(double speed) {
        // TODO: Actually send the speed change command.
        Log.d(LOG_TAG, "Setting treadmill to speed: " + speed);
    }
}