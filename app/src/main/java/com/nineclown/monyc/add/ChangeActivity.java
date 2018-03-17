package com.nineclown.monyc.add;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nineclown.monyc.R;
import com.nineclown.monyc.system.DataService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChangeActivity extends AppCompatActivity {
    private static final String TAG = "ChangeActivity";
    private RelativeLayout layout;
    //맨위의 버튼 3개
    private TextView tv_expenses;
    private TextView tv_income;
    private TextView tv_withdraw;

    //사용자가 입력하는 나머지 창
    private EditText et_price;
    private EditText et_category;
    private EditText et_pay;
    private EditText et_contents;
    private TextView tv_date;
    private TextView tv_time;

    //xml의 값(사용자가 입력한)을 받을 object
    private String s_id;
    private String s_type;
    private String s_price;
    private String s_category;
    private String s_pay;
    private String s_contents;
    private String s_date;

    private int i_bt_type; // 버튼(3가지) 중 한 값을 받아서 임시저장(string으로 변환 전)

    //local xml(DB)에서 사용자 정보 가져옴
    private String s_email;
    private SharedPreferences auto;

    //set broadcast
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Calendar calendar;

    //service로 전송할 때 쓸 object
    private JSONObject jsonObject;
    private String send_data;
    private String received_data;
    private Intent ServiceIntent;

    //Calendar에 사용될 변수
    private int i_year;
    private int i_month;
    private int i_day;
    private int i_hour;
    private int i_minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        Log.d(TAG, "onCreate");

        tv_expenses = findViewById(R.id.ca_tv_expenses);
        tv_income = findViewById(R.id.ca_tv_income);
        tv_withdraw = findViewById(R.id.ca_tv_withdraw);

        et_price = findViewById(R.id.ca_et_price);
        et_category = findViewById(R.id.ca_et_category);
        et_pay = findViewById(R.id.ca_et_pay);
        et_contents = findViewById(R.id.ca_et_contents);
        tv_date = findViewById(R.id.ca_tv_ondate);
        tv_time = findViewById(R.id.ca_tv_ontime);

        layout = findViewById(R.id.ca_class_box);
        calendar = Calendar.getInstance();

        initLocal();
        //todo adapter로부터 데이터를 받아옴(사용자가 리스트 클릭시)
        getItemData();
        setBroadcast();
        initData();

    }

    private void initLocal() {
        //todo local xml에 접근해 사용자 정보 가져옴.
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
    }

    private void initData() {
        //todo 버튼으로 고르는 부분
        setTypeButton();

        et_price.setText(s_price);
        et_category.setText(s_category);
        et_pay.setText(s_pay);
        et_contents.setText(s_contents);

        //todo date 부분
        setDate();
    }

    private void setDate() {
        String dates[] = s_date.split(" ");
        //Log.d("ChangeActivity", "dates[0] : " + dates[0]);
        String date[] = dates[0].split("\\.");
        //Log.d("ChangeActivity", "date[0] : " + date[0]);
        i_year = Integer.parseInt(date[0]);
        i_month = Integer.parseInt(date[1]);
        i_day = Integer.parseInt(date[2]);
        String time[] = dates[1].split(":");
        i_hour = Integer.parseInt(time[0]);
        i_minute = Integer.parseInt(time[1]);

        calendar.set(i_year, i_month - 1, i_day, i_hour, i_minute);
        tv_date.setText(new SimpleDateFormat("yyyy.MM.dd").format(calendar.getTime()));
        tv_time.setText(new SimpleDateFormat("a hh시 mm분").format(calendar.getTime()));
    }

    private void setTypeButton() {
        View.OnClickListener movePageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                i_bt_type = tag;
                int i = 0;
                while (i < 3) {
                    if (tag == i) {
                        layout.findViewWithTag(i).setSelected(true);
                    } else {
                        layout.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
            }
        };

        tv_expenses.setOnClickListener(movePageListener);
        tv_expenses.setTag(0);
        tv_income.setOnClickListener(movePageListener);
        tv_income.setTag(1);
        tv_withdraw.setOnClickListener(movePageListener);
        tv_withdraw.setTag(2);

        switch (s_type) {
            case "expenses":
                tv_expenses.setSelected(true);
                i_bt_type = 0;
                break;
            case "income":
                tv_income.setSelected(true);
                i_bt_type = 1;
                break;
            case "withdraw":
                tv_withdraw.setSelected(true);
                i_bt_type = 2;
                break;
        }
    }

    private void getItemData() {
        Intent intent = getIntent();
        s_id = intent.getStringExtra("id");
        s_type = intent.getStringExtra("type");
        s_price = intent.getStringExtra("price");
        s_category = intent.getStringExtra("category");
        s_pay = intent.getStringExtra("pay");
        s_contents = intent.getStringExtra("contents");
        s_date = intent.getStringExtra("date");
    }

    protected String setStrDataToComma(String str) {
        try {
            if (str.length() == 0)
                return "";
            long value = Long.parseLong(str);
            DecimalFormat format = new DecimalFormat("###,###원");
            return format.format(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "숫자만 입력하세요.", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void setBroadcast() {
        //todo Service로 부터 서버에서 전송된 값을 전달받기 위한 브로드캐스트 설정

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nineClown.monyc.CHANGE");

        //todo 동적 리시버 구현
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                received_data = intent.getStringExtra("data_result");
                Log.d(TAG, "Broadcastreceived received : " + received_data);

                if ("0020".equals(received_data)) {
                    stopService(ServiceIntent);
                    Toast.makeText(getApplicationContext(), "가계부를 수정했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("0030".equals(received_data)) {
                    stopService(ServiceIntent);
                    Toast.makeText(getApplicationContext(), "가계부를 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("9999".equals(received_data)) {
                    stopService(ServiceIntent);
                    Toast.makeText(getApplicationContext(), "서버가 처자고 있네요 ^^;;", Toast.LENGTH_SHORT).show();
                } else {
                    stopService(ServiceIntent);
                    Toast.makeText(getApplicationContext(), "요청을 처리하지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(receiver, intentFilter);
        Log.d(TAG, "BroadcastReceiver setup");
    }

    @Override
    protected void onResume() {
        super.onResume();


        et_price.addTextChangedListener(new TextWatcher() {
            String amount;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(amount)) {
                    try {
                        amount = setStrDataToComma(s.toString().replaceAll(",|원", ""));
                        et_price.setText(amount);
                        Editable e = et_price.getText();
                        if (amount.length() == 0) {
                            Selection.setSelection(e, amount.length());
                        } else
                            Selection.setSelection(e, amount.length() - 1);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //todo category field check
        et_category.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    showCategory();
            }
        });

        //todo 결제 수단 필드
        et_pay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    showPay();
            }
        });

        //todo date field
        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDate();
            }
        });
        tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTime();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("내역 변경");

        getMenuInflater().inflate(R.menu.menu_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_change_complete) {
            sendToServerAll();
        }
        if (id == R.id.menu_change_delete) {
            sendToServerId();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendToServerAll() {
        if (i_bt_type == 0)
            s_type = "expenses";
        else if (i_bt_type == 1)
            s_type = "income";
        else
            s_type = "withdraw";

        s_price = et_price.getText().toString().replaceAll(",|원", "");
        s_category = et_category.getText().toString();
        s_pay = et_pay.getText().toString();
        s_contents = et_contents.getText().toString();
        s_date = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.getTime());
        Log.d(TAG, "date : " + s_date);

        if (!"".equals(s_price)) {
            //todo 제대로 입력한 경우
            Log.d(TAG, "입력 값 valid");

            jsonObject = new JSONObject();
            try {
                jsonObject.put("type", "change");
                jsonObject.put("id", s_id);
                jsonObject.put("email", s_email);
                jsonObject.put("types", s_type);
                jsonObject.put("price", s_price);
                jsonObject.put("category", s_category);
                jsonObject.put("pay", s_pay);
                jsonObject.put("contents", s_contents);
                jsonObject.put("date", s_date);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            send_data = jsonObject.toString();
            Log.d(TAG, "onOptionsItemSelected json : " + send_data);

            //todo DataService를 통해 서버와 통신
            ServiceIntent = new Intent(getApplicationContext(), DataService.class);
            ServiceIntent.putExtra("comp_type", "change");
            ServiceIntent.putExtra("change_data", send_data);
            startService(ServiceIntent);
        } else {
            //todo 금액을 입력 안한 경우
            Log.d(TAG, "invalid : input price");
            Toast.makeText(getApplicationContext(), "금액을 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToServerId() {
        s_price = et_price.getText().toString().replaceAll(",|원", "");
        s_date = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.getTime());

        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "delete");
            jsonObject.put("id", s_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_data = jsonObject.toString();
        Log.d(TAG, "onOptionsItemSelected json : " + send_data);

        //todo DataService를 통해 서버와 통신
        ServiceIntent = new Intent(getApplicationContext(), DataService.class);
        ServiceIntent.putExtra("comp_type", "change");
        ServiceIntent.putExtra("change_data", send_data);
        startService(ServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (this.receiver != null) {
            unregisterReceiver(receiver);
            Log.d(TAG, "BroadcastReceiver unregister");
        }
    }

    private void showCategory() {
        final CharSequence info[] = new CharSequence[]{"식사", "카페/간식", "의료/건강", "술/유흥", "아르바이트", "용돈"};
        new AlertDialog.Builder(this)
                .setTitle("카테고리")
                .setItems(info, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                et_category.setText("식사");
                                et_pay.requestFocus();
                                break;
                            case 1:
                                et_category.setText("카페/간식");
                                et_pay.requestFocus();
                                break;
                            case 2:
                                et_category.setText("의료/건강");
                                et_pay.requestFocus();
                                break;
                            case 3:
                                et_category.setText("술/유흥");
                                et_pay.requestFocus();
                                break;
                            case 4:
                                et_category.setText("아르바이트");
                                et_pay.requestFocus();
                                break;
                            case 5:
                                et_category.setText("용돈");
                                et_pay.requestFocus();
                                break;
                        }
                        dialog.dismiss();
                    }
                }).show();

    }

    private void showPay() {
        final CharSequence info[] = new CharSequence[]{"카드", "현금"};
        new AlertDialog.Builder(this)
                .setTitle("결제수단")
                .setItems(info, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                et_pay.setText("카드");
                                et_contents.requestFocus();
                                break;
                            case 1:
                                et_pay.setText("현금");
                                et_contents.requestFocus();
                                break;
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showDate() {
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                //month는 1을 뺀 값이 출력된다. ex) 11월을 눌렀는데 month return value는 10이다.
                // 아마 0 ~ 11인 듯?
                //Log.d("달 체크", "month : " + month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                //Log.d("일 체크", "day : " + dayOfMonth);
                tv_date.setText(new SimpleDateFormat("yyyy.MM.dd").format(calendar.getTime()));
            }
        };
        DatePickerDialog alert = new DatePickerDialog(this, onDateSetListener, i_year, i_month - 1, i_day);
        alert.show();

    }

    private void showTime() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // 0 - 23 까지. 12가 오후 12시. 0이 오전 12시.
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                tv_time.setText(new SimpleDateFormat("a hh시 mm분").format(calendar.getTime()));
            }
        };

        TimePickerDialog alert = new TimePickerDialog(this, onTimeSetListener, i_hour, i_minute, false);
        alert.show();

    }
}
