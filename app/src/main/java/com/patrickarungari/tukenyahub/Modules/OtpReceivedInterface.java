package com.patrickarungari.tukenyahub.Modules;

public interface OtpReceivedInterface {
    void onOtpReceived(String otp);
    void onOtpTimeout();
}