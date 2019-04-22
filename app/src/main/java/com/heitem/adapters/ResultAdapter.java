package com.heitem.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.GooglePlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Heitem on 01/06/2015.
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context context;
    private List<GooglePlace> data = new ArrayList<>();

    public ResultAdapter(Context context){
        this.context = context;
    }

    public void setData(List<GooglePlace> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.main_row_layout_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GooglePlace current = data.get(position);
        holder.bind(current);
    }

    private Bitmap changeColor(Bitmap myBitmap){

        /*int[] pixels = new int[myBitmap.getHeight()*myBitmap.getWidth()];
        myBitmap.getPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        for (int i=0; i<myBitmap.getWidth()*5; i++)
            pixels[i] = Color.rgb(200, 191, 231);
        myBitmap.setPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
        Bitmap mutableBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
        return mutableBitmap;*/
        Bitmap mutableBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint pnt = new Paint();
        Canvas myCanvas = new Canvas(mutableBitmap);

        int myColor = mutableBitmap.getPixel(0, 0);

        // Set the colour to replace.
        ColorFilter filter = new LightingColorFilter(myColor, Color.rgb(200, 191, 231) );
        pnt.setColorFilter(filter);

        // Draw onto new bitmap. result Bitmap is newBit
        myCanvas.drawBitmap(mutableBitmap,0,0, pnt);

        return mutableBitmap;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView img;
        private TextView txt1;
        private TextView txt2;
        private RatingBar rating;

        ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image1);
            txt1 = itemView.findViewById(R.id.txt1);
            txt2 = itemView.findViewById(R.id.txt2);
            rating = itemView.findViewById(R.id.rating);
        }

        void bind(GooglePlace place) {
            txt1.setText(place.getName());
            txt2.setText(place.getAddress());
            if(place.getRating() != null) rating.setRating(place.getRating());
            if(place.getImage() != null) img.setImageBitmap(changeColor(place.getImage()));
            else {
                img.setBackgroundResource(R.color.background_material_dark);
                rating.setVisibility(View.GONE);
            }
        }
    }
}
