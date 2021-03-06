package com.ncu.efpg.mysop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Ormlite.DatabaseHelper;
import Ormlite.member_accountDao;
import Ormlite.member_accountVo;
import Ormlite.sop_detailDao;
import Ormlite.sop_detailVo;


public class StepActionControlGPS extends Activity {
    private LocationManager mLocationManager;               //宣告定位管理控制
    private ArrayList<Poi> Pois = new ArrayList<Poi>();   //建立List，屬性為Poi物件
    public TextView gps;

    private ProgressDialog pDialog;
    private ProgressDialog pDialog1;
    JSONParser jsonParser = new JSONParser();
    //讀取 gps
    //private static String url_create_product = "http://140.115.80.237/front/mysop_ACgps.jsp";
    //private static final String TAG_SUCCESS = "success";

    private static final String TAG_Latitude = "Latitude";
    private static final String TAG_Longitude = "Longitude";

    private static double DLatitude=0 ;
    private  static double DLongitude=0;

    String TAG_CASE_NUMBER = "";
    String TAG_STEP_NUMBER = "";
    int TAG_STEP_ORDER = 0;
    String TAG_ACCOUNT;

    private RuntimeExceptionDao<sop_detailVo, Integer> sop_detailRuntimeDao;
    private sop_detailDao msop_detailDao;

    int TAG_START_REMIND = 0;
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_action_control_gps);

        TextView ss = (TextView)findViewById(R.id.textView2);
        TextView StartMessage = (TextView)findViewById(R.id.textView17);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();	//取得Bundle
        TAG_CASE_NUMBER = bundle.getString("TAG_CASE_NUMBER");
        TAG_STEP_NUMBER = bundle.getString("TAG_STEP_NUMBER");
        TAG_STEP_ORDER = bundle.getInt("TAG_STEP_ORDER");
        ss.setText(Integer.toString(TAG_STEP_ORDER));

        //orm 用stepnumber去抓資料庫的東西
        msop_detailDao = new sop_detailDao();
        DatabaseHelper mDatabaseHelper = DatabaseHelper.getHelper(this);
        List<sop_detailVo> list = null;
        list = msop_detailDao.selectRaw(mDatabaseHelper,"Step_number =\""+TAG_STEP_NUMBER+"\"");
        Log.d("抓", list.get(0).getStart_value1()+" "+list.get(0).getStart_value2());
        DLongitude = Double.parseDouble(list.get(0).getStart_value1());
        DLatitude = Double.parseDouble(list.get(0).getStart_value2());
        TAG_START_REMIND = Integer.valueOf(list.get(0).getStart_remind());
        StartMessage.setText(list.get(0).getStart_message());
        //Log.d("TAG_START_REMIND",list.get(0).getStart_remind());

        //1語音2手錶3響鈴
        if(TAG_START_REMIND == 1){
            mp = new MediaPlayer();
            try {
                mp.setDataSource("/sdcard/MYSOPTEST/start"+TAG_STEP_NUMBER+".mp3");
                mp.prepare();
            } catch (IllegalArgumentException e) {
            } catch (IllegalStateException e) {
            } catch (IOException e) {
            }
            mp.start();
        }else if(TAG_START_REMIND == 2){
            Notification notification = new NotificationCompat.Builder(getApplication())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Step"+Integer.toString(TAG_STEP_ORDER))
                    .setContentText("啟動方式：GPS啟動")
                    .extend(
                            new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
            Random random = new Random();
            int notificationId = random.nextInt(9999 - 1000) + 1000;
            notificationManager.notify(notificationId, notification);
        }else if(TAG_START_REMIND == 3){

            Log.d("TAG_START_REMIND","震動響鈴");
            //震動
            Vibrator mVibrator;
            mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            mVibrator.vibrate(1000);
            //響鈴
            NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification nt = new Notification();
            nt.defaults = Notification.DEFAULT_SOUND;
            int soundId = new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
            mgr.notify(soundId, nt);

        }


        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "已開啟定位服務", Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG)
                    .show();
        }


        gps=(TextView)findViewById(R.id.gps);

        //取得定位權限
        mLocationManager = (LocationManager) getSystemService(StepActionControlGPS.LOCATION_SERVICE);
       //連線取目的地
       // new CheckGPS().execute();



    }

    public void GPSonClick1 (View v){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Toast.makeText(StepCutControlGPS.this, "已開啟定位服務", Toast.LENGTH_LONG)
            //         .show();
            Pois.add(new Poi(DLatitude ,DLongitude ));
            System.out.println("Now "+DLongitude+" AND "+DLatitude);
            //按下按鈕後讀取我的位置，定位抓取方式為網路讀取
            //(若欲以GPS為定位抓取方式則更改成LocationManager.GPS_PROVIDER)
            // 最後則帶入定位更新Listener。
            mLocationManager.requestLocationUpdates
                    (LocationManager.GPS_PROVIDER,0,10000.0f,LocationChange);

            AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
            dialog1.setMessage("GPS...Please... wait");
            dialog1.show();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG)
                    .show();
        }

    }


    //更新定位Listener
    public LocationListener LocationChange = new LocationListener()
    {
        public void onLocationChanged(Location mLocation)
        {
            for(Poi mPoi : Pois)
            {
                //for迴圈將距離帶入，判斷距離為Distance function
                //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。
                mPoi.setDistance(Distance(mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        mPoi.getLatitude(),
                        mPoi.getLongitude()));
            }


            //印出我的座標-經度緯度

            Log.d("TAG", "我的座標 - 經度 : " + mLocation.getLongitude() + "  , 緯度 : " + mLocation.getLatitude());




            //for迴圈，印出景點店家名稱及距離，並依照距離由近至遠排列
            //第一筆為最近的景點店家，最後一筆為最遠的景點店家
            for(int i = 0 ; i < 1 ; i++ )
            {

                Log.d("TAG", " , 距離為 : " + DistanceText(Pois.get(i).getDistance()) );
                if(Pois.get(i).getDistance()>100){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(StepActionControlGPS.this);
                    dialog.setTitle("");
                    dialog.setMessage("還未到目的地，距離還有"+DistanceText(Pois.get(i).getDistance()));
                    dialog.show();

                }else{
                    Intent intent = new Intent(StepActionControlGPS.this,Stepdescription.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("TAG_CASE_NUMBER",TAG_CASE_NUMBER);
                    bundle.putString("TAG_STEP_NUMBER", TAG_STEP_NUMBER);
                    bundle.putInt("TAG_STEP_ORDER", TAG_STEP_ORDER);
                    intent.putExtras(bundle);//將參數放入intent
                    startActivity(intent);
                    finish();

                }
            }


        }
        public void onProviderDisabled(String provider)
        {
        }

        public void onProviderEnabled(String provider)
        {
        }

        public void onStatusChanged(String provider, int status,Bundle extras)
        {
        }
    };

    @Override
    protected void onDestroy()
    {
        if(mp != null) {
            mp.release();
        }
        super.onDestroy();
        mLocationManager.removeUpdates(LocationChange);  //程式結束時停止定位更新
    }

    //帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
    private String DistanceText(double distance)
    {
        if(distance < 1000 ) return String.valueOf((int)distance) + "m" ;
        else return new DecimalFormat("#.00").format(distance/1000) + "km" ;
    }



    //帶入使用者及景點店家經緯度可計算出距離
    public double Distance(double longitude1, double latitude1, double longitude2,double latitude2)
    {
        double EARTH_RADIUS = 6378137;
        double radLat1 = latitude1*Math.PI / 180.0;
        double radLat2 = latitude2*Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = longitude1*Math.PI / 180.0 - longitude2*Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_step_action_control_g, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //物件類別class，名稱為Poi
    public class Poi
    {

        private double Latitude;    //景點店家緯度
        private double Longitude;   //景點店家經度
        private double Distance;    //景點店家距離

        //建立物件時需帶入景點店家名稱、景點店家緯度、景點店家經度
        public Poi(double latitude , double longitude)
        {
            //將資訊帶入類別屬性

            Latitude = latitude ;
            Longitude = longitude ;
        }

        //取得店家緯度
        public double getLatitude()
        {
            return Latitude;
        }

        //取得店家經度
        public double getLongitude()
        {
            return Longitude;
        }

        //寫入店家距離
        public void setDistance(double distance)
        {
            Distance = distance;
        }

        //取的店家距離
        public double getDistance()
        {

            return Distance;

        }
    }
//
//    class CheckGPS extends AsyncTask<String, String, Integer> {
//
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pDialog = new ProgressDialog(StepActionControlGPS.this);
//            pDialog.setMessage("Loading.... Please wait...");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//
//        protected Integer doInBackground(String... args) {
//
//            //String Stepnumber="3";
//            String Stepnumber = TAG_STEP_NUMBER;
//            int valoreOnPostExecute = 0;
//
//            ArrayList params = new ArrayList();
//            params.add(new BasicNameValuePair("Stepnumber", Stepnumber));
//
//            JSONObject json = StepActionControlGPS.this.jsonParser.makeHttpRequest(StepActionControlGPS.url_create_product, "GET", params);
//
//
//            try {
//                int e = json.getInt(TAG_SUCCESS);
//                if(e == 1) {
//                    DLongitude = Double.parseDouble(json.getString(TAG_Longitude));
//                    DLatitude=Double.parseDouble(json.getString(TAG_Latitude));
//                    System.out.println("Here "+DLongitude+" AND "+DLatitude);
//
//
//                }else if(e == 6){
//
//
//                }
//            } catch (JSONException var9) {
//                var9.printStackTrace();
//            }
//
//            return valoreOnPostExecute;
//        }
//
//        protected void onPostExecute(Integer valoreOnPostExecute) {
//
//            pDialog.dismiss();
//
//        }
//    }
    @Override
    public void onPause(){
        if(mp != null){
            mp.pause();
            //mp.release();
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        if(mp != null) {
            try {
                mp.prepare();
            } catch (IllegalArgumentException e) {
            } catch (IllegalStateException e) {
            } catch (IOException e) {
            }
            mp.start();
        }
        super.onResume();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //orm account
            DatabaseHelper mDatabaseHelper4 = DatabaseHelper.getHelper(StepActionControlGPS.this);
            member_accountDao mmember_accountDao = new  member_accountDao();
            List<member_accountVo> memberlist = null;
            memberlist = mmember_accountDao.selectColumns(mDatabaseHelper4, "FIELD_Account");
            TAG_ACCOUNT = memberlist.get(0).getAccount();

            Bundle bundle = new Bundle();
            bundle.putString("TAG_ACCOUNT",TAG_ACCOUNT);
            Intent it = new Intent(this,Mysop.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            it.putExtras(bundle);//將參數放入intent
            startActivity(it);
            finish();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
