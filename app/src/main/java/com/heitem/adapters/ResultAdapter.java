package com.heitem.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.GooglePlace;

import java.util.Collections;
import java.util.List;

/**
 * Created by Heitem on 01/06/2015.
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    List<GooglePlace> data = Collections.emptyList();

    public ResultAdapter(Context context, List<GooglePlace> data){
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public ResultAdapter(){

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.main_row_layout_result, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GooglePlace current = data.get(position);
        holder.txt1.setText(current.getName());
        holder.txt2.setText(current.getAdress());
        if(current.getRating() != null) holder.rating.setRating(current.getRating());
        if(current.getImage() != null) holder.img.setImageBitmap(changeCouleur(current.getImage()));
        else {
            holder.img.setBackgroundResource(R.color.background_material_dark);
            holder.rating.setVisibility(View.GONE);
        }
    }

    public Bitmap changeCouleur(Bitmap myBitmap){

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

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView txt1;
        TextView txt2;
        RatingBar rating;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.image1);
            txt1 = (TextView) itemView.findViewById(R.id.txt1);
            txt2 = (TextView) itemView.findViewById(R.id.txt2);
            rating = (RatingBar) itemView.findViewById(R.id.rating);
        }
    }
}
