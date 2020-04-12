package com.mcdev.memery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class NonSwipeableViewpager extends ViewPager {

    public NonSwipeableViewpager(@NonNull Context context) {
        super(context);
        allowSmoothScrolling();     //allowing smooth scrolling
    }

    public NonSwipeableViewpager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        allowSmoothScrolling();     //allowing smooth scrolling
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*return super.onInterceptTouchEvent(ev);*/
        return false;       //returning false to disable swiping to switch between pages
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*return super.onTouchEvent(ev);*/
        return false;       //returning false to disable swiping to switch between pages
    }

    //below code is added to allow smooth scrolling when switching pages when not swiping
    private void allowSmoothScrolling(){
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class MyScroller extends Scroller {
        MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, duration);
        }
    }
}
