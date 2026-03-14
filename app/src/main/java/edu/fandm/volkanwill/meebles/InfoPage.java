package edu.fandm.volkanwill.meebles;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InfoPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton info_close = (ImageButton) findViewById(R.id.info_close_button);
        info_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView infoText = findViewById(R.id.info_text);

        int biome = getIntent().getIntExtra("biome", MeebleConstants.ENV_FOREST);
        int level = getIntent().getIntExtra("level", MeebleConstants.VILLAGE);
        double growthRate;

        switch(biome){
            case MeebleConstants.ENV_VOLCANO:
                growthRate = 0.5;
                break;
            case MeebleConstants.ENV_FOREST:
                growthRate = 2.0;
                break;
            case MeebleConstants.ENV_DESERT:
                growthRate = 1.0;
                break;
            case MeebleConstants.ENV_TUNDRA:
                growthRate = 1.5;
                break;
            default:
                growthRate = 1.0;
        }

        String biomeMessage = "";
        String nextCheckpoint = "";

        switch (biome) {
            case MeebleConstants.ENV_FOREST:
                biomeMessage = "• Forests are nice to live in. The meebles will grow quickly.";
                break;

            case MeebleConstants.ENV_TUNDRA:
                biomeMessage = "• Tundras are a little chilly. It's a little rough for the meebles.";
                break;

            case MeebleConstants.ENV_DESERT:
                biomeMessage = "• High heat and no water means less fun for the meebles.";
                break;

            case MeebleConstants.ENV_VOLCANO:
                biomeMessage = "• Is it just me or is it really really scorching in here?";
                break;
        }

        switch (level) {
            case MeebleConstants.VILLAGE:
                nextCheckpoint = "town";
                break;

            case MeebleConstants.TOWN:
                nextCheckpoint = "city";
                break;

            case MeebleConstants.CITY:
                nextCheckpoint = "You've mastered exponential growth!";
                break;
        }

        String growthMessage = "";

        if(level == MeebleConstants.CITY){
            growthMessage = "• Growth rate: " + growthRate +
                    ". Your meebles are growing incredibly fast. " + nextCheckpoint;
        }
        else if (level == MeebleConstants.TOWN){
            growthMessage = "• Growth rate: " + growthRate +
                    ". At this rate your town will become a " + nextCheckpoint + " soon!";
        }
        else if (level == MeebleConstants.VILLAGE){
            growthMessage = "• Growth rate: " + growthRate +
                    ". It will take a while for your village to reach a " + nextCheckpoint;
        }

        String plotMessage =
                "• Notice how the population starts slow " +
                        "but increases faster and faster over time. Each generation multiplies the number of meebles!";

        String fullText = biomeMessage + "\n\n" +
                growthMessage + "\n\n" +
                plotMessage;

        infoText.setText(fullText);

        PopulationPlotView plotView = findViewById(R.id.population_plot);
        plotView.setGrowthRate(growthRate);
        plotView.startDotAnimation();
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