package edu.fandm.volkanwill.meebles;

import android.os.Bundle;
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

        ImageButton exit = findViewById(R.id.exit_button);
        exit.setOnClickListener(v -> {
            finish();
        });
    }
}