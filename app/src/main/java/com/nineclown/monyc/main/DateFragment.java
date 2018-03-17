package com.nineclown.monyc.main;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nineclown.monyc.R;
import com.nineclown.monyc.add.AddActivity;
import com.nineclown.monyc.main.list.AccountBook;
import com.nineclown.monyc.main.list.DividerItemDecoration;
import com.nineclown.monyc.main.list.RecyclerAdapter;
import com.nineclown.monyc.system.DataService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DateFragment extends Fragment {
    private static final String TAG = "DateFragment";

    //UI
    private ArrayList<AccountBook> accountBooks = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager manager;
    private Button bt_add_item;
    private TextView tv_calendar;
    private ImageButton bt_left;
    private ImageButton bt_right;
    private TextView tv_expenses_price;
    private TextView tv_income_price;
    private TextView tv_notify;

    //통신을 위한 변수
    private JSONObject jsonObject;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Intent ServiceIntent;
    private String received_data;

    //날짜 관리
    private Calendar calendar;
    private String s_year;
    private String s_month;

    //local db관리
    private String s_email;
    private String send_data;
    private SharedPreferences auto;

    //DB로 받은 데이터를 저장
    private String item_id;
    private String item_type;
    private String item_price;
    private String item_category;
    private String item_pay;
    private String item_contents;
    private String item_dates;
    private Long expenses_price;
    private Long income_price;
    private DecimalFormat format;

    public DateFragment() {
        // Required empty public constructor
    }

    public static DateFragment newInstance() {
        Bundle args = new Bundle();
        DateFragment fragment = new DateFragment();
        fragment.setArguments(args);
        Log.d(TAG, "newInstance");
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        calendar = Calendar.getInstance();

        initLocal();
        setBroadcast();
    }

    private void initLocal() {
        //todo local DB에 접근
        auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
        auto = getActivity().getSharedPreferences("account_book", Activity.MODE_PRIVATE);
    }

    private void setBroadcast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nineClown.monyc.DATE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                received_data = intent.getStringExtra("data_result");
                Log.d(TAG, "BroadcastReceiver received");

                //todo 리시버로 받아온 데이터(서버에서 가져온)를 리스트에 뿌려줘야 한다.
                setItemView();
                getActivity().stopService(ServiceIntent);
                Log.d(TAG, "리스트를 가져왔습니다");
            }
        };

        getActivity().registerReceiver(receiver, intentFilter);
        Log.d(TAG, "BroadcastReceiver setup");
    }

    private void setItemView() {
        expenses_price = Long.valueOf(0);
        income_price = Long.valueOf(0);
        try {
            JSONArray jsonArray = new JSONArray(received_data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                item_id = jsonObject.getString("id");
                item_type = jsonObject.getString("types");
                item_price = jsonObject.getString("price");
                item_category = jsonObject.getString("category");
                item_pay = jsonObject.getString("pay");
                item_contents = jsonObject.getString("contents");
                item_dates = jsonObject.getString("date");

                //todo 지출과 수입 금액을 합치기 위해 string을 long으로 바꿔서 더한 뒤에 다시 string으로 바꿈
                if ("expenses".equals(item_type)) {
                    expenses_price += Long.parseLong(item_price);
                } else if ("income".equals(item_type)) {
                    income_price += Long.parseLong(item_price);
                } else {
                }
                setData(item_id, item_type, item_price, item_category, item_contents, item_pay, item_dates);
            }

            if (accountBooks.size() == 0) {
                tv_notify.setText("가게부를 추가해 주세요");
            } else {
                tv_notify.setText("");
            }
            format = new DecimalFormat("###,###원");
            tv_expenses_price.setText(format.format(expenses_price));
            tv_income_price.setText(format.format(income_price));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        //todo 여기가 activity의 oncreate와 같은 곳? view 객체를 생성하는 작업을 하는 곳.
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date, container, false);
        bt_left = view.findViewById(R.id.fd_bt_left);
        tv_calendar = view.findViewById(R.id.fd_tv_calendar);
        bt_right = view.findViewById(R.id.fd_bt_right);
        tv_expenses_price = view.findViewById(R.id.fd_tv_expenses_price);
        tv_income_price = view.findViewById(R.id.fd_tv_income_price);
        tv_notify = view.findViewById(R.id.fd_tv_notify);

        //todo recyclerview 처리
        recyclerView = view.findViewById(R.id.recycler_account_book);
        adapter = new RecyclerAdapter(accountBooks, getContext());
        manager = new LinearLayoutManager(getActivity().getApplicationContext());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), 40));
        }
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        bt_add_item = view.findViewById(R.id.fd_bt_add_item);
        tv_calendar.setText(new SimpleDateFormat("MM월").format(calendar.getTime()));
        s_month = new SimpleDateFormat("yyyy.MM").format(calendar.getTime());

        setCalendarSwipe();

        return view;
    }

    private void setCalendarSwipe() {
        bt_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                s_year = new SimpleDateFormat("yyyy").format(calendar.getTime());
                s_month = new SimpleDateFormat("yyyy.MM").format(calendar.getTime());
                if ("2017".equals(s_year)) {
                    tv_calendar.setText(new SimpleDateFormat("MM월").format(calendar.getTime()));
                } else
                    tv_calendar.setText(new SimpleDateFormat("yyyy년 MM월").format(calendar.getTime()));
                //todo 갱신을 해야대.
                requestToServer();

            }
        });

        bt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, +1);
                s_year = new SimpleDateFormat("yyyy").format(calendar.getTime());
                s_month = new SimpleDateFormat("yyyy.MM").format(calendar.getTime());
                if ("2017".equals(s_year)) {
                    tv_calendar.setText(new SimpleDateFormat("MM월").format(calendar.getTime()));
                } else
                    tv_calendar.setText(new SimpleDateFormat("yyyy년 MM월").format(calendar.getTime()));
                requestToServer();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //todo view에 data를 추가하는 작업등을 하는 곳. UI 변경 작업하는 곳.
        Log.d(TAG, "onActivityCreated");
    }

    private void requestToServer() {
        accountBooks.clear();
        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "date");
            jsonObject.put("email", s_email);
            jsonObject.put("month", s_month);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_data = jsonObject.toString();
        ServiceIntent = new Intent(getActivity().getApplicationContext(), DataService.class);
        ServiceIntent.putExtra("comp_type", "date");
        ServiceIntent.putExtra("date_data", send_data);
        Log.d("DateFragment", "send data : " + send_data);
        getActivity().startService(ServiceIntent);
    }

    private void setData(String id, String type, String price, String category, String contents, String pay, String date) {
        //todo Recycler View에 들어갈 데이터를 추가하는 곳accountBooks.clear();
        AccountBook item = new AccountBook();
        item.setId(id);
        item.setType(type);
        item.setPrice(price);
        item.setCategory(category);
        item.setContents(contents);
        item.setPay(pay);
        item.setDate(date);
        accountBooks.add(item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        requestToServer();
        bt_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddActivity.class);
                startActivity(intent);
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
}
