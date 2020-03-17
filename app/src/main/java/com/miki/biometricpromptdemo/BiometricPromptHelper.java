package com.miki.biometricpromptdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/**
 * @author：cai_gp on 2020/3/17
 */
@TargetApi(Build.VERSION_CODES.P)
public class BiometricPromptHelper extends BiometricPrompt.AuthenticationCallback {

    public static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 10;

    private Activity activity;
    private BiometricPrompt biometricPrompt;
    private CancellationSignal cancellationSignal;
    private FingerprintManagerCompat fingerprintManager;
    private KeyguardManager keyguardManager;
    private FingerPrintCallBack callBack;

    /**
     * 是否开启过指纹功能
     */
    public boolean isStartFinger;

    private int count;

    public BiometricPromptHelper(final Activity activity, FingerPrintCallBack callBack) {
        fingerprintManager = FingerprintManagerCompat.from(activity);
        keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);

        if (!fingerprintManager.isHardwareDetected()) {
            // 没有指纹识别模块
            if (callBack != null) {
                callBack.doNotSupportFinger();
            }
            return;
        }
        /*if(!keyguardManager.isKeyguardSecure()) {
            // 没有开启锁屏密码
            LogUtil.logd("没有开启锁屏密码");
        }*/
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // 没有录入指纹
            if (callBack != null) {
                callBack.FingerClosed();
            }
        }

        isStartFinger = false;
        this.activity = activity;
        this.callBack = callBack;
        biometricPrompt = new BiometricPrompt
                .Builder(activity)
                .setTitle("Verification")
                .setDescription("Verify fingerprint to continue")
                .setSubtitle("subtitle")
                .setNegativeButton("Cancel", this.activity.getMainExecutor(), (dialogInterface, i)
                        -> this.activity.finish())
                .build();
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> LogUtil.logd("cancellationSignal"));
    }

    public void startBiometricPrompt() {
        isStartFinger = true;
        count = 5;
        biometricPrompt.authenticate(cancellationSignal, activity.getMainExecutor(),
                this);
    }

    public void stopBiometricPrompt() {
        if(isStartFinger) {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (errorCode == 5) {
            if (callBack != null) {
                callBack.FingerClosed();
            }
            return;
        }
        if (errorCode == 7) {
            if (callBack != null) {
                callBack.onAuthenticationError();
            }
            return;
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if(callBack != null) {
            callBack.onAuthenticationSucceeded();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        count--;
        if(count > 0) {
            if(callBack != null) {
                callBack.onAuthenticationFailed(count);
            }
        }
    }

    /**
     * 多次调用指纹识别失败后,调用此方法
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void jumpToGesturePassCheck(Activity activity) {
        KeyguardManager keyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        Intent intent =
                keyguardManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
    }

    interface FingerPrintCallBack {
        /**
         * 识别成功
         */
        void onAuthenticationSucceeded();
        /**
         * 识别失败
         *
         * @param count 还可以尝试的次数
         * @param count
         */
        void onAuthenticationFailed(int count);
        /**
         * 失败次数过多
         */
        void onAuthenticationError();
        /**
         * 未开启指纹功能
         */
        void FingerClosed();
        /**
         * 不支持指纹功能
         */
        void doNotSupportFinger();
    }
}
