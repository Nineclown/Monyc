package com.nineclown.monyc.join;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nineclown.monyc.R;
import com.nineclown.monyc.main.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";
    private String s_email = null;
    private String s_password = null;
    private EditText et_email;
    private EditText et_password;
    private Button bt_sign_in;

    //login 정보를 JSON 형태로 바꿔주는 객체
    private JSONObject jsonObject;
    //서버로 전송할 데이터(json형태로 바껴진)
    private String sign_in_data;
    //서버로부터 받은 데이터
    private String msg_type;
    //서버와 통신하는 기능을 갖고 있는 서비스(JoinService)로 넘겨줄 intent
    private Intent ServiceIntent;

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private SharedPreferences auto;
    private SharedPreferences.Editor autoLogin;
    private boolean isChecked = false;

    public SignInFragment() {
        // Required empty public constructor
    }

    public static SignInFragment newInstance() {
        Bundle args = new Bundle();

        SignInFragment fragment = new SignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
        s_password = auto.getString("inputPassword", null);
        Log.d(TAG, "onCreate");

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nineClown.monyc.SIGN_IN");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                msg_type = intent.getStringExtra("msg_type");
                Log.d(TAG, "BroadcastReceiver received");

                //todo 회원정보가 일치한 경우
                if ("0001".equals(msg_type)) {
                    if (!isChecked) {
                        autoLogin = auto.edit();
                        autoLogin.putString("inputEmail", s_email);
                        autoLogin.putString("inputPassword", s_password);
                        //이 정보를 xml 로 저장.
                        autoLogin.commit();
                        isChecked = true;
                    }
                    getActivity().stopService(ServiceIntent);
                    Intent mainIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    startActivity(mainIntent);
                    getActivity().finish();
                } else if ("0003".equals(msg_type)) {
                    isChecked = false;
                    getActivity().stopService(ServiceIntent);
                    Toast.makeText(getActivity(), "회원 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                } else if ("9999".equals(msg_type)) {
                    getActivity().stopService(ServiceIntent);
                    Toast.makeText(getActivity(), "서버가 처자고 있네요 ^^;;", Toast.LENGTH_SHORT).show();
                }
            }
        };

        getActivity().registerReceiver(receiver, intentFilter);
        Log.d(TAG, "BroadcastReceiver setup");


        if (s_email != null && s_password != null) {
            sendData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        /* fragment 는 여기서 View 객체를 초기화 한다. */
        et_email = view.findViewById(R.id.fsi_et_email);
        et_password = view.findViewById(R.id.fsi_et_password);
        bt_sign_in = view.findViewById(R.id.fsi_bt_sign_in);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        bt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_email = et_email.getText().toString();
                s_password = et_password.getText().toString();

                if ("".equals(s_email)) {
                    //todo 입력이 없는 경우
                    Toast.makeText(getActivity(), "아이디를 입력해라.", Toast.LENGTH_SHORT).show();
                } else if ("".equals(s_password)) {
                    //todo 아이디는 입력했으나 비밀번호를 입력 안한 경우
                    Toast.makeText(getActivity(), "비밀번호를 입력해라.", Toast.LENGTH_SHORT).show();
                } else {
                    //todo 둘 다 제대로 입력한 경우
                    Log.d(TAG, "Sign in valid");
                    sendData();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        //todo 반드시 리시버는 프래그먼트가 종료될 때, 반환되어야 자원 누수가 안생긴다(lack)
        if (this.receiver != null) {
            getActivity().unregisterReceiver(receiver);
            Log.d(TAG, "BroadcastReceiver unregister");
        }
    }

    private void sendData() {
        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "sign_in");
            jsonObject.put("email", s_email);
            jsonObject.put("password", s_password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sign_in_data = jsonObject.toString();
        Log.d(TAG, "onResume setOnClickListener json : " + sign_in_data);

        //todo JoinService를 통해 서버와 통신
        //todo Activity를 인자로 넘길경우 메모리 누수가 생길 수 있다(화면 회전), 따라서 Application의 context를 넘긴다.
        ServiceIntent = new Intent(getActivity().getApplicationContext(), JoinService.class);
        ServiceIntent.putExtra("frag_type", "sign_in");
        ServiceIntent.putExtra("join", sign_in_data);
        getActivity().startService(ServiceIntent);
    }
}
