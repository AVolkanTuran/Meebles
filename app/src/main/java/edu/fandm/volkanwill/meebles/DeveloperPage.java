package edu.fandm.volkanwill.meebles;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;
import java.time.LocalTime;

public class DeveloperPage extends AppCompatActivity {

    private NfcAdapter adapter;
    private int savedMeebleCount;
    private char savedCityType;
    private char savedEnvType;
    private int savedButtonId = R.id.read_button;
    EditText meebleET;
    EditText cityET;
    EditText envET;
    MaterialButtonToggleGroup mbtg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        adapter = NfcAdapter.getDefaultAdapter(this);

        meebleET = findViewById(R.id.meeble_et);
        cityET = findViewById(R.id.city_type_et);
        envET = findViewById(R.id.environment_type_et);
        mbtg = findViewById(R.id.read_write_toggle);

        // Save values whenever user changes them
        meebleET.addTextChangedListener(new SimpleTextWatcher() {
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (!s.toString().isEmpty())
                    savedMeebleCount = Integer.parseInt(s.toString());
            }
        });
        cityET.addTextChangedListener(new SimpleTextWatcher() {
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (!s.toString().isEmpty())
                    savedCityType = s.charAt(0);
            }
        });
        envET.addTextChangedListener(new SimpleTextWatcher() {
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (!s.toString().isEmpty())
                    savedEnvType = s.charAt(0);
            }
        });
        mbtg.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) savedButtonId = checkedId;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle options = new Bundle();
        MYNFCCallBackClass myCallback = new MYNFCCallBackClass();
        adapter.enableReaderMode(this, myCallback, NfcAdapter.FLAG_READER_NFC_A, options);
    }

    @Override
    protected void onPause(){
        super.onPause();
        adapter.disableReaderMode(this);
    }

    private class MYNFCCallBackClass implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag) {
            if (savedButtonId == R.id.write_button) {
                EnvironmentData data = new EnvironmentData(savedMeebleCount, savedCityType, savedEnvType, LocalTime.now());
                try{
                    HomePage.writeToTag(data, tag);}
                catch (IOException e) {
                    runOnUiThread(() -> {Toast.makeText(getApplicationContext(), "Could not write to NFC. Try again.", Toast.LENGTH_SHORT).show();});
                }

            } else if (savedButtonId == R.id.read_button) {
                try{
                    EnvironmentData readData = HomePage.readFromTag(tag);
                    if (readData != null) {
                        runOnUiThread(() -> {
                            meebleET.setText(String.valueOf(readData.getMeebleCount()));
                            cityET.setText(String.valueOf(readData.getCityType()));
                            envET.setText(String.valueOf(readData.getEnvironmentType()));
                        });
                    }
                } catch(IOException e){
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Could not read tag. Try again.", Toast.LENGTH_SHORT).show());
                }
            }
        }
    }

    public abstract class SimpleTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}
    }
}