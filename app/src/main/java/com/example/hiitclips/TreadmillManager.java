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
    private final BluetoothLeScanner scanner;

    // Whether the bluetooth scanner is currently scanning.
    private boolean isScanning = false;

    // Used to schedule disabling the scanner to reduce unnecessary battery usage.
    private final Handler handler = new Handler(Looper.getMainLooper());

    // The maximum time in milliseconds that the bluetooth scanner can scan for once started.
    private static final long SCAN_PERIOD = 10000;

    public TreadmillManager(BluetoothAdapter adapter) {
        this.scanner = adapter.getBluetoothLeScanner();
    }

    /**
     * The callback method that will execute when the bluetooth scanner identifies a bluetooth device.
     */
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (device.getName() != null && device.getName().contains("HORIZON_T303 454E")) {
                stopScan();
                initiateConnection(device);
            }
        }
    };

    /**
     * Triggers the bluetooth scanner to start scanning for devices.
     */
    public void startScan() {
        if (isScanning) return;

        // Schedule the disabling of the scanner to save battery.
        handler.postDelayed(this::stopScan, SCAN_PERIOD);

        // Start scanning.
        isScanning = true;
        scanner.startScan(leScanCallback);
    }

    /**
     * Triggers the bluetooth scanner to stop scanning for devices.
     */
    public void stopScan() {
        if (!isScanning) return;

        // Stop scanning.
        isScanning = false;
        scanner.stopScan(leScanCallback);

        // Remove the callback from the handler so it doesn't fire again in case the scanner was stopped manually.
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Establishes a bluetooth GATT (Generic Attribute Profile) connection.
     * @param device The bluetooth device to establish the GATT connection with.
     */
    private void initiateConnection(BluetoothDevice device) {
        // TODO: Actually establish the connection.
        Log.d("HIIT_CLIPS", "Treadmill found! Connecting to: " + device.getName());
    }

    /**
     * Sends a speed change command to the connected treadmill.
     * @param speed The target speed to set on teh connected treadmill.
     */
    private void setSpeed(double speed) {
        // TODO: Actually send the speed change command.
        Log.d("HIIT_CLIPS", "Setting treadmill to speed: " + speed);
    }
}