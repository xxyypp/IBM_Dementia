package helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.shreyus.myapp.MainActivity;

public class onAlarmReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start the MainActivity
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}

