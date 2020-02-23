package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLimagecheck extends AppCompatActivity {

    ImageView urlimageview;
    ImageButton urlselectbt;
    ImageButton urlcancelbt;
    ImageButton urlbackbt;
    Handler handler = new Handler();
    //테스트용 이미지 url
 //   String imageurl ="https://search.pstatic.net/common?type=f&size=192x192&quality=90&direct=true&src=https%3A%2F%2Fdbscthumb-phinf.pstatic.net%2F0727_000_18%2F20190906091526002_MYG6OCN0Y.jpg%2F55449B3A6BB7E34F.jpg%3Ftype%3Dm120_120" ;
   String imageurl;
    Bitmap bm;
    boolean imageloadcheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urlimagecheck);

        urlbackbt = findViewById(R.id.urlbackbt);
        urlimageview = findViewById(R.id.urlimageview);
        urlselectbt = findViewById(R.id.urlselectbt);
        urlcancelbt = findViewById(R.id.urlcancelbt);
        imageurl = getIntent().getStringExtra("URL");

        urlbackbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //이미지 검색 실패시 사용될 다이얼로그
        final AlertDialog.Builder ad = new AlertDialog.Builder(URLimagecheck.this);

        ad.setTitle("이미지 검색 실패");       // 제목 설정
        ad.setMessage("검색된 이미지가 없습니다. URL을 다시 입력해주세요");   // 내용 설정

        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
                Intent backintent = new Intent();
                setResult(RESULT_CANCELED, backintent);
                finish();

            }
        });


        //쓰레드에서 이미지를 확인하고 이미지를 표시한다.
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try{
                    //URL이 검색되면 화면에 이미지를 표시
                    URL url = new URL(imageurl);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {  // 화면에 그려줄 작업
                    urlimageview.setImageBitmap(bm);
                    imageloadcheck=true;
                        }
                    });

                } catch(Exception e){
                     //이미지가 검색되지 않으면 다이얼로그를 띄워 돌아가도록 만든다.
                      //URL 주소를 다시 입력해주세요
                    handler.post(new Runnable() {
                        @Override
                        public void run() {  // 화면에 그려줄 작업
                            ad.show();
                        }
                    });
                }
            }
        });
        t.start();

        //url 선택 하면 bitmap이미지 전달
        urlselectbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageloadcheck){
                    Intent backintent = new Intent();
                    backintent.putExtra("urlbitmap",bm);
                    setResult(RESULT_OK, backintent);
                    finish();
                }
            }
        });

        //url 취소하면 액티비티 종료
        urlcancelbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backintent = new Intent();
                setResult(RESULT_CANCELED, backintent);
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() { super.onBackPressed(); }
}
