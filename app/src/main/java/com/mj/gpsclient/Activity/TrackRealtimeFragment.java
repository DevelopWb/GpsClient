package com.mj.gpsclient.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.fragment.AbFragment;
import com.ab.http.AbHttpUtil;
import com.ab.http.AbRequestParams;
import com.ab.http.AbStringHttpResponseListener;
import com.ab.soap.AbSoapListener;
import com.ab.soap.AbSoapParams;
import com.ab.soap.AbSoapUtil;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbToastUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.mj.gpsclient.AppHttpUtil;
import com.mj.gpsclient.R;
import com.mj.gpsclient.global.DebugLog;
import com.mj.gpsclient.global.MyApplication;
import com.mj.gpsclient.model.DeviceLocation;
import com.mj.gpsclient.model.DevicePosition;
import com.mj.gpsclient.view.DevicesDetailView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by majin on 15/5/28.
 */
public class TrackRealtimeFragment extends AbFragment implements View.OnClickListener,
        OnGetGeoCoderResultListener,OnGetRoutePlanResultListener {
    private MyApplication application;
    private Activity mActivity = null;
    // 地图相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private ImageView buCarLocation;
    private ImageView buMyLocation;
    private ImageView buNaviLocation;
    private Marker mMarkerA;
    private InfoWindow mInfoWindow;
    private AbSoapUtil mAbSoapUtil;
    private BitmapDescriptor bdA, bdS;
    private DevicePosition devicePosition;
    private boolean isFristLoad = true;
    private List<DeviceLocation> locationList;
    private Marker markerStart;
    private Marker markerNow;
    private TextView textViewLoction;
    //GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private LatLng prePoint = null;
    List<LatLng> points = new ArrayList<LatLng>();
    private double test_plus;
    boolean isFirstLoc = true;// 是否首次定位
    BitmapDescriptor mCurrentMarker;
    boolean useDefaultIcon = false;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    // 定位相关
    LocationClient mLocClient;
    //搜索相关
    RoutePlanSearch mSearchRoute = null;    // 搜索模块，也可去掉地图模块独立使用

    private LatLng carLatLng;
    private LatLng myLatLng;
    OverlayManager routeOverlay = null;
    private BDLocation myBDLocation;

    public MyLocationListenner myListener = new MyLocationListenner();
    public static final int HANDLE_MSG_WHAT = -1;
    public static final int DRAW_MAP = HANDLE_MSG_WHAT + 1;
    public static final int GET_DEVICE_DATA = HANDLE_MSG_WHAT + 2;
    public static final int LOOP_MSG_ONE = HANDLE_MSG_WHAT + 3;
    public static final int LOOP_MSG_TWO = HANDLE_MSG_WHAT + 4;
    public static final int MAP_PAUSE = HANDLE_MSG_WHAT + 5;
    public static final int MAP_AUTO_TRACK_SWITCH = HANDLE_MSG_WHAT + 6;
    public static final int ROUT_DRAW_END = HANDLE_MSG_WHAT + 7;
    public static final int START_GET_MY_LOCATION = HANDLE_MSG_WHAT + 8;
    public static final int GET_ROUNT = HANDLE_MSG_WHAT + 9;
    public static final int GET_MYLOCATION = HANDLE_MSG_WHAT + 10;

    //是否是导航模式
    public boolean isNaviModel = false;
    private AbHttpUtil mAbHttpUtil = null;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            int arg1 = msg.arg1;
            switch (result) {
                case DRAW_MAP:
                    mj_hideLoadView();
                    if (null != devicePosition) {
                        AbToastUtil.showToast(mActivity, "数据获取成功！");
                        initOverlay();
                    } else {
                        AbToastUtil.showToast(mActivity, "数据获取失败！");
                    }
                    break;
                case GET_DEVICE_DATA:
                    getDeviceDetail();
                    break;

                case LOOP_MSG_ONE:
                    if (isAutoModle()) {
                        mHandler.sendEmptyMessageDelayed(LOOP_MSG_TWO, 1000);
                        sendEmptyMessage(GET_DEVICE_DATA);//自动获取数据
                    }
                    break;

                case LOOP_MSG_TWO:
                    if (isAutoModle()) {
                        mHandler.sendEmptyMessageDelayed(LOOP_MSG_ONE, 9000);
                    }
                    break;

                case MAP_AUTO_TRACK_SWITCH:
                    resetMap();
                    if (arg1 == 1) {
                        startTimer();
                    } else if (arg1 == 0) {
                        endTimer();
                        sendEmptyMessage(GET_DEVICE_DATA);
                    }
                    break;

                case MAP_PAUSE:
                    resetMap();
                    endTimer();
                    break;
                case ROUT_DRAW_END:
                    mj_hideLoadView();
                    showMyLocation(myBDLocation);
                    break;

                case START_GET_MY_LOCATION:
                    getMyLocation(false);
                    break;
                case GET_ROUNT:
                    mj_hideLoadView();
                    getRouteOverlay();

                    break;
                case  GET_MYLOCATION:
                    //getMyLocation(true);
                    mj_hideLoadView();
                    showMyLocation(myBDLocation);
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        mActivity = this.getActivity();
        application = (MyApplication) mActivity.getApplication();
        View v = inflater.inflate(R.layout.fragment_track_realtime, container, false);
        initDatas();
        initViews(v);
        buCarLocation.setOnClickListener(this);
        buMyLocation.setOnClickListener(this);
        buNaviLocation.setOnClickListener(this);
        //加载数据必须
        this.setAbFragmentOnLoadListener(new AbFragmentOnLoadListener() {

            @Override
            public void onLoad() {

            }

        });
        mHandler.sendEmptyMessageDelayed(START_GET_MY_LOCATION, 400);
        return v;
    }

    @Override
    public void setResource() {
        //设置加载的资源
        this.setLoadDrawable(R.drawable.ic_load);
        this.setLoadMessage("正在加载,请稍候...");
        this.setTextColor(getResources().getColor(R.color.moji_black_text));
        this.setRefreshDrawable(R.drawable.ic_refresh_f);
        this.setRefreshMessage("请求出错，请重试");
    }

    private void initDatas() {
//        bdA = BitmapDescriptorFactory.fromResource(R.drawable.item_27_xx);
//        bdS = BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_end);
        bdA = BitmapDescriptorFactory.fromResource(R.drawable.historymark);
        bdS = BitmapDescriptorFactory.fromResource(R.drawable.icon_track_navi_end);
        // 初始化搜索模块，注册事件监听
//        mSearch = GeoCoder.newInstance();
//        mSearch.setOnGetGeoCodeResultListener(this);
        isNaviModel = false;
        mAbHttpUtil = AbHttpUtil.getInstance(mActivity);
    }

    private LatLng convert(LatLng latLng) {
        CoordinateConverter c = new CoordinateConverter();
        c.from(CoordinateConverter.CoordType.GPS);
        c.coord(latLng);
        return c.convert();
    }

    /*
    * 开启定时任务
    * */
    private void startTimer() {
        mHandler.removeMessages(LOOP_MSG_ONE);
        mHandler.removeMessages(LOOP_MSG_TWO);
        mHandler.sendEmptyMessage(LOOP_MSG_ONE);

    }

    private void endTimer() {
        mHandler.removeMessages(GET_DEVICE_DATA);
        mHandler.removeMessages(LOOP_MSG_ONE);
        mHandler.removeMessages(LOOP_MSG_TWO);
    }

    private boolean isAutoModle() {
        return ((DevicesTrackActivity) mActivity).isRealtimeMode();
    }

    private boolean isRealTimeModle(){
        return ((DevicesTrackActivity) mActivity).isRealTimeModle();
    }

    private void initOverlay() {
        mBaiduMap.hideInfoWindow();
        LatLng llA = null;
//        if(((DevicesTrackActivity)mActivity).isRealtimeMode()){
//            test_plus=test_plus+0.002;
//            llA= new LatLng( devicePosition.getLatitude(),devicePosition.getLongitude()+test_plus);
//        }else{
//
//        }
        llA = new LatLng(devicePosition.getLatitude(), devicePosition.getLongitude());
        getLngLatToAddress(llA);
        llA = convert(llA);
        carLatLng = llA;
        devicePosition.setLatitude(llA.latitude);
        devicePosition.setLongitude(llA.longitude);
        Log.d("majin", llA.toString());
        OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
                .zIndex(9).draggable(true);
//        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
//                .location(llA));
//        getLngLatToAddress(carLatLng);


        if (mMarkerA == null) {
            mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
        } else {
            mMarkerA.setPosition(llA);

        }
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("majin", "onMarkerClick---");
                showMarkerInfoWindow(marker);
                return true;
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                mBaiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });


        if (isPause()) {
            return;
        }
        if (markerStart == null) {
            OverlayOptions ooAStart = new MarkerOptions().position(llA).icon(bdS)
                    .draggable(true);
            markerStart = (Marker) mBaiduMap.addOverlay(ooAStart);
        }

        if (prePoint != null) {
            //画出当前的点和之前的一个点以前的轨迹

            double d = DistanceUtil.getDistance(llA, prePoint);
            DebugLog.e("实时 d=" + d);
            if (d > 5) {
                points.clear();
                points.add(prePoint);
                points.add(llA);
                OverlayOptions ooPolyline = new PolylineOptions().width(6)
                        .color(0xff379bff).points(points);
                if (isPause()) {
                    return;
                }
                mBaiduMap.addOverlay(ooPolyline);
            }

        }


        //定位到该点
        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(llA);
        mBaiduMap.setMapStatus(u);
        //保存当前点的坐标，以便下一次划线
        prePoint = llA;


    }

    private void showMarkerInfoWindow(Marker marker) {
        DevicesDetailView devicesDetailView = new DevicesDetailView(mActivity);
        devicesDetailView.setData(devicePosition);
        LatLng ll = marker.getPosition();
        mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(devicesDetailView), ll, -47, null);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    private void convert(DeviceLocation deviceLocation) {
        CoordinateConverter c = new CoordinateConverter();
        c.from(CoordinateConverter.CoordType.GPS);
        c.coord(deviceLocation.getLatLng());

        LatLng lg = c.convert();
        deviceLocation.setLatitude(lg.latitude + "");
        deviceLocation.setLongitude(lg.longitude + "");
        deviceLocation.setLatLng(new LatLng(lg.latitude, lg.longitude));

    }


    private boolean isPause() {
        return !((DevicesTrackActivity) mActivity).isRealTimeModle();
    }

    private void getDeviceDetail() {

        String nameSpace = "http://tempuri.org/";
        String methodName = "GetMemberPositioning";
        AbSoapParams params = new AbSoapParams();
        params.put("UserName", application.mUser.getUserName());
        params.put("Names", ((DevicesTrackActivity) mActivity).getDeviceName());
        mAbSoapUtil = AbSoapUtil.getInstance(mActivity);
        mAbSoapUtil.setTimeout(10000);
//        AbProgressDialogFragment dialogFragment= AbDialogUtil.showProgressDialog(mActivity, 0,
//                "信息获取中...");
        mj_showLoadView();
        mAbSoapUtil.call(AppHttpUtil.BASE_URL, nameSpace, methodName, params, new AbSoapListener() {

            //获取数据成功会调用这里
            @Override
            public void onSuccess(int statusCode, SoapObject object) {

                String GetMemberPositioningResult = object.getPrimitivePropertyAsString("GetMemberPositioningResult");
                Log.d("majin", "statusCode=" + statusCode + "SoapObject=" + GetMemberPositioningResult);
                AbDialogUtil.removeDialog(mActivity);
                JSONObject jobj = null;
                try {
                    jobj = new JSONObject(GetMemberPositioningResult);
                } catch (JSONException e) {
                    AbToastUtil.showToast(mActivity, "系统返回异常！");
                    e.printStackTrace();
                }
                String result = jobj.optString("Result");

                if (result.equals("ok")) {
                    JSONArray jarray = jobj.optJSONArray("Model");
                    JSONObject jmode = null;
                    try {
                        if (jarray != null || jarray.length() > 0) {
                            jmode = jarray.getJSONObject(0);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jmode != null) {
                        devicePosition = new DevicePosition();
                        devicePosition.setName(jmode.optString("Name"));
                        devicePosition.setOnTime(jmode.optString("OnTime"));
                        devicePosition.setLocType(jmode.optString("LocType"));
                        devicePosition.setLongitude(jmode.optDouble("Longitude"));
                        devicePosition.setLatitude(jmode.optDouble("Latitude"));
                        devicePosition.setBaseInfo(jmode.optString("BaseInfo"));
                        devicePosition.setSpeed(jmode.optString("Speed"));
                        devicePosition.setDirection(jmode.optString("Direction"));
                        devicePosition.setDeviceStatus(jmode.optString("DeviceStatus"));
                    }
                    isFristLoad = false;
                    mHandler.sendEmptyMessage(0);
                } else {
                    AbToastUtil.showToast(mActivity, "系统异常！");
                }

            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, String content,
                                  Throwable error) {
                mj_hideLoadView();
            }

            // 失败，调用
            @Override
            public void onFailure(int statusCode, SoapFault fault) {

            }

            // 开始执行前
            @Override
            public void onStart() {

            }

            // 完成后调用，失败，成功
            @Override
            public void onFinish() {
                Log.d("majin", "onFinish");
            }

            ;

        });

    }


    @Override
    public void onPause() {
        mMapView.onPause();
        Log.d("majin", "onPause");
        mHandler.sendEmptyMessage(9);
        if (mMarkerA != null) {
            mMarkerA.remove();
            mMarkerA = null;
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mHandler.postDelayed(runnable, 4000);
        showContentView();
        mMapView.onResume();
        if (((DevicesTrackActivity) mActivity).getCurrentIndexFragmentIndex() == 0) {
            //请求一次数据
            mHandler.removeMessages(GET_DEVICE_DATA);
            mHandler.sendEmptyMessage(GET_DEVICE_DATA);
        }

        //mHandler.sendEmptyMessage(3);

    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        // 退出时销毁定位
        if (mLocClient != null) {
            mLocClient.stop();
        }

        bdA.recycle();
        super.onDestroy();
    }

    private void initViews(View view) {
        // 初始化地图
        mMapView = (MapView) view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        buCarLocation = (ImageView) view.findViewById(R.id.car_location_bu);
        buNaviLocation = (ImageView) view.findViewById(R.id.nav_bu);
        buMyLocation = (ImageView) view.findViewById(R.id.mobile_bu);
        textViewLoction = (TextView) view.findViewById(R.id.loaction_GeoCode);
        resetMap();
    }

    private void resetMap() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.clear();
        mBaiduMap.setMaxAndMinZoomLevel(3.0f, 19.0f);
        MapStatus mMapStatus = new MapStatus.Builder()
                .zoom(14)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        mMarkerA = null;
        markerStart = null;
        prePoint = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.car_location_bu:
                ((DevicesTrackActivity) getActivity()).exitNavigationMode();
//                LatLng llA = new LatLng( devicePosition.getLatitude(),devicePosition.getLongitude());
//                llA =convert(llA);
//                if(mMarkerA==null){
//                    OverlayOptions ooA = new MarkerOptions().position(llA).icon(bdA)
//                            .zIndex(9).draggable(true);
//                    mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
//                }else{
//                    mMarkerA.setPosition(llA);
//
//                }
//
//                //定位到该点
//                MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
//                MapStatusUpdate u = MapStatusUpdateFactory
//                        .newLatLng(llA);
//                mBaiduMap.setMapStatus(msu);
//                mBaiduMap.setMapStatus(u);
                resetMap();
                getCarLocation();
                isNaviModel = false;
                break;
            case R.id.nav_bu:
                mj_showLoadView();
                endTimer();
                ((DevicesTrackActivity) getActivity()).toNavigationMode("导航");
                resetMap();
                isNaviModel = true;

                mHandler.removeMessages(GET_ROUNT);
                mHandler.sendEmptyMessageDelayed(GET_ROUNT,1000);
                break;
            case R.id.mobile_bu:
                //getLngLatToAddress(myLatLng);
                mj_showLoadView();
                endTimer();
                ((DevicesTrackActivity)getActivity()).toNavigationMode("定位");
                resetMap();

                mHandler.removeMessages(GET_MYLOCATION);
                mHandler.sendEmptyMessageDelayed(GET_MYLOCATION,1000);
                isNaviModel =false;
                break;
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            AbToastUtil.showToast(mActivity, "反编码地址信息错误！");
            return;
        }
        //textViewLoction.setText(result.getAddress());
    }

    public void getCarLocation() {
        mHandler.removeMessages(GET_DEVICE_DATA);
        mHandler.sendEmptyMessage(GET_DEVICE_DATA);
    }

    public void getMyLocation(boolean isShowInMap) {

        // 传入null则，恢复默认图标
        // 修改为自定义marker
//        mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_geo);
        isFirstLoc = isShowInMap;
        mCurrentMarker = null;
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(mActivity);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mj_showLoadView();
        mLocClient.start();
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            // DebugLog.e("onReceiveLocation=-----");
            myBDLocation = location;
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            myLatLng = ll;
            if (isNaviModel) {
                // showMyLocation(myBDLocation);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            //mj_hideLoadView();
            DebugLog.e("onReceivePoi=-----");
        }
    }

    private void showMyLocation(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null) {
            Toast.makeText(mActivity, "定位异常", Toast.LENGTH_SHORT).show();
            return;
        }

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        LatLng ll = new LatLng(location.getLatitude(),
                location.getLongitude());
        // ll =convert(ll);
        myLatLng = ll;
        // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        MapStatus mMapStatus = new MapStatus.Builder().target(ll)
                .zoom(18)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
        getLngLatToAddress(myLatLng);
//        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
//                .location(ll));
    }

    //定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }


    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {

        final DrivingRouteResult result1 = result;
        DebugLog.e(result.toString());
//        DebugLog.e(result.getSuggestAddrInfo().toString());
//        DebugLog.e(result.getTaxiInfo().getDistance()+"   "+result.getTaxiInfo().getDuration());
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            mj_hideLoadView();
            Toast.makeText(mActivity, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();

        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            mj_hideLoadView();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//            nodeIndex = -1;
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);
//            route = result.getRouteLines().get(0);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
                    routeOverlay = overlay;
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result1.getRouteLines().get(0));
                    overlay.addToMap();
                    // overlay.zoomToSpan();

                }
            }.start();
            mHandler.sendEmptyMessageDelayed(ROUT_DRAW_END, 4000);
        }
    }

    public void getRouteOverlay() {
        mj_showLoadView();
        mBaiduMap = mMapView.getMap();
        mSearchRoute = RoutePlanSearch.newInstance();
        mSearchRoute.setOnGetRoutePlanResultListener(this);
//        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", "西单");
//        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", "龙泽");
        if (null==myLatLng||null==carLatLng){
            Toast.makeText(mActivity,"定位失败",Toast.LENGTH_SHORT).show();
            return;
        }
        PlanNode stNode = PlanNode.withLocation(myLatLng);
        PlanNode enNode = PlanNode.withLocation(carLatLng);
        mSearchRoute.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)
                .to(enNode));


    }


    private void getLngLatToAddress(LatLng latLng) {

        AbRequestParams params =new AbRequestParams();
        params.put("config","POSDES");
        params.put("x1",String.valueOf(latLng.longitude));
        params.put("y1",String.valueOf(latLng.latitude));
        params.put("a_k","38dc8a0ba054afb20cd68e635dae946519ab5bb1d05814a8be31ddb04c2662a66c9da235aac3ced7");
        params.put("config","POSDES");
        mAbHttpUtil.get("http://search1.mapabc.com/sisserver", params, new AbStringHttpResponseListener() {


            @Override
            public void onSuccess(int statusCode, String content) {
                DebugLog.e("content ="+content);
                if(!TextUtils.isEmpty(content)&&null!=textViewLoction){
                    String showString = content.trim().replace(';','/');
                    textViewLoction.setText(showString);
                }

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onFailure(int statusCode, String content, Throwable error) {

            }
        });


    }

}