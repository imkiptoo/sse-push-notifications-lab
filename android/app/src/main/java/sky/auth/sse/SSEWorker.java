package sky.auth.sse;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SSEWorker extends Worker {

    public static BufferedReader reader;
    public static HttpURLConnection urlConnection;
    public static final String TAG = "SSEWorker";
    private static final String CHANNEL_ID = "sse_channel";
    private static final int NOTIFICATION_ID = 1;

    public static final String SERVER_URL = "http://172.20.102.31:8094/events?userID=imkiptoo";

    public SSEWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private void showNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "SSE Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("SSE Notification")
                .setContentText(message)
                .setSilent(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if(reader != null || urlConnection != null){
                return Result.success();
            }

            // Check for existing connection before reading
            Log.d("SSE", "Opening SSE Connection");
            URL url = new URL(SERVER_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "text/event-stream");

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                // Handle received notifications here
                // For example, log the message
                Log.d("SSE", "Received: " + line);

                if (line.contains("data: ")) {
                    String receivedData = line.split("data: ")[1];
                    showNotification(getApplicationContext(), "Received: " + receivedData);
                }
            }

            urlConnection.disconnect();
            urlConnection = null;
            reader.close();
            reader = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SSE", "Server connection error.");
        }
        return Result.success();
    }
}
