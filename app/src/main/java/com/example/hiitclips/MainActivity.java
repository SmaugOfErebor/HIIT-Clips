package com.example.hiitclips;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

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

        checkPermissions();
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
}