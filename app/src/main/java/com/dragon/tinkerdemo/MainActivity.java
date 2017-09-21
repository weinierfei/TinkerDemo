package com.dragon.tinkerdemo;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import cn.bingoogolapple.titlebar.BGATitleBar;

public class MainActivity extends BaseAppActivity {

    @Override
    protected void setFitsSystemWindowsUI() {
        super.setFitsSystemWindowsUI();
        ViewGroup contentFrameLayout = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View parentView = contentFrameLayout.getChildAt(0);
        if (parentView != null) {
            parentView.setFitsSystemWindows(true);
            contentFrameLayout.setClipToPadding(true);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onInitView() {

    }

    @Override
    protected void onInitData() {

    }

    @Override
    protected void initActionBar(ActionBar actionBar) {
        BGATitleBar titleBar = (BGATitleBar) getLayoutInflater().inflate(R.layout.title_bar_white, null);
        actionBar.setDisplayShowHomeEnabled(false);//去掉导航
        actionBar.setDisplayShowTitleEnabled(false);//去掉标题
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(titleBar);
        actionBar.setBackgroundDrawable(null);
        Toolbar parent = (Toolbar) titleBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        // 监听事件
        titleBar.setDelegate(new BGATitleBar.Delegate() {
            @Override
            public void onClickLeftCtv() {

            }

            @Override
            public void onClickTitleCtv() {

            }

            @Override
            public void onClickRightCtv() {

            }

            @Override
            public void onClickRightSecondaryCtv() {

            }
        });
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            QMUIStatusBarHelper.translucent(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
            QMUIStatusBarHelper.setStatusBarLightMode(this);
        }
    }
}
