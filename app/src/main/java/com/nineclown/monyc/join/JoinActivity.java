package com.nineclown.monyc.join;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineclown.monyc.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {
    private static final String TAG = "JoinActivity";
    //git test

    private ViewPager pager;
    private RelativeLayout relativeLayout;
    private TextView tv_sign_in;
    private TextView tv_sign_up;


    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$"); // 4자리 ~ 16자리까지 가능


    public static boolean validateEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    public static boolean validatePassword(String password) {
        Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(password);
        return matcher.find();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_join);
        /* 사용할 view 객체 초기화 */
        relativeLayout = findViewById(R.id.aj_Join_Tab);
        tv_sign_in = findViewById(R.id.aj_tv_sign_in);
        tv_sign_up = findViewById(R.id.aj_tv_sign_up);
        pager = findViewById(R.id.aj_pager);

        /*얜 버튼을 누를 경우, 변경되는 걸 구현 */
        View.OnClickListener movePageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();

                int i = 0;
                while (i < 2) {
                    if (tag == i) {
                        relativeLayout.findViewWithTag(i).setSelected(true);
                    } else {
                        relativeLayout.findViewWithTag(i).setSelected(false);
                    }
                    i++;
                }
                pager.setCurrentItem(tag);
            }
        };

        /* sign_in, sign_up 설정 */
        tv_sign_in.setOnClickListener(movePageListener);
        tv_sign_in.setTag(0);

        tv_sign_up.setOnClickListener(movePageListener);
        tv_sign_up.setTag(1);
        tv_sign_in.setSelected(true);


        /* adapter setting: View Pager 와 Adapter 는 함께 쓴다? */
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
                while (i < 2) {
                    if (position == i) {
                        relativeLayout.findViewWithTag(i).setSelected(true);
                    } else {
                        relativeLayout.findViewWithTag(i).setSelected(false);
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
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
       /* if (isService) {
            unbindService(serviceConnection);
            isService = false;
        }
        */
    }

    /* 해당하는 fragment 띄워주는 Adapter */
    private class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SignInFragment.newInstance();
                case 1:
                    return SignUpFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /*
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
    */
}
