package com.example.voicecommands;

public class Command {
    private String mCode;
    private String mNumbers;

    public Command(String code, String numbers) {
        mCode = code;
        mNumbers = numbers;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getNumbers() {
        return mNumbers;
    }

    public void setNumbers(String numbers) {
        mNumbers = numbers;
    }

    public String getFullCommand() { return mCode + " " + mNumbers; }

    public void clear() {
        mNumbers = "";
        mCode = "";
    }
}
