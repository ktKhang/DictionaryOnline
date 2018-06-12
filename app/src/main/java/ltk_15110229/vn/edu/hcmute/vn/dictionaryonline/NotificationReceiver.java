package ltk_15110229.vn.edu.hcmute.vn.dictionaryonline;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    private String TAG = "PackageInfoActivity";
    @Override
    public void onReceive(Context context, Intent intent) {

        //lấy wordList từ intent
        Bundle args = intent.getBundleExtra("wordArrayList");
        ArrayList<Word> wordList = (ArrayList<Word>) args.getSerializable("ARRAYLIST");

        //lấy random 1 từ trong list word
        final int random = new Random().nextInt(wordList.size()) + 0; // [0, size-1] + 20 => [20, size-1]
        Log.d(TAG, "random: "+ random);
        Word wordRandom = wordList.get(random);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //tạo intent truyền word random đến Repeating activity
        Intent repeatingIntent = new Intent(context, RepeatingActivity.class);
        repeatingIntent.putExtra("wordInfo", wordRandom);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);    //chuyển logo sang dạng bitmap

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification))
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Daily vocabulary: " + wordRandom.getWord())
                .setContentText(wordRandom.getDetail2()).setAutoCancel(true);

        notificationManager.notify(100, builder.build());
    }


}
