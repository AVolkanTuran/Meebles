package edu.fandm.volkanwill.meebles;

import android.content.Intent;
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
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

public class HomePage extends AppCompatActivity {

    public static NfcAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adapter = NfcAdapter.getDefaultAdapter(this);

        Button tutorial_button = findViewById(R.id.tutorial_button);

        tutorial_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TutorialPage.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle options = new Bundle();
        MYNFCCallbackClass myCallback = new MYNFCCallbackClass();
        adapter.enableReaderMode(this, myCallback, NfcAdapter.FLAG_READER_NFC_A,options);
    }

    @Override
    protected  void onPause() {
        super.onPause();
        adapter.disableReaderMode(this);
    }
    private class MYNFCCallbackClass implements NfcAdapter.ReaderCallback {
        @Override
        public void onTagDiscovered(Tag tag) {
            Intent i = new Intent(getApplicationContext(), EnvironmentPage.class);
            startActivity(i);
        }

    }

    public static void writeToTag(EnvironmentData data, Tag tag) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void formatAndWriteToTag(Tag tag, NdefMessage message) {
        Ndef ndef = Ndef.get(tag);

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
                NdefFormatable formattable = NdefFormatable.get(tag);
                if (formattable != null) {
                    formattable.connect();
                    formattable.format(message);
                }
            }
        } catch (Exception e) {
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
        }
    }

    public static EnvironmentData readFromTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        EnvironmentData data = new EnvironmentData(0,'0','0', LocalTime.now());
        try {
            ndef.connect();
            NdefMessage msg = ndef.getNdefMessage();
            if (msg != null && msg.getRecords().length > 0) {
                byte[] payload = msg.getRecords()[0].getPayload();
                try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {
                    data = (EnvironmentData) ois.readObject();
                    return data;
                }
                catch (ClassNotFoundException | ClassCastException e) {
                    Log.e("NFC_READ", "Class mismatch or not found", e);
                }
            }
            else {
                Log.e("NFC_READ", "Tag is empty.");
            }
        } catch (Exception e) {
            // Handle read error
            e.printStackTrace();
        }finally {
            try {
                ndef.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }
}