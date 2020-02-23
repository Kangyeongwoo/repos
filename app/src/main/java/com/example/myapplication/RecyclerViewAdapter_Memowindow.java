package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter_Memowindow extends RecyclerView.Adapter<RecyclerViewAdapter_Memowindow.ViewHolder> {

    private Context context;
    ArrayList<Bitmap> imagegviewroup;
    int itemLayout;

    public RecyclerViewAdapter_Memowindow(Context context , ArrayList<Bitmap> imagegviewroup , int itemLayout) {
        this.context = context;
        this.imagegviewroup = imagegviewroup;
        this.itemLayout = itemLayout;
    }


    @NonNull
    @Override
    public RecyclerViewAdapter_Memowindow.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout,parent,false);
        return new RecyclerViewAdapter_Memowindow.ViewHolder(v) ;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter_Memowindow.ViewHolder holder, int position) {
        //메모 보기 페이지의 이미지 리사이클러뷰
        holder.imageView.setImageBitmap(imagegviewroup.get(holder.getLayoutPosition()));
    }

    @Override
    public int getItemCount() {
        return imagegviewroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagewindowview);


        }
    }


}
