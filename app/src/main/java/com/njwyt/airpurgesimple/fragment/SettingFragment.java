package com.njwyt.airpurgesimple.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.njwyt.airpurgesimple.R;
import com.njwyt.airpurgesimple.activity.DataManageActivity;
import com.njwyt.airpurgesimple.activity.SettingModeActivity;
import com.njwyt.airpurgesimple.activity.SettingSystemActivity;
import com.njwyt.airpurgesimple.content.Type;
import com.njwyt.airpurgesimple.databinding.FragmentSettingBinding;
import com.njwyt.airpurgesimple.db.ReservoirHelper;
import com.njwyt.airpurgesimple.entity.MessageEvent;
import com.njwyt.airpurgesimple.entity.SystemMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by jason_samuel on 2018/2/25.
 * 系统设置
 */

public class SettingFragment extends Fragment implements View.OnClickListener {

    public static SettingFragment fragment;
    private FragmentSettingBinding binding;
    private Handler reflashHandler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        EventBus.getDefault().register(this);
        reflashHandler = new Handler();
        onClick();
        initToolBar();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refresh();
        } else {
            // 窗体不显示时
        }
    }

    private void onClick() {
        binding.systemSet.setOnClickListener(this);
        binding.modeSet.setOnClickListener(this);
        binding.dataSet.setOnClickListener(this);

    }

    public static SettingFragment newInstance() {
        fragment = new SettingFragment();
        return fragment;
    }

    private void initToolBar() {
        // 去除Toolbar的默认标题
        binding.toolbar.setTitle("");
        // 使用自己的布局
        binding.titleTextView.setText(R.string.setting);
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
    }

    /**
     * 刷新当前模式
     */
    private void refresh() {

        SystemMode systemMode = ReservoirHelper.INSTANCE.getSystemMode();
        if (systemMode.getOpenTimer() && systemMode.getRunningTime().getTime() > 0) {
            // 显示定时模式
            binding.currentMode.setText(R.string.timer2);
        } else {
            // 读取当前模式显示
            switch (systemMode.getSelectedMode()) {
                case 0:
                    binding.currentMode.setText(R.string.shutdown);
                    break;
                case 1:
                    binding.currentMode.setText(R.string.intelligent_mode);
                    break;
                case 2:
                    binding.currentMode.setText(R.string.strong);
                    break;
                case 3:
                    binding.currentMode.setText(R.string.medium);
                    break;
                case 4:
                    binding.currentMode.setText(R.string.weak);
                    break;
                case 5:
                    binding.currentMode.setText(R.string.force);
                    break;
                case 6:
                    binding.currentMode.setText(R.string.formalde_removal);
                    break;
            }
        }
    }
    /**
     * 从Application实时获取数据
     */
    @Subscribe
    public void getSystemModeByEventBus(MessageEvent<SystemMode> msg) {
        if (msg.getMessage()== Type.INSTANCE.getSYSTEM_MODE()) {
            reflashHandler.post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //系统设置
            case R.id.system_set:
                startActivity(new Intent(getActivity(), SettingSystemActivity.class));
                break;
            //模式设置
            case R.id.mode_set:
                startActivity(new Intent(getActivity(), SettingModeActivity.class));
                break;
            //数据管理
            case R.id.data_set:
                startActivity(new Intent(getActivity(), DataManageActivity.class));
                break;
        }
    }
}
