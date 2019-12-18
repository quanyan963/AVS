package com.txtled.avs.di.module;

import android.app.Activity;

import androidx.fragment.app.Fragment;


import com.txtled.avs.di.scope.FragmentScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by KomoriWu
 * on 2017/9/15.
 */
@Module
public class FragmentModule {
    private Fragment fragment;

    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @FragmentScope
    Activity provideActivity() {
        return fragment.getActivity();
    }

}
