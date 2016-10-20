package com.heitem.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.heitem.augmentedjourney.R;
import com.heitem.data_localization.GooglePlace;
import com.heitem.others.AutoResizeTextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Heitem on 01/06/2015.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    List<GooglePlace> data = Collections.emptyList();
    //private int mPreviousPosition = 0;
    OnItemClickListener mItemClickListener;

    public HomeAdapter(Context context, List<GooglePlace> data){
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public HomeAdapter(){

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.main_row_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        GooglePlace current = data.get(position);
        holder.txt1.setText(current.getName());
        holder.txt2.setText(current.getAdress());
        holder.img.setImageBitmap(current.getImage());
        int r = position+1;
        holder.ranking.setText("#" + r);

        /*if (position > mPreviousPosition) {
            //AnimationUtils.animateSunblind(holder, true);
//            AnimationUtils.animateSunblind(holder, true);
//            AnimationUtils.animate1(holder, true);
            AnimationUtils.animate(holder,true);
        } else {
            //AnimationUtils.animateSunblind(holder, false);
//            AnimationUtils.animateSunblind(holder, false);
//            AnimationUtils.animate1(holder, false);
            AnimationUtils.animate(holder, false);
        }
        mPreviousPosition = position;*/
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView img;
        TextView txt1;
        TextView txt2;
        TextView ranking;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.image1);
            txt1 = (AutoResizeTextView) itemView.findViewById(R.id.txt1);
            txt2 = (TextView) itemView.findViewById(R.id.txt2);
            ranking = (TextView) itemView.findViewById(R.id.ranking);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getPosition());
        }
    }
    public interface OnItemClickListener {

        public void onItemClick(View view , int position);
    }
    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
