package com.txtled.avs.di.module;

import android.app.Activity;
import android.content.Intent;


import com.txtled.avs.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by KomoriWu
 *  on 2017/9/15.
 */
@Module
public class ActivityModule {
    private Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    Activity provideActivity() {
        return activity;
    }


    @Provides
    Intent provideIntent() {
        return new Intent();
    }
}
