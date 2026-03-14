package edu.fandm.volkanwill.meebles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import android.animation.ObjectAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;

public class HomePage extends AppCompatActivity {

    public static NfcAdapter adapter; //Removed the public static so that we can't scan in other pages
    FrameLayout meebleContainer;

    // Counts for each environment type
    private int meeblesVolcano = 0;
    private int meeblesForest  = 0;
    private int meeblesDesert  = 0;
    private int meeblesTundra  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        meebleContainer = findViewById(R.id.meebles_container);

        adapter = NfcAdapter.getDefaultAdapter(this);

        //Putting the number of meebles we have in SharedPreferences so that it is shared between activities
        SharedPreferences prefs = getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        boolean firstTime = prefs.getBoolean("first_time", true);

        if(firstTime){
            showIntroDialog();
        }

        Button tutorial_button = findViewById(R.id.tutorial_button);

        tutorial_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TutorialPage.class);
                startActivity(i);
            }
        });

        MaterialButton readTag = findViewById(R.id.read_tag_button);
        readTag.setOnClickListener(v -> {
            if (readTag.isChecked()) {
                readTag.setText("Read Meebles Tag: On");
                Bundle options = new Bundle();
                MYNFCCallbackClass myCallback = new MYNFCCallbackClass();
                adapter.enableReaderMode(this, myCallback, NfcAdapter.FLAG_READER_NFC_A,options);
            } else {
                readTag.setText("Read Meebles Tag: Off");
                adapter.disableReaderMode(this);
            }
        });

        Button map_button = findViewById(R.id.map_button);

        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MuseumMapPage.class);
                startActivity(i);
            }
        });

    }

    public static void saveMeebles(Context context, int count){
        SharedPreferences prefs = context.getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        prefs.edit().putInt("meeble_count", count).apply();
    }

    public static int loadMeebles(Context context){
        SharedPreferences prefs = context.getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        return prefs.getInt("meeble_count", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView counter = findViewById(R.id.meebles_counter);

        int count = loadMeebles(this);
        counter.setText(String.valueOf(count));

        meebleContainer.removeAllViews();

        // Load environment-specific meebles
        int[] counts = loadMeeblesData(this);
        meeblesVolcano = counts[0];
        meeblesForest  = counts[1];
        meeblesDesert  = counts[2];
        meeblesTundra  = counts[3];

        int totalCount = meeblesVolcano + meeblesForest + meeblesDesert + meeblesTundra;
        counter.setText(String.valueOf(totalCount));

        meebleContainer.removeAllViews();

        int func;
        if(totalCount <= 14){
            func = totalCount;
        }
        else{
            func = (int) (Math.log10(totalCount + 1) * 12);
        }

        int visibleMeebles = Math.min(50, func);

        // Compute percentage of each type
        float percVolcano = totalCount > 0 ? (float) meeblesVolcano / totalCount : 0.25f;
        float percForest  = totalCount > 0 ? (float) meeblesForest / totalCount : 0.25f;
        float percDesert  = totalCount > 0 ? (float) meeblesDesert / totalCount : 0.25f;
        float percTundra  = totalCount > 0 ? (float) meeblesTundra / totalCount : 0.25f;

        // Determine how many meebles to draw for each type
        int volcanoCount = Math.round(visibleMeebles * percVolcano);
        int forestCount  = Math.round(visibleMeebles * percForest);
        int desertCount  = Math.round(visibleMeebles * percDesert);
        int tundraCount  = Math.round(visibleMeebles * percTundra);

        // Add meebles
        for(int i = 0; i < volcanoCount; i++) addRandomMeeble(R.drawable.meeble_1);
        for(int i = 0; i < forestCount; i++)  addRandomMeeble(R.drawable.meeble_2);
        for(int i = 0; i < desertCount; i++)  addRandomMeeble(R.drawable.meeble_3);
        for(int i = 0; i < tundraCount; i++)  addRandomMeeble(R.drawable.meeble_4);
    }

    @Override
    protected  void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.disableReaderMode(this);
        }
    }
    private class MYNFCCallbackClass implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag) {
            EnvironmentData data;
            Intent i = new Intent(getApplicationContext(), EnvironmentPage.class);
            try {
                data = readFromTag(tag);
                if(data != null) {
                    EnvironmentData updatedData = growMeebles(data);
                    i.putExtra("environment_data", updatedData);
                    i.putExtra("new_env", false);
                    startActivity(i);
                }
                if (data == null) {
                    data = generateRandomEnvironment();
                    try {
                        writeToTag(data, tag);
                        i.putExtra("environment_data", data);
                        i.putExtra("new_env", true);
                        startActivity(i);
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Could not initialize tag. Try again.", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e){
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Could not read tag. Try again.", Toast.LENGTH_SHORT).show());
            }
        }

    }

    public static EnvironmentData generateRandomEnvironment(){
        char city = MeebleConstants.VILLAGE;
        char[] envTypes = {MeebleConstants.ENV_VOLCANO, MeebleConstants.ENV_FOREST, MeebleConstants.ENV_DESERT, MeebleConstants.ENV_TUNDRA};
        char env = envTypes[(int)(Math.random() * envTypes.length)];
        int startingMeebles = 4;
        return new EnvironmentData(startingMeebles, city, env, System.currentTimeMillis());
    }
    public static void writeToTag(EnvironmentData data, Tag tag) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.flush();
            byte[] serializedData = bos.toByteArray();
            oos.close();

            String mimeType = "application/edu.fandm.volkanwill.meebles.environmentdata";
            NdefRecord record = NdefRecord.createMime(mimeType, serializedData);


            NdefMessage message = new NdefMessage(new NdefRecord[]{record});

            formatAndWriteToTag(tag, message);

            bos.close();
        }catch(IOException e){
            throw new IOException(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void formatAndWriteToTag(Tag tag, NdefMessage message) throws IOException {
        Ndef ndef = Ndef.get(tag);
        NdefFormatable formattable = null;

        try {
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return;
                }
                if (ndef.getMaxSize() < message.getByteArrayLength()) {
                    return;
                }
                ndef.writeNdefMessage(message);
            } else {
                formattable = NdefFormatable.get(tag);
                if (formattable != null) {
                    formattable.connect();
                    formattable.format(message);
                }
            }
        }catch(IOException e){
            throw new IOException(e);
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                if(ndef!=null){
                    ndef.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (formattable != null) {
                    formattable.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static EnvironmentData readFromTag(Tag tag) throws IOException{

        try (Ndef ndef = Ndef.get(tag)) {
            try {
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                if (msg != null && msg.getRecords().length > 0) {
                    byte[] payload = msg.getRecords()[0].getPayload();
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
                         ObjectInputStream ois = new ObjectInputStream(bis)) {
                        return (EnvironmentData) ois.readObject();
                    } catch (ClassNotFoundException | ClassCastException | InvalidClassException e) {
                        Log.e("NFC_READ", "Class mismatch or not found", e);
                        return null;
                    }
                } else {
                    Log.e("NFC_READ", "Tag is empty.");
                    return null;
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Timescale is minutes. Doubles every minute given a rate of 1.
    public static int exponentialGrowth(int p0, double rate, long time){
        return (int) (p0*Math.pow(2, rate * (time/60000.0)));
    }

    public static EnvironmentData growMeebles(EnvironmentData data){
        if(data == null){
            return null;
        }

        //reset at noon and midnight
        long now = System.currentTimeMillis();
        long lastTime = data.getTime();

        java.util.Calendar last = java.util.Calendar.getInstance();
        last.setTimeInMillis(lastTime);

        java.util.Calendar current = java.util.Calendar.getInstance();
        current.setTimeInMillis(now);

        java.util.Calendar midnight = (java.util.Calendar) current.clone();
        midnight.set(java.util.Calendar.HOUR_OF_DAY, 0);
        midnight.set(java.util.Calendar.MINUTE, 0);
        midnight.set(java.util.Calendar.SECOND, 0);
        midnight.set(java.util.Calendar.MILLISECOND, 0);

        java.util.Calendar noon = (java.util.Calendar) current.clone();
        noon.set(java.util.Calendar.HOUR_OF_DAY, 12);
        noon.set(java.util.Calendar.MINUTE, 0);
        noon.set(java.util.Calendar.SECOND, 0);
        noon.set(java.util.Calendar.MILLISECOND, 0);

        boolean resetHappened = (lastTime < midnight.getTimeInMillis() && now >= midnight.getTimeInMillis())
                || (lastTime < noon.getTimeInMillis() && now >= noon.getTimeInMillis());

        if(resetHappened){
            data.setMeebleCount(4);
            if (now >= noon.getTimeInMillis() && lastTime < noon.getTimeInMillis()) {
                data.setTime(noon.getTimeInMillis());
            } else {
                data.setTime(midnight.getTimeInMillis());
            }
        }

        long timeElapsed = now-data.getTime();

        double rate;
        switch (data.getEnvironmentType()){
            case MeebleConstants.ENV_VOLCANO:
                rate = 0.5;
                break;
            case MeebleConstants.ENV_FOREST:
                rate = 2;
                break;
            case MeebleConstants.ENV_DESERT:
                rate = 1;
                break;
            case MeebleConstants.ENV_TUNDRA:
                rate = 1.5;
                break;
            default:
                rate = 1;
        }

        int meebleCount = exponentialGrowth(data.getMeebleCount(), rate, timeElapsed);
        if(meebleCount > 4000000){
            meebleCount = 4000000;
        }

        if(meebleCount < 0){
            meebleCount = 4;
        }

        char cityType = '1';

        if(meebleCount > 500000){
            cityType = '3';
        }
        else if(meebleCount > 20000){
            cityType = '2';
        }

        long newTime;
        if (meebleCount != data.getMeebleCount()){
            newTime = System.currentTimeMillis();
        } else{ newTime = data.getTime();}
        return new EnvironmentData(meebleCount, cityType, data.getEnvironmentType(), newTime);
    }

    private void showIntroDialog(){

        new AlertDialog.Builder(this)
                .setTitle("Welcome!")
                .setMessage("Do you know what exponential growth is?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    SharedPreferences prefs = getSharedPreferences("meebles_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("first_time", false).apply();

                    Toast.makeText(this,"Great! Let's start exploring!",Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {

                    SharedPreferences prefs = getSharedPreferences("meebles_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("first_time", false).apply();

                    Intent i = new Intent(HomePage.this, TutorialPage.class);
                    startActivity(i);
                })
                .show();
    }

    private void addRandomMeeble(int resId) {

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
            meeble.setImageResource(resId);

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(size, size);

            params.leftMargin = randomX;
            params.topMargin = randomY;

            meeble.setLayoutParams(params);

            meebleContainer.addView(meeble);

            startFloatingAnimation(meeble);
        });
    }

    private void startFloatingAnimation(View view) {

        ObjectAnimator animator =
                ObjectAnimator.ofFloat(view, "translationY", -10f, 10f);

        animator.setDuration(1500 + (int)(Math.random()*1000));
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);

        animator.start();
    }

    public static void saveMeeblesData(Context context, int volcano, int forest, int desert, int tundra) {
        SharedPreferences prefs = context.getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("meeble_count", volcano + forest + desert + tundra)
                .putInt("meebles_volcano", volcano)
                .putInt("meebles_forest", forest)
                .putInt("meebles_desert", desert)
                .putInt("meebles_tundra", tundra)
                .apply();
    }

    public static int[] loadMeeblesData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("meebles_prefs", MODE_PRIVATE);
        return new int[]{
                prefs.getInt("meebles_volcano", 0),
                prefs.getInt("meebles_forest", 0),
                prefs.getInt("meebles_desert", 0),
                prefs.getInt("meebles_tundra", 0)
        };
    }
}