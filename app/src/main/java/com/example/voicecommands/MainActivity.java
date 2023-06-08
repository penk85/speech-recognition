package com.example.voicecommands;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public Boolean isWaiting = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView stateTextView = (TextView) findViewById(R.id.state_text_view);

        stateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isWaiting = !isWaiting;
                stateTextView.setText(isWaiting ? R.string.idle : R.string.listening);
            }
        });
    }
}