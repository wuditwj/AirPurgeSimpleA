package com.njwyt.airpurgesimple.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.njwyt.airpurgesimple.R;
import com.njwyt.airpurgesimple.application.App;
import com.njwyt.airpurgesimple.databinding.ActivityMainBinding;
import com.njwyt.airpurgesimple.fragment.RoomFragment;
import com.njwyt.airpurgesimple.fragment.SettingFragment;
import com.njwyt.airpurgesimple.service.ClientService;
import com.njwyt.airpurgesimple.view.IconTextView;
import com.njwyt.airpurgesimple.view.NoScrollViewPager;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jason_samuel on 2018/2/24.
 */

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private NoScrollViewPager viewPager;
    private MainPagerAdapter mainPagerAdapter;
    private List<Fragment> fragments;
    private String[] iconsNormal;       // 未选中的iconfont
    private String[] iconsSelects;      // 选中时的iconfont
    private Toolbar toolbar;

    // 底部导航时间处理
    private int currentSelect = 0;
    private int[] itvs = {R.id.itv_room, R.id.itv_setting};
    private int[] tvs = {R.id.tv_room, R.id.tv_setting};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initdata();
        initLayout();
        initListener();
    }

    private void initdata() {

        iconsNormal = getResources().getStringArray(R.array.icon_unSelect);
        iconsSelects = getResources().getStringArray(R.array.icon_selected);
    }


    private void initLayout() {
        viewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        fragments = new ArrayList<>();
        fragments.add(RoomFragment.newInstance());
        fragments.add(SettingFragment.newInstance());
        mainPagerAdapter.setPages(fragments);
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setNoScroll(true); //禁止手动滑动
        viewPager.setOffscreenPageLimit(3);
        setSelect(0);
    }

    private void initListener() {

        findViewById(R.id.ll_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(0);
            }
        });

        findViewById(R.id.ll_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(1);
            }
        });
    }

    public void changeState(int index) {
        if (currentSelect != index) {
            cancelSelect();
            setSelect(index);
        }
    }

    /**
     * 恢复所有底部按钮式样（未选中）
     */
    public void cancelSelect() {
        IconTextView itvCurrent = (IconTextView) findViewById(itvs[currentSelect]);
        TextView tvCurrent = (TextView) findViewById(tvs[currentSelect]);
        itvCurrent.setText(iconsNormal[currentSelect]);
        //itvCurrent.setTextColor(getResources().getColor(R.color.tab_host_txt_color));
    }

    /**
     * 设置选中项
     *
     * @param i
     */
    public void setSelect(int i) {

        IconTextView itv = (IconTextView) findViewById(itvs[i]);
        TextView tv = (TextView) findViewById(tvs[i]);
        itv.setText(iconsSelects[i]);
        viewPager.setCurrentItem(i);
        currentSelect = i;
        itv.setTextColor(getResources().getColor(R.color.colorGreen));
    }


    /**
     * ViewPager适配器
     */
    private class MainPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> pages = null;

        public MainPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public MainPagerAdapter(FragmentManager fragmentManager, List<Fragment> pages) {
            super(fragmentManager);
            this.pages = pages;
        }

        public void setPages(List<Fragment> pages) {
            this.pages = pages;
        }

        public List<Fragment> getPages() {
            return pages;
        }

        @Override
        public Fragment getItem(int i) {
            return pages.get(i);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }

    @Override
    public void finish() {
        super.finish();
        Intent intent = new Intent(this, ClientService.class);
        stopService(intent);
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();        //调用双击退出函数
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            //overridePendingTransition(0, android.R.anim.push_right_out);
        }
    }
}
