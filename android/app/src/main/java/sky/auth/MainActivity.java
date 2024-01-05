package sky.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.flutter.embedding.android.FlutterActivity;
import sky.auth.services.NetworkChangeService;

public class MainActivity extends FlutterActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    Thread thKeepAliveWorkerThread;
    Thread thSSEWorkerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canShowNotifications = NotificationManagerCompat.from(this).areNotificationsEnabled();
            if (!canShowNotifications) {
                showEnableNotificationsDialog();
            }
        }

        Intent intent = new Intent(this, NetworkChangeService.class);
        startService(intent);
    }

    private void showEnableNotificationsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Notifications are disabled");
        builder.setMessage("Please enable notifications for this app to receive updates.");
        builder.setPositiveButton("Go to Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle the logic for notification here
                Log.d("Notification", "Permission granted");
            } else {
                // Permission denied, handle accordingly or inform the user
                Log.d("Notification", "Permission denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            System.out.println("App Closed");

            if(thKeepAliveWorkerThread.isAlive()){
                thKeepAliveWorkerThread.interrupt();
            }
            if(thSSEWorkerThread.isAlive()){
                thSSEWorkerThread.interrupt();
            }

            /*Intent intent = new Intent(this, NetworkChangeService.class);
            stopService(intent);*/
        } catch (Exception ignored) {
        }
    }
}
