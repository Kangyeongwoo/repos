package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

//memotable 은 메모id, 제목, 내용, 썸네일 이미지, 작성 또는 수정 시간을 포함
//imagetable 은 이 이미지가 어떤 메모에 있었는지 식별하기 위한 메모id , 몇번째 순서로 저장됬는지에 관한 순서, byte[] 이미지 를 포함한다.
//sqlite 에 삽입, 삭제 , 수정, 확인 작업을 하는 함수
public class SQLiteManager {

    SQLiteDatabase database;
    String databaseName = "memodb";
    String table1name = "memotable";
    String table2name = "imagetable";


    public void openDatabase(Context context) {
        Log.d("dbtest","opendb");
        DatabaseHelper helper = new DatabaseHelper(context , databaseName, null, 3);
        //헬퍼를 생성함
        database = helper.getWritableDatabase(); //데이터베이스에 쓸수 있는 권한을 리턴해줌(갖게됨)
    }

    //테이블을 생성하는 함수
    public void createTable(String tableName) {
        if (database != null) {
            String sql = "create table " + tableName + "(_id integer PRIMARY KEY autoincrement, name text, age integer, mobile text)";
            database.execSQL(sql);
        } else {

        }
    }


    //메모 수정 후 기존에 있던 이미지를 삭제할때 사용하는 함수
    public void imagemodifydelete(int memoid , int deletenum) {
        if (database != null) {
                String sql = "delete from imagetable where memoid="+memoid+" AND imagenumber ="+deletenum;
                database.execSQL(sql);
        } else {

        }
    }

    //메모 내용을 삭제 할때 사용하는 함수 메모에 관한 내용을 지우고 이미지들을 삭제
    public void memodelete(int memoid) {
        //println("createTable() 호출됨.");
        if (database != null) {

            String sql = "delete from memotable where memo_id="+memoid;
            database.execSQL(sql);
            String sql2 = "delete from imagetable where memoid="+memoid;
            database.execSQL(sql2);

        } else {

        }
    }

    //메모의 데이터를 수정했을 때 기존의 것과 데이터를 바꿔준다.
    public void memoupdateData(int memoid, String title, String content, byte[] thumbnail, String day) {
        //println("createTable() 호출됨.");
        if (database != null) {
            Log.d("dbselectthum3",":"+thumbnail);

            ContentValues newValues = new ContentValues();
            newValues.put("title", title);
            newValues.put("content",content);
            newValues.put("thumbnail",thumbnail);
            newValues.put("day",day);
            database.update("memotable",newValues,"memo_id="+memoid, null);

        } else {

        }
    }


    //메모 데이터를 메모테이블에 추가
    public void insertmemoData(String title, String content, byte[] thumbnail, String day) {
        if (database != null) {
            String sql = "insert into memotable(title, content, thumbnail,day) values(?, ?, ?,?)";
            Object[] params = {title, content, thumbnail,day};
            database.execSQL(sql, params);
        } else {

        }
    }

    //이미지를 이미지 테이블에 추가한다.
    public void insertimageData(int memoid, int imagenumber, byte[] image) {
        if (database != null) {

            String sql = "insert into imagetable(memoid, imagenumber, image) values(?, ?, ?)";
            Object[] params = {memoid, imagenumber, image};
            database.execSQL(sql, params);

        } else {

        }
    }

    // 가장 최근 생성된 메모id에 대한 데이터를 읽어온다.
    public int selectmemoidData() {
        int memoid = 0;
        if (database != null) {
            String sql = "select memo_id from memotable ORDER BY memo_id DESC LIMIT 1;";
            Cursor cursor = database.rawQuery(sql, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                memoid = cursor.getInt(0);
            } cursor.close();
        }return memoid;
    }

    // 메모에 대한 데이터를 읽어온다.
    public void selectmemoData(ArrayList<Memodata> memoarray) {
        //println("selectData() 호출됨.");
        if (database != null) {
            String sql = "select memo_id,title, content, thumbnail, day from memotable ORDER BY day DESC ";
            Cursor cursor = database.rawQuery(sql, null);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                int memoid = cursor.getInt(0);
                String title = cursor.getString(1);
                String content = cursor.getString(2);
                byte[] thumbnail = cursor.getBlob(3);
                Bitmap thumbnailbitmap = null;
                if(thumbnail != null) {
                   thumbnailbitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                }

                String day = cursor.getString(4);
                Memodata memodata = new Memodata(memoid,title,content,thumbnailbitmap,day);
                memoarray.add(memodata);

            } cursor.close();

        }
    }

    //메모 보기에서 호출하는 메모 데이터 id가 지정되어 있다.
    public void selectmemoviewData(int memoid, ArrayList<String> dataarray) {
        if (database != null) {
            String sql = "select title, content, day from memotable where memo_id ="+ memoid ;
            Cursor cursor = database.rawQuery(sql, null);
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                dataarray.add( cursor.getString(0));
                dataarray.add( cursor.getString(1));
                dataarray.add(cursor.getString(2));
            } cursor.close();

        }
    }


    //이미지 데이터를 호출하는 함수
    public void selectimageData(int memoid ,ArrayList<Bitmap> imageviewgroup ,ArrayList<Integer> imageunmbergroup) {
        if (database != null) {
            String sql = "select imagenumber,image from imagetable where memoid="+memoid+" ORDER BY imagenumber ";
            Cursor cursor = database.rawQuery(sql, null);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                byte[] image = cursor.getBlob(1);
                Bitmap imagebitmap = BitmapFactory.decodeByteArray( image, 0, image.length ) ;
                imageviewgroup.add(imagebitmap);
                imageunmbergroup.add(cursor.getInt(0));
            } cursor.close();
        }
    }


   //헬퍼 클래스
    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //아직 sqlite를 쓴 적이 없을 때만 실행되서 테이블 생성
        @Override
        public void onCreate(SQLiteDatabase db) { //데이터베이스를 처음 생성해주는 경우(기존에 사용자가 데이터베이스를 사용하지 않았던 경우)

            String sql1 = "create table if not exists " + table1name + "( memo_id integer PRIMARY KEY autoincrement, title text, content text, thumbnail blob, day datetime)";
            db.execSQL(sql1);
            String sql2 = "create table if not exists " + table2name + "( image_id integer PRIMARY KEY autoincrement, memoid integer, imagenumber integer ,image blob )";
            db.execSQL(sql2);

        }

        //추후 버전 업그레이드에 사용
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (newVersion > 1) {

            }
        }
    }





}
