package sky.auth.sse;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.*;

import java.util.concurrent.TimeUnit;

public class KeepAliveWorker extends Worker {
    public static final String TAG = "KeepAliveWorker";

    public KeepAliveWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Network connected, sending request.");
            Thread.sleep((1000 * 60 * 15) - 30);
        } catch (Exception e) {
            Log.d(TAG, "Interrupted while waiting to send next request: " + e.getMessage());
            return Result.failure();
        }
        return Result.success();
    }

    public static void start(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        try {
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                    KeepAliveWorker.class, 15, TimeUnit.MINUTES)
                    .addTag(KeepAliveWorker.TAG)
                    .build();
            workManager.enqueueUniquePeriodicWork(KeepAliveWorker.TAG, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, periodicWorkRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
