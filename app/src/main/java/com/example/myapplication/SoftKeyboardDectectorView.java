package com.example.myapplication;

import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

//키보드가 올라왔는지 안올라왔는지 확인하는 함수
//키보드가 있고 없음에 따라 editext의 끝 내용이 보이도록 사이즈를 조절
public class SoftKeyboardDectectorView extends View {

    private boolean mShownKeyboard;
    private OnShownKeyboardListener mOnShownSoftKeyboard;
    private OnHiddenKeyboardListener onHiddenSoftKeyboard;


    public SoftKeyboardDectectorView(Context context) {
        this(context, null);
    }

    public SoftKeyboardDectectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Activity activity = (Activity)getContext();
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
      //  int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        Display screen = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screen.getSize(size);
        int screenHeight = size.y;
        int diffHeight = (screenHeight - statusBarHeight) - h;
        if (diffHeight > 100 && !mShownKeyboard) { // 모든 키보드는 100px보다 크다고 가정
            mShownKeyboard = true;
            onShownSoftKeyboard();
        } else if (diffHeight < 100 && mShownKeyboard) {
            mShownKeyboard = false;
            onHiddenSoftKeyboard();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void onHiddenSoftKeyboard() {
        if (onHiddenSoftKeyboard != null)
            onHiddenSoftKeyboard.onHiddenSoftKeyboard();
    }

    public void onShownSoftKeyboard() {
        if (mOnShownSoftKeyboard != null)
            mOnShownSoftKeyboard.onShowSoftKeyboard();
    }

    public void setOnShownKeyboard(OnShownKeyboardListener listener) {
        mOnShownSoftKeyboard = listener;
    }

    public void setOnHiddenKeyboard(OnHiddenKeyboardListener listener) {
        onHiddenSoftKeyboard = listener;
    }

    public interface OnShownKeyboardListener {
        public void onShowSoftKeyboard();
    }

    public interface OnHiddenKeyboardListener {
        public void onHiddenSoftKeyboard();
    }
}

