package com.example.voicecommands;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    private Intent recognizerIntent;
    private ImageView micIV;
    private TextView liveTextTV, commandListTV, currentCommandTV;
    private String lang = "en-US";
    private ArrayList<Command> recognizedCommands = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request audio recording permission
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_PERMISSION);

        // Initialize views
        liveTextTV = (TextView) findViewById(R.id.live_text_tv);
        micIV = (ImageView) findViewById(R.id.mic_iv);
        currentCommandTV = (TextView) findViewById(R.id.current_command_tv);
        commandListTV = (TextView) findViewById(R.id.command_list_tv);

        // Set click listener for mic button
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.startListening(recognizerIntent);
            }
        });

        RadioGroup langSelect = (RadioGroup) findViewById(R.id.lang_select);
        RadioButton checkedRadioButton = (RadioButton) langSelect.findViewById(langSelect.getCheckedRadioButtonId());

        // Set listener for language selection radio group
        langSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(R.id.lang_eng);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    lang = "en-US";
                } else {
                    lang = "de-DE";
                }
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
            }
        });

        // Create SpeechRecognizer and set recognition listener
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        // Create intent for speech recognition
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
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
                Toast.makeText(MainActivity.this, getResources().getText(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        speechRecognizer.destroy();
        liveTextTV.setText("");
        currentCommandTV.setText("");
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
        super.onDestroy();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        micIV.setImageResource(R.drawable.ic_mic_black_24dp_fill);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float v) {
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
    }

    @Override
    public void onEndOfSpeech() {
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
    }

    @Override
    public void onError(int i) {
        // Log the error and display a toast with the relevant error message
        Log.i(TAG, "onError: " + getResources().getString(getErrorText(i)));
        micIV.setImageResource(R.drawable.ic_mic_black_24dp);
        String errorMessage = getResources().getString(getErrorText(i));
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResults(Bundle bundle) {
        // Get the recognized speech results
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // Ensure results are not empty
        if (matches == null || matches.isEmpty()) return;
        // Split results into individual words
        String[] words = matches.get(0).split("\\s");

        for (String word : words) {
            // Handle synonyms for English (US) language
            if (lang.equals("en-US")) {
                word = handleSynonyms(word);
            }
            // Check for recognized code words or in listening mode, a number
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
                    int lastIndex = recognizedCommands.size() - 1;
                    if (TextUtils.isDigitsOnly(word) && isListening) {
                        recognizedCommands.get(lastIndex).setCodeNumbers((recognizedCommands.get(lastIndex).getCodeNumbers() + word));
                    } else {
                        // disable listening mode if the result is neither code word nor number
                        isListening = false;
                    }
            }

        }
        // Clear the display of the existing live text
        currentCommandTV.setText("");
        commandListTV.setText("");
        liveTextTV.setText("");

        // Display all the full recognized commands in the command list text view
        for (Command command : recognizedCommands) {
            commandListTV.append(command.getFullCommand() + "\n");

        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        // Use partialResults for live display of recognized speech
        // Get the partial speech results
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // Ensure results are not empty
        if (matches == null || matches.isEmpty()) return;
        // Split the latest partial result into individual words
        String[] latestResult = matches.get(matches.size() - 1).split("\\s");
        // Extract the last word from the latest result
        String currentWord = latestResult[latestResult.length - 1].toLowerCase().trim(); // Extract the last word
        if (currentWord.length() == 0) return;

        // Display the current word in the live text view
        liveTextTV.setText(currentWord);

        // Check the current word against predefined keywords and display it in the live command text
        switch (currentWord) {
            case KEY_CODE:
                currentCommandTV.setText(KEY_CODE);
                break;
            case KEY_COUNT:
                currentCommandTV.setText(KEY_COUNT);
                break;
            case KEY_BACK:
                currentCommandTV.setText(KEY_BACK);
                break;
            case KEY_RESET:
                currentCommandTV.setText(KEY_RESET);
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
    }

    // Handle synonyms for English (US) language
    private String handleSynonyms(String word) {
        switch (word) {
            case "zero":
            case "oh":
                word = "'0";
                break;
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

    // Method to get the error message based on error code
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

    private void code() {
        // Method for when code is called. Ready to listen for number input.
        isListening = true;
        recognizedCommands.add(new Command(KEY_CODE, ""));
    }

    private void count() {
        // Method for when count is called. Ready to listen for number input.
        isListening = true;
        recognizedCommands.add(new Command(KEY_COUNT, ""));
    }

    private void reset() {
        // Method for when reset is called.
        // No longer ready to listen for number input and removes last code word
        isListening = false;
        recognizedCommands.get(recognizedCommands.size() - 1).setCodeWord("");
    }

    private void back() {
        // Method for when back is called.
        // No longer listening for number input and removes last full command.
        isListening = false;
        recognizedCommands.remove(recognizedCommands.size() - 1);
    }
}
