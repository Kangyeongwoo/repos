package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ImageAsyncTask extends AsyncTask<Integer, Void, Void> {


    Context context;
    ArrayList<Bitmap> imagegroup;
    SQLiteManager sqLiteManager;

    public ImageAsyncTask( Context context, SQLiteManager sqLiteManager, ArrayList<Bitmap> imagegroup ){

        this.context = context;
        this.sqLiteManager = sqLiteManager;
        this.imagegroup = imagegroup;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        //백 그라운드에서 이미지를 저장
        int memoid = integers[0];
        Log.d("memoidtest2", String.valueOf(memoid) );
        byte[] imagebytedata = null;
        for(int i =0; i<imagegroup.size(); i++){
            Bitmap plusimage = imagegroup.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            plusimage.compress(Bitmap.CompressFormat.PNG,100,stream);
            imagebytedata = stream.toByteArray();
            sqLiteManager.insertimageData(memoid,i,imagebytedata);
        }
        //이미지 저장이 완료되면 액티비티 종료
        ((Activity) context).finish();

            return null;


    }





}
