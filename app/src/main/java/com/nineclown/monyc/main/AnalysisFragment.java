package com.nineclown.monyc.main;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.nineclown.monyc.R;
import com.nineclown.monyc.main.chart.AnalysisBook;
import com.nineclown.monyc.main.chart.AnalysisMap;
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
public class AnalysisFragment extends Fragment {
    private static final String TAG = "AnalysisFragment";
    //ui
    private TextView tv_calendar;
    private ImageButton bt_left;
    private ImageButton bt_right;
    private TextView tv_expenses_price;
    private TextView tv_income_price;
    private RelativeLayout rl_income_box;
    private RelativeLayout rl_expenses_box;

    //local db관리
    private String s_email;
    private String send_data;
    private SharedPreferences auto;

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

    //차트 관리
    private PieChart pieChart;
    private ArrayList<String> xEntries;
    private ArrayList<PieEntry> yEntries;
    private AnalysisMap analysisMap;
    private String total_income;
    private String total_expenses;


    //DB로 받은 데이터를 저장
    private ArrayList<AnalysisBook> accountBooks;
    private String item_type;
    private String item_price;
    private String item_category;
    private String item_pay;
    private Long expenses_price;
    private Long income_price;
    private DecimalFormat format;

    public AnalysisFragment() {
        // Required empty public constructor
    }

    public static AnalysisFragment newInstance() {
        Bundle args = new Bundle();

        AnalysisFragment fragment = new AnalysisFragment();
        fragment.setArguments(args);
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
        //todo init data
        xEntries = new ArrayList<>();
        yEntries = new ArrayList<>();
        accountBooks = new ArrayList<>();
        analysisMap = new AnalysisMap();

        //todo local DB에 접근
        auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
        auto = getActivity().getSharedPreferences("account_book", Activity.MODE_PRIVATE);
    }

    private void initChart() {
        setIncome();
        addDataSet(total_income);
        setOnSelectPieChart(total_income);
    }

    private void setBroadcast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nineClown.monyc.ANALYSIS");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                received_data = intent.getStringExtra("data_result");
                Log.d(TAG, "BroadcastReceiver received");

                //todo 리시버로 받아온 데이터(서버에서 가져온)를 화면에 뿌려줘야 한다.
                setItemView();
                getActivity().stopService(ServiceIntent);
                Log.d(TAG, "리스트를 가져왔습니다");
                Log.d(TAG, "onReceive: accountBooks data length " + accountBooks.size());

                initChart();

            }
        };

        getActivity().registerReceiver(receiver, intentFilter);
        Log.d(TAG, "BroadcastReceiver setup");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        //todo 여기가 activity의 oncreate와 같은 곳? view 객체를 생성하는 작업을 하는 곳.
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);
        bt_left = view.findViewById(R.id.fa_bt_left);
        tv_calendar = view.findViewById(R.id.fa_tv_calendar);
        bt_right = view.findViewById(R.id.fa_bt_right);
        tv_expenses_price = view.findViewById(R.id.fa_tv_expenses_price);
        tv_income_price = view.findViewById(R.id.fa_tv_income_price);
        pieChart = view.findViewById(R.id.fa_pie_chart);
        rl_income_box = view.findViewById(R.id.fa_income_box);
        rl_expenses_box = view.findViewById(R.id.fa_expenses_box);
        tv_calendar.setText(new SimpleDateFormat("MM월").format(calendar.getTime()));
        s_month = new SimpleDateFormat("yyyy.MM").format(calendar.getTime());
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(30f);
        pieChart.setTransparentCircleAlpha(5);
        pieChart.setCenterTextSize(13);
        //pieChart.setDrawEntryLabels(true);
        setButtons();
        setCalendarSwipe();

        return view;
    }

    private void setOnSelectPieChart(String type) {
        String types = type.toString().replaceAll(",|원", "");
        final Float price = Float.parseFloat(types);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int position = e.toString().indexOf("y: ");
                String sales = e.toString().substring(position + 3);
                //Log.d(TAG, "onValueSelected: sales" + sales);
                for (int i = 0; i < analysisMap.size(); i++) {
                    if (analysisMap.getPrice(i) / price * 100 == Float.parseFloat(sales)) {
                        position = i;
                        break;
                    }
                }
                int employee = analysisMap.getCount(position);
                Float data = analysisMap.getPrice(position);
                format = new DecimalFormat("###,###원");
                String result = format.format(data);
                Toast.makeText(getContext(), "총액 : " + result + ", 횟수: " + Integer.toString(employee) + "번", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void setButtons() {
        rl_income_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountBooks.size() != 0) {
                    setIncome();
                    addDataSet(total_income);
                    setOnSelectPieChart(total_income);
                }
            }
        });

        rl_expenses_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountBooks.size() != 0) {
                    setExpenses();
                    addDataSet(total_expenses);
                    setOnSelectPieChart(total_expenses);
                }
            }
        });

    }

    private void setIncome() {
        analysisMap.clear();
        if (accountBooks.size() == 0) {
            pieChart.setCenterText("데이터가 없습니다.");
        } else
            pieChart.setCenterText("수입 내역");

        for (int i = 0; i < accountBooks.size(); i++) {
            if ("income".equals(accountBooks.get(i).getType())) {
                analysisMap.add(accountBooks.get(i).getCategory(), accountBooks.get(i).getPrice());
                Log.d(TAG, "setIncome: analysisMap add");
            }
        }
    }

    public void setExpenses() {
        analysisMap.clear();
        if (accountBooks.size() == 0) {
            pieChart.setCenterText("데이터가 없습니다.");
        } else
            pieChart.setCenterText("지출 내역");

        for (int i = 0; i < accountBooks.size(); i++) {
            if ("expenses".equals(accountBooks.get(i).getType())) {
                analysisMap.add(accountBooks.get(i).getCategory(), accountBooks.get(i).getPrice());
            }
        }
    }

    private void addDataSet(String type) {
        String types = type.toString().replaceAll(",|원", "");
        Float price = Float.parseFloat(types);
        yEntries.clear();
        xEntries.clear();
        Log.d(TAG, "addDataSet started");

        for (int i = 0; i < analysisMap.size(); i++) {
            yEntries.add(new PieEntry(analysisMap.getPrice(i) / price * 100, analysisMap.getCategory(i)));
            //Log.d(TAG, "addDataSet: MapPrice + " + analysisMap.getPrice(i) + " + yEntries " + yEntries.get(i) + "xEntries " + analysisMap.getCategory(i));
        }

        for (int i = 0; i < analysisMap.size(); i++) {
            xEntries.add(analysisMap.getCategory(i));
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntries, "");
        pieDataSet.setValueTextSize(13);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#F74961"));
        colors.add(Color.parseColor("#78B2F9"));
        colors.add(Color.parseColor("#a769ff"));
        colors.add(Color.parseColor("#8aff80"));
        colors.add(Color.parseColor("#ffba69"));

        pieDataSet.setColors(colors);


        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        requestToServer();
    }

    private void requestToServer() {
        accountBooks.clear();
        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "analysis");
            jsonObject.put("email", s_email);
            jsonObject.put("month", s_month);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_data = jsonObject.toString();
        ServiceIntent = new Intent(getActivity().getApplicationContext(), DataService.class);
        ServiceIntent.putExtra("comp_type", "analysis");
        ServiceIntent.putExtra("analysis_data", send_data);
        Log.d(TAG, "send data : " + send_data);
        getActivity().startService(ServiceIntent);
    }

    private void setItemView() {
        expenses_price = Long.valueOf(0);
        income_price = Long.valueOf(0);
        try {
            JSONArray jsonArray = new JSONArray(received_data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                item_type = jsonObject.getString("types");
                item_price = jsonObject.getString("price");
                item_category = jsonObject.getString("category");
                item_pay = jsonObject.getString("pay");
                setData(item_type, item_price, item_category, item_pay);
                setPrice();
            }
            format = new DecimalFormat("###,###원");
            tv_expenses_price.setText(format.format(expenses_price));
            tv_income_price.setText(format.format(income_price));
            total_expenses = tv_expenses_price.getText().toString();
            total_income = tv_income_price.getText().toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPrice() {
        //todo 지출과 수입 금액을 합치기 위해 string을 long으로 바꿔서 더한 뒤에 다시 string으로 바꿈
        if ("expenses".equals(item_type)) {
            expenses_price += Long.parseLong(item_price);
        } else if ("income".equals(item_type)) {
            income_price += Long.parseLong(item_price);
        } else {
        }
    }

    private void setData(String type, String price, String category, String pay) {
        //todo Recycler View에 들어갈 데이터를 추가하는 곳accountBooks.clear();
        AnalysisBook item = new AnalysisBook();
        item.setType(type);
        item.setPrice(price);
        item.setCategory(category);
        item.setPay(pay);
        accountBooks.add(item);
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
