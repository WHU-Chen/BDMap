package com.example.cooorem.bdmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CheckActivity extends Activity {
    private final String THIS_ACTIVITY="CheckActivity";
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private TextView tvWhere,tvDescription,tvDbValue,tvTime;
    private Button btnLast,btnNext,btnDelete;
    private List<DbData> list;
    private GeoCoder mSearch;
    private boolean isChange=false;
    private Queue<Integer> Qposition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 开启定位图层
        mBaiduMap = mMapView.getMap();
        mSearch=GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                }
                //获取地理编码结果
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                }
                //获取反向地理编码结果
                list.get(Qposition.poll()).setLocalNear(result.getPoiList().get(0).name);

                isChange=true;
            }
        };
        mSearch.setOnGetGeoCodeResultListener(listener);
        list=new ArrayList<>();
        new Thread(new MyThread()).start();
        Log.d(THIS_ACTIVITY, "onCreate");
        Qposition=new LinkedBlockingQueue<>();
        tvWhere= (TextView) findViewById(R.id.tvWhere);
        tvDescription= (TextView) findViewById(R.id.tvDescription);
        tvDbValue= (TextView) findViewById(R.id.tvDbValue);
        tvTime= (TextView) findViewById(R.id.tvTime);
        btnLast= (Button) findViewById(R.id.btnLast);
        btnNext= (Button) findViewById(R.id.btnNext);
        btnDelete= (Button) findViewById(R.id.btnDelete);

    }

    void getLocationNear(int position){
        Qposition.add(position);
        LatLng latLng=new LatLng(list.get(position).getLatitude(),
                list.get(position).getLongitude());
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }
    @Override
    protected void onStop() {
        mSearch.destroy();
        if(isChange)
            new Thread(new SaveThread()).start();
        super.onStop();
    }
    private void start(final int position){
        if(position==-1){
            tvWhere.setText("无记录");
            tvDbValue.setText(null);
            tvDescription.setText(null);
            tvTime.setText(null);
            Button button = new Button(getApplicationContext());
            LatLng pt = new LatLng(0, 0);
            //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
            InfoWindow mInfoWindow = new InfoWindow(button, pt, 0);
            //显示InfoWindow
            mBaiduMap.showInfoWindow(mInfoWindow);
            return;
        }
        DbData data=list.get(position);
        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
        //定义用于显示该InfoWindow的坐标点
        button.setText(data.getAverageDb());
        button.setBackgroundResource(R.drawable.pop);
        button.setTextColor(0xFFFF00FF);
        final int size=16;
        button.setTextSize(size);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(0, 0, 0, 0);
        button.setMinimumHeight(0);
        button.setWidth(dip2px(this,size*3));
        button.setHeight(dip2px(this,size));
        LatLng pt = new LatLng(data.getLatitude(), data.getLongitude());
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, pt, 0);
        //显示InfoWindow
        mBaiduMap.showInfoWindow(mInfoWindow);

        LatLng ll = new LatLng(data.getLatitude(),
                data.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.animateMapStatus(u);
        tvWhere.setText(data.getLocalNear());
        if(data.getDescription().isEmpty())tvDescription.setText("简介：无");
        else tvDescription.setText("简介："+data.getDescription());
        tvDbValue.setText("分贝值："+data.getAverageDb());
        tvTime.setText("时间："+data.getTime());
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.size()==0)return;
                start((position + 1) % list.size());
            }
        });
        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.size()==0)return;
                start((position-1+list.size())%list.size());
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog =new AlertDialog.Builder(CheckActivity.this);
                dialog.setTitle("注意");
                dialog.setMessage("确定要删除这条记录吗");
                dialog.setCancelable(true);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(list.size()==0)return;
                        list.remove(position);
                        isChange=true;
                        if(list.isEmpty())start(-1);
                        else
                            start((position - 1 + list.size()) % list.size());
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                dialog.show();
            }
        });
    }
    boolean hasSharpe(String s){
        for(int i=0;i<Math.min(s.length(), 1);i++){
            if(s.charAt(i)=='#')return true;
        }
        return false;
    }
    private class MyThread implements Runnable {
        @Override
        public void run() {
            DbData data;
            FileInputStream in=null;
            BufferedReader reader=null;
            StringBuilder content=new StringBuilder();
            try {
                in=openFileInput("data");
                reader=new BufferedReader(new InputStreamReader(in));
                String s="";
                Log.d(THIS_ACTIVITY,"StartRead");
                int num=0;
                while((s=reader.readLine())!=null){
                    Log.d(THIS_ACTIVITY,"Read Data");
                    content=new StringBuilder();
                    data=new DbData();
                    while(s.isEmpty()&&s!=null)s=reader.readLine();
                    if(s==null)break;
                    while (!hasSharpe(s)) {
                        Log.d(THIS_ACTIVITY,"#");
                        content.append(s);
                        s=reader.readLine();
                    }
                    data.setDescription(content.toString());
                    data.setAverageDb(reader.readLine());
                    data.setLongitude(Double.parseDouble(reader.readLine()));
                    data.setLatitude(Double.parseDouble(reader.readLine()));
                    data.setLocalNear(reader.readLine());
                    data.setTime(reader.readLine());
                    list.add(data);
                    Log.d(THIS_ACTIVITY,data.toString());
                    if(data.getLocalNear().isEmpty())getLocationNear(num);
                    else if(data.getLocalNear().charAt(0)=='*')getLocationNear(num);
                    ++num;
                }
                if(num==0)start(-1);
                else start(list.size()-1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class SaveThread implements Runnable{
        @Override
        public void run() {
            if(list.isEmpty()){
                BufferedWriter writer = null;
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput("data", Context.MODE_PRIVATE);
                    writer = new BufferedWriter(new OutputStreamWriter(fos));
                    writer.write(new String());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for(DbData i:list) {
                BufferedWriter writer = null;
                try {
                    FileOutputStream fos = openFileOutput("data", Context.MODE_PRIVATE);
                    writer = new BufferedWriter(new OutputStreamWriter(fos));
                    writer.write(getData(i));
                    Log.d(THIS_ACTIVITY, "Write Succeed");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (writer != null) writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public int dip2px(Context context, float dipValue){
        float m=context.getResources().getDisplayMetrics().density ;
        return (int)(dipValue * m + 0.5f) ;
    }
    public int px2dip(Context context, float pxValue){
        float m=context.getResources().getDisplayMetrics().density ;
        return (int)(pxValue / m + 0.5f) ;
    }
    private String getData(DbData i) {
        StringBuffer sb=new StringBuffer();
        sb.append(i.getDescription()+"\n###\n");
        sb.append(""+i.getAverageDb()+"\n");
        sb.append(""+i.getLongitude()+"\n"+i.getLatitude()+"\n");
        sb.append(i.getLocalNear() + "\n");
        sb.append(i.getTime() + "\n");
        return sb.toString();
    }
}
