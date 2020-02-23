package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
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
import java.util.Date;
import java.util.HashMap;

public class RecyclerViewAdapter_Mainactivity  extends RecyclerView.Adapter<RecyclerViewAdapter_Mainactivity.ViewHolder>  {
    Context context;
    int itemLayout;
    ArrayList< Memodata> memoarray;
    String[] timepm = {"13","14","15","16","17","18","19","20","21","22","23"};
    String[] timeam = {"01","02","03","04","05","06","07","08","09","10","11"};

    public RecyclerViewAdapter_Mainactivity(Context context , int itemLayout,ArrayList< Memodata> memoarray) {
        this.context = context;
       this.itemLayout = itemLayout;
       this.memoarray = memoarray;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter_Mainactivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout,parent,false);
        return new RecyclerViewAdapter_Mainactivity.ViewHolder(v) ;
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewAdapter_Mainactivity.ViewHolder holder, int position) {
         //메인액티비티의 리사이클러뷰
         String title =  memoarray.get(holder.getLayoutPosition()).title;
         String content = memoarray.get(holder.getLayoutPosition()).content;
         Bitmap thumbnailbitmap = memoarray.get(holder.getLayoutPosition()).thumbnail;
         String day = memoarray.get(holder.getLayoutPosition()).day;

         //제목이 없을 때는 내용을 제목 대신에 표시한다
         if(title.equals(null)){
             holder.titleview.setText(title);
         }else{
             holder.titleview.setText(content);
         }

         //내용을 표시
         holder.contentview.setText(content);

         //날짜를 찾아서 현재 날짜와 비교 -> 같은 날짜라면 시간을 보여주고 / 다른 날짜라면 년,월,일 을 보여준다
        String[] dayarray = day.split(" ");
        String[] timearray = dayarray[1].split(":");

        //현재 날짜 찾기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = sdfNow.format(date);

        String time = "";
        if(formatDate.equals(dayarray[0])){
            //오늘이면 시간으로 표시 오전 오후 로 표기
            boolean pmcheck = false;
            for(int i=0; i<timepm.length;i++){
                if(timearray[0].equals(timepm[i])){
                    //오후 일때
                    time = "오후 "+timeam[i]+":"+timearray[1];
                    pmcheck=true;
                    break;
                }
            }
            if(pmcheck==false){
                time = "오전 "+dayarray[1];
            }
            //시간으로 표기
            holder.dateview.setText(time);
        }else {
            //오늘이 아니면 날짜로 표시
            holder.dateview.setText(dayarray[0]);
        }

        //이미지가 없으면 빈공간으로 있으면 이미지로 표시
         if(thumbnailbitmap!=null) {
             holder.thumbnailview.setImageBitmap(thumbnailbitmap);
         }else{
             holder.thumbnailview.setVisibility(View.GONE);
         }

         //이 뷰 홀더를 클릭하면 메모 보기 페이지로 이동
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 int memoid =memoarray.get(holder.getLayoutPosition()).memoid;
                 Intent tomemowindow = new Intent( context , Memowindow.class );
                 tomemowindow.putExtra("memoid",memoid);
                 context.startActivity(tomemowindow);
             }
         });


    }


    @Override
    public int getItemCount() {

        return memoarray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleview;
        TextView contentview;
        TextView dateview;
        ImageView thumbnailview;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleview = itemView.findViewById(R.id.titleview);
            contentview = itemView.findViewById(R.id.contentview);
            dateview = itemView.findViewById(R.id.dateview);
            thumbnailview = itemView.findViewById(R.id.thumbnailview);
        }
    }

}
