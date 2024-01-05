package sky.auth.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import sky.auth.sse.SSEWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.util.concurrent.TimeUnit;

public class NetworkChangeMonitor {
    private static final String TAG = "NetworkChangeMonitor";

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Context localContext;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NetworkChangeMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        localContext = context;
        registerNetworkCallback();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.i(TAG, "Network status: Connected");

                // Check port connectivity when network is available
                boolean connected = checkPortConnectivity(); // Replace with your server IP and port

                if(connected){
                    startSSEWorker(localContext);
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.i(TAG, "Network status: Disconnected");
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void startSSEWorker(Context localContext) {
        WorkManager workManager = WorkManager.getInstance(localContext);
        try {
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                    SSEWorker.class, 15, TimeUnit.MINUTES)
                    .addTag(SSEWorker.TAG)
                    .build();
            workManager.enqueueUniquePeriodicWork(SSEWorker.TAG, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, periodicWorkRequest);
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage());
        }
    }

    private boolean checkPortConnectivity() {
        String host = "";
        int port = 80;

        try {
            String urlString = SSEWorker.SERVER_URL;
            URL url = new URL(urlString);

            // Get host (IP or domain)
            host = url.getHost();

            // Get port
            port = url.getPort();
            if (port == -1) {
                // If port is not specified in the URL, use default HTTP port 80
                port = url.getDefaultPort();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            Socket socket = new Socket();
            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, 5000); // Timeout set to 5 seconds

            // If connection succeeds, the port is connectable
            Log.i(TAG, "Port " + port + " is connectable");
            // Start your Worker here or perform any other action

            socket.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Port " + port + " is not connectable: " + e.getMessage());
            // Handle the case when the port is not connectable or connection fails
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
