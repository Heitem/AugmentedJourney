package com.heitem.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.heitem.augmentedjourney.MainActivity;
import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.GooglePlace;
import com.heitem.others.AutoResizeTextView;
import com.heitem.ui.CameraActivity;
import com.heitem.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

import static com.heitem.utils.Constants.GOOGLE_KEY;

/**
 * Created by Heitem on 01/06/2015.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private Context context;
    private List<GooglePlace> data = new ArrayList<>();

    public HomeAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<GooglePlace> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.main_row_layout, parent, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        GooglePlace current = data.get(position);
        holder.bind(current);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView txt1;
        TextView txt2;
        TextView ranking;

        MyViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image1);
            txt1 = (AutoResizeTextView) itemView.findViewById(R.id.txt1);
            txt2 = itemView.findViewById(R.id.txt2);
            ranking = itemView.findViewById(R.id.ranking);
        }

        void bind(GooglePlace place) {
            txt1.setText(place.getName());
            txt2.setText(place.getAddress());
            int r = getLayoutPosition() + 1;
            ranking.setText("#" + r);
            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, CameraActivity.class);
                i.putExtra("O", place);
                Toast.makeText(context, "Vous avez basculer vers le mode Réalité Augmentée.\nCible : " + place.getName(), Toast.LENGTH_LONG).show();
                context.startActivity(i);
            });

            String url = "https://maps.googleapis.com/maps/api/place/photo?photo_reference=" + place.getPhoto_reference() + "&maxheight=300&maxwidth=1440&key=" + GOOGLE_KEY;
            Glide.with(context).asBitmap().load(url).into(img);
        }
    }
}
