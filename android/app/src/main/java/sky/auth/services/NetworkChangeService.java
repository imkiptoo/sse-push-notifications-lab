package sky.auth.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import sky.auth.network.NetworkChangeMonitor;

public class NetworkChangeService extends Service {
    private NetworkChangeMonitor networkChangeMonitor;
    private static boolean isServiceRunning = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        if (isServiceRunning) return;
        networkChangeMonitor = new NetworkChangeMonitor(this);
        isServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        networkChangeMonitor.unregisterNetworkCallback();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
