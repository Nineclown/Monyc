package com.nineclown.monyc.main;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineclown.monyc.R;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ViewPager pager;
    private LinearLayout linearLayout;
    private ImageButton bt_date;
    private ImageButton bt_calender;
    private ImageButton bt_analysis;
    private TextView tv_log_out;
    private TextView tv_email;

    private SharedPreferences auto;
    private SharedPreferences.Editor autoLogin;
    private String s_email;

    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.am_Tab_Box);
        bt_date = findViewById(R.id.am_bt_date);
        bt_calender = findViewById(R.id.am_bt_calendar);
        bt_analysis = findViewById(R.id.am_bt_analysis);
        pager = findViewById(R.id.am_pager);
        tv_log_out = findViewById(R.id.am_tv_log_out);
        tv_email = findViewById(R.id.am_tv_email);

        initLocal();
        setNavigation();
        setButton();
        setFragment();

    }

    private void initLocal() {
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        autoLogin = auto.edit();
        s_email = auto.getString("inputEmail", null);
    }

    private void checkPermission() {
        //todo 권한이 있는지 check. api 23? 이상 부터 이거 해줘야댐.
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS 수신권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS 수신권한 없음", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                Toast.makeText(this, "SMS 권한 설정 필요", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
            }
        }
    }

    private void setNavigation() {
        //todo navigation bar에 보여줄 정보(email, log_out) 초기화.
        tv_email.setText(s_email);
        tv_log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoLogin.clear();
                autoLogin.commit();
                Toast.makeText(getApplicationContext(), "로그 아웃", Toast.LENGTH_SHORT).show();
                DrawerLayout drawer = findViewById(R.id.drawer);
                drawer.closeDrawer(Gravity.LEFT);
                finish();
            }
        });
    }

    private void setButton() {
        View.OnClickListener movePageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                int i = 0;
                while (i < 3) {
                    if (tag == i) {
                        linearLayout.findViewWithTag(i).setSelected(true);
                    } else {
                        linearLayout.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
                pager.setCurrentItem(tag);
            }
        };

        bt_date.setOnClickListener(movePageListener);
        bt_date.setTag(0);
        bt_calender.setOnClickListener(movePageListener);
        bt_calender.setTag(1);
        bt_analysis.setOnClickListener(movePageListener);
        bt_analysis.setTag(2);

        bt_date.setSelected(true);
    }

    private void setFragment() {
        pager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(0);

        /* 얜 슬라이드 할 경우, 변경되는 걸 구현 */
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int i = 0;
                while (i < 3) {
                    if (position == i) {
                        linearLayout.findViewWithTag(i).setSelected(true);
                    } else {
                        linearLayout.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        // actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DateFragment.newInstance();
                case 1:
                    return CalendarFragment.newInstance();
                case 2:
                    return AnalysisFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("액티비티", "MainActivity onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            Toast.makeText(getApplicationContext(), "뒤로버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).cancel();
        }

    }

     /*
    //todo 권한이 없는 경우, 승인을 위한 콜백 함수.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 거부됨.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    */

    /* //todo 동적 다이얼로그
        new AlertDialog.Builder(this)
                .setTitle("종료 창")
                .setMessage("종료 할꺼야?")
                .setCancelable(false)
                .setPositiveButton("응", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("아니", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    */
}
