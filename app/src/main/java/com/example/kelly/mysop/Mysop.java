package com.example.kelly.mysop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.json.JSONArray;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Ormlite.DatabaseHelper;
import Ormlite.case_masterDao;
import Ormlite.case_masterVo;
import Ormlite.mysopVo;
import Ormlite.sop_detailDao;
import Ormlite.sop_detailVo;
import Ormlite.sop_masterDao;
import Ormlite.sop_masterVo;


public class Mysop extends Activity {



    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> productsList;
    ArrayList<HashMap<String, String>> productsList1;
    //private static String url_all_products = "http://localhost:8080/kelly/test_getall.jsp";
    // private static String url_all_products = "http://140.115.80.237/front/test_getall.jsp";
    private static String url_all_products = "http://140.115.80.237/front/mysop_mysop.jsp";
    private static String url_all_products1 = "http://140.115.80.237/front/mysop_mysop1.jsp";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_CASENUMBER = "casenumber";
    private static final String TAG_SOPNAME = "sopname";
    private static final String TAG_STARTRULE = "startrule";
    private static final String TAG_STARTVALUE = "startvalue";
    private static final String TAG_PICTURE = "picture";
    private static final String TAG_ORDER = "order";
    private static final String TAG_TATOL = "total";
    private static final String TAG_SOPNUMBER = "sopnumber";
    private static String str,timedifference;
    public int check;
    //計算product 長度
    public int x;

    private ListView listInput;
    private ListView listInput1;
    //private ArrayAdapter<String> adapter;
    MyAdapter adapter = null;
    MyAdapter1 adapter1 = null;

    private ImageView picture;
    private TextView title;
    private TextView master;

    JSONArray products = null;
    JSONArray products1 = null;

   //帳號先寫死
    String TAG_ACCOUNT = "";
    //String TAG_ACCOUNT = "test@gmail.com";


    //存casenumber  sopname
    private String[] list;
    private String[] name;
    private String[] steporder;
    private String[] steptotal;

    private int[] logos = new int[] { R.drawable.nfc, R.drawable.beacon,
            R.drawable.gps,R.drawable.qrcode,R.drawable.white };
    int[] key;
    private String[] timesee;
    private Drawable[] photo;
    private Bitmap[] bmplist;

    private String[] list1;
    private String[] name1;
    int[] key1;
    private String[] timesee1;
    private Drawable[] photo1;
    private Bitmap[] bmplist1;
    private String[] steporder1;
    private String[] steptotal1;


    //orm
    private RuntimeExceptionDao<case_masterVo, Integer> case_masterRuntimeDao;
    private case_masterDao mcase_masterDao;
    private RuntimeExceptionDao<sop_masterVo, Integer> sop_masterRuntimeDao;
    private sop_masterDao msop_masterDao;
    private RuntimeExceptionDao<sop_detailVo, Integer> sop_detailRuntimeDao;
    private sop_detailDao msop_detailDao;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mysop);
        listInput = (ListView)findViewById(R.id.list_sop);
        listInput1 = (ListView)findViewById(R.id.list_sop2);
       // adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);


        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        TAG_ACCOUNT=bundle.getString("TAG_ACCOUNT");
       // Log.d("Mysop's TAG_ACCOUNT",TAG_ACCOUNT);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Bundle b1 = new Bundle();
            b1.putString("TAG_ACCOUNT", TAG_ACCOUNT);
            Intent i1 = new Intent(this, RegistrationIntentService.class);
            i1.putExtras(bundle);
            startService(i1);
        }


                // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();
        productsList1 = new ArrayList<HashMap<String, String>>();
        // Loading products in Background Thread
       // new LoadAllProducts().execute();

        // TAG_ACCOUNT = bundle.getString("TAG_ACCOUNT");	//輸出Bundle內容

        //時間
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間
        str = formatter.format(curDate);

        //orm 用stepnumber去抓資料庫的東西

        mcase_masterDao = new case_masterDao();
        DatabaseHelper mDatabaseHelper = DatabaseHelper.getHelper(this);
        List<case_masterVo> caselist = null ;
     //   caselist = mcase_masterDao.selectRaw(mDatabaseHelper,"Account="+"'"+TAG_ACCOUNT+"'");
      //  Log.d("抓", caselist.get(0).getCase_number());
//        list = new String[caselist.size()];
//        for(int i=0;i<caselist.size();i++){
//            list[i]=caselist.get(i).getCase_number();
//        }

        msop_masterDao = new sop_masterDao();
        List<sop_masterVo>sopmasterlist = null;
       // sopmasterlist = msop_masterDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='"+TAG_ACCOUNT+"')");
        sopmasterlist = msop_masterDao.selectRawByNest(mDatabaseHelper, "Account",TAG_ACCOUNT, "Sop_number") ;
        List<mysopVo>mysoplist = null;
      //  mysoplist= sop_masterDao.join(mDatabaseHelper, "Account=" + "'" + TAG_ACCOUNT+"'");
      //  Log.d("MYSOP1", mysoplist.get(0).getSop_name());
                //getcase_masterVo().getCase_number());

        msop_detailDao = new sop_detailDao();
        List<sop_detailVo>sopdetaillist = null;
        //sopdetaillist = msop_detailDao.selectRaw(mDatabaseHelper,"Step_number IN(SELECT Last_do_order FROM case_masterVo WHERE Account='"+TAG_ACCOUNT+"')");
        sopdetaillist = msop_detailDao.selectRawByNest(mDatabaseHelper,"Account",TAG_ACCOUNT,"Step_number");

      //  Log.d("抓2",sopdetaillist.get(0).getStep_order());

        //判斷mysop是否是空的
        if(sopmasterlist.size()!=0) {
            int k = 0;
            if (sopmasterlist.size() % 2 == 0) {
                x = sopmasterlist.size() / 2;
            } else {
                x = (sopmasterlist.size() + 1) / 2;
            }


            list = new String[x];
            name = new String[x];
            key = new int[x];
            timesee = new String[x];
            photo = new Drawable[x];
            bmplist = new Bitmap[x];
            steporder = new String[x];
            steptotal = new String[x];
            list1 = new String[sopmasterlist.size() / 2];
            name1 = new String[sopmasterlist.size() / 2];
            key1 = new int[sopmasterlist.size() / 2];
            timesee1 = new String[sopmasterlist.size() / 2];
            photo1 = new Drawable[sopmasterlist.size() / 2];
            bmplist1 = new Bitmap[sopmasterlist.size()/ 2];
            steporder1 = new String[sopmasterlist.size() / 2];
            steptotal1 = new String[sopmasterlist.size() / 2];

            for (int i = 0; i < x; i++) {
                List<case_masterVo>casemasterlist=null;
                casemasterlist = mcase_masterDao.selectRaw2(mDatabaseHelper,"Account ="+"'"+TAG_ACCOUNT+"'","Sop_number ="+"'"+sopmasterlist.get(i).getSop_number()+"'","Case_mark ="+"'"+"0"+"'");
                list[i]=casemasterlist.get(0).getCase_number();
                Log.d("案號",casemasterlist.get(0).getCase_number());
                //list[i] = caselist.get(i).getCase_number();
                name[i] = sopmasterlist.get(i).getSop_name();
                //圖片
//            byte bytes[] = Base64.decode(sopmasterlist.get(i).getSop_graph_src(), Base64.DEFAULT);
//            bmplist[i] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Drawable drawable = new BitmapDrawable(bmplist[i]);
//            photo[i]=drawable;
                //
                steporder[i] = sopdetaillist.get(i).getStep_order();
                List<sop_detailVo> listforcount = null;
                //listforcount = msop_detailDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='" + TAG_ACCOUNT + "')");
                listforcount = msop_detailDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number");
                steptotal[i] = String.valueOf(listforcount.size());
                //steptotal[i]=productsList1.get(i).get(TAG_TATOL);
                switch (sopdetaillist.get(i).getStart_rule()) {
                    case "1":
                        // cagetory.setText("人工啟動");
                        key[i] = 4;
                        break;
                    case "2":
                        //cagetory.setText("前一步驟\n完工");
                        key[i] = 4;
                        break;
                    case "3":
                        //cagetory.setText("Beacon");
                        key[i] = 1;
                        break;
                    case "4":
                        //cagetory.setText("QR code");
                        key[i] = 3;
                        break;
                    case "5":
                        //cagetory.setText("NFC");
                        key[i] = 0;
                        break;
                    case "6":
                        //cagetory.setText("定位");
                        key[i] = 2;
                        break;
                    case "7":
                        //cagetory.setText("時間到期");
                        key[i] = 4;
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                        Date now = null;
                        System.out.println("TIME is " + sopdetaillist.get(i).getStart_value1());
                        System.out.println("NOW is " + str);

                        try {
                            now = df.parse(sopdetaillist.get(i).getStart_value1());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date date = null;
                        try {
                            date = df.parse(str);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long l;

                        //比較時間大小
                        if (now.getTime() > date.getTime()) {
                            //未過期
                            l = now.getTime() - date.getTime();
                            check = 0;
                        } else {
                            //過期
                            l = date.getTime() - now.getTime();
                            check = 1;
                        }
                        long l2 = l / (30 * 24 * 60 * 60);

                        //計算時間差
                        long month = l2 / 1000;
                        long day = l / (24 * 60 * 60 * 1000) - month * 30;
                        long hour = (l / (60 * 60 * 1000) - month * 30 * 24 - day * 24);
                        long min = ((l / (60 * 1000)) - month * 30 * 24 * 60 - day * 24 * 60 - hour * 60);
                        long s = (l / 1000 - month * 30 * 24 * 60 * 60 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                        System.out.println(month + "月" + day + "天" + hour + "小时" + min + "分" + s + "秒");

                        if (check == 0) {

                            if (month <= 0) {
                                timedifference = "還差" + day + "天";
                            } else if (month <= 0 && day == 0) {
                                timedifference = "還差" + hour + "小时" + min + "分";
                            } else if (month <= 0 && day == 0 && hour == 0) {
                                timedifference = "還差" + min + "分";
                            } else {
                                timedifference = "還差" + month + "月" + day + "天";
                            }
                        } else {
                            //過期
                            // timedifference.setTextColor(Color.RED);
                            if (month == 0) {
                                timedifference = "過期" + day + "天";
                            } else if (month == 0 && day == 0) {
                                timedifference = "過期" + hour + "小时" + min + "分";
                            } else if (month == 0 && day == 0 && hour == 0) {
                                timedifference = "過期" + min + "分";
                            } else {
                                timedifference = "過期" + month + "月" + day + "天";
                            }
                        }
                        timesee[i] = timedifference;
                        break;
                }
            }

            //另一邊
            for (int i = sopmasterlist.size() - 1; i >= x; i--) {
                List<case_masterVo>casemasterlist=null;
                casemasterlist = mcase_masterDao.selectRaw2(mDatabaseHelper, "Account =" + "'" + TAG_ACCOUNT + "'", "Sop_number =" + "'" + sopmasterlist.get(i).getSop_number()+"'","Case_mark ="+"'"+"0"+"'");
                list1[k]=casemasterlist.get(0).getCase_number();
               // list1[k] = caselist.get(i).getCase_number();
                name1[k] = sopmasterlist.get(i).getSop_name();
                //圖片
//            byte bytes[] = Base64.decode(sopmasterlist.get(i).getSop_graph_src(), Base64.DEFAULT);
//            bmplist1[k] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Drawable drawable = new BitmapDrawable(bmplist1[k]);
//            photo1[k]=drawable;
                //
                steporder1[k] = sopdetaillist.get(i).getStep_order();
                //steptotal1[k]=productsList1.get(i).get(TAG_TATOL);
                List<sop_detailVo> listforcount1 = null;
                //listforcount = msop_detailDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='" + TAG_ACCOUNT + "')");
                listforcount1 = msop_detailDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number");
                steptotal1[k] = String.valueOf(listforcount1.size());
                switch (sopdetaillist.get(i).getStart_rule()) {
                    case "1":
                        // cagetory.setText("人工啟動");
                        key1[k] = 4;
                        break;
                    case "2":
                        //cagetory.setText("前一步驟\n完工");
                        key1[k] = 4;
                        break;
                    case "3":
                        //cagetory.setText("Beacon");
                        key1[k] = 1;
                        break;
                    case "4":
                        //cagetory.setText("QR code");
                        key1[k] = 3;
                        break;
                    case "5":
                        //cagetory.setText("NFC");
                        key1[k] = 0;
                        break;
                    case "6":
                        //cagetory.setText("定位");
                        key1[k] = 2;
                        break;
                    case "7":
                        //cagetory.setText("時間到期");
                        key1[k] = 4;
                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                        Date now = null;

                        try {
                            now = df.parse(sopdetaillist.get(i).getStart_value1());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date date = null;
                        try {
                            date = df.parse(str);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long l;

                        //比較時間大小
                        if (now.getTime() > date.getTime()) {
                            //未過期
                            l = now.getTime() - date.getTime();
                            check = 0;
                        } else {
                            //過期
                            l = date.getTime() - now.getTime();
                            check = 1;
                        }
                        long l2 = l / (30 * 24 * 60 * 60);

                        //計算時間差
                        long month = l2 / 1000;
                        long day = l / (24 * 60 * 60 * 1000) - month * 30;
                        long hour = (l / (60 * 60 * 1000) - month * 30 * 24 - day * 24);
                        long min = ((l / (60 * 1000)) - month * 30 * 24 * 60 - day * 24 * 60 - hour * 60);
                        long s = (l / 1000 - month * 30 * 24 * 60 * 60 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                        System.out.println(month + "月" + day + "天" + hour + "小时" + min + "分" + s + "秒");
                        if (check == 0) {

                            if (month == 0) {
                                timedifference = "還差" + day + "天";
                            } else if (month == 0 && day == 0) {
                                timedifference = "還差" + hour + "小时" + min + "分";
                            } else if (month == 0 && day == 0 && hour == 0) {
                                timedifference = "還差" + min + "分";
                            } else {
                                timedifference = "還差" + month + "月" + day + "天";
                            }
                        } else {
                            //過期
                            // timedifference.setTextColor(Color.RED);
                            if (month == 0) {
                                timedifference = "過期" + day + "天";
                            } else if (month == 0 && day == 0) {
                                timedifference = "過期" + hour + "小时" + min + "分";
                            } else if (month == 0 && day == 0 && hour == 0) {
                                timedifference = "過期" + min + "分";
                            } else {
                                timedifference = "過期" + month + "月" + day + "天";
                            }
                        }
                        timesee1[k] = timedifference;
                        break;
                }
                k++;
            }

            adapter = new MyAdapter(Mysop.this);
            adapter1 = new MyAdapter1(Mysop.this);
            listInput.setAdapter(adapter);
            listInput1.setAdapter(adapter1);

            listInput.setOnItemClickListener(listener);
            listInput1.setOnItemClickListener(listener1);
        }else{

        }
    }



    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GCM", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mysop, menu);
        return true;
    }

    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(getApplicationContext(), "你選擇的是" + list[position], Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putString("TAG_CASE_NUMBER", list[position]);

            if(steporder[position].equals("1")){

                Intent it = new Intent(Mysop.this,Start.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }else{
                Intent it = new Intent(Mysop.this,StepActionControl.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }

        }

    };

    private ListView.OnItemClickListener listener1 = new ListView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(getApplicationContext(), "你選擇的是" + list1[position], Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putString("TAG_CASE_NUMBER", list1[position]);

            if(steporder1[position].equals("1")){

                Intent it = new Intent(Mysop.this,Start.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }else{
                Intent it = new Intent(Mysop.this,StepActionControl.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }

        }

    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.title_activity_changepassword:
                startActivity(new Intent().setClass(Mysop.this, Changepassword.class));
                break;
            case R.id.title_activity_searching:
                //startActivity(new Intent().setClass(Mysop.this, Search.class));
                Intent i = new Intent(this, Search.class);
                Bundle bundle = new Bundle();
                Bundle bundle2 = new Bundle();
                bundle.putString("TAG_Key", "");
                bundle2.putString("TAG_ACCOUNT", TAG_ACCOUNT);
                i.putExtras(bundle);	//將參數放入intent
                i.putExtras(bundle2);
                startActivity(i);
                break;
            case R.id.title_activity_dynamic_action:
                startActivity(new Intent().setClass(Mysop.this, DynamicAction.class));
                break;
            case R.id.title_activity_logout:
                startActivity(new Intent().setClass(Mysop.this, Logout.class));

                return super.onOptionsItemSelected(item);
        }
        //少下面的return
        return false;
    }

    //圖片網址
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
//    class LoadAllProducts extends AsyncTask<String, String, String> {
//
//        /**
//         * Before starting background thread Show Progress Dialog
//         * */
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pDialog = new ProgressDialog(Mysop.this);
//            pDialog.setMessage("Loading products. Please wait...");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//
//        /**
//         * getting All products from url
//         * */
//        protected String doInBackground(String... args) {
//            // Building Parameters
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("Account", TAG_ACCOUNT) );
//            // getting JSON string from URL
//            JSONObject json = Mysop.this.jsonParser.makeHttpRequest(Mysop.url_all_products,"GET", params);
//            JSONObject json1 = Mysop.this.jsonParser.makeHttpRequest(Mysop.url_all_products1,"GET", params);
//
//            // Check your log cat for JSON reponse
//            Log.d("All Products: ", json.toString());
//
//            try {
//                // Checking for SUCCESS TAG
//                int success = json.getInt(TAG_SUCCESS);
//
//                if (success == 1) {
//                    // products found
//                    // Getting Array of Products
//                    products = json.getJSONArray(TAG_PRODUCTS);
//
//                    // looping through All Products
//                    for (int i = 0; i < products.length(); i++) {
//                        JSONObject c = products.getJSONObject(i);
//
//                        // Storing each json item in variable
//                        String sopname = c.getString(TAG_SOPNAME);
//                        String sopnumber = c.getString(TAG_CASENUMBER);
//                        String startrule = c.getString(TAG_STARTRULE);
//                        String startvalue = c.getString(TAG_STARTVALUE);
//                        String picture = c.getString(TAG_PICTURE);
//                        String order = c.getString(TAG_ORDER);
//
//                        // creating new HashMap
//                        HashMap<String, String> map = new HashMap<String, String>();
//
//                        // adding each child node to HashMap key => value
//                        map.put(TAG_SOPNAME, sopname);
//                        map.put(TAG_CASENUMBER, sopnumber);
//                        map.put(TAG_STARTRULE, startrule);
//                        map.put(TAG_STARTVALUE,startvalue);
//                        map.put(TAG_PICTURE,picture);
//                        map.put(TAG_ORDER,order);
//
//
//                        // adding HashList to ArrayList
//                        productsList.add(map);
//                    }
//                } else {
//
//
//                }
//                // Checking for SUCCESS TAG
//                int success1 = json1.getInt(TAG_SUCCESS);
//
//                if (success1 == 1) {
//                    // products found
//                    // Getting Array of Products
//                    products1 = json1.getJSONArray(TAG_PRODUCTS);
//
//                    // looping through All Products
//                    for (int i = 0; i < products1.length(); i++) {
//                        JSONObject c = products1.getJSONObject(i);
//
//                        // Storing each json item in variable
//                        String total = c.getString(TAG_TATOL);
//                        String number = c.getString(TAG_SOPNUMBER);
//
//                        // creating new HashMap
//                        HashMap<String, String> map = new HashMap<String, String>();
//
//                        // adding each child node to HashMap key => value
//                        map.put(TAG_TATOL, total);
//                        map.put(TAG_SOPNUMBER, number);
//
//                        // adding HashList to ArrayList
//                        productsList1.add(map);
//                    }
//                } else {
//
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        /**
//         * After completing background task Dismiss the progress dialog
//         * **/
//        protected void onPostExecute(String file_url) {
//            // dismiss the dialog after getting all products
//            pDialog.dismiss();
//
//            int k=0;
//            if(products.length()%2==0){
//                x=products.length()/2;
//            }else{
//                x=(products.length()+1)/2;
//            }
//
//            list = new String[x];
//            name = new String[x];
//            key = new int[x];
//            timesee = new String[x];
//            photo = new String[x];
//            steporder = new String[x];
//            steptotal = new String[x];
//            list1 = new String[products.length()/2];
//            name1 = new String[products.length()/2];
//            key1 = new int[products.length()/2];
//            timesee1 = new String[products.length()/2];
//            photo1 = new String[products.length()/2];
//            steporder1 = new String[products.length()/2];
//            steptotal1 = new String[products.length()/2];
//            // updating UI from Background Thread
//            for (int i = 0; i < x; i++) {
//                list[i] = productsList.get(i).get(TAG_CASENUMBER);
//                name[i] = productsList.get(i).get(TAG_SOPNAME);
//                photo[i]=productsList.get(i).get(TAG_PICTURE);
//                steporder[i]=productsList.get(i).get(TAG_ORDER);
//                steptotal[i]=productsList1.get(i).get(TAG_TATOL);
//                switch (productsList.get(i).get(TAG_STARTRULE)){
//                    case "1":
//                       // cagetory.setText("人工啟動");
//                        key[i]=4;
//                        break;
//                    case "2":
//                        //cagetory.setText("前一步驟\n完工");
//                        key[i]=4;
//                        break;
//                    case "3":
//                        //cagetory.setText("Beacon");
//                        key[i]=1;
//                        break;
//                    case "4":
//                        //cagetory.setText("QR code");
//                        key[i]=3;
//                        break;
//                    case "5":
//                        //cagetory.setText("NFC");
//                        key[i]=0;
//                        break;
//                    case "6":
//                        //cagetory.setText("定位");
//                        key[i]=2;
//                        break;
//                    case "7":
//                        //cagetory.setText("時間到期");
//                        key[i]=4;
//                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
//                        Date now = null;
//                        System.out.println("TIME is "+productsList.get(i).get(TAG_STARTVALUE));
//                        System.out.println("NOW is "+str);
//
//                        try {
//                            now = df.parse(productsList.get(i).get(TAG_STARTVALUE));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        Date date = null;
//                        try {
//                            date = df.parse(str);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        long l;
//
//                        //比較時間大小
//                        if(now.getTime()>date.getTime()) {
//                            //未過期
//                            l = now.getTime() - date.getTime();
//                            check=0;
//                        }else{
//                            //過期
//                            l = date.getTime() - now.getTime();
//                            check=1;
//                        }
//                        long l2 = l/(30*24*60*60);
//
//                        //計算時間差
//                        long month=l2/1000;
//                        long day = l / (24 * 60 * 60 * 1000)- month * 30;
//                        long hour = (l / (60 * 60 * 1000) - month * 30 * 24 - day * 24);
//                        long min = ((l / (60 * 1000)) - month * 30 * 24 * 60- day * 24 * 60 - hour * 60);
//                        long s = (l / 1000 - month * 30 * 24 * 60 * 60- day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
//                        System.out.println(month+"月" + day + "天" + hour + "小时" + min + "分" + s + "秒");
//
//                        if(check==0) {
//
//                            if (month <= 0) {
//                                timedifference="還差" + day + "天" ;
//                            } else if (month <= 0 && day == 0) {
//                                timedifference="還差" + hour + "小时" + min + "分";
//                            } else if (month <= 0 && day == 0 && hour == 0) {
//                                timedifference="還差" + min + "分";
//                            } else {
//                                timedifference="還差" + month + "月" + day + "天";
//                            }
//                        }else{
//                            //過期
//                           // timedifference.setTextColor(Color.RED);
//                            if (month == 0) {
//                                timedifference="過期" + day + "天" ;
//                            } else if (month == 0 && day == 0) {
//                                timedifference="過期" + hour + "小时" + min + "分";
//                            } else if (month == 0 && day == 0 && hour == 0) {
//                                timedifference="過期" + min + "分";
//                            } else {
//                                timedifference="過期" + month + "月" + day + "天";
//                            }
//                        }
//                        timesee[i]=timedifference;
//                        break;
//                }
//            }
//
//            //另一邊
//            for (int i = products.length()-1; i >=x; i--) {
//
//
//                list1[k] = productsList.get(i).get(TAG_CASENUMBER);
//                name1[k] = productsList.get(i).get(TAG_SOPNAME);
//                photo1[k]=productsList.get(i).get(TAG_PICTURE);
//                steporder1[k]=productsList.get(i).get(TAG_ORDER);
//                steptotal1[k]=productsList1.get(i).get(TAG_TATOL);
//                switch (productsList.get(i).get(TAG_STARTRULE)){
//                    case "1":
//                        // cagetory.setText("人工啟動");
//                        key1[k]=4;
//                        break;
//                    case "2":
//                        //cagetory.setText("前一步驟\n完工");
//                        key1[k]=4;
//                        break;
//                    case "3":
//                        //cagetory.setText("Beacon");
//                        key1[k]=1;
//                        break;
//                    case "4":
//                        //cagetory.setText("QR code");
//                        key1[k]=3;
//                        break;
//                    case "5":
//                        //cagetory.setText("NFC");
//                        key1[k]=0;
//                        break;
//                    case "6":
//                        //cagetory.setText("定位");
//                        key1[k]=2;
//                        break;
//                    case "7":
//                        //cagetory.setText("時間到期");
//                        key1[k]=4;
//                        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
//                        Date now = null;
//
//                        try {
//                            now = df.parse(productsList.get(i).get(TAG_STARTVALUE));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        Date date = null;
//                        try {
//                            date = df.parse(str);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        long l;
//
//                        //比較時間大小
//                        if(now.getTime()>date.getTime()) {
//                            //未過期
//                            l = now.getTime() - date.getTime();
//                            check=0;
//                        }else{
//                            //過期
//                            l = date.getTime() - now.getTime();
//                            check=1;
//                        }
//                        long l2 = l/(30*24*60*60);
//
//                        //計算時間差
//                        long month=l2/1000;
//                        long day = l / (24 * 60 * 60 * 1000)- month * 30;
//                        long hour = (l / (60 * 60 * 1000) - month * 30 * 24 - day * 24);
//                        long min = ((l / (60 * 1000)) - month * 30 * 24 * 60- day * 24 * 60 - hour * 60);
//                        long s = (l / 1000 - month * 30 * 24 * 60 * 60- day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
//                        System.out.println(month+"月" + day + "天" + hour + "小时" + min + "分" + s + "秒");
//                        if(check==0) {
//
//                            if (month == 0) {
//                                timedifference="還差" + day + "天" ;
//                            } else if (month == 0 && day == 0) {
//                                timedifference="還差" + hour + "小时" + min + "分";
//                            } else if (month == 0 && day == 0 && hour == 0) {
//                                timedifference="還差" + min + "分";
//                            } else {
//                                timedifference="還差" + month + "月" + day + "天";
//                            }
//                        }else{
//                            //過期
//                            // timedifference.setTextColor(Color.RED);
//                            if (month == 0) {
//                                timedifference="過期" + day + "天" ;
//                            } else if (month == 0 && day == 0) {
//                                timedifference="過期" + hour + "小时" + min + "分";
//                            } else if (month == 0 && day == 0 && hour == 0) {
//                                timedifference="過期" + min + "分";
//                            } else {
//                                timedifference="過期" + month + "月" + day + "天";
//                            }
//                        }
//                        timesee1[k]=timedifference;
//                        break;
//                }
//                k++;
//            }
//
//
//            adapter = new MyAdapter(Mysop.this);
//            adapter1= new MyAdapter1(Mysop.this);
//            listInput.setAdapter(adapter);
//            listInput1.setAdapter(adapter1);
//
//            listInput.setOnItemClickListener(listener);
//            listInput1.setOnItemClickListener(listener1);
//        }
//
//    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater myInflater;


        public MyAdapter(Context c) {
            myInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return name.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return name[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = myInflater.inflate(R.layout.myxml, null);

            ImageView Logo = (ImageView) convertView.findViewById(R.id.imglogo);
            TextView Name = (TextView) convertView.findViewById(R.id.name);
            TextView number = (TextView) convertView
                    .findViewById(R.id.txtengname);
            TextView time = (TextView)convertView.findViewById(R.id.timetext);
            ImageView MysopLogo = (ImageView) convertView.findViewById(R.id.mysoplogo);
            ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progresssop);

           // new DownloadImageTask(MysopLogo)
           //         .execute(photo[position]);
            //MysopLogo.setImageDrawable(photo[position]);
            if(logos[key[position]]!=R.drawable.white){
                Logo.setVisibility(0);
                time.setVisibility(8);
            }
            Logo.setImageResource(logos[key[position]]);
            Name.setText(" "+name[position]);
            number.setText(list[position]);
            time.setText(timesee[position]);
            progressBar.setMax(Integer.valueOf(steptotal[position]));
            progressBar.setProgress(Integer.valueOf(steporder[position]) - 1);

            return convertView;
        }

    }

    public class MyAdapter1 extends BaseAdapter {
        private LayoutInflater myInflater;


        public MyAdapter1(Context c) {
            myInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return name1.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return name1[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = myInflater.inflate(R.layout.myxml, null);

            ImageView Logo1 = (ImageView) convertView.findViewById(R.id.imglogo);
            TextView Name1 = (TextView) convertView.findViewById(R.id.name);
            TextView number1 = (TextView) convertView
                    .findViewById(R.id.txtengname);
            TextView time1 = (TextView)convertView.findViewById(R.id.timetext);
            ImageView MysopLogo1 = (ImageView) convertView.findViewById(R.id.mysoplogo);
            ProgressBar progressBar1 = (ProgressBar)convertView.findViewById(R.id.progresssop);


//            new DownloadImageTask(MysopLogo1)
//                    .execute(photo1[position]);
         //   MysopLogo1.setImageDrawable(photo1[position]);
            if(logos[key1[position]]!=R.drawable.white){
                Logo1.setVisibility(0);
                time1.setVisibility(8);
            }
            Logo1.setImageResource(logos[key1[position]]);
            Name1.setText(" " + name1[position]);
            number1.setText(list1[position]);
            time1.setText(timesee1[position]);
            progressBar1.setMax(Integer.valueOf(steptotal1[position]));
            progressBar1.setProgress(Integer.valueOf(steporder1[position])-1);

            return convertView;
        }

    }
}