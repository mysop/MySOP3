package com.ncu.efpg.mysop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import Ormlite.DatabaseHelper;
import Ormlite.member_accountDao;
import Ormlite.member_accountVo;
import Ormlite.sop_detailDao;
import Ormlite.sop_detailVo;


public class StepCutControlIbeacon extends Activity implements BeaconConsumer{

    public static final String TAG = "BeaconsEverywhere";
    private BeaconManager beaconManager;

    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    //讀取 qrcode 圖片
    //private static String url_uuid = "http://140.115.80.237/front/mysop_CCibeacon.jsp";
    //private static final String TAG_SUCCESS = "success";
    //private static final String TAG_UUID = "UUID";
    String UUID = "00000000-0000-0000-0000-000000000000";
    String TAG_ACCOUNT;

    int connectfinish = 0;

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();//獲得當前的藍芽

    String TAG_CASE_NUMBER = "";
    String TAG_STEP_NUMBER = "";
    int TAG_STEP_ORDER = 0;

    private sop_detailDao msop_detailDao;
    private DatabaseHelper mDatabaseHelper;

    Button ibeacon_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_cut_control_ibeacon);
        Log.d("oncreateee", Integer.toString(connectfinish));

        mDatabaseHelper = DatabaseHelper.getHelper(this);

        TextView ss = (TextView)findViewById(R.id.CC_ibeacon_textView2);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();	//取得Bundle
        TAG_CASE_NUMBER = bundle.getString("TAG_CASE_NUMBER");
        TAG_STEP_NUMBER = bundle.getString("TAG_STEP_NUMBER");
        TAG_STEP_ORDER = bundle.getInt("TAG_STEP_ORDER");
        ss.setText(Integer.toString(TAG_STEP_ORDER));

        ibeacon_button = (Button) findViewById(R.id.CC_ibeacon_button);
/*        if (adapter == null){
            Toast.makeText(this, "藍芽藍芽?", Toast.LENGTH_LONG).show();
        }else if(adapter.isEnabled()!=true){//如果藍芽未開啟
            //打開藍芽(會問使用者)
            Intent enabler=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
            Toast.makeText(this, "藍芽藍芽?ohno", Toast.LENGTH_LONG).show();
            beaconManager = BeaconManager.getInstanceForApplication(StepCutControlIbeacon.this);
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            beaconManager.bind(StepCutControlIbeacon.this);
            //onDestroy();
            refresh();

            //則打開藍芽(不問使用者)
            //adapter.enable();

        }else {
            //new Check_beacon().execute();

            Log.d("isithere", Integer.toString(connectfinish));

        }*/
        //beaconManager = BeaconManager.getInstanceForApplication(this);
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        //beaconManager.bind(this);
    }


    public void refresh(){

        Intent intent = new Intent();
        intent.setClass(StepCutControlIbeacon.this, StepCutControlIbeacon.class);
        //Intent intent = getIntent();
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if(connectfinish==1){
            beaconManager.unbind(this);
        }
        super.onDestroy();

    }

    @Override
    public void onBeaconServiceConnect() {

        Log.d("connectfinish?",Integer.toString(connectfinish));

        Region region = new Region("myBeaons", Identifier.parse(UUID), null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                    Intent intent = new Intent();
                    intent.setClass(StepCutControlIbeacon.this, StepNextControl.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("TAG_CASE_NUMBER",TAG_CASE_NUMBER);
                    bundle.putString("TAG_STEP_NUMBER", TAG_STEP_NUMBER);
                    bundle.putInt("TAG_STEP_ORDER", TAG_STEP_ORDER);
                    intent.putExtras(bundle);//將參數放入intent
                    startActivity(intent);
                    // 设置切换动画，从右边进入，左边退出
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    finish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(Beacon oneBeacon : beacons) {
                    Log.d(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                }
            }
        });

        try {
            Log.d("uuid",region.getIdentifier(0).toString());
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void start_beacon(View view){
        if (adapter == null){
            Toast.makeText(this, "藍芽藍芽?不支持藍芽!", Toast.LENGTH_LONG).show();
        }else if(adapter.isEnabled()!=true){//如果藍芽未開啟
            //打開藍芽(會問使用者)
            Intent enabler=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
            Toast.makeText(this, "藍芽藍芽?沒打開喔!", Toast.LENGTH_LONG).show();
        }else {
            //new Check_beacon().execute();

            msop_detailDao = new sop_detailDao();
            List<sop_detailVo> list = null;
            list = msop_detailDao.selectRaw(mDatabaseHelper, "Step_number =\""+TAG_STEP_NUMBER+"\"");
            Log.d("抓", list.get(0).getFinish_value1());
            UUID = list.get(0).getFinish_value1();
            connectfinish=1;
            beaconManager = BeaconManager.getInstanceForApplication(StepCutControlIbeacon.this);
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
            beaconManager.bind(StepCutControlIbeacon.this);


            ibeacon_button.setText("搜尋中");
            ibeacon_button.setEnabled(false);

            Log.d("isithere", Integer.toString(connectfinish));

        }
    }


/*    class Check_beacon extends AsyncTask<String, String, Integer> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(StepCutControlIbeacon.this);
            pDialog.setMessage("Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }


        protected Integer doInBackground(String... args) {

            int returnvalue = 0;

            String StepNumber = TAG_STEP_NUMBER;
            //String StepNumber = "2";

            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("StepNumber", StepNumber));

            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest(url_uuid, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    UUID = json.getString(TAG_UUID);
                    returnvalue = 1;
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return returnvalue;
        }


        protected void onPostExecute(Integer returnvalue) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            connectfinish=1;
            if (returnvalue == 1){
                beaconManager = BeaconManager.getInstanceForApplication(StepCutControlIbeacon.this);

                beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

                beaconManager.bind(StepCutControlIbeacon.this);
            }

        }
    }*/

    @Override
    public void onPause(){
        if(connectfinish==1){
            beaconManager.unbind(this);
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        connectfinish=0;
        ibeacon_button.setEnabled(true);
        ibeacon_button.setText("開始");
        super.onResume();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //orm account
            DatabaseHelper mDatabaseHelper4 = DatabaseHelper.getHelper(StepCutControlIbeacon.this);
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
