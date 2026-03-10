package edu.fandm.volkanwill.meebles;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

public class DeveloperPage extends AppCompatActivity {

    private NfcAdapter adapter;

    EditText meebleET;
    EditText cityET;
    EditText envET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_page);

        adapter = NfcAdapter.getDefaultAdapter(this);


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
        public void onTagDiscovered(Tag tag){
            Log.d("", "Tag Discovered: " + tag.toString());
            meebleET = findViewById(R.id.meeble_et);
            cityET = findViewById(R.id.city_type_et);
            envET = findViewById(R.id.environment_type_et);
            runOnUiThread(() -> {
                try {
                    MaterialButtonToggleGroup mbtg = findViewById(R.id.read_write_toggle);
                    int selectedId = mbtg.getCheckedButtonId();

                    if (selectedId == R.id.write_button) {
                        int meebleCount = Integer.parseInt(meebleET.getText().toString());
                        char cityType = cityET.getText().toString().charAt(0);
                        char envType  = envET.getText().toString().charAt(0);
                        EnvironmentData data = new EnvironmentData(meebleCount, cityType, envType, LocalTime.now());

                        new Thread(() -> HomePage.writeToTag(data, tag)).start();

                    } else if (selectedId == R.id.read_button) {
                        new Thread(() -> {
                            EnvironmentData readData = HomePage.readFromTag(tag);
                            if (readData != null) {
                                runOnUiThread(() -> {
                                    meebleET.setText(String.valueOf(readData.getMeebleCount()));
                                    cityET.setText(String.valueOf(readData.getCityType()));
                                    envET.setText(String.valueOf(readData.getEnvironmentType()));
                                });
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}