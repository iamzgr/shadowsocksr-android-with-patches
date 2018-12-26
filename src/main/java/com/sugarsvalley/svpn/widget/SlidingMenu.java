package com.sugarsvalley.svpn.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.sugarsvalley.svpn.utils.ScreenUtils;

public class SlidingMenu extends HorizontalScrollView
{
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;

    /**
     * 菜单距右侧距离
     */
    private int mMenuRightPadding = 20; //单位 dp
    /**
     * 菜单的宽度
     */
    private int mMenuWidth;
    private int mHalfMenuWidth;
    private int dragStartPostion;
    private long dragStartTime;

    private boolean once;

    private boolean isOpen = false;

    public SlidingMenu(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mScreenWidth = ScreenUtils.getScreenWidth(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        /**
         * 显示的设置一个宽度
         */
        if (!once)
        {
            LinearLayout wrapper = (LinearLayout) getChildAt(0);
            ViewGroup menu = (ViewGroup) wrapper.getChildAt(0);
            ViewGroup content = (ViewGroup) wrapper.getChildAt(1);
            // dp to px
            mMenuRightPadding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, mMenuRightPadding, content
                            .getResources().getDisplayMetrics());

            mMenuWidth = mScreenWidth - mMenuRightPadding;
            mHalfMenuWidth = mMenuWidth / 2;
            menu.getLayoutParams().width = mMenuWidth;
            content.getLayoutParams().width = mScreenWidth;

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (changed)
        {
            // 将菜单隐藏
            this.scrollTo(mMenuWidth, 0);
            isOpen = false;
            once = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                dragStartPostion = getScrollX();
                dragStartTime = System.currentTimeMillis();
                break;

                // Up时，进行判断，如果显示区域大于菜单宽度一半则完全显示，否则隐藏
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                long dur = System.currentTimeMillis() - dragStartTime;
                // less than 400ms, means sliding
                if (dur < 400) {
                    if (scrollX > dragStartPostion) {
                        if (scrollX - dragStartPostion > mHalfMenuWidth / 3) {
                            this.smoothScrollTo(mMenuWidth, 0);
                            isOpen = false;
                        } else {
                            this.smoothScrollTo(0, 0);
                            isOpen = true;
                        }
                    } else if (scrollX <= dragStartPostion) {
                        if (dragStartPostion - scrollX > mHalfMenuWidth / 3) {
                            this.smoothScrollTo(0, 0);
                            isOpen = true;
                        } else {
                            this.smoothScrollTo(mMenuWidth, 0);
                            isOpen = false;
                        }
                    }
                } else {
                    if (scrollX > mHalfMenuWidth)
                    {
                        this.smoothScrollTo(mMenuWidth, 0);
                        isOpen = false;
                    } else
                    {
                        this.smoothScrollTo(0, 0);
                        isOpen = true;
                    }
                }

                return true;
        }
        return super.onTouchEvent(ev);
    }

    public void openMenu()
    {
        if (isOpen)
            return;
        this.smoothScrollTo(0, 0);
        isOpen = true;
    }

    public void closeMenu()
    {
        if (isOpen)
        {
            this.smoothScrollTo(mMenuWidth, 0);
            isOpen = false;
        }
    }

    public void toggle()
    {
        if (isOpen)
        {
            closeMenu();
        } else
        {
            openMenu();
        }
    }
}