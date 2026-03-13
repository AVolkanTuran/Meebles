package edu.fandm.volkanwill.meebles;

import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.FloorViewHolder> {

    private final int[] floors = {R.drawable.floor_0, R.drawable.floor_1, R.drawable.floor_2};

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor_image, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        holder.floorImage.setImage(ImageSource.resource(floors[position]));
        holder.floorImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
    }

    @Override
    public int getItemCount() {
        return floors.length;
    }

    static class FloorViewHolder extends RecyclerView.ViewHolder {
        SubsamplingScaleImageView floorImage;

        FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            floorImage = itemView.findViewById(R.id.floor_image);
        }
    }
}