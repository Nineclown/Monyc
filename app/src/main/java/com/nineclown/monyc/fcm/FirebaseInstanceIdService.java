package com.nineclown.monyc.fcm;

import com.google.firebase.iid.FirebaseInstanceId;

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    //앱이 처음 깔리면 Token을 생성해서 뿌려줌.
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("ID is this !!!! : " + token);
    }
}
