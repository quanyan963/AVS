package com.txtled.avs.cognito;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.SignerFactory;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.core.signin.SignInManager;
import com.amazonaws.mobile.auth.core.signin.SignInProvider;
import com.amazonaws.mobile.auth.core.signin.SignInProviderResultHandler;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.cognito.mvp.AuthContract;
import com.txtled.avs.cognito.mvp.AuthPresenter;
import com.txtled.avs.start.StartActivity;

/**
 * Created by Mr.Quan on 2019/12/30.
 */
public class AuthenticatorActivity extends MvpBaseActivity<AuthPresenter> implements AuthContract.View,AmazonSignInProvider {
    private AmazonSignInProvider amazonSignInProvider;
    @Override
    public void init() {
// Create IdentityManager and set it as the default instance.
        IdentityManager idm = new IdentityManager(getApplicationContext(),
                new AWSConfiguration(getApplicationContext()));
        IdentityManager.setDefaultIdentityManager(idm);
        // Use IdentityManager to retrieve the CognitoCachingCredentialsProvider
        // object.
        Button amazonLogin = new Button(this);
        amazonLogin.setBackgroundResource(R.mipmap.btnlwa_gold_loginwithamazon);
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(
                        AuthenticatorActivity.this,
                        SignInUI.class);
                signin.login(
                        AuthenticatorActivity.this,
                        StartActivity.class).execute();
            }
        }).execute();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_authenticator;
    }

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public boolean isRequestCodeOurs(int requestCode) {
        return false;
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public View.OnClickListener initializeSignInButton(Activity signInActivity, View buttonView, SignInProviderResultHandler resultsHandler) {
        return null;
    }

    @Override
    public void initialize(Context context, AWSConfiguration configuration) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getCognitoLoginKey() {
        return null;
    }

    @Override
    public boolean refreshUserSignInState() {
        return false;
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public String refreshToken() {
        return null;
    }

    @Override
    public void signOut() {

    }
}
