package com.example.cooorem.bdmap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private ProgressBar mProgressBar;
    private TextView mTextView,mTextLocationNear;
    private DbService.DbBinder dbBinder;
    private EditText edDescription;
    private double longitude,latitude;
    private boolean isLacateFail=false;
    private String locationNear;
    private List<Integer> dbArray;
    private int arrayNum;
    private ServiceConnection connection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            dbBinder=(DbService.DbBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    public Handler getHandler(){
        return handler;
    }
    boolean isFirstLoc = true;// 是否首次定位
    public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            int db=msg.what;
            mTextView.setText(Integer.toString(db) + "分贝");
            mProgressBar.setProgress(db);
            if(arrayNum<1000){
                ++arrayNum;
                dbArray.add(db);
            }else {
                dbArray.remove(0);
                dbArray.add(db);
            }
        }
    };
    MapView mMapView;
    BaiduMap mBaiduMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Button btn= (Button) findViewById(R.id.btnRecord);
        btn.setOnClickListener(this);
        Button btnCheck= (Button) findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(this);
        edDescription= (EditText) findViewById(R.id.edDescription);
        mProgressBar= (ProgressBar) findViewById(R.id.pbDB);
        dbArray=new LinkedList<>();
        mProgressBar.setProgress(0);
        mTextView= (TextView) findViewById(R.id.tvNum);
        mTextView.setText("0");
        mTextLocationNear= (TextView) findViewById(R.id.tvLocationNear);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 开启定位图层
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        initLocation();
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));
        mLocClient.start();
        if(mLocClient.isStarted()){
            Log.d("MainActivity", mLocClient.getLastKnownLocation().getCity() + "  Location Client Started");

        }else{
            Log.d("MainActivity", "Location Client Starting fail");
        }

    }
    @Override
    protected void onPause() {
        arrayNum=0;
        mMapView.onPause();
        Log.d("MainActivity", "onPause");
        dbArray.clear();
        super.onPause();
    }

    @Override
    protected void onStart() {
        DbService.startSevice(MainActivity.this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        Intent stopIntent= new Intent(this, DbService.class);
        stopService(stopIntent);
        super.onStop();
    }
    @Override
    protected void onResume() {
        arrayNum=0;
        mMapView.onResume();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }



    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=3000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocClient.setLocOption(option);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRecord:
                if(isLacateFail){
                    Toast.makeText(this,"定位失败，记录无法保存",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedWriter writer=null;
                        try{
                            FileOutputStream fos=openFileOutput("data", Context.MODE_APPEND);
                            writer=new BufferedWriter(new OutputStreamWriter(fos));
                            writer.write(getData());
                            Log.d("MainActivity","Write Succeed");
                        }catch(FileNotFoundException e){
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally{
                            try{
                                if(writer!=null)writer.close();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                Toast.makeText(this,"记录已保存",Toast.LENGTH_SHORT).show();
                edDescription.setText(null);
                break;
            case R.id.btnCheck:
                Intent in=new Intent(this,CheckActivity.class);
                startActivity(in);
                break;
        }
    }
    private String getData() {
        StringBuffer sb=new StringBuffer();
        Date date=new Date();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(date);
        sb.append(edDescription.getText().toString()+"\n###\n");
        sb.append(""+getAverage()+"\n");
        sb.append(""+longitude+"\n"+latitude+"\n");
        sb.append(locationNear + "\n");
        sb.append(time+"\n");
        Log.d("MainActivity", getAverage());
        return sb.toString();
    }
    private String getAverage(){
        if(dbArray.isEmpty())
            return mTextView.getText().toString();
        double sum=0;
        int num=0;
        for(int i:dbArray){
            ++num;
            sum+=i;
        }
        return Integer.toString((int)Math.round(sum/num))+"分贝";
    }
    public class MyLocationListener implements BDLocationListener {

        public void onReceivePoi(BDLocation poiLocation) {
        }

        @Override
        public void onReceiveLocation(BDLocation location) {
            //showDebug(location);
            if (location == null || mMapView == null) {
                return;
            }
            if (location.getLocType() == BDLocation.TypeServerError){
                Toast.makeText(MainActivity.this,"服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因",Toast.LENGTH_SHORT).show();
                isLacateFail=true;
                return;
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                Toast.makeText(MainActivity.this,"网络不同导致定位失败，请检查网络是否通畅",Toast.LENGTH_SHORT).show();
                isLacateFail=true;
                return;
            }else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                Toast.makeText(MainActivity.this,"无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机",Toast.LENGTH_SHORT).show();
                isLacateFail=true;
                return;
            }
            if(((List<Poi>)location.getPoiList())==null||((List<Poi>)location.getPoiList()).isEmpty())locationNear="*";
            else locationNear=((List<Poi>)location.getPoiList()).get(0).getName();
            mTextLocationNear.setText(locationNear);
            longitude=location.getLongitude();
            latitude=location.getLatitude();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(latitude)
                    .longitude(longitude).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }

        private void showDebug(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

}

