package com.nineclown.monyc.system;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nineclown.monyc.R;
import com.nineclown.monyc.main.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class DataService extends IntentService {

    private TCPClient tcpClient;
    private String comp_type; //컴포넌트(activity, fragment) 타입
    private String send_data; //Activity 에서 Service 로 주는 데이터(가계부 정보).
    private String received_data; //서버에서 온 결과 값
    private Intent broadcast; //Service 에서 Activity 로 결과를 전송하기 위한 broadcast

    public DataService() {
        super("DataService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DataService", "onCreate");
        tcpClient = new TCPClient("221.146.40.90", 9998);
        tcpClient.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DataService", "onDestroy");
    }

    //todo 재사용성을 고려하지 않음 ㅠㅠ
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("DataService", "onHandleIntent");
        comp_type = intent.getStringExtra("comp_type");
        Log.d("DataService", "received from : " + comp_type);
        if ("add".equals(comp_type)) {
            send_data = intent.getStringExtra("add_data");
            broadcast = new Intent("com.nineClown.monyc.ADD");
        } else if ("change".equals(comp_type)) {
            send_data = intent.getStringExtra("change_data");
            broadcast = new Intent("com.nineClown.monyc.CHANGE");
        } else if ("date".equals(comp_type)) {
            send_data = intent.getStringExtra("date_data");
            broadcast = new Intent("com.nineClown.monyc.DATE");
        } else if ("analysis".equals(comp_type)) {
            send_data = intent.getStringExtra("analysis_data");
            broadcast = new Intent("com.nineClown.monyc.ANALYSIS");
        } else if ("sms".equals(comp_type)) {
            send_data = intent.getStringExtra("add_data");
        } else {
            Log.d("DataService", "broadcast failed");
            stopSelf();
        }
    }

    private void notification() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "myChannel");

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Monyc")
                .setContentText("가계부가 추가되었습니다.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakeLock.acquire(5000);
        notificationManager.notify(0, builder.build());

    }

    public class TCPClient extends Thread {
        private Socket socket = null;
        private BufferedReader socketReader = null;
        private BufferedWriter socketWriter = null;
        SocketAddress socketAddress = null;
        private final int connection_timeout = 3000;


        public TCPClient(String ip, int port) throws RuntimeException {
            socketAddress = new InetSocketAddress(ip, port);

        }

        private void send(String args) {
            try {
                socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC_KR"));
                socketWriter.write(args);
                socketWriter.newLine();
                socketWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void receive() {
            try {
                socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC_KR"));
                received_data = socketReader.readLine();
                Log.d("DataService", "Socket received from server : " + received_data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                socket = new Socket();
                Log.d("DataService", "create socket");
                /* 응답을 기다리는 시간 */
                socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, connection_timeout);

                /* 소켓 연결 */
                socket.connect(socketAddress, connection_timeout);
                Log.d("DataService", "success socket connection");

                //todo auto_create
                if ("sms".equals(comp_type)) {
                    send(send_data);
                    receive();
                    if ("0010".equals(received_data)) {
                        notification();
                        socket.close();
                        stopSelf();
                    } else {
                        socket.close();
                        stopSelf();
                    }
                    //todo else
                } else {
                    send(send_data);
                    receive();
                    broadcast.putExtra("data_result", received_data);
                    getBaseContext().sendBroadcast(broadcast);
                    socket.close();
                    Log.d("DataService", "socket disconnected");
                }

            } catch (Exception e) {
                try {
                    Log.d("DataService", "server is dead");
                    received_data = "9999";
                    broadcast.putExtra("data_result", received_data);
                    getBaseContext().sendBroadcast(broadcast);

                    socket.close();
                    Log.d("DataService", "socket disconnected");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}
