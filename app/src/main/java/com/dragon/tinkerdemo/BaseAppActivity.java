package com.dragon.tinkerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

/**
 * Description:Activity基类
 *
 * @author: guoyongping
 * @date: 2016/4/22 13:44
 */
public abstract class BaseAppActivity extends AppCompatActivity {

    private static final String TAG = "BaseAppActivity";

    protected ActionBar mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
        }
        //super.onCreate之前执行
        onBeforeSuperOnCreate(savedInstanceState);

        super.onCreate(savedInstanceState);
        //去掉窗口默认背景颜色,减少绘制次数
        getWindow().setBackgroundDrawable(null);
        //预留夜间模式
        setAppBaseThemeMode();
        //如果没有actionBar则不显示
        if (!hasActionBar()) {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        //设置布局之前执行
        onBeforeSetContentLayout();

        //设置布局
        if (getLayoutResource() != 0) {
            setContentView(getLayoutResource());
        }

        //设定状态栏的颜色，当版本大于等于4.4时起作用
        setStatusBarColor();

        //获取actionBar
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            //actionBar消除阴影
            mActionBar.setElevation(0);
        }

        setFitsSystemWindowsUI();

        if (hasActionBar()) {
            initActionBar(mActionBar);
        }

        //恢复状态等
        init(savedInstanceState);
        //初始化view
        onInitView();
        //加载数据开始
        onInitData();
    }

    /**
     * 通过代码设置fitsSystemWindows
     */
    protected void setFitsSystemWindowsUI() {

    }

    /**
     * super.onCreate(savedInstanceState)之前操作
     */
    protected void onBeforeSuperOnCreate(Bundle savedInstanceState) {
    }

    /**
     * 设置夜间模式等
     */
    protected void setAppBaseThemeMode() {
    }

    /**
     * 如果需要在加载布局之前做一些操作，则重写该方法
     */
    protected void onBeforeSetContentLayout() {
    }

    /**
     * 如果需要初始化bundle对象，则重写该方法
     *
     * @param savedInstanceState 需要保存的当前状态
     */
    protected void init(Bundle savedInstanceState) {
    }

    /**
     * 绑定布局
     *
     * @return 资源文件id
     */
    protected abstract int getLayoutResource();

    /**
     * 初始化布局
     */
    protected abstract void onInitView();

    /**
     * 加载数据
     */
    protected abstract void onInitData();

    /**
     * 是否有actionBar，默认有
     *
     * @return true-有，false-没有
     */
    protected boolean hasActionBar() {
        return true;
    }

    /**
     * 初始化actionBar
     *
     * @param actionBar actionBar
     */
    protected abstract void initActionBar(ActionBar actionBar);


    /**
     * 跳转Activity
     *
     * @param paramClass  跳转目标Activity
     * @param paramBundle 需要携带的参数
     */
    @SuppressWarnings("rawtypes")
    public void startActivity(Class paramClass, Bundle paramBundle) {
        Intent localIntent = new Intent(this, paramClass);
        if (paramBundle != null) {
            localIntent.putExtras(paramBundle);
        }
        startActivity(localIntent);
    }

    /**
     * 跳转Activity,需要回传值
     *
     * @param paramClass  跳转目标Activity
     * @param paramBundle 需要携带的参数
     * @param requestCode 请求码
     */
    @SuppressWarnings("rawtypes")
    public void startActivityForResult(Class paramClass, Bundle paramBundle, int requestCode) {
        Intent localIntent = new Intent(this, paramClass);
        if (paramBundle != null) {
            localIntent.putExtras(paramBundle);
        }
        startActivityForResult(localIntent, requestCode);
    }


    /**
     * 设置StatusBar颜色值
     */
    protected abstract void setStatusBarColor();
}
