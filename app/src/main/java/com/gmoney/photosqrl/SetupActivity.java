package com.gmoney.photosqrl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

// Shared preferences can encrypted.  Should add later

public class SetupActivity extends AppCompatActivity {
    EditText editTextIp1;
    EditText editTextIp2;
    EditText editTextIp3;
    EditText editTextIp4;
    EditText editTextPort;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getViews();
        setupClickListener();
    }

    private void getViews() {
        editTextIp1 = findViewById(R.id.ip1);
        editTextIp2 = findViewById(R.id.ip2);
        editTextIp3 = findViewById(R.id.ip3);
        editTextIp4 = findViewById(R.id.ip4);
        editTextPort = findViewById(R.id.port_number_entry);
        saveButton = findViewById(R.id.save_button);
    }

    private String getPortNumber() {
        return editTextPort.getText().toString();
    }

    private String getIp() {
        String ip1 = editTextIp1.getText().toString();
        String ip2 = editTextIp2.getText().toString();
        String ip3 = editTextIp3.getText().toString();
        String ip4 = editTextIp4.getText().toString();
        return formatIp(ip1, ip2, ip3,ip4);
    }

    private String formatIp(String ip1, String ip2, String ip3, String ip4) {
        return ip1 + "." + ip2 + "." + ip3 + "." + ip4;
    }

    private void saveSettings() {
        String ip = getIp();
        String port = getPortNumber();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_ip_address_key), ip);
        editor.putString(getString(R.string.saved_port_number_key), port);
        editor.apply();
    }

    public void setupClickListener() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                backToMainActivity();
            }
        });
    }

    private void backToMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        SetupActivity.this.startActivity(intent);
    }
}