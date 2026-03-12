package edu.fandm.volkanwill.meebles;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import me.relex.circleindicator.CircleIndicator3;

public class TutorialPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial_page);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        //TODO: Can we even animate the exponential growth?
        //TODO: Add cartoon meeble illustrations and make the slides look nicer
        ViewPager2 pager = findViewById(R.id.tutorial_pager);
        TutorialAdapter adapter = new TutorialAdapter();

        pager.setAdapter(adapter);

        CircleIndicator3 indicator = findViewById(R.id.tutorial_dots);
        indicator.setViewPager(pager); // links the indicator to your ViewPager2

        ImageButton exit = findViewById(R.id.exit_button);
        exit.setOnClickListener(v -> {
            finish();
        });
    }

    //5 slides

//    Slide 1
//    What are Meebles?
//
//    Meebles are tiny creatures that live in cities
//    around the museum!

//    Slide 2
//    Meebles grow FAST!
//
//            1 meeble becomes 2
//            2 becomes 4
//            4 becomes 8
//
//    This is called EXPONENTIAL GROWTH!

//    Slide 3
//    Find Meebles Cities
//
//    Look around the museum for paper cities.
//    Each city has a secret NFC tag!

//    Slide 4
//    Scan the city
//
//    Use the "Read Meebles Tag" button
//    to scan the city and see how many
//    meebles live there.

//    Slide 5
//    Deposit or Withdraw
//
//    You can move Meebles between cities!
//
//    Watch how the population changes!

//    There needs to be big X button to exit the tutorial
}