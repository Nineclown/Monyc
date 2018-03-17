package com.nineclown.monyc.join;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class JoinService extends IntentService {
    private static final String TAG = "JoinService";

    private TCPClient tcpClient;
    private String frag_type;
    private String join_data; //프래그먼트에서 서비스로 주는 데이터(회원 가입을 위한 정보).

    private String received_data; //서버에서 온 결과 값
    private Intent broadcast; //결과 값을 전송하기 위한 broadcast
    //IBinder iBinder = new Binder();

    public JoinService() {
        super("JoinService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        tcpClient = new TCPClient("221.146.40.90", 9999);
        tcpClient.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        frag_type = intent.getStringExtra("frag_type");
        join_data = intent.getStringExtra("join");
        Log.d(TAG, "프래그먼트로부터 받은 데이터 json : " + join_data);
        if ("sign_in".equals(frag_type)) {
            broadcast = new Intent("com.nineClown.monyc.SIGN_IN");
        } else if ("sign_up".equals(frag_type)) {
            broadcast = new Intent("com.nineClown.monyc.SIGN_UP");
        } else {
            Log.d(TAG, "unregisted Access");
            stopSelf();
        }
    }

    /* 서비스는 메인 스레드가 돌아가고 애는 다른 스레드가 돌기 때문에*/
    /* 서비스가 먼저 종료될 수 있다. 그런데, 얘가 돌아간 그 결과를 어떻게 전달하냐 그럼? */
    public class TCPClient extends Thread {
        private Socket socket = null;
        private BufferedReader socketReader = null;
        private BufferedWriter socketWriter = null;
        private SocketAddress socketAddress = null;
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
                socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                received_data = socketReader.readLine();
                Log.d(TAG, "Socket received from server : " + received_data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                socket = new Socket();
                Log.d(TAG, "소켓 생성");
                /* 응답을 기다리는 시간 */
                socket.setSoTimeout(connection_timeout);
                socket.setSoLinger(true, connection_timeout);

                /* 소켓 연결 */
                socket.connect(socketAddress, connection_timeout);
                Log.d(TAG, "소켓 연결 성공");

                send(join_data);

                receive();

                broadcast.putExtra("msg_type", received_data);
                getBaseContext().sendBroadcast(broadcast);
                socket.close();
                Log.d(TAG, "socket disconnected");
            } catch (Exception e) {
                try {
                    Log.d(TAG, "server is dead");
                    received_data = "9999";
                    broadcast.putExtra("msg_type", received_data);
                    getBaseContext().sendBroadcast(broadcast);

                    socket.close();
                    Log.d(TAG, "socket disconnected");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}
