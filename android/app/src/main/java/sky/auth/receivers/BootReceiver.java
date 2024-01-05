package sky.auth.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            /*new Thread(() -> {
                try {
                    SynchronizationWorker.start(context);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    ConnectionBackWorker.start(context);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }).start();*/
        }
    }
}
