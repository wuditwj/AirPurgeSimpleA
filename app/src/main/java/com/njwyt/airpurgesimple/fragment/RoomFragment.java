package com.njwyt.airpurgesimple.fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.njwyt.airpurgesimple.BR;
import com.njwyt.airpurgesimple.R;
import com.njwyt.airpurgesimple.activity.RoomDetailActivity;
import com.njwyt.airpurgesimple.adapter.UniversalAdapter;
import com.njwyt.airpurgesimple.biz.WeatherService;
import com.njwyt.airpurgesimple.broadcast.NetBroadcastReceiver;
import com.njwyt.airpurgesimple.content.Type;
import com.njwyt.airpurgesimple.databinding.FragmentRoomBinding;
import com.njwyt.airpurgesimple.databinding.ItemRoomBinding;
import com.njwyt.airpurgesimple.db.ReservoirHelper;
import com.njwyt.airpurgesimple.entity.Forecast;
import com.njwyt.airpurgesimple.entity.MessageEvent;
import com.njwyt.airpurgesimple.entity.Room;
import com.njwyt.airpurgesimple.entity.SocketFan;
import com.njwyt.airpurgesimple.entity.SystemMode;
import com.njwyt.airpurgesimple.entity.WeatherFreeInfo;
import com.njwyt.airpurgesimple.util.LocationUtil;
import com.njwyt.airpurgesimple.util.NetUtil;
import com.njwyt.airpurgesimple.view.ItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

/**
 * Created by jason_samuel on 2018/2/25.
 * 系统设置
 */

public class RoomFragment extends Fragment {

    private static String TAG = "RoomFragment";

    public static RoomFragment fragment;

    private FragmentRoomBinding binding;
    private UniversalAdapter<Room> universalAdapter;
    private ArrayList<Room> roomList;
    private ArrayList<Boolean> haveSensorList;      // 房间是否有传感器
    private int progressCount = 0;
    private int progressWidth = 0;

    private Handler progressHandler;
    private Handler reflashHandler;

    private WeatherService mWeatherService;
    private NetBroadcastReceiver mNetBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_room, container, false);
        EventBus.getDefault().register(this);
        initData();
        initRecyclerView();
        initView();
        initListener();
        registerBroadcast();

        // 显示上一次默认的本地数据
        getWeather(ReservoirHelper.INSTANCE.getCity());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // 左右切换屏幕时刷新本地位置，刷新成功后再获取当前城市数据
            if (getActivity() != null) {
                getLocation();
            }
        } else {
            // 窗体不显示时
        }
    }

    public static RoomFragment newInstance() {
        fragment = new RoomFragment();
        return fragment;
    }

    private void initData() {
        mWeatherService = new WeatherService();
        progressHandler = new Handler();
        reflashHandler = new Handler();

        // 读取房间列表
        roomList = new ArrayList<>();
        roomList.addAll(ReservoirHelper.INSTANCE.getRoomList());
        // 读取房间是否有传感器
        haveSensorList = new ArrayList<>();
        haveSensorList.addAll(ReservoirHelper.INSTANCE.getSocketData().getHaveSensorList());
        cleanRoomList();

        // 读取风机功率
        if (ReservoirHelper.INSTANCE.getFanList().size() > 0) {
            binding.tvFanPower.setText(ReservoirHelper.INSTANCE.getFanList().get(0).getTargetPower() + "");
        }

    }

    private void initRecyclerView() {

        universalAdapter = new UniversalAdapter<>(roomList, R.layout.item_room, BR.roomItem, new UniversalAdapter.AdapterView() {
            @Override
            public void getViewDataBinding(final UniversalAdapter.ViewHolder viewHolder, int position) {
                final Room room = roomList.get(position);
                ItemRoomBinding binding = (ItemRoomBinding) viewHolder.getBinding();

                binding.tvRoomName.setText(room.getRoomName());

                //"室外", "主卧", "次卧", "客房", "客厅", "厨房", "厕所一", "厕所二", "空闲", "空闲"
                if (room.getRoomName().equals("室外")) {
                    binding.tvRoom.setText(R.string.ic_house);
                } else if (room.getRoomName().equals("主卧") || room.getRoomName().equals("次卧") || room.getRoomName().equals("客房")) {
                    binding.tvRoom.setText(R.string.ic_bed);
                } else if (room.getRoomName().equals("客厅")) {
                    binding.tvRoom.setText(R.string.ic_drawing_room);
                } else if (room.getRoomName().equals("厨房")) {
                    binding.tvRoom.setText(R.string.ic_pot);
                } else if (room.getRoomName().equals("厕所一") || room.getRoomName().equals("厕所二")) {
                    binding.tvRoom.setText(R.string.ic_closestool);
                }

                switch (room.getVoc()) {
                    case 0:
                        binding.tvVoc.setText("优");
                        break;
                    case 1:
                        binding.tvVoc.setText("良");
                        break;
                    case 2:
                        binding.tvVoc.setText("中");
                        break;
                    case 3:
                        binding.tvVoc.setText("差");
                        break;
                }

                // 有传感器才提供房间的点击
                if (true) {
                    binding.rlRoot.setBackgroundResource(R.drawable.rounded_rectangle_selector);
                    binding.rlRoot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
                            intent.putExtra("room", room);
                            startActivity(intent);
                        }
                    });
                } else {
                    binding.rlRoot.setBackgroundResource(R.drawable.rounded_rectangle_disable_shape);
                    binding.rlRoot.setOnClickListener(null);
                }
            }
        });
    }

    private void initView() {
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        // 下拉刷新转圈样式
        //binding.recyclerview.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        binding.recyclerview.addItemDecoration(new ItemDecoration(40));
        binding.recyclerview.setAdapter(universalAdapter);
        binding.recyclerview.setLoadingMoreEnabled(false);
        binding.recyclerview.setPullRefreshEnabled(true);
        binding.recyclerview.getDefaultRefreshHeaderView().setArrowImageView(R.drawable.ic_arrow);


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                // 获取组件的宽度
                progressWidth = binding.llProgress.getWidth();
                int startTime = 20, stopTime = 40;

                // 按比例设置启动时间和停止时间
                LinearLayout.LayoutParams startParams = (LinearLayout.LayoutParams) binding.tvStartTime.getLayoutParams();
                // 获取当前控件的布局对象
                startParams.width = (int) ((float) progressWidth * ((float) startTime / (startTime + stopTime)));
                binding.tvStartTime.setLayoutParams(startParams);//将设置好的布局参数应用到控件中
                binding.tvStartTime.setText(startTime + "");

                LinearLayout.LayoutParams stopParams = (LinearLayout.LayoutParams) binding.tvStopTime.getLayoutParams();
                // 获取当前控件的布局对象
                stopParams.width = (int) ((float) progressWidth * ((float) stopTime / (startTime + stopTime)));
                binding.tvStopTime.setLayoutParams(stopParams);//将设置好的布局参数应用到控件中
                binding.tvStopTime.setText(stopTime + "");

                startProgressMove();
            }
        });
    }

    private void initListener() {

        binding.recyclerview.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                getLocation();
                binding.recyclerview.refreshComplete();
            }

            @Override
            public void onLoadMore() {

            }
        });
    }

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        mNetBroadcastReceiver = new NetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // 频段设置为获取连接状态
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetBroadcastReceiver.setNetEventChangeListener(new NetBroadcastReceiver.OnNetEventListener() {
            @Override
            public void onNetChange(int netMobile) {
                if (netMobile != NetUtil.NETWORK_NONE) {
                    getLocation();
                }
            }
        });
        getActivity().registerReceiver(mNetBroadcastReceiver, intentFilter);
    }

    /**
     * 底部进度条
     */
    private Runnable progressRunnbale = new Runnable() {
        @Override
        public void run() {

            try {
                // 获取起始结束两块的大小
                SystemMode systemMode = ReservoirHelper.INSTANCE.getSystemMode();

                if (systemMode.getOpenTimer() && systemMode.getRunningTime().getTime() > 0) {
                    // 开启定时模式
                    long usedTime = System.currentTimeMillis() / 1000L - systemMode.getRunningTime().getStartTime();    // 已用时间
                    long timeLenght = systemMode.getRunningTime().getTime();     // 时间总长度

                    binding.tvStopTime.setVisibility(View.GONE);
                    binding.tvStartTime.setVisibility(View.VISIBLE);
                    String text = getString(R.string.hint_count_down) + ((timeLenght - usedTime) / 60 + 1) + getString(R.string.minute);
                    binding.tvStartTime.setText(text);
                    binding.tvStartTime.getLayoutParams().width = progressWidth;

                    // 移动的进度条
                    // 已用时间 / 定时长度 = 当前占总长度的比例
                    float postionRatio = (float) usedTime / (float) timeLenght;
                    FrameLayout.LayoutParams progressParams = (FrameLayout.LayoutParams) binding.viewProgress.getLayoutParams();
                    // 获取当前控件的布局对象
                    progressParams.width = (int) (postionRatio * progressWidth);
                    binding.viewProgress.setLayoutParams(progressParams);//将设置好的布局参数应用到控件中

                } else if (systemMode.getSelectedMode() == 5) {
                    // 直排模式
                    binding.tvStopTime.setVisibility(View.GONE);
                    binding.tvStartTime.setVisibility(View.VISIBLE);
                    binding.tvStartTime.setText(R.string.infinite);
                    binding.tvStartTime.getLayoutParams().width = progressWidth;
                    binding.viewProgress.getLayoutParams().width = 0;

                } else if (systemMode.getSelectedMode() == 0) {
                    // 关机模式
                    binding.tvStartTime.setVisibility(View.GONE);
                    binding.tvStopTime.setVisibility(View.VISIBLE);
                    binding.tvStopTime.setText(R.string.shutdown);
                    binding.tvStopTime.getLayoutParams().width = progressWidth;
                    binding.viewProgress.getLayoutParams().width = 0;

                } else if (systemMode.getSelectedMode() == 1) {
                    // 智能模式
                    binding.tvStopTime.setVisibility(View.GONE);
                    binding.tvStartTime.setVisibility(View.VISIBLE);
                    binding.tvStartTime.setText(R.string.intelligent_mode);
                    binding.tvStartTime.getLayoutParams().width = progressWidth;
                    binding.viewProgress.getLayoutParams().width = 0;

                } else {
                    binding.tvStartTime.setVisibility(View.VISIBLE);
                    binding.tvStopTime.setVisibility(View.VISIBLE);
                    // 除甲醛selectMode是6，强中弱的是234
                    int startTime = systemMode.getWindsParmList().get(systemMode.getSelectedMode() - (systemMode.getSelectedMode() == 6 ? 3 : 2)).getNewWindTime();
                    int stopTime = systemMode.getWindsParmList().get(systemMode.getSelectedMode() - (systemMode.getSelectedMode() == 6 ? 3 : 2)).getIntervalTime();
                    if (startTime == 65535) {
                        binding.tvStartTime.setText(R.string.infinite);
                    } else {
                        binding.tvStartTime.setText(startTime + "");
                    }
                    if (stopTime == 65535) {
                        binding.tvStopTime.setText(R.string.infinite);
                    } else {
                        binding.tvStopTime.setText(stopTime + "");
                    }

                    // 修改页面上起始结束模块宽度
                    float startRatio = (float) startTime / (startTime + stopTime);
                    float stopRatio = (float) stopTime / (startTime + stopTime);

                    ViewGroup.LayoutParams tvStartParams = binding.tvStartTime.getLayoutParams();
                    tvStartParams.width = (int) (startRatio * progressWidth);
                    ViewGroup.LayoutParams tvStopParams = binding.tvStopTime.getLayoutParams();
                    tvStopParams.width = (int) (stopRatio * progressWidth);

                    // 计算当前所在位置，28800代表服务端需要加上的8个小时
                    float position = (startTime + stopTime) - (ReservoirHelper.INSTANCE.getFanList().get(0).getStopTime() - 28800 - System.currentTimeMillis() / 1000);

                    // 判断是否在开机区间
                    if (ReservoirHelper.INSTANCE.getFanList().get(0).getTargetPower() >= 40) {
                        position = position - stopTime;
                    }

                    // 移动的进度条
                    float postionRatio = position / (startTime + stopTime);  // 当前位置占总长度的比例
                    FrameLayout.LayoutParams progressParams = (FrameLayout.LayoutParams) binding.viewProgress.getLayoutParams();
                    // 获取当前控件的布局对象
                    progressParams.width = (int) (postionRatio * progressWidth);
                    binding.viewProgress.setLayoutParams(progressParams);//将设置好的布局参数应用到控件中
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressHandler.postDelayed(this, 500);     // 这里控制时间
        }
    };

    private void startProgressMove() {

        progressHandler.postDelayed(progressRunnbale, 100);
    }

    private void stopProgressMove() {

    }

    @Subscribe
    public void getRoomDataByEventbus(final MessageEvent<ArrayList<Room>> msg) {

        if (msg.getMessage() == Type.INSTANCE.getROOM_DATA()) {
            reflashHandler.post(new Runnable() {
                @Override
                public void run() {
                    //roomList = msg.getBody();
                    roomList.clear();
                    roomList.addAll(msg.getBody());
                    //cleanRoomList();
                    //universalAdapter.refresh(roomList);
                    //getView().requestLayout();
                }
            });
        }
    }

    @Subscribe
    public void getFanPowerDataByEventbus(final MessageEvent<ArrayList<SocketFan>> msg) {
        if (msg.getMessage() == Type.INSTANCE.getFAN_PWOER()) {
            reflashHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (msg.getBody().size() > 0) {
                        binding.tvFanPower.setText(msg.getBody().get(0).getTargetPower() + "");
                    }
                }
            });
        }
    }

    @Subscribe
    public void getHaveSensorListByEventbus(final MessageEvent<ArrayList<Boolean>> msg) {

        if (msg.getMessage() == Type.INSTANCE.getHAVE_SENSOR()) {
            reflashHandler.post(new Runnable() {
                @Override
                public void run() {
                    haveSensorList.clear();
                    haveSensorList.addAll(msg.getBody());
                    cleanRoomList();
                    universalAdapter.refresh(roomList);
                }
            });
        }
    }

    /**
     * 获取天气
     */
    private void getWeather(String city) {

        // 读取本地数据
        if (binding != null) {
            initWeather(ReservoirHelper.INSTANCE.getWeatherFreeInfo());
            binding.tvCityName.setText(city);
        }

        // 读取网络数据
        if (mWeatherService == null) {
            return;
        }
        mWeatherService.getWeather(city, new WeatherService.OnGetWeather() {
            @Override
            public void getWeather(@NonNull WeatherFreeInfo weatherFreeInfo) {
                initWeather(weatherFreeInfo);
                binding.tvCityName.setText(ReservoirHelper.INSTANCE.getCity());
                ReservoirHelper.INSTANCE.setWeatherFreeInfo(weatherFreeInfo);
            }
        });
    }

    /**
     * 获取位置
     */
    private void getLocation() {
        LocationUtil.getLocation(getActivity().getApplicationContext(), new LocationUtil.OnLocationGetListener() {
            @Override
            public void onLocationGet(BDLocation location) {
                String city = location.getCity();
                if (!city.equals("")) {
                    ReservoirHelper.INSTANCE.setCity(city);
                }
                getWeather(city);
            }
        });
    }

    /**
     * 通过天气数据刷新页面
     *
     * @param weatherFreeInfo
     */
    private void initWeather(WeatherFreeInfo weatherFreeInfo) {
        if (weatherFreeInfo.getData() != null) {
            Forecast todayWeather = weatherFreeInfo.getData().getForecast().get(0);
            String highTemp = todayWeather.getHigh();
            String lowTemp = todayWeather.getLow();
            highTemp = highTemp.substring(highTemp.indexOf(' ') + 1, highTemp.length());    //  去掉前面的文字
            lowTemp = lowTemp.substring(lowTemp.indexOf(' ') + 1, lowTemp.length());
            highTemp = highTemp.substring(0, highTemp.indexOf(".")) + "℃";
            lowTemp = lowTemp.substring(0, lowTemp.indexOf(".")) + "℃";
            binding.tvTemperature.setText(lowTemp + " ~ " + highTemp);
            binding.tvWeather.setText(todayWeather.getType());
        }
    }

    /**
     * 清理房间列表的多余项
     */
    private void cleanRoomList() {

        // 根据传感器数量清理
        /*while (roomList.size() > ReservoirHelper.INSTANCE.getSocketData().getSensorCount()) {
            roomList.remove(roomList.size() - 1);
        }*/

        /*haveSensorList.set(0, true);
        haveSensorList.set(1, true);
        haveSensorList.set(2, true);
        haveSensorList.set(5, true);
        haveSensorList.set(7, true);*/

        // 根据房间是否有传感器清理
        for (int i = 0; i < roomList.size() - 1; i++) {
            roomList.get(i).setHaveSensor(haveSensorList.get(i));
        }

        // romve未启动项
        ArrayList<Room> roomListTemp = new ArrayList<Room>();
        for (int i = 0; i < roomList.size(); i++) {
            Room r = roomList.get(i);
            if (r.getHaveSensor()) {
                roomListTemp.add(r);
            }
        }
        roomList.clear();
        roomList.addAll(roomListTemp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getActivity().unregisterReceiver(mNetBroadcastReceiver);
    }
}
