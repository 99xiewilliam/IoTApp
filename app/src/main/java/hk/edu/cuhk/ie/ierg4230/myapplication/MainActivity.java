package hk.edu.cuhk.ie.ierg4230.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

        /*
    1.開一個線程不停讀數據
    2.開一個線程傳數據
    3.判斷如果heartbeat太高或太低就警告
     */

    String instantHeartbeat = "0";
    String AvgHeartbeat = "0";
    int ResultID;

    private TextView textView;
    private TextView textView2;
    Integer count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        fetchResultID();

        textView.setText("0");
        textView2.setText("0");
        Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Runnable runnable = () -> {
                    fetchData();
                    textView.setText(instantHeartbeat);
                    textView2.setText(AvgHeartbeat);
                    if (Double.parseDouble(AvgHeartbeat) > 100) {
                        sendNotification(AvgHeartbeat, 0);
                    }else if (Double.parseDouble(AvgHeartbeat) < 30) {
                        sendNotification(AvgHeartbeat, 1);
                    }
                };
                handler.postDelayed(runnable, 500);
            }
        }, 200, 1000);


        //handler.removeCallbacks(runnable);


//        MainActivity.this.runOnUiThread(() -> {
//
//            //fetchData();
//            count++;
//            try {
//                Thread.sleep(2000);
//                textView.setText(String.valueOf(count));
//                textView2.setText(String.valueOf(count));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        });

//        Runnable getHeartbeat = new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    fetchData();
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//
//        Thread getHB = new Thread(getHeartbeat,"Thread");
//        getHB.start();



    }

    private NotificationManager createNotificationChannel(String channelID, String channelNAME, int level, NotificationManager manager) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            channel.setDescription("A New Notification");
            manager.createNotificationChannel(channel);
            return manager;
        } else {
            return manager;
        }
    }

    private void sendNotification(String Avgheartbeat, int id) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        switch (id){
            case 0:
                Notification notif0 = new NotificationCompat.Builder(this, "1")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Warning!!!!")
                        .setContentText("Your hearbeat is really high!!!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
                System.out.println("111");


                NotificationManager notificationManager0 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager0 = createNotificationChannel("1", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT, notificationManager0);
                notificationManager0.notify(0,notif0);
                break;
            case 1:
                Notification notif1 = new NotificationCompat.Builder(this, "1")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Warning!!!!")
                        .setContentText("Your hearbeat is really low!!!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
                System.out.println("000");

                NotificationManager notificationManager1 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager1 = createNotificationChannel("1", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT, notificationManager1);
                notificationManager1.notify(1,notif1);
                break;
        }

    }

    private void fetchResultID(){//這個是看總共傳了多少次數據，不過現在沒用了
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.thingspeak.com/channels/1613032/status.json?api_key=W384HQ0H7MTFBVRE";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("feeds");
                    ResultID = jsonArray.length();
                    System.out.println("------------" + ResultID + "-----------");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    private void fetchData(){ //獲取數據每次都會取到最新的, results=1代表最取最近的1條
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.thingspeak.com/channels/1613032/feeds.json?api_key=W384HQ0H7MTFBVRE&results=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("feeds");
                    instantHeartbeat = jsonArray.getJSONObject(0).getString("field1");
                    AvgHeartbeat = jsonArray.getJSONObject(0).getString("field2");
                    System.out.println(instantHeartbeat);
                    System.out.println(AvgHeartbeat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }


}