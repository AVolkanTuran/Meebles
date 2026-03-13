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

    private EnvironmentData envData;

    private int currentMeebles;

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

        currentMeebles = HomePage.loadMeebles(getApplicationContext());
        envTypeView = (TextView) findViewById(R.id.environment_type);
        cityTypeView =  (TextView) findViewById(R.id.city_type);
        meebleCountView  = (TextView) findViewById(R.id.meeble_count);

        envData = (EnvironmentData) getIntent().getSerializableExtra("environment_data");

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
            boolean success = false;
            Log.d("", "Tag Discovered: " + tag.toString());
            EditText et = (EditText) findViewById(R.id.nfc_number_et);
            int value = Integer.parseInt(et.getText().toString());
            EnvironmentData data = new EnvironmentData(envData.getMeebleCount(), envData.getCityType(), envData.getEnvironmentType(), envData.getTime());

            MaterialButtonToggleGroup mbtg = (MaterialButtonToggleGroup) findViewById(R.id.withdraw_deposit_toggle);
            int selectedId = mbtg.getCheckedButtonId();
            if(selectedId == R.id.withdraw){
                if((data.getMeebleCount() - value) >= 2 && currentMeebles + value > 0){
                    data.setMeebleCount(data.getMeebleCount() - value);
                    data.setTime(LocalTime.now());
                    success = true;
                }
                else if ((data.getMeebleCount() - value) < 2) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "There needs to be at least 2 Meebles in a city!", Toast.LENGTH_LONG).show());
                }
                else if(data.getMeebleCount() + value > 4000000){
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You reached the maximum number of Meebles in a city.", Toast.LENGTH_LONG).show());
                }
            } else if(selectedId == R.id.deposit){
                if(currentMeebles >= value && data.getMeebleCount() + value <= 4000000){
                    data.setMeebleCount(data.getMeebleCount()+value);
                    data.setTime(LocalTime.now());
                    success = true;
                }
                else if(currentMeebles < value) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You don't have enough Meebles.", Toast.LENGTH_LONG).show());
                }
                else if(currentMeebles + value > 0){
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You reached the maximum number of Meebles on your device.", Toast.LENGTH_LONG).show());
                }
            }

            if(!success){
                return;
            }

            try{
                HomePage.writeToTag(data, tag);
                envData = data;
                runOnUiThread(() -> updateUI(data));
                if(selectedId == R.id.withdraw){
                    HomePage.saveMeebles(getApplicationContext(), currentMeebles+value);
                }
                else if(selectedId == R.id.deposit){
                    HomePage.saveMeebles(getApplicationContext(), currentMeebles-value);
                }
            } catch (IOException e) {
                runOnUiThread(() -> {Toast.makeText(getApplicationContext(), "Could not write to NFC. Try again.", Toast.LENGTH_SHORT).show();});
            }
        }
    }
}