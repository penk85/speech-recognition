package com.example.voicecommands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private static final String TAG = "Main Activity";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private static final String KEY_CODE = "code";
    private static final String KEY_COUNT = "count";
    private static final String KEY_RESET = "reset";
    private static final String KEY_BACK = "back";
    public Boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private Intent recognizerIntent;
    private ImageView micIV;
    private TextView textTV, commandListTV, listeningTV;
    private ArrayList<Command> recognizedWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_PERMISSION);

        textTV = (TextView) findViewById(R.id.live_text_tv);
        micIV = (ImageView) findViewById(R.id.mic_iv);
        listeningTV = (TextView) findViewById(R.id.current_command_tv);
        commandListTV = (TextView) findViewById(R.id.command_list_tv);

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.startListening(recognizerIntent);
            }
        });


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_RESULTS, true);
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, RecognizerIntent.FORMATTING_OPTIMIZE_LATENCY);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_RECORD_PERMISSION == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: has permission");
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        speechRecognizer.destroy();
        textTV.setText("");
        listeningTV.setText("");
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
        super.onDestroy();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        micIV.setImageResource(R.drawable.ic_mic_black_24dp_fill);
    }

    @Override
    public void onBeginningOfSpeech() {}
    @Override
    public void onRmsChanged(float v) {}
    @Override
    public void onBufferReceived(byte[] bytes) {}

    @Override
    public void onEndOfSpeech() {
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
    }

    @Override
    public void onError(int i) {
        Log.i(TAG, "onError: " + getResources().getString(getErrorText(i)));
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
        String errorMessage = getResources().getString(getErrorText(i));
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) return;
        String[] words = matches.get(0).split("\\s");

        for (String word : words) {
            word = getNumber(word);
            switch (word) {
                case KEY_CODE:
                    code();
                    break;
                case KEY_COUNT:
                    count();
                    break;
                case KEY_BACK:
                    back();
                    break;
                case KEY_RESET:
                    reset();
                    break;
                default:
                    int lastIndex = recognizedWords.size() - 1;
                    if (TextUtils.isDigitsOnly(word) && isListening) {

                            recognizedWords.get(lastIndex).setNumbers((recognizedWords.get(lastIndex).getNumbers() + word));

                    } else {
                        isListening = false;
                    }
            }

        }
        listeningTV.setText("");
        commandListTV.setText("");
        for (Command word : recognizedWords) {
            commandListTV.append(word.getFullCommand() + "\n");

        }
        textTV.setText("");
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) return;
        String[] latestResult = matches.get(matches.size() - 1).split("\\s");

        String currentWord = latestResult[latestResult.length - 1].toLowerCase().trim(); // Extract the last word
        if (currentWord.length() == 0) return;
        textTV.setText(currentWord);

        switch (currentWord) {
            case KEY_CODE:
                listeningTV.setText("Code");
                break;
            case KEY_COUNT:
                listeningTV.setText("Count");
                break;
            case KEY_BACK:
                listeningTV.setText("Back");
                break;
            case KEY_RESET:
               listeningTV.setText("Reset");
                break;
            default:
               break;
        }


    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(TAG, "onEvent: ");
    }

    private String getNumber(String word) {
        switch (word) {
            case "zero":
            case "oh":
                word = "'0";
            case "one":
            case "won":
                word = "1";
                break;
            case "two":
            case "too":
            case "to":
                word = "2";
                break;
            case "three":
                word = "3";
                break;
            case "four":
            case "for":
            case "fore":
                word = "4";
                break;
            case "five":
                word = "5";
                break;
            case "six":
                word = "6";
                break;
            case "seven":
                word = "7";
                break;
            case "eight":
            case "ate":
                word = "8";
                break;
            case "nine":
                word = "9";
                break;
            default:
                return word;
        }
        return word;
    }

    private void code() {
     /*   if (!currentCommand.getCode().isEmpty()) {
            recognizedWords.add(currentCommand);
            currentCommand.clear();
        }

        listeningTV.setText("listening");
        currentCommand.setCode(KEY_CODE);
        isListening = true;
      */
        isListening = true;
        recognizedWords.add(new Command(KEY_CODE, ""));
    }

    private void count() {
        /*
        if (!currentCommand.getCode().isEmpty()) {
            recognizedWords.add(currentCommand);
            currentCommand.clear();
        }
        isListening = true;
        listeningTV.setText("listening");
        currentCommand.setCode(KEY_COUNT);
        isListening = true;
         */
        isListening = true;
        recognizedWords.add(new Command(KEY_COUNT, ""));
    }

    private void reset() {
        /*
        if (!currentCommand.getCode().isEmpty()) {
            currentCommand.clear();
        }
        recognizedWords.clear();
        textTV.setText("");
        isListening = false;
        listeningTV.setText("");
        commandListTV.setText("");
        recognizedWords.add(new Command(KEY_RESET, ""));
        for (Command word : recognizedWords) {
            commandListTV.append(word.getFullCommand());
        }
         */
        isListening = false;
        recognizedWords.get(recognizedWords.size() - 1).setCode("");
    }

    private void back() {
        /*
        if (!currentCommand.getCode().isEmpty()) {
            recognizedWords.add(currentCommand);
            currentCommand.clear();
        }
        isListening = false;
        recognizedWords.get(recognizedWords.size() - 1).clear();
        commandListTV.setText("");
        recognizedWords.add(new Command(KEY_BACK, ""));
        for (Command word : recognizedWords) {
            commandListTV.append(word.getFullCommand());
        }
         */
        isListening = false;
        recognizedWords.remove(recognizedWords.size() - 1);
    }

    public static int getErrorText(int errorCode) {
        int message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = R.string.error_recording;
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = R.string.client_side;
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = R.string.insufficient_permissions;
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = R.string.network_error;
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = R.string.network_timeout;
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = R.string.no_match;
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = R.string.recognition_service_busy;
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = R.string.server_error;
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = R.string.speech_timeout;
                break;
            default:
                message = R.string.default_error;
                break;
        }
        return message;
    }
}
