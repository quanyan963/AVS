package com.txtled.avs.application;

import android.app.Activity;
import android.app.Application;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.txtled.avs.di.component.AppComponent;
import com.txtled.avs.di.component.DaggerAppComponent;
import com.txtled.avs.di.module.AppModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class MyApplication extends Application {

    private static MyApplication sInstance;
    private List<Activity> mActivityList;
    private static AppComponent mAppComponent;
    private static final Regions MY_REGION = Regions.US_EAST_1;
    private String identityPoolId;
    private static CognitoCachingCredentialsProvider credentialsProvider;


    @Override
    public void onCreate() {
        super.onCreate();
        if (sInstance == null) {
            sInstance = this;
        }
        mActivityList = new ArrayList<>();

        identityPoolId = "us-east-1:a7fd6e3b-f444-41e7-ae9b-38f8ccef53cb";
        // 初始化 Amazon Cognito 凭证提供程序
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                identityPoolId, // 身份池 ID
                MY_REGION // 区域
        );
    }

    public static CognitoCachingCredentialsProvider getCredentialsProvider(){
        return credentialsProvider;
    }

//    private void getIdentity() {
//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//
//                    // 先只获取身份ID ，验证 Cognito 已正常启用。
//                    String identityId = credentialsProvider.getIdentityId();
//                    Utils.Logger("Cognito", "my ID is ", identityId);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    public static MyApplication getInstance() {
        if (sInstance == null) {
            return new MyApplication();
        } else {
            return sInstance;
        }
    }

    public static AppComponent getAppComponent() {
        if (mAppComponent == null) {
            mAppComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(sInstance))
                    .build();
        }
        return mAppComponent;
    }

    public void addActivity(Activity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity);
        }
    }


    public void removeAllActivity() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
    }
}
