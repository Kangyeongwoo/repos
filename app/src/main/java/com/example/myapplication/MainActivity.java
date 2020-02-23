package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


   SQLiteManager sqLiteManager;
    RecyclerView recyclerView;
    RecyclerViewAdapter_Mainactivity adapter;
    LinearLayoutManager layoutManger;
    ArrayList< Memodata> memoarray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteManager = new SQLiteManager();


        // 메모 작성 액티비티로 넘어가는 버튼
        ImageButton creatememobt;


        //카메라 저장소 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            &&checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }


        //메모작성으로 넘어가는 클릭함수
        creatememobt = (ImageButton)findViewById(R.id.creatememobt);
        creatememobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent tocreate = new Intent(MainActivity.this, Creatememo.class);
               startActivity(tocreate);
            }
        });


        sqLiteManager.openDatabase(MainActivity.this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 리사이클러뷰 생성
        memoarray = new ArrayList< Memodata>();
        sqLiteManager.selectmemoData(memoarray);
        recyclerView = findViewById(R.id.memorecyclerview);
        adapter = new RecyclerViewAdapter_Mainactivity(MainActivity.this, R.layout.row_mainactivity_memo ,memoarray);
        recyclerView.setAdapter(adapter);
        layoutManger = new LinearLayoutManager(MainActivity.this);
        layoutManger.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }
}
