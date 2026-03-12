package edu.fandm.volkanwill.meebles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

    String[] titles = {
            "What are Meebles?",
            "Meebles Grow FAST!",
            "Find Meebles Cities",
            "Scan the City",
            "Deposit or Withdraw"
    };

    String[] texts = {
            "Meebles are tiny creatures that live in cities around the museum!",
            "1 becomes 2\n2 becomes 4\n4 becomes 8\n\nThis is exponential growth!",
            "Look around the museum for paper cities. Each city has a secret NFC tag!",
            "Use the Read Meebles Tag button to scan a city and see how many live there.",
            "You can move Meebles between cities and watch how the population changes!"
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
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.slide_title);
            text = itemView.findViewById(R.id.slide_text);
        }
    }
}