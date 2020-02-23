package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class RecyclerViewAdapter_Creatememo extends RecyclerView.Adapter<RecyclerViewAdapter_Creatememo.ViewHolder> {

    private Context context;
    ArrayList<Bitmap> imagegroup;
    int itemLayout;


    public RecyclerViewAdapter_Creatememo(Context context , ArrayList<Bitmap> imagegroup , int itemLayout) {
        this.context = context;
        this.imagegroup = imagegroup;
        this.itemLayout = itemLayout;
    }


    @NonNull
    @Override
    public RecyclerViewAdapter_Creatememo.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout,parent,false);
        return new RecyclerViewAdapter_Creatememo.ViewHolder(v) ;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        //메모 생성 페이지의 리사이클러 뷰 에 이미지를 세팅
        holder.imageView.setImageBitmap(imagegroup.get(holder.getLayoutPosition()));

        // x 버튼을 눌러 이미지 삭제 가능
        holder.imagedeletebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagegroup.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), imagegroup.size());
            }
        });



    }

    @Override
    public int getItemCount() {
        Log.d("vvv2",": "+imagegroup.size());
        return imagegroup.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        ImageButton imagedeletebt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagecreateview);
            imagedeletebt = itemView.findViewById(R.id.imagedeletebt);

        }
    }
}

