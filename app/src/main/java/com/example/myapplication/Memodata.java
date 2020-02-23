package com.example.myapplication;

import android.graphics.Bitmap;

//memodata를 구조화 하기 위한 클래스
public class Memodata {

    int memoid;
    String title;
    String content;
    Bitmap thumbnail;
    String day;

    public Memodata(int memoid, String title, String content,Bitmap thumbnail,String day){
        this.memoid = memoid;
        this.title = title;
        this.content=content;
        this.thumbnail = thumbnail;
        this.day = day;
    }

}


