package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewadapter_Memomodify extends RecyclerView.Adapter<RecyclerViewadapter_Memomodify.ViewHolder> {

    Context context;
    int itemLayout;
    ArrayList<Bitmap> imagegroup ;
    ArrayList<Integer> imagenumbergroup;
    ArrayList<Integer> newimagenumbergroup;
    ArrayList<Bitmap> newimagebitmapgroup;
    ArrayList<Integer> deleteimagenumbergroup;

    public RecyclerViewadapter_Memomodify(Context context , int itemLayout, ArrayList<Bitmap> imagegroup,
                                          ArrayList<Integer> imagenumbergroup, ArrayList<Integer> newimagenumbergroup,
                                          ArrayList<Bitmap> newimagebitmapgroup,ArrayList<Integer> deleteimagenumbergroup  ) {
        this.context = context;
        this.itemLayout = itemLayout;
        this.imagegroup = imagegroup;
        this.imagenumbergroup = imagenumbergroup;
        this.newimagenumbergroup = newimagenumbergroup;
        this.newimagebitmapgroup = newimagebitmapgroup;
        this.deleteimagenumbergroup = deleteimagenumbergroup;
    }


    @NonNull
    @Override
    public RecyclerViewadapter_Memomodify.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout,parent,false);
        return new RecyclerViewadapter_Memomodify.ViewHolder(v) ;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewadapter_Memomodify.ViewHolder holder, int position) {
        //메모 수정페이지의 이미지를 보여주는 리사이클러뷰

        holder.imageView.setImageBitmap(imagegroup.get(holder.getLayoutPosition()));

        //이미지를 삭제할 때 이번 수정 과정에서 새로 추가한 것인지 기존에 있던 이미지 인지 확이하여 처리
        //기존에 있던 것은 sqlite에서 지워줘야 하고 이번에 추가된 것이라면 리스트에서만 삭제하면 된다.
        holder.imagedeletebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newonecheck =0;
                imagegroup.remove(holder.getLayoutPosition());
                Log.d("imagedelete1",":"+imagenumbergroup.get(holder.getLayoutPosition()));
                Log.d("imagedelete1",":"+imagenumbergroup.size());
                int deletenum = imagenumbergroup.get(holder.getLayoutPosition());

                if(newimagenumbergroup.size()!=0) {
                    for (int i = 0; i < newimagenumbergroup.size(); i++) {
                        if (newimagenumbergroup.get(i) == deletenum) {
                            newimagenumbergroup.remove(i);
                            newimagebitmapgroup.remove(i);
                            newonecheck = 1;
                            break;
                        }
                    }
                }

                if(newonecheck==0){
                    //기존에 있던 거에서 삭제
                    deleteimagenumbergroup.add(deletenum);
                }

                imagenumbergroup.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
                notifyItemRangeChanged(holder.getLayoutPosition(), imagegroup.size());
            }
        });



    }

    @Override
    public int getItemCount() {
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
