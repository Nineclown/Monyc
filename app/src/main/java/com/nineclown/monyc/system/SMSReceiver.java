package com.nineclown.monyc.system;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.nineclown.monyc.system.DataService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nineClown on 2017-11-24.
 */

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSReceiver";
    private Bundle bundle;
    private SmsMessage currentSMS;
    private String message;

    private String s_type;
    private String s_price;
    private String s_category;
    private String s_pay;
    private String s_contents;
    private String s_date;

    private String s_email;
    private SharedPreferences auto;

    private JSONObject jsonObject;
    private String send_data;
    private Intent ServiceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        setData(context);
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_o = (Object[]) bundle.get("pdus");
                if (pdu_o != null) {
                    for (Object o : pdu_o) {
                        //todo 여기서 이제 처리하면 됨. 메시지 날아오면 여기서 다 관리함ㄴ 됌. 여기서 db에 저장하거나 그 앞에 했떤거 다 쓰셈.ㅇㅋ?
                        currentSMS = getIncomingMessage(o, bundle);
                        //String senderNo = currentSMS.getDisplayOriginatingAddress();
                        message = currentSMS.getDisplayMessageBody();
                        s_contents = parsCard(message);
                        if ("".equals(s_contents)) {
                            Log.d(TAG, "결제 관련이 아닌 경우, 종료.");
                            break;
                        }
                        Log.d(TAG, "지금 받은 메시지가 결제관련이다.");
                        s_price = parsPrice(message);
                        Date curDate = new Date(currentSMS.getTimestampMillis());
                        s_date = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(curDate);
                        sendData(context);
                        //Toast.makeText(context,  "message: " + message + "\n:date: " + receivedDate, Toast.LENGTH_LONG).show();
                    }
                    this.abortBroadcast();
                }
            }
        }
    }

    private void setData(Context context) {
        auto = context.getSharedPreferences("auto", Activity.MODE_PRIVATE);
        s_email = auto.getString("inputEmail", null);
        s_type = "expenses";
        s_pay = "카드";
    }

    private String parsCard(String message) {
        Pattern pattern = Pattern.compile("[가-힣A-Z]*카드[A-Z]?|[가-힣A-Z]* 카드[A-Z]?");
        Matcher matcher = pattern.matcher(message);
        String result = "";
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    private String parsPrice(String message) {
        Pattern pattern = Pattern.compile("([0-9]{1,3},)*([0-9]{1,3}원)");
        Matcher matcher = pattern.matcher(message);
        String result = "";
        if (matcher.find()) {
            result = matcher.group().replaceAll(",|원", "");
        }
        return result;
    }

    private void sendData(Context context) {
        jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "add");
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
        Log.d(TAG, "send Data json : " + send_data);

        //todo DataService를 통해 서버와 통신
        ServiceIntent = new Intent(context, DataService.class);
        ServiceIntent.putExtra("comp_type", "sms");
        ServiceIntent.putExtra("add_data", send_data);
        context.startService(ServiceIntent);
    }

    /*
        @Override
        public void onReceive(Context context, Intent intent) {
            //todo 브로드캐스트를 여기서 받는데요.
            Log.d(TAG, "onReceive");
            if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                Object[] messages = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessage = new SmsMessage[messages.length];

                for (int i = 0; i < messages.length; i++) {
                    smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
                }

                String message = smsMessage[0].getMessageBody().toString();
                Log.d(TAG, "SMS Message: " + message);
            }
        }
        */
    private SmsMessage getIncomingMessage(Object o, Bundle bundle) {
        SmsMessage currentSMS;
        String format = bundle.getString("format");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentSMS = SmsMessage.createFromPdu((byte[]) o, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) o);
        }
        return currentSMS;
    }
}
