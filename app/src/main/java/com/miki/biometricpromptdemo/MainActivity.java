package com.miki.biometricpromptdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements BiometricPromptHelper.FingerPrintCallBack{

    private BiometricPromptHelper biometricPromptHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        biometricPromptHelper = new BiometricPromptHelper(this, this);
        biometricPromptHelper.startBiometricPrompt();
    }

    @Override
    protected void onDestroy() {
        if(biometricPromptHelper != null) {
            biometricPromptHelper.stopBiometricPrompt();
        }
        super.onDestroy();
    }

    @Override
    public void onAuthenticationSucceeded() {
        LogUtil.logd("onAuthenticationSucceeded");
    }

    @Override
    public void onAuthenticationFailed(int count) {
        LogUtil.logd("onAuthenticationFailed count=" + count);
    }

    @Override
    public void onAuthenticationError() {
        LogUtil.logd("onAuthenticationError");
        BiometricPromptHelper.jumpToGesturePassCheck(this);
    }

    @Override
    public void FingerClosed() {
        LogUtil.logd("FingerClosed");
    }

    @Override
    public void doNotSupportFinger() {
        LogUtil.logd("doNotSupportFinger");
    }
}
