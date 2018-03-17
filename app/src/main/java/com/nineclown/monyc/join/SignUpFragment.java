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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nineclown.monyc.R;
import com.nineclown.monyc.main.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";

    private EditText et_email;
    private EditText et_password;
    private EditText et_confirm_password;
    private TextView tv_confirm_password;
    private TextView tv_confirm_email;
    private Button bt_sign_up;

    private String s_email;
    private String s_password;
    private String s_confirm_password;

    private JSONObject jsonObject;
    private String sign_up_data;

    //서버로부터 받은 데이터
    private String msg_type;

    private boolean isValidEmail = false;
    private boolean isValidPassword = false;
    private Intent ServiceIntent;

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    //todo autologin 기능
    private SharedPreferences auto;
    private SharedPreferences.Editor autoLogin;
    private boolean isChecked = false;

    public SignUpFragment() {

    }

    public static SignUpFragment newInstance() {
        Bundle args = new Bundle();

        SignUpFragment fragment = new SignUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
        s_password = auto.getString("inputPassword", null);
        /* broadcast Action을 등록하기 위한 intentfilter */
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nineClown.monyc.SIGN_UP");

        //todo 동적 리시버 구현
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                msg_type = intent.getStringExtra("msg_type");
                Log.d(TAG, "BroadcastReceiver received");

                if ("0000".equals(msg_type)) {
                    //한번만 등록하면 돼.
                    if (!isChecked) {
                        SharedPreferences.Editor autoLogin = auto.edit();
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
                } else if ("0002".equals(msg_type)) {
                    getActivity().stopService(ServiceIntent);
                    Toast.makeText(getActivity(), "이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                } else if ("9999".equals(msg_type)) {
                    getActivity().stopService(ServiceIntent);
                    Toast.makeText(getActivity(), "서버가 처자고 있네요 ^^;;", Toast.LENGTH_SHORT).show();
                }
            }
        };

        getActivity().registerReceiver(receiver, intentFilter);
        Log.d(TAG, "BroadcastReceiver setup");

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");
        /* tab 간의 이동시 반드시 호출된다. 따라서 중복정의하면 안되는 것들을 여기에 적으면 안돼. */
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        /* fragment 는 여기서 View 객체를 초기화 한다. */
        et_email = getView().findViewById(R.id.fsu_et_email);
        et_password = getView().findViewById(R.id.fsu_et_password);
        et_confirm_password = getView().findViewById(R.id.fsu_et_confirm_password);
        tv_confirm_email = getView().findViewById(R.id.fsu_tv_confirm_email);
        tv_confirm_password = getView().findViewById(R.id.fsu_tv_confirm_password);
        bt_sign_up = getView().findViewById(R.id.fsu_bt_sign_up);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "nResume");

        //todo 사용자에게 입력이 잘되었는지 직관적으로 보여주기 위한 소스 코드
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //todo 택스트 입력이 끝났을 때 call back
                //Toast.makeText(getApplicationContext(), "beforeTextChanged 뭘까?", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //todo 텍스트가 변경 될때마다 call back

                //Toast.makeText(getApplicationContext(), "onTextChanged 뭘까??", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //todo 텍스트가 입력하기 전에 call back
                if (!JoinActivity.validateEmail(s.toString())) {
                    System.out.println("invalid\n");
                    tv_confirm_email.setVisibility(View.VISIBLE);
                    tv_confirm_email.setText("invalid");
                    isValidEmail = false;

                    //et_email.setSelection(0, et_email.length());
                } else {
                    tv_confirm_email.setVisibility(View.INVISIBLE);
                    isValidEmail = true;

                }
                //Toast.makeText(getActivity(), "afterTextChanged 뭘까??", Toast.LENGTH_SHORT).show();
            }
        });

        //todo 사용자에게 입력이 잘되었는지 직관적으로 보여주기 위한 소스 코드
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!JoinActivity.validatePassword(s.toString())) {
                    tv_confirm_password.setVisibility(View.VISIBLE);
                    tv_confirm_password.setText("invalid");
                    isValidPassword = false;
                } else {
                    tv_confirm_password.setVisibility(View.INVISIBLE);
                    isValidPassword = true;
                }
            }
        });

        //todo 실제로 입력한 값을 체크해서 결과를 toast
        bt_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                s_email = et_email.getText().toString();
                s_password = et_password.getText().toString();
                s_confirm_password = et_confirm_password.getText().toString();

                if ((s_password.equals(s_confirm_password)) && isValidEmail && isValidPassword) {
                    //todo 제대로 입력한 경우
                    Log.d(TAG, "valid : valid");
                    sendData();
                } else if (!isValidEmail) {
                    Toast.makeText(getActivity(), "이메일을 다시 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword) {
                    Toast.makeText(getActivity(), "비밀번호를 다시 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    //todo 비밀번호가 틀린 경우
                    Toast.makeText(getActivity(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (this.receiver != null) {
            getActivity().unregisterReceiver(receiver);
            Log.d(TAG, "BroadcastReceiver unregister");
        }
    }

    private void sendData() {
        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "sign_up");
            jsonObject.put("email", s_email);
            jsonObject.put("password", s_password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sign_up_data = jsonObject.toString();
        Log.d(TAG, "onResume setOnClickListener json : " + sign_up_data);

        //todo JoinService를 통해 서버와 통신
        ServiceIntent = new Intent(getActivity(), JoinService.class);
        ServiceIntent.putExtra("frag_type", "sign_up");
        ServiceIntent.putExtra("join", sign_up_data);
        getActivity().startService(ServiceIntent);
    }
}
