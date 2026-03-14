package edu.fandm.volkanwill.meebles;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.IOException;

public class EnvironmentPage extends AppCompatActivity {

    public static NfcAdapter adapter;

    private volatile boolean isProcessing = false;

    private TextView envTypeView;
    private TextView cityTypeView;
    private TextView meebleCountView;

    private EnvironmentData envData;

    private long openedAt;
    private AlertDialog writingDialog;

    private boolean firstCity;
    private SharedPreferences prefs;
    private int currentMeebles;

    FrameLayout meebleContainer;
    private int meeblesVolcano;
    private int meeblesForest;
    private int meeblesDesert;
    private int meeblesTundra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openedAt = System.currentTimeMillis();
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

        if(getIntent().getBooleanExtra("new_env", false)){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("New Meebles moved in and built a new village!")
                    .create();
            dialog.show();

            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                // re-enable scanning after dialog closes
                Bundle options = new Bundle();
                MYNFCCallBackClass myCallback = new MYNFCCallBackClass();
                adapter.enableReaderMode(this, myCallback, NfcAdapter.FLAG_READER_NFC_A, options);
            }, 3000);
        }

        meebleContainer = findViewById(R.id.meebles_container);

        currentMeebles = HomePage.loadMeebles(getApplicationContext());
        int[] counts = HomePage.loadMeeblesData(getApplicationContext());
        meeblesVolcano = counts[0];
        meeblesForest  = counts[1];
        meeblesDesert  = counts[2];
        meeblesTundra  = counts[3];
        meebleCountView  = (TextView) findViewById(R.id.meeble_count);

        envData = (EnvironmentData) getIntent().getSerializableExtra("environment_data");

        prefs = getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        firstCity = prefs.getBoolean("first_city", true);

        if(envData.getCityType() == MeebleConstants.CITY && firstCity){
            showFirstCityDialog();
            prefs.edit().putBoolean("first_city", false).apply();
        }


        adapter = NfcAdapter.getDefaultAdapter(this);

        ImageButton go_back = (ImageButton) findViewById(R.id.environment_go_back_button);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isProcessing){
                    Intent i = new Intent(getApplicationContext(), HomePage.class);
                    startActivity(i);
                }
            }
        });

        ImageButton info = (ImageButton) findViewById(R.id.info_open_button);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isProcessing){
                    Intent i = new Intent(getApplicationContext(), InfoPage.class);
                    // Send environment type
                    i.putExtra("biome", (int) envData.getEnvironmentType());

                    // Send city level
                    i.putExtra("level", (int) envData.getCityType());

                    // Calculate correct growth rate
                    double rate;
                    switch(envData.getEnvironmentType()){
                        case MeebleConstants.ENV_VOLCANO:
                            rate = 0.5;
                            break;
                        case MeebleConstants.ENV_FOREST:
                            rate = 2.0;
                            break;
                        case MeebleConstants.ENV_DESERT:
                            rate = 1.0;
                            break;
                        case MeebleConstants.ENV_TUNDRA:
                            rate = 1.5;
                            break;
                        default:
                            rate = 1.0;
                    }

                    i.putExtra("growthRate", rate);

                    startActivity(i);
                }
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

    private class MYNFCCallBackClass implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag){
            if (System.currentTimeMillis() - openedAt < 1500) return;
            if (isProcessing) return;
            isProcessing = true;

            if(envData.getCityType() == MeebleConstants.CITY && firstCity){
                runOnUiThread(() -> showFirstCityDialog());
                prefs.edit().putBoolean("first_city", false).apply();
            }

            EnvironmentData updatedData = HomePage.growMeebles(envData);

            boolean success = false;
            Log.d("", "Tag Discovered: " + tag.toString());
            EditText et = (EditText) findViewById(R.id.nfc_number_et);
            String input = et.getText().toString().trim();
            if(input.isEmpty()) {
                showWritingDialog();
                try{
                    HomePage.writeToTag(updatedData, tag);
                    envData = updatedData;
                    runOnUiThread(() -> updateUI(envData));
                } catch(IOException e){
                    runOnUiThread(() -> {Toast.makeText(getApplicationContext(), "Could not write to NFC. Try again.", Toast.LENGTH_SHORT).show();});
                } finally {
                    dismissWritingDialog();
                    isProcessing = false;
                }
                return;
            }

            int value = Integer.parseInt(input);
            EnvironmentData data = new EnvironmentData(updatedData.getMeebleCount(), updatedData.getCityType(), updatedData.getEnvironmentType(), envData.getTime());

            MaterialButtonToggleGroup mbtg = (MaterialButtonToggleGroup) findViewById(R.id.withdraw_deposit_toggle);
            int selectedId = mbtg.getCheckedButtonId();
            if(selectedId == R.id.withdraw){
                if((data.getMeebleCount() - value) >= 2 && currentMeebles + value > 0){
                    data.setMeebleCount(data.getMeebleCount() - value);
                    data.setTime(System.currentTimeMillis());
                    success = true;

                    // Increment the environment-specific meebles on the device
                    switch(envData.getEnvironmentType()){
                        case MeebleConstants.ENV_VOLCANO: meeblesVolcano += value; break;
                        case MeebleConstants.ENV_FOREST:  meeblesForest  += value; break;
                        case MeebleConstants.ENV_DESERT:  meeblesDesert  += value; break;
                        case MeebleConstants.ENV_TUNDRA:  meeblesTundra  += value; break;
                    }
                }
                else if ((data.getMeebleCount() - value) < 2) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "There needs to be at least 2 Meebles in a city!", Toast.LENGTH_LONG).show());
                }
                else if((long) currentMeebles + value > Integer.MAX_VALUE){
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You reached the maximum number of Meebles on your device.", Toast.LENGTH_LONG).show());
                }

            } else if(selectedId == R.id.deposit){
                if(currentMeebles >= value && data.getMeebleCount() + value <= 4000000){
                    data.setMeebleCount(data.getMeebleCount()+value);
                    data.setTime(System.currentTimeMillis());
                    success = true;

                    // Split the dropped meebles proportionally
                    float total = meeblesVolcano + meeblesForest + meeblesDesert + meeblesTundra;
                    if(total == 0) total = 1; // avoid divide by zero

                    meeblesVolcano -= Math.round(value * meeblesVolcano / total);
                    meeblesForest  -= Math.round(value * meeblesForest  / total);
                    meeblesDesert  -= Math.round(value * meeblesDesert  / total);
                    meeblesTundra  -= Math.round(value * meeblesTundra  / total);

                    // make sure none go negative
                    meeblesVolcano = Math.max(meeblesVolcano, 0);
                    meeblesForest  = Math.max(meeblesForest, 0);
                    meeblesDesert  = Math.max(meeblesDesert, 0);
                    meeblesTundra  = Math.max(meeblesTundra, 0);
                }
                else if(currentMeebles < value) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You don't have enough Meebles.", Toast.LENGTH_LONG).show());
                }
                else if((long) data.getMeebleCount() + value > 4000000){
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You reached the maximum number of Meebles in a city.", Toast.LENGTH_LONG).show());
                }
            }

            showWritingDialog();
            try{
                if(success){
                    HomePage.writeToTag(data, tag);
                    envData = data;
                    runOnUiThread(() -> updateUI(data));
                    if(selectedId == R.id.withdraw){
                        // Save total and per-environment counts
                        HomePage.saveMeeblesData(getApplicationContext(),
                                meeblesVolcano,
                                meeblesForest,
                                meeblesDesert,
                                meeblesTundra);
                        // Update currentMeebles for display
                        currentMeebles = meeblesVolcano + meeblesForest + meeblesDesert + meeblesTundra;
                        HomePage.saveMeebles(getApplicationContext(), currentMeebles);
                    }
                    else if(selectedId == R.id.deposit){
                        HomePage.saveMeeblesData(getApplicationContext(),
                                meeblesVolcano,
                                meeblesForest,
                                meeblesDesert,
                                meeblesTundra);

                        // Update total device meebles
                        currentMeebles = meeblesVolcano + meeblesForest + meeblesDesert + meeblesTundra;
                        HomePage.saveMeebles(getApplicationContext(), currentMeebles);
                    }
                }
                else{
                    HomePage.writeToTag(updatedData, tag);
                    envData = updatedData;
                    runOnUiThread(() -> updateUI(envData));
                }
            } catch (IOException e) {
                runOnUiThread(() -> {Toast.makeText(getApplicationContext(), "Could not write to NFC. Try again.", Toast.LENGTH_SHORT).show();});
            }
            finally{
                dismissWritingDialog();
                isProcessing = false;
            }


            if(envData.getCityType() == MeebleConstants.CITY && firstCity){
                runOnUiThread(() -> showFirstCityDialog());
                prefs.edit().putBoolean("first_city", false).apply();
            }
        }
    }

    private void updateUI(EnvironmentData data){
        // Update the Meeble count with number
        meebleCountView.setText(data.getMeebleCount() + " Meeble(s)");

        // Update bottom panels
        TextView bottomCityText = findViewById(R.id.bottom_city_text);
        ImageView bottomCityImage = findViewById(R.id.bottom_city_image);
        bottomCityText.setText(MeebleConstants.cityTypeToString(data.getCityType()));
        int cityResId = getResources().getIdentifier(
                "city_" + data.getCityType(), "drawable", getPackageName());
        bottomCityImage.setImageResource(cityResId);

        TextView bottomEnvText = findViewById(R.id.bottom_env_text);
        ImageView bottomEnvImage = findViewById(R.id.bottom_env_image);
        bottomEnvText.setText(MeebleConstants.envTypeToString(data.getEnvironmentType()));
        int envResId = getResources().getIdentifier(
                "environment_" + data.getEnvironmentType(), "drawable", getPackageName());
        bottomEnvImage.setImageResource(envResId);

        // Populate meebles in container
        meebleContainer.removeAllViews();

        int maxAnimatedMeebles = 100;
        int meebleCount = data.getMeebleCount();

        // Minimum meebles to display even if count is very small
        int minVisibleMeebles = 2;

        // Use a density function: linear until a certain value, log-based curve + minimum for larger values
        int func;
        if(meebleCount <= 14){
            func = meebleCount;
        }
        else{
            func = (int) (Math.log10(meebleCount + 1) * 12);
        }

        int visibleMeebles = Math.max(minVisibleMeebles,
                Math.min(maxAnimatedMeebles,
                        func));

        for(int i = 0; i < visibleMeebles; i++){
            addRandomMeeble(data.getEnvironmentType());
        }
    }

    private void addRandomMeeble(char envType) {
        int size = 80;

        meebleContainer.post(() -> {
            int paddingLeft = meebleContainer.getPaddingLeft();
            int paddingRight = meebleContainer.getPaddingRight();
            int paddingTop = meebleContainer.getPaddingTop();
            int paddingBottom = meebleContainer.getPaddingBottom();

            int containerWidth = meebleContainer.getWidth();
            int containerHeight = meebleContainer.getHeight();

            int usableWidth = containerWidth - paddingLeft - paddingRight;
            int usableHeight = containerHeight - paddingTop - paddingBottom;

            int randomX = paddingLeft + (int)(Math.random() * (usableWidth - size));
            int randomY = paddingTop + (int)(Math.random() * (usableHeight - size));

            ImageView meeble = new ImageView(this);
            int meebleResId = getResources().getIdentifier(
                    "meeble_" + envType, "drawable", getPackageName());
            meeble.setImageResource(meebleResId);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = randomX;
            params.topMargin = randomY;
            meeble.setLayoutParams(params);

            meebleContainer.addView(meeble);

            startFloatingAnimation(meeble);
        });
    }

    private void startFloatingAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -10f, 10f);
        animator.setDuration(1500 + (int)(Math.random()*1000));
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.start();
    }

    private void showWritingDialog() {
        runOnUiThread(() -> {
            writingDialog = new AlertDialog.Builder(this)
                    .setMessage("Processing... Hold your phone still...")
                    .setCancelable(false)
                    .create();
            writingDialog.show();
        });
    }

    private void dismissWritingDialog() {
        runOnUiThread(() -> {
            if (writingDialog != null && writingDialog.isShowing()) {
                writingDialog.dismiss();
            }
        });
    }

    private void showFirstCityDialog(){
        new android.app.AlertDialog.Builder(this)
                .setTitle("Congratulations!")
                .setMessage("Your Meebles created a city for the first time! You can keep playing with Meebles to make more cities!")
                .setPositiveButton("Awesome!", null)
                .show();
    }
}