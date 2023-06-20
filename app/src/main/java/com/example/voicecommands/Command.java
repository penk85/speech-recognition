package com.example.voicecommands;

public class Command {
    private String mCodeWord;
    private String mCodeNumbers;

    public Command(String codeWord, String codeNumbers) {
        mCodeWord = codeWord;
        mCodeNumbers = codeNumbers;
    }

    public String getCodeWord() {
        return mCodeWord;
    }

    public void setCodeWord(String codeWord) {
        mCodeWord = codeWord;
    }

    public String getCodeNumbers() {
        return mCodeNumbers;
    }

    public void setCodeNumbers(String codeNumbers) {
        mCodeNumbers = codeNumbers;
    }

    public String getFullCommand() { return mCodeWord + " " + mCodeNumbers; }
}
