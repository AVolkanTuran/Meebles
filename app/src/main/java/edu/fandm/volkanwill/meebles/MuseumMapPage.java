package edu.fandm.volkanwill.meebles;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import me.relex.circleindicator.CircleIndicator3;

public class MuseumMapPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_map_page);

        ViewPager2 pager = findViewById(R.id.map_pager);
        MapAdapter adapter = new MapAdapter();
        pager.setAdapter(adapter);

        CircleIndicator3 indicator = findViewById(R.id.tutorial_dots);
        indicator.setViewPager(pager); // links the indicator to your ViewPager2

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton exit = findViewById(R.id.exit_button);
        exit.setOnClickListener(v -> {
            finish();
        });
    }

    //Added this code to ignore scanning
    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if(adapter != null) {
            // Enable reader mode but do nothing on tag discovered
            adapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
                @Override
                public void onTagDiscovered(Tag tag) {
                    // intentionally empty to ignore tags
                    Log.d("NFC_IGNORE", "Tag scanned but ignored on WelcomePage");
                }
            }, NfcAdapter.FLAG_READER_NFC_A, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if(adapter != null) {
            adapter.disableReaderMode(this);
        }
    }
}