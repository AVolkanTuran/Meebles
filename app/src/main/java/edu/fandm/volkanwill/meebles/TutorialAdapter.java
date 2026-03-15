package edu.fandm.volkanwill.meebles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    String[] titles = {
            "Meet the Meebles",
            "Meebles Grow really really fast! EXPONENTIALLY fast!",
            "Find Meebles Cities",
            "Scan the City",
            "Biome of that City",
            "Deposit or Withdraw"
    };

    String[] texts = {
            "Meebles are cute tiny creatures that live in the paper cities around the museum!",
            "1 can become 2\n2 can become 4\n4 can become 8\n\nThis is called exponential growth!",
            "Look around the museum for paper cities. Each city has a special white Meebles tag!",
            "Use the Read Meebles Tag button to scan a city and see how many live there.",
            "The places you come across might have different environments which will affect how fast the meebles can grow!",
            "You can move Meebles between cities and watch how the population grows.\n\nHelp the Meebles grow from a village to a town and then finally a city!"
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tutorial_slide, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.title.setText(titles[position]);
        holder.text.setText(texts[position]);

        holder.imageContainer.removeAllViews();

        switch (position) {
            case 0: // Slide 1: 4 meebles lined up
                int[] meebles = {
                        R.drawable.meeble_1,
                        R.drawable.meeble_2,
                        R.drawable.meeble_3,
                        R.drawable.meeble_4
                };
                for (int res : meebles) {
                    ImageView iv = new ImageView(holder.itemView.getContext());
                    iv.setImageResource(res);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
                    params.setMargins(10,0,10,0);
                    iv.setLayoutParams(params);
                    holder.imageContainer.addView(iv);
                }
                break;

            case 1: // Slide 2: 1 -> 2 -> 4 -> 8
                ImageView growthIllustration = new ImageView(holder.itemView.getContext());
                growthIllustration.setImageResource(R.drawable.meeble_multiply);
                LinearLayout.LayoutParams growthParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                growthIllustration.setLayoutParams(growthParams);
                holder.imageContainer.addView(growthIllustration);
                break;

            case 2: // Slide 3: paper city
                ImageView paperCity = new ImageView(holder.itemView.getContext());
                paperCity.setImageResource(R.drawable.paper_city);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                paperCity.setLayoutParams(params2);
                paperCity.setAdjustViewBounds(true);
                holder.imageContainer.addView(paperCity);
                break;

            case 3: // Slide 4: NFC tag
                ImageView nfcTag = new ImageView(holder.itemView.getContext());
                nfcTag.setImageResource(R.drawable.nfc_tag);
                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                nfcTag.setLayoutParams(params3);
                nfcTag.setAdjustViewBounds(true);
                holder.imageContainer.addView(nfcTag);
                break;

            case 4: // Slide 5: Biomes
                int[] biomes = {
                        R.drawable.environment_1,
                        R.drawable.environment_2,
                        R.drawable.environment_3,
                        R.drawable.environment_4
                };
                for (int i = 0; i < biomes.length; i++) {

                    ImageView iv = new ImageView(holder.itemView.getContext());
                    iv.setImageResource(biomes[i]);

                    LinearLayout.LayoutParams params;

                    if (i == 3) { // 4th image
                        params = new LinearLayout.LayoutParams(250, 250);
                    } else {
                        params = new LinearLayout.LayoutParams(200, 200);
                    }

                    params.setMargins(10, 0, 10, 0);
                    iv.setLayoutParams(params);

                    holder.imageContainer.addView(iv);
                }
                break;

            case 5: // Slide 6: meeble -> city
                ImageView meeble_to_city = new ImageView(holder.itemView.getContext());
                meeble_to_city.setImageResource(R.drawable.meeble_to_city);
                LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                meeble_to_city.setLayoutParams(params4);
                meeble_to_city.setAdjustViewBounds(true);
                holder.imageContainer.addView(meeble_to_city);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView text;
        LinearLayout imageContainer;

        ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.slide_title);
            text = itemView.findViewById(R.id.slide_text);
            imageContainer = itemView.findViewById(R.id.slide_images_container);
        }
    }
}