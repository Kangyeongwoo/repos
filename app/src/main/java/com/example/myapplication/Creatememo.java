package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class Creatememo extends AppCompatActivity {


    SQLiteManager sqLiteManager;
    ImageView image;
    EditText contenttext ;
    EditText titletext ;
    final int PICTURE_REQUEST_CODE = 100;
    final int CAPTURE_IMAGE_CODE = 200;
    final int URL_IMAGE_CODE = 300;
    ImageButton imageselectbt;
    ImageButton memosavebt;
    ArrayList<Bitmap> imagegroup;

    RecyclerView recyclerView;
    RecyclerViewAdapter_Creatememo adapter;
    LinearLayoutManager layoutManger;

    AlertDialog alert;

    int memoid ;
    ImageAsyncTask imageAsyncTask;

    ScrollView memocreatescrollview;

    boolean btclickcheck = false;
    boolean keyboardcheck = false;
    ImageButton memocreatebackbt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creatememo);

        memocreatescrollview = findViewById(R.id.memocreatescrollview);
        sqLiteManager = new SQLiteManager();
        titletext =(EditText)findViewById(R.id.titleedittext);
        contenttext = (EditText)findViewById(R.id.contentedittext);
        imageselectbt = (ImageButton)findViewById(R.id.imageselectbt);
        memosavebt =findViewById(R.id.memosavebt);
        memocreatebackbt = findViewById(R.id.memocreatbackbt);
        imagegroup = new ArrayList<Bitmap>();

        //뒤로가기 버튼
        memocreatebackbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //이미지 첨부 버튼 클릭시 이미지를 어떤 방식으로 가져올지 선택하는 함수
        imageselectbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoselectradio();
            }
        });

        //sqlite를 사용하기 위해 database에 접근하는 함수
        sqLiteManager.openDatabase(Creatememo.this);

        //저장 버튼 클릭시 데이터를 저장
        memosavebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //한번만 클릭되도록 조건 주기
                if(btclickcheck==false) {
                    btclickcheck = true;
                    //입력받은 제목
                    String title = titletext.getText().toString().trim();
                    //입력받은 내용
                    String content = contenttext.getText().toString().trim();

                    //입력받은 내용이 존재하면 데이터를 저장한다.
                    if (content.length() != 0) {
                        //첨부된 이미지중 가장 먼저 첨부된 것을 썸네일로 저장
                        //sqlite에 blob 형태로 저장할 것
                        byte[] thumbnaildata = null;
                        if (imagegroup.size() != 0) {
                            Bitmap thumbnail = imagegroup.get(0);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            thumbnaildata = stream.toByteArray();
                        } else {
                            Bitmap thumbnail = null;
                            thumbnaildata = null;
                        }
                        //현재 시간을 연원일 시간분으로 저장한다.
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String formatDate = sdfNow.format(date);

                        //데이터들을 메모 테이블에 저장한다.
                        sqLiteManager.insertmemoData(title, content, thumbnaildata, formatDate);
                        //지금 저장한 메모 테이블의 id 값을 받아온다
                        memoid = sqLiteManager.selectmemoidData();

                        //asynctask의 백그라운드에서 memoid를 활용하여 image들을 저장한다.
                        imageAsyncTask = new ImageAsyncTask(Creatememo.this, sqLiteManager, imagegroup);
                        imageAsyncTask.execute(memoid);
                        //이미지가 모두 저장되면 액티비티 종료


                    } else {
                        //내용이 하나도 없다면 토스트로 내용을 채워달라는 메세지 전달
                        Toast nocontenttoast = Toast.makeText(Creatememo.this,"내용을 입력해주세요", Toast.LENGTH_SHORT);
                        nocontenttoast.show();
                    }
                    btclickcheck = false;
                }else{
                    //여러번 클릭되는 중에는 실행안되게
                }
            }catch( Exception e){
                    //혹시 저장중 문제가 발생하면 나타나는 토스트메세지
                    btclickcheck = false;
                    Toast nocontenttoast = Toast.makeText(Creatememo.this,"다시 시도해 주세요", Toast.LENGTH_SHORT);
                    nocontenttoast.show();
                }
            }
        });


        //키보드가 생성 되는지 확인하는 함수
        final SoftKeyboardDectectorView softKeyboardDecector = new SoftKeyboardDectectorView(this);
        addContentView(softKeyboardDecector, new FrameLayout.LayoutParams(-1, -1));
        softKeyboardDecector.setOnShownKeyboard(new SoftKeyboardDectectorView.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                //키보드 등장할 때
                //내용 텍스트가 있는 스크롤 뷰 사이즈를 줄임
                keyboardcheck=true;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) memocreatescrollview.getLayoutParams();
                params.height = (int) getResources().getDimension(R.dimen.scrollsm);
                memocreatescrollview.setLayoutParams(params);
            }
        });

        softKeyboardDecector.setOnHiddenKeyboard(new SoftKeyboardDectectorView.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                // 키보드 사라질 때
                //내용 텍스트가 있는 스크롤 뷰 사이즈를 늘림
                keyboardcheck=false;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) memocreatescrollview.getLayoutParams();
                params.height = (int) getResources().getDimension(R.dimen.scrolllg);
                memocreatescrollview.setLayoutParams(params);
                //키보드에 리사이클러 뷰가 가려지는 경우 다시 리사이클러뷰를 생성해줌
                onResume();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        //이미지를 가져온 후 반영하기 위해서 onresume에서 리사이클러뷰 실행
        if(imagegroup.size() != 0){
            //이미지가 있으면 리사이클러뷰를 만들어서 이미지를 보여준다
            recyclerView = findViewById(R.id.imagecreaterecycle);
            adapter = new RecyclerViewAdapter_Creatememo(Creatememo.this, imagegroup,R.layout.row_creatememo_imageplus );
            recyclerView.setAdapter(adapter);
            layoutManger = new LinearLayoutManager(Creatememo.this);
            layoutManger.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManger);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    //이미지 첨부 앱틱비티로 갔다가 받아오는 데이터
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICTURE_REQUEST_CODE &&resultCode == RESULT_OK)
        {
            try {
                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    //이미지가 여러장일 때
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri urione = clipData.getItemAt(i).getUri();
                        //갤러리 이미지는 회전하기 때문에 이미지 경로에서 회전 값을 확인해준다.
                        String imagePath = getRealPathFromURI(urione);
                        ExifInterface exif = null;
                        try {

                            exif = new ExifInterface(imagePath);
                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int exifDegree = exifOrientationToDegrees(exifOrientation);


                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
                        Bitmap bitmap2 =rotate(bitmap, exifDegree);//회전 값을 이용해 이미지를 원래 상태로 회전시킴

                        //이미지의 사이즈가 너무 클 경우 저장에 문제가 발생하기 때문에 사이즈를 적절하게 조절해준다.
                        int width = bitmap2.getWidth();
                        int heigt = bitmap2.getHeight();
                        Bitmap resized = null;
                        if (bitmap2.getHeight() > 270) {
                            resized = Bitmap.createScaledBitmap(bitmap2, (width*270)/heigt , 270, true);
                        }else{
                            resized=bitmap2;
                        }

                        imagegroup.add(resized);//이미지 뷰에 비트맵 넣기


                    }
                } else if (uri != null) {
                    //이미지가 한장일때
                    String imagePath = getRealPathFromURI(uri);
                    //갤러리 이미지는 회전하기 때문에 이미지 경로에서 회전 값을 확인해준다.
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);

                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
                    Bitmap bitmap2 =rotate(bitmap, exifDegree);//회전 값을 이용해 이미지를 원래 상태로 회전시킴

                    //이미지의 사이즈가 너무 클 경우 저장에 문제가 발생하기 때문에 사이즈를 적절하게 조절해준다.
                    int width = bitmap2.getWidth();
                    int heigt = bitmap2.getHeight();
                    Bitmap resized = null;
                    if (bitmap2.getHeight() > 270) {
                        resized = Bitmap.createScaledBitmap(bitmap2, (width*270)/heigt , 270, true);
                    }else{
                        resized=bitmap2;
                    }
                    imagegroup.add(resized);//이미지 뷰에 비트맵 넣기
                }
            }catch (Exception e){
                //갤러리에서 이미지 가져오기 실패시 메세지
                Toast gallerytoast = Toast.makeText(Creatememo.this,"다시 시도해주세요", Toast.LENGTH_SHORT);
                gallerytoast.show();
            }

        }else if(requestCode == CAPTURE_IMAGE_CODE &&resultCode == RESULT_OK ){
            //카메라에서 사진 가져오기
            Bitmap camerabbitmap = (Bitmap) data.getExtras().get("data");
            Log.d("bitmapsize1",":"+camerabbitmap.getHeight());
            Log.d("bitmapsize2",":"+camerabbitmap.getWidth());
            //사진의 사이즈 조절
            if(camerabbitmap != null){
                int width = camerabbitmap.getWidth();
                int heigt = camerabbitmap.getHeight();
                Bitmap resized = null;
                if (camerabbitmap.getHeight() > 270) {
                    resized = Bitmap.createScaledBitmap(camerabbitmap, (width*270)/heigt , 270, true);
                }else{
                    resized=camerabbitmap;
                }
                imagegroup.add(resized);
            }
        }else if(requestCode == URL_IMAGE_CODE &&resultCode == RESULT_OK ){
            //url로 사진 가져오기
            Bitmap urlbitmap = (Bitmap) data.getParcelableExtra("urlbitmap");
            if(urlbitmap != null){
                //url 이미지 사이즈 조절
                int width = urlbitmap.getWidth();
                int heigt = urlbitmap.getHeight();
                Bitmap resized = null;
                if (urlbitmap.getHeight() > 270) {
                    resized = Bitmap.createScaledBitmap(urlbitmap, (width*270)/heigt , 270, true);
                }else{
                    resized=urlbitmap;
                }
                imagegroup.add(resized);
            }
        }
    }


    //사진 첨부 선택 함수
    //갤러리에서 가져오기, 카메라에서 촬영, URL로 가져오기 세가지 방식
    void photoselectradio(){

        final CharSequence[] Photoselet = {"갤러리에서 가져오기", "카메라 촬영" ,"URL로 가져오기"};

        AlertDialog.Builder altbld = new AlertDialog.Builder(this);

        altbld.setTitle("사진 첨부");

        altbld.setSingleChoiceItems(Photoselet, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Creatememo.this, Photoselet[which]+"가 선택" ,Toast.LENGTH_SHORT).show();
                if(which==0){
                    //갤러리로 이동후 사진을 가져온다
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    //사진을 여러개 선택할수 있도록 한다
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*");
                    alert.dismiss();
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"),  PICTURE_REQUEST_CODE);

                }else if(which==1){

                    //카메로 이동후 사진을 가져온다
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    alert.dismiss();
                    startActivityForResult(cameraIntent, CAPTURE_IMAGE_CODE);

                }else{
                    //URL 입력받아 가져오기
                    alert.dismiss();
                    //URL을 입력받을 다이얼로그 생성
                    AlertDialog.Builder ad = new AlertDialog.Builder(Creatememo.this);
                    ad.setTitle("URL로 이미지 가져오기");       // 제목 설정
                    ad.setMessage("이미지 URL을 입력해주세요");   // 내용 설정
                    //다이얼로그에 edittext 생성
                    final EditText et = new EditText(Creatememo.this);
                    ad.setView(et);
                    String ok = "확인";
                    String cancel = "취소";
                    ad.setPositiveButton(ok, null);
                    ad.setNegativeButton(cancel, null);

                    //다이얼로그 취소 버튼을 눌렀을 때도 다이얼로그가 사라지지 않게 버튼 구현
                    final AlertDialog alertDialog = ad.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {

                            Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = et.getText().toString().trim();
                                    if(url.length()!=0) {
                                        try {

                                            //url이 입력되어 있으면 이미지 확인 페이지로 넘어간다.
                                            Intent tourlimagecheck = new Intent(Creatememo.this, URLimagecheck.class);
                                            tourlimagecheck.putExtra("URL", url);
                                            startActivityForResult(tourlimagecheck, URL_IMAGE_CODE);
                                            alertDialog.dismiss();

                                        }catch (Exception e){
                                            //URL이 검색되지 않으면 토스트 생성
                                            Toast gallerytoast = Toast.makeText(Creatememo.this,"URL을 다시 확인해주세요", Toast.LENGTH_SHORT);
                                            gallerytoast.show();
                                        }

                                    }else{
                                        //URL이 비어있어도 토스트 생성
                                        Toast gallerytoast = Toast.makeText(Creatememo.this,"URL을 다시 확인해주세요", Toast.LENGTH_SHORT);
                                        gallerytoast.show();
                                    }
                                }
                            });
                            //취소버튼 생성
                            Button cancel = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });
                        }
                    });
                    alertDialog.show();
                }
            }
        });
        //갤러리 ,카메라 ,URL 다이얼로그 생성
        alert = altbld.create();
        alert.show();
    }

    //갤러리 이미지 회전 값을 확인하는 함수
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //갤러리 이미지의 절대경로를 찾는 함수
    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    //갤러리 이미지를 회전시키는 함수
    private Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    @Override
    public void onBackPressed() { super.onBackPressed(); }


}







