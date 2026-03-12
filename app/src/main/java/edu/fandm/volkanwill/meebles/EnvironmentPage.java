package edu.fandm.volkanwill.meebles;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;
import java.time.LocalTime;

public class EnvironmentPage extends AppCompatActivity {

    public static NfcAdapter adapter;

    private TextView envTypeView;
    private TextView cityTypeView;
    private TextView meebleCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_environment_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        envTypeView = (TextView) findViewById(R.id.environment_type);
        cityTypeView =  (TextView) findViewById(R.id.city_type);
        meebleCountView  = (TextView) findViewById(R.id.meeble_count);

        EnvironmentData envData = (EnvironmentData) getIntent().getSerializableExtra("environment_data");

        adapter = NfcAdapter.getDefaultAdapter(this);

        ImageButton go_back = (ImageButton) findViewById(R.id.environment_go_back_button);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), HomePage.class);
                startActivity(i);
            }
        });

        ImageButton info = (ImageButton) findViewById(R.id.info_open_button);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InfoPage.class);
                startActivity(i);
            }
        });

        if(envData != null) {
            updateUI(envData);
        }

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

    private void updateUI(EnvironmentData data){
        String envTypeText = "Environment: " + MeebleConstants.envTypeToString(data.getEnvironmentType());
        envTypeView.setText(envTypeText);
        String cityTypeText = "Type: " + MeebleConstants.cityTypeToString(data.getCityType());
        cityTypeView.setText(cityTypeText);
        String meebleCountText = data.getMeebleCount() + " Meeble(s)";
        meebleCountView.setText(meebleCountText);
    }

    private class MYNFCCallBackClass implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag){
            Log.d("", "Tag Discovered: " + tag.toString());
            EditText et = (EditText) findViewById(R.id.nfc_number_et);
            int value = Integer.parseInt(et.getText().toString());
            EnvironmentData data;
            try{
                data = HomePage.readFromTag(tag);
            } catch(IOException e){
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Could not read tag. Try again.", Toast.LENGTH_SHORT).show());
                return;
            }

            MaterialButtonToggleGroup mbtg = (MaterialButtonToggleGroup) findViewById(R.id.withdraw_deposit_toggle);
            int selectedId = mbtg.getCheckedButtonId();
            if(selectedId == R.id.withdraw){
                if(value < data.getMeebleCount()) {
                    data.setMeebleCount(data.getMeebleCount() - value);
                    data.setTime(LocalTime.now());
                }
            } else if(selectedId == R.id.deposit){
                data.setMeebleCount(data.getMeebleCount()+value);
                data.setTime(LocalTime.now());
            }

            try{
                HomePage.writeToTag(data, tag);
                runOnUiThread(() -> {
                    if(data != null) {
                        updateUI(data);
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {Toast.makeText(getApplicationContext(), "Could not write to NFC. Try again.", Toast.LENGTH_SHORT).show();});
            }
        }
    }
}