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
import android.widget.EditText;
import android.widget.ImageButton;

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

public class EnvironmentPage extends AppCompatActivity {

    public static NfcAdapter adapter;

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
            EditText et = (EditText) findViewById(R.id.nfc_number_et);
            int value = Integer.parseInt(et.getText().toString());
            EnvironmentData data = readFromTag(tag);

            MaterialButtonToggleGroup mbtg = (MaterialButtonToggleGroup) findViewById(R.id.withdraw_deposit_toggle);
            int selectedId = mbtg.getCheckedButtonId();
            if(selectedId == R.id.withdraw){
                if(value < data.getMeebleCount()) {
                    data.setMeebleCount(data.getMeebleCount() - value);
                }
            } else if(selectedId == R.id.deposit){
                data.setMeebleCount(data.getMeebleCount()+value);
                data.setTime(LocalTime.now());
            }

            writeToTag(data, tag);
        }
    }


    private void writeToTag(EnvironmentData data, Tag tag) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.flush();
            byte[] serializedData = bos.toByteArray();

            String mimeType = "application/edu.fandm.volkanwill.meebles.environmentdata";
            NdefRecord record = NdefRecord.createMime(mimeType, serializedData);


            NdefMessage message = new NdefMessage(new NdefRecord[]{record});

            formatAndWriteToTag(tag, message);

            oos.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void formatAndWriteToTag(Tag tag, NdefMessage message) {
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
    }

    private EnvironmentData readFromTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        EnvironmentData data = new EnvironmentData(0,'0','0',LocalTime.now());
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