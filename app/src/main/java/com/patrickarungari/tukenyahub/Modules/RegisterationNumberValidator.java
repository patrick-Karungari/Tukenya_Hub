package com.patrickarungari.tukenyahub.Modules;

import android.graphics.Color;

public enum RegisterationNumberValidator {
    INCORRECT("Registeration Number is incorrect", Color.parseColor("#FF0000"), 0),

    OK("OK", Color.parseColor("#FF0000"), 1);

    public String msg;
    public int color;
    public int code;

    RegisterationNumberValidator(String msg, int color, int code) {
        this.msg = msg;
        this.code = code;
        this.color = color;
    }

    public static RegisterationNumberValidator calculate(String uniNum) {
        int score = 0;
        int count = 0;
        int digitCount = 0;
        for (int i = 0; i < uniNum.length(); i++) {
            char c = uniNum.charAt(i);
            if (uniNum.charAt(i) == '\u002F') {
                count++;
            }
            if (Character.isDigit(c)) {
                digitCount++;
            }
        }
        int length = uniNum.length();
        int MIN_LENGTH = 15;
        int MAX_LENGTH = 16;
        if (length >= MIN_LENGTH && length <= MAX_LENGTH) {
            if ((count == 2) && (digitCount == 9)) {
                score = 1;
            }
        } else {
            score = 2;
        }
        // return enum following the score
        switch (score) {
            case 2:
                return INCORRECT;
            case 1:
                return OK;
            default:
        }
        return OK;
    }
}
