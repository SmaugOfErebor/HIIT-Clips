package com.example.hiitclips;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ensure that permissions are granted and that bluetooth is enabled.
        // TODO: If permissions are requested and enabling bluetooth is requested, their dialogs overlap.
        //       It's works just fine, but it doesn't look/feel professional.
        //       Make these checks follow a chain of responsibility.
        checkPermissions();
        ensureBluetoothEnabled();
    }

    /**
     * Checks if the required permissions have been granted for the app.
     * Requests any missing permissions from the user if they have not been granted.
     * TODO: Handle denial of required permissions.
     *       Revocation of permissions should not be an issue because the OS kills the app when a permission is revoked.
     *       onCreate will be called the next time the app is opened and will re-check permissions.
     */
    private void checkPermissions() {
        boolean hasFineLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and higher requires specific Bluetooth permissions
            boolean hasBTScan = checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
            boolean hasBTConnect = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;

            if (!hasBTScan || !hasBTConnect || !hasFineLocation) {
                String[] perms = {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                };
                requestPermissions(perms, 1);
            }
        } else {
            // Older versions only require Location to "see" Bluetooth devices
            if (!hasFineLocation) {
                String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(perms, 1);
            }
        }
    }

    /**
     * Ensures that bluetooth hardware is present and enabled.
     * The AndroidManifest requires that bluetooth_le is present, but that is only policed by installs through Google Play.
     * Installing directly from an APK circumvents this protection.
     * This is highly unlikely to ever occur on a physical device since this is just a personal project for myself.
     * However, this will keep me in check if I ever accidentally go to run this code on the Android emulator.
     * TODO: Handle the disabling of bluetooth while the app is running.
     */
    private void ensureBluetoothEnabled() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Device doesn't support Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Request that the user enable bluetooth if it is disabled.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // TODO: This activity requires bluetooth permission and will throw a SecurityException if permission is not granted.
            //       Ensure that bluetooth permission is granted before starting this activity.
            startActivity(enableBtIntent);
        }
    }
}