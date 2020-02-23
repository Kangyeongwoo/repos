package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Memowindow extends AppCompatActivity {

    int memoid ;
    String title;
    String content;
    String day;
    ArrayList<Bitmap> imageviewgroup;
    ArrayList<Integer> imageunmbergroup;
    ArrayList<String> dataarray;
    SQLiteManager sqLiteManager;
    TextView titleview;
    TextView contentview;

    RecyclerView recyclerView;
    RecyclerViewAdapter_Memowindow adapter;
    LinearLayoutManager layoutManger;

    ImageButton memowindowdeletebt;
    ImageButton memomodifybt;
    ImageButton memowindowbackbt;

    public static Activity _memowindow_activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memowindow);

        _memowindow_activity = Memowindow.this;
        Intent intent = getIntent();
        memoid = intent.getExtras().getInt("memoid");
        imageunmbergroup = new ArrayList<>();
        sqLiteManager = new SQLiteManager();
        memowindowbackbt = findViewById(R.id.memowindowbackbt);
        titleview = (TextView)findViewById(R.id.titleview);
        contentview = (TextView)findViewById(R.id.contentview);
        sqLiteManager.openDatabase(Memowindow.this);

        //타이틀 merquree 효과를 주기 위한 함수
        titleview.setSelected(true);

        //백버튼 함수
        memowindowbackbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //메모 삭제시 함수
        memowindowdeletebt = findViewById(R.id.memowindowdeletebt);
        memowindowdeletebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //메모 삭제 확인을 위한 다이얼로그 생성
                AlertDialog.Builder ad = new AlertDialog.Builder(Memowindow.this);
                ad.setTitle("메모 삭제 확인");       // 제목 설정
                ad.setMessage("정말로 메모를 삭제하시겠습니까?");   // 내용 설정
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteManager.memodelete(memoid);
                        finish();
                    }
                });
                ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.show();
            }
        });

        //수정 페이지로 넘어가는 함수
        memomodifybt = findViewById(R.id.memomodifybt);
        memomodifybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tomemomodify = new Intent(Memowindow.this , Memomodify.class);
                tomemomodify.putExtra("memoid",memoid);
                tomemomodify.putExtra("title",titleview.getText().toString());
                tomemomodify.putExtra("content",contentview.getText().toString());
                startActivity(tomemomodify);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        imageviewgroup = new ArrayList<>();
        dataarray = new ArrayList<>();

        //sqlite 에서 데이터 가져오기
        sqLiteManager.selectmemoviewData(memoid,dataarray);

        titleview.setText(dataarray.get(0));
        contentview.setText(dataarray.get(1));

        sqLiteManager.selectimageData(memoid,imageviewgroup,imageunmbergroup);

        //리사이클러뷰 생성
        recyclerView = findViewById(R.id.imagewindowrecycle);
        adapter = new RecyclerViewAdapter_Memowindow(Memowindow.this, imageviewgroup ,R.layout.row_memowindow_image );
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        layoutManger = new LinearLayoutManager(Memowindow.this);
        layoutManger.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    @Override
    public void onBackPressed() { super.onBackPressed(); }
}
