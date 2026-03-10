package edu.fandm.volkanwill.meebles;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;

public class DeveloperPage extends AppCompatActivity {

    private NfcAdapter adapter;

    boolean writeMode = false;
    boolean readMode = false;

    EditText meebleET;
    EditText cityET;
    EditText envET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_page);

        adapter = NfcAdapter.getDefaultAdapter(this);

        meebleET = findViewById(R.id.meeble_et);
        cityET = findViewById(R.id.city_type_et);
        envET = findViewById(R.id.environment_type_et);

        Button writeButton = findViewById(R.id.write_button);
        Button readButton = findViewById(R.id.read_button);

        writeButton.setOnClickListener(v -> {
            writeMode = true;
            readMode = false;
            Toast.makeText(this, "Tap NFC tag to WRITE", Toast.LENGTH_SHORT).show();
        });

        readButton.setOnClickListener(v -> {
            readMode = true;
            writeMode = false;
            Toast.makeText(this, "Tap NFC tag to READ", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.enableReaderMode(
                this,
                new NFCReaderCallback(),
                NfcAdapter.FLAG_READER_NFC_A,
                null
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.disableReaderMode(this);
    }

    private class NFCReaderCallback implements NfcAdapter.ReaderCallback {

        @Override
        public void onTagDiscovered(Tag tag) {

            runOnUiThread(() -> {

                try {

                    if (writeMode) {

                        int meeble = Integer.parseInt(meebleET.getText().toString());
                        int city = Integer.parseInt(cityET.getText().toString());
                        int env = Integer.parseInt(envET.getText().toString());

                        EnvironmentData data = new EnvironmentData(
                                meeble,
                                (char) city,
                                (char) env,
                                LocalTime.now()
                        );

                        writeToTag(data, tag);

                        Toast.makeText(getApplicationContext(),
                                "Tag written!",
                                Toast.LENGTH_SHORT).show();
                    }

                    else if (readMode) {

                        EnvironmentData data = readFromTag(tag);

                        meebleET.setText(String.valueOf(data.getMeebleCount()));
                        cityET.setText(String.valueOf((int) data.getCityType()));
                        envET.setText(String.valueOf((int) data.getEnvironmentType()));

                        Toast.makeText(getApplicationContext(),
                                "Tag loaded!",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error reading/writing tag",
                            Toast.LENGTH_SHORT).show();
                }

            });
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

    private EnvironmentData readFromTag(Tag tag) {

        EnvironmentData data = new EnvironmentData(0,'0','0',LocalTime.now());

        try {

            Ndef ndef = Ndef.get(tag);
            ndef.connect();

            NdefMessage msg = ndef.getNdefMessage();

            if (msg != null && msg.getRecords().length > 0) {

                byte[] payload = msg.getRecords()[0].getPayload();

                ByteArrayInputStream bis = new ByteArrayInputStream(payload);
                ObjectInputStream ois = new ObjectInputStream(bis);

                data = (EnvironmentData) ois.readObject();

                ois.close();
                bis.close();
            }

            ndef.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private void formatAndWriteToTag(Tag tag, NdefMessage message) {

        Ndef ndef = Ndef.get(tag);

        try {

            if (ndef != null) {

                ndef.connect();

                if (!ndef.isWritable()) return;

                if (ndef.getMaxSize() < message.getByteArrayLength()) return;

                ndef.writeNdefMessage(message);
                ndef.close();

            }

            else {

                NdefFormatable formattable = NdefFormatable.get(tag);

                if (formattable != null) {

                    formattable.connect();
                    formattable.format(message);
                    formattable.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}