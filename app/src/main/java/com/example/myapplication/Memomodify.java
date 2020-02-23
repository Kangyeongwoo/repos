package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class Memomodify extends AppCompatActivity {

    int memoid;
    String title;
    String content;
    ArrayList<Bitmap> imagegroup;
    ArrayList<Integer> imagenumbergroup;
    ArrayList<Integer> newimagenumbergroup;
    ArrayList<Bitmap> newimagebitmapgroup;
    ArrayList<Integer> deleteimagenumbergroup;


    SQLiteManager sqLiteManager;

    EditText titlemodify;
    EditText contentmodify;

    RecyclerView recyclerView;
    RecyclerViewadapter_Memomodify adapter;
    LinearLayoutManager layoutManger;

    final int PICTURE_REQUEST_CODE = 100;
    final int CAPTURE_IMAGE_CODE = 200;
    final int URL_IMAGE_CODE = 300;
    AlertDialog alert;

    ImageButton imagemodifyselectbt;
    ImageButton memomodifysavebt;
    ImageButton memomodifydeletebt;
    ImageButton memomodifybackbt;

    ImagemodifyAsyncTask imagemodifyAsyncTask;

    ScrollView memomodifyscrollview;
    Memowindow mw;
    boolean btclickcheck = false;
    boolean keyboardcheck = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memomodify);

        mw = (Memowindow)Memowindow._memowindow_activity;

        memomodifyscrollview=findViewById(R.id.memomodifyscrollview);
        titlemodify=findViewById(R.id.titlemodifyedittext);
        contentmodify = findViewById(R.id.contentmodifyedittext);

        Intent intent = getIntent();
        memoid = intent.getExtras().getInt("memoid");
        title = intent.getExtras().getString("title");
        content = intent.getExtras().getString("content");

        imagegroup = new ArrayList<>();
        imagenumbergroup = new ArrayList<>();

        newimagenumbergroup = new ArrayList<>();
        newimagebitmapgroup = new ArrayList<>();
        deleteimagenumbergroup = new ArrayList<>();

        sqLiteManager = new SQLiteManager();
        sqLiteManager.openDatabase(Memomodify.this);

        titlemodify.setText(title);
        contentmodify.setText(content);


        //키보드가 생성 되는지 확인하는 함수
        final SoftKeyboardDectectorView softKeyboardDecector = new SoftKeyboardDectectorView(this);
        addContentView(softKeyboardDecector, new FrameLayout.LayoutParams(-1, -1));
        softKeyboardDecector.setOnShownKeyboard(new SoftKeyboardDectectorView.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                //키보드 등장할 때
                //내용 텍스트가 있는 스크롤 뷰 사이즈를 줄임
                Log.d("keybo","생성");
                keyboardcheck = true;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) memomodifyscrollview.getLayoutParams();
               params.height = (int) getResources().getDimension(R.dimen.scrollsm);
                memomodifyscrollview.setLayoutParams(params);

            }
        });

        softKeyboardDecector.setOnHiddenKeyboard(new SoftKeyboardDectectorView.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                // 키보드 사라질 때
                //내용 텍스트가 있는 스크롤 뷰 사이즈를 늘림
                Log.d("keybo","삭제");
                keyboardcheck = false;
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) memomodifyscrollview.getLayoutParams();
               params.height = (int) getResources().getDimension(R.dimen.scrolllg);
               memomodifyscrollview.setLayoutParams(params);
               //키보드 때문에 사라진 리사이클러뷰 재생성
               onResume();
            }
        });

        //백버튼
        memomodifybackbt=findViewById(R.id.memomodifybackbt);
        memomodifybackbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //편집도중 메모 삭제
        memomodifydeletebt = findViewById(R.id.memomodifydeletebt);
        memomodifydeletebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메모 삭제를 확인하는 다이얼로그 생성
                AlertDialog.Builder ad2 = new AlertDialog.Builder(Memomodify.this);
                ad2.setTitle("메모 삭제 확인");       // 제목 설정
                ad2.setMessage("정말로 메모를 삭제하시겠습니까?");   // 내용 설정
                ad2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mw.finish();
                        //sqlite에서 데이터 삭제
                        sqLiteManager.memodelete(memoid);
                        finish();
                    }
                });
                ad2.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                ad2.show();
            }
        });

        //이미지 첨부 선택
        imagemodifyselectbt = findViewById(R.id.imagemodifyselectbt);
        imagemodifyselectbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoselectradio();
            }
        });

        //수정 후 저장
        memomodifysavebt = findViewById(R.id.memomodifysavebt);
        memomodifysavebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //저장 버튼을 한번 만 누를수 있도록 플래그 사용
                if(btclickcheck==false) {
                    btclickcheck = true;
                    //제목 데이터
                    String title = titlemodify.getText().toString().trim();
                    //내용 데이터
                    String content = contentmodify.getText().toString().trim();

                    //입력받은 내용이 존재하면 데이터를 저장한다.
                    if (content.length() != 0) {
                        byte[] thumbnaildata2 = null;
                        Log.d("dbselectthum1", ":" + imagegroup.size());
                        if (imagegroup.size() != 0) {
                            Bitmap thumbnail = imagegroup.get(0);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            thumbnaildata2 = stream.toByteArray();
                        } else {
                            Bitmap thumbnail = null;
                            thumbnaildata2 = null;
                        }
                        //수정된 시간 계산 후 저장
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String formatDate = sdfNow.format(date);

                        sqLiteManager.memoupdateData(memoid, title, content, thumbnaildata2, formatDate);

                        //키보드를 강제로 종료
                        if(keyboardcheck==true) {
                            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        }

                        //asynctask에서 이미지 저장 및 삭제
                        imagemodifyAsyncTask = new ImagemodifyAsyncTask(Memomodify.this, sqLiteManager, newimagebitmapgroup, newimagenumbergroup, deleteimagenumbergroup);
                        imagemodifyAsyncTask.execute(memoid);

                    } else {
                        //내용이 하나도 없다면 토스트로 내용을 채워달라는 메세지 전달
                        Toast nocontenttoast = Toast.makeText(Memomodify.this,"내용을 입력해주세요", Toast.LENGTH_SHORT);
                        nocontenttoast.show();
                    }
                    btclickcheck=false;
                }else{
                    //여러번 클릭되는 중에는 실행안되게
                }
            }catch (Exception e){
                    //혹시 저장중 오류가 발생하면 나타나는 메세지
                    btclickcheck=false;
                    Toast nocontenttoast = Toast.makeText(Memomodify.this,"다시 시도해 주세요", Toast.LENGTH_SHORT);
                    nocontenttoast.show();
                }
            }
        });

        sqLiteManager.selectimageData(memoid,imagegroup,imagenumbergroup);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //이미지 리사이클러뷰 생성
        recyclerView = findViewById(R.id.imagemodifyrecycle);
        adapter = new RecyclerViewadapter_Memomodify(Memomodify.this, R.layout.row_creatememo_imageplus, imagegroup, imagenumbergroup,newimagenumbergroup,newimagebitmapgroup ,deleteimagenumbergroup );
        recyclerView.setAdapter(adapter);
        layoutManger = new LinearLayoutManager(Memomodify.this);
        layoutManger.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManger);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }


    //이미지 첨부 앱틱비티로 갔다가 받아오는 데이터 새로 이미지 생성 하기 때문에 new이미지 그룹에 추가
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICTURE_REQUEST_CODE &&resultCode == RESULT_OK)
        {

            try {
                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();
                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri urione = clipData.getItemAt(i).getUri();
                        int lastnum=0;
                        if(imagenumbergroup.size()!=0){
                            lastnum = imagenumbergroup.get(imagenumbergroup.size() - 1)+1;
                        }
                        //절대 경로와 갤러리 이미지 회전값 찾기
                        String imagePath = getRealPathFromURI(urione);
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(imagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int exifDegree = exifOrientationToDegrees(exifOrientation);

                        Bitmap bitmap1 = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
                        Bitmap bitmap2 =rotate(bitmap1, exifDegree);//비트맵 회전

                        //이미지 리사이즈
                        int width = bitmap2.getWidth();
                        int heigt = bitmap2.getHeight();
                        Bitmap resized = null;
                        if (bitmap2.getHeight() > 270) {
                            resized = Bitmap.createScaledBitmap(bitmap2, (width*270)/heigt , 270, true);
                        }else{
                            resized = bitmap2;
                        }

                        imagegroup.add(resized);//이미지 뷰에 비트맵 넣기
                        imagenumbergroup.add(lastnum);
                        newimagebitmapgroup.add(resized);
                        newimagenumbergroup.add(lastnum);
                    }
                } else if (uri != null) {

                    int lastnum=0;
                    if(imagenumbergroup.size()!=0){
                        lastnum = imagenumbergroup.get(imagenumbergroup.size() - 1)+1;
                    }
                    //이미지 경로와 회전값 확인
                    String imagePath = getRealPathFromURI(uri);
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);

                    Bitmap bitmap1 = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
                    Bitmap bitmap2 =rotate(bitmap1, exifDegree);//이미지 회전 시키기

                    //이미지 리사이즈
                    int width = bitmap2.getWidth();
                    int heigt = bitmap2.getHeight();
                    Bitmap resized = null;
                    if (bitmap2.getHeight() > 270) {
                        resized = Bitmap.createScaledBitmap(bitmap2, (width*270)/heigt , 270, true);
                    }else{
                        resized = bitmap2;
                    }

                    imagegroup.add(resized);
                    imagenumbergroup.add(lastnum);
                    newimagebitmapgroup.add(resized);
                    newimagenumbergroup.add(lastnum);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(requestCode == CAPTURE_IMAGE_CODE &&resultCode == RESULT_OK ){
            //&& data.hasExtra("data")
            Bitmap camerabbitmap = (Bitmap) data.getExtras().get("data");
            if(camerabbitmap != null){
                 //카메라 이미지 리사이즈
                int width = camerabbitmap.getWidth();
                int heigt = camerabbitmap.getHeight();
                Bitmap resized = null;
                if (camerabbitmap.getHeight() > 270) {
                    resized = Bitmap.createScaledBitmap(camerabbitmap, (width*270)/heigt , 270, true);
                }else{
                    resized = camerabbitmap;
                }

                int lastnum=0;
                if(imagenumbergroup.size()!=0){
                    lastnum = imagenumbergroup.get(imagenumbergroup.size() - 1)+1;
                }

                imagegroup.add(resized);
                imagenumbergroup.add(lastnum);
                newimagebitmapgroup.add(resized);
                newimagenumbergroup.add(lastnum);
            }

        }else if(requestCode == URL_IMAGE_CODE &&resultCode == RESULT_OK ){

            Bitmap urlbitmap = (Bitmap) data.getParcelableExtra("urlbitmap");
            if(urlbitmap != null){
                //url이미지 리사이즈
                int width = urlbitmap.getWidth();
                int heigt = urlbitmap.getHeight();
                Bitmap resized = null;
                if (urlbitmap.getHeight() > 270) {
                    resized = Bitmap.createScaledBitmap(urlbitmap, (width*270)/heigt , 270, true);
                }else{
                    resized = urlbitmap;
                }

                int lastnum=0;
                if(imagenumbergroup.size()!=0){
                    lastnum = imagenumbergroup.get(imagenumbergroup.size() - 1)+1;
                }
                imagegroup.add(resized);
                imagenumbergroup.add(lastnum);
                newimagebitmapgroup.add(resized);
                newimagenumbergroup.add(lastnum);
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
                Toast.makeText(Memomodify.this, Photoselet[which]+"가 선택" ,Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder ad = new AlertDialog.Builder(Memomodify.this);
                    ad.setTitle("URL로 이미지 가져오기");       // 제목 설정
                    ad.setMessage("이미지 URL을 입력해주세요");   // 내용 설정
                    //다이얼로그에 edittext 생성
                    final EditText et = new EditText(Memomodify.this);
                    ad.setView(et);
                    String ok = "확인";
                    String cancel = "취소";
                    ad.setPositiveButton(ok, null);
                    ad.setNegativeButton(cancel, null);

                    //취소 버튼 눌러도 다이얼로그가 사라지지 않도록 버튼 생성
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
                                            Intent tourlimagecheck = new Intent(Memomodify.this, URLimagecheck.class);
                                            tourlimagecheck.putExtra("URL", url);
                                            startActivityForResult(tourlimagecheck, URL_IMAGE_CODE);
                                            alertDialog.dismiss();

                                        }catch (Exception e){
                                            //URL이 검색되지 않으면 토스트 생성
                                            Toast gallerytoast = Toast.makeText(Memomodify.this,"URL을 다시 확인해주세요", Toast.LENGTH_SHORT);
                                            gallerytoast.show();
                                        }

                                    }else{
                                        //URL이 비어있어도 토스트 생성
                                        Toast gallerytoast = Toast.makeText(Memomodify.this,"URL을 다시 확인해주세요", Toast.LENGTH_SHORT);
                                        gallerytoast.show();
                                    }
                                }
                            });
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

    //이미지 회전 값 찾는 함수
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

    //이미지 절대경로 찾는 함수
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

    //이미지 회전시키는 함수
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
