package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ImagemodifyAsyncTask extends AsyncTask<Integer, Void, Void> {
    Context context;
    ArrayList<Bitmap> newimagebitmapgroup;
    ArrayList<Integer> newimagenumbergroup;
    ArrayList<Integer> deleteimagenumbergroup;
    SQLiteManager sqLiteManager;

    public ImagemodifyAsyncTask( Context context, SQLiteManager sqLiteManager, ArrayList<Bitmap> newimagebitmapgroup , ArrayList<Integer> newimagenumbergroup, ArrayList<Integer> deleteimagenumbergroup){

        this.context = context;
        this.sqLiteManager = sqLiteManager;
        this.newimagebitmapgroup = newimagebitmapgroup;
        this.newimagenumbergroup = newimagenumbergroup;
        this.deleteimagenumbergroup = deleteimagenumbergroup;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        //편집 후 저장시 백그라운드에서 이미지 삭제와 저장 진행
        int memoid = integers[0];
        byte[] imagebytedata = null;
        if(deleteimagenumbergroup.size() !=0){
         for(int i=0;i<deleteimagenumbergroup.size();i++){
             //기존 이미지들 중 삭제된 이미지들을 sqlite에서 지움
            sqLiteManager.imagemodifydelete(memoid, deleteimagenumbergroup.get(i));
         }
        }
        if(newimagebitmapgroup.size() !=0) {
            //추가로 첨부된 이미지  들을 sqlite에 저장
            for (int i = 0; i < newimagebitmapgroup.size(); i++) {
                Bitmap plusimage = newimagebitmapgroup.get(i);
                int imagenumber = newimagenumbergroup.get(i);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                plusimage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                imagebytedata = stream.toByteArray();
                sqLiteManager.insertimageData(memoid, imagenumber, imagebytedata);
            }
        }
        //이미지 저장과 삭제가 끝나면 액티비티 종료
        ((Activity)context).finish();
        return null;
    }
}
