package com.ncu.efpg.mysop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.json.JSONArray;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
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

public class DynamicAction extends Activity {

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    ArrayList<HashMap<String, String>> productsList;
    ArrayList<HashMap<String, String>> productsList1;
    //private static String url_all_products = "http://localhost:8080/kelly/test_getall.jsp";
    //private static String url_all_products = "http://140.115.80.237/front/mysop_mysop.jsp";
    private static String url_all_products = "http://140.115.82.211/front/mysop_dynamicAction.jsp";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_CASENUMBER = "casenumber";
    private static final String TAG_SOPNAME = "sopname";
    private static final String TAG_STARTRULE = "startrule";
    private static final String TAG_STARTVALUE = "startvalue";
    private static final String TAG_PICTURE = "picture";
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
    String TAG_ACCOUNT ="";
    //String TAG_ACCOUNT = "test@gmail.com";
    String TAG_RULE="";


    //存casenumber  sopname
    private String[] list;
    private String[] name;
    private String[] steporder;
    private String[] steptotal;

    private int[] logos = new int[] { R.drawable.nfc, R.drawable.beacon,
            R.drawable.gps,R.drawable.qrcode,R.drawable.white };
    int[] key;
    private String[] timesee;
    private String[] photo;
    private Bitmap[] bmplist;

    private String[] list1;
    private String[] name1;
    int[] key1;
    private String[] timesee1;
    private String[] photo1;
    private Bitmap[] bmplist1;
    private String[] steporder1;
    private String[] steptotal1;

    private ScrollView dynamicscroll;

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
        setContentView(R.layout.activity_dynamic_action);
        listInput = (ListView) findViewById(R.id.list_dynamic);
        listInput1 = (ListView) findViewById(R.id.list_dynamic2);
        // adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);

        dynamicscroll=(ScrollView)findViewById(R.id.dynamicscroll);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        TAG_ACCOUNT = bundle.getString("TAG_ACCOUNT");
        if(intent.hasExtra("TAG_RULE")) {
            TAG_RULE = bundle.getString("TAG_RULE");
        }


        // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();
        productsList1 = new ArrayList<HashMap<String, String>>();
        // Loading products in Background Thread
        // new LoadAllProducts().execute();

        // TAG_ACCOUNT = bundle.getString("TAG_ACCOUNT");	//輸出Bundle內容

        //時間
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        str = formatter.format(curDate);

        //orm 用stepnumber去抓資料庫的東西

        mcase_masterDao = new case_masterDao();
        DatabaseHelper mDatabaseHelper = DatabaseHelper.getHelper(this);
        List<case_masterVo> caselist = null;

        msop_masterDao = new sop_masterDao();
        List<sop_masterVo> sopmasterlist = null;
        // sopmasterlist = msop_masterDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='"+TAG_ACCOUNT+"')");

        List<mysopVo> mysoplist = null;

        //List<sop_detailVo> sopdetaillist = null;
        msop_detailDao = new sop_detailDao();
        if(TAG_RULE == "") {
            sopmasterlist = msop_masterDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number");
            //sopdetaillist = msop_detailDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Step_number");
        }else{
            sopmasterlist = msop_masterDao.selectRawByNest1(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number","Start_rule",TAG_RULE, "Step_number");
            //sopdetaillist = msop_detailDao.selectRawByNest3(mDatabaseHelper, "Account", TAG_ACCOUNT,"Step_number","Start_rule",TAG_RULE);
        }
        //  Log.d("抓2",sopdetaillist.get(0).getStep_order());

        //判斷mysop是否是空的
        if (sopmasterlist.size() != 0) {
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
            photo = new String[x];
            bmplist = new Bitmap[x];
            steporder = new String[x];
            steptotal = new String[x];
            list1 = new String[sopmasterlist.size() / 2];
            name1 = new String[sopmasterlist.size() / 2];
            key1 = new int[sopmasterlist.size() / 2];
            timesee1 = new String[sopmasterlist.size() / 2];
            photo1 = new String[sopmasterlist.size() / 2];
            bmplist1 = new Bitmap[sopmasterlist.size() / 2];
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
                String[] graph = sopmasterlist.get(i).getSop_graph_src().split("/");
                photo[i] ="MYSOPTEST/"+graph[graph.length-1];
//            byte bytes[] = Base64.decode(sopmasterlist.get(i).getSop_graph_src(), Base64.DEFAULT);
//            bmplist[i] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Drawable drawable = new BitmapDrawable(bmplist[i]);
//            photo[i]=drawable;
                //

                //0310，且下面sop_detaillist get(i)=>get(0)
                List<sop_detailVo>sopdetaillist=null;
                sopdetaillist = msop_detailDao.selectRaw(mDatabaseHelper,"Step_number ="+"'"+casemasterlist.get(0).getStep_number()+"'");

                steporder[i] = sopdetaillist.get(0).getStep_order();
                List<sop_detailVo> listforcount = null;
                //listforcount = msop_detailDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='" + TAG_ACCOUNT + "')");
                listforcount = msop_detailDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number");
                steptotal[i] = String.valueOf(listforcount.size());
                //steptotal[i]=productsList1.get(i).get(TAG_TATOL);
                switch (sopdetaillist.get(0).getStart_rule()) {
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
                        System.out.println("TIME is " + sopdetaillist.get(0).getStart_value1());
                        System.out.println("NOW is " + str);

                        try {
                            now = df.parse(sopdetaillist.get(0).getStart_value1());
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
                String[] graph = sopmasterlist.get(i).getSop_graph_src().split("/");
                photo1[k] ="MYSOPTEST/"+graph[graph.length-1];
//            byte bytes[] = Base64.decode(sopmasterlist.get(i).getSop_graph_src(), Base64.DEFAULT);
//            bmplist1[k] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            Drawable drawable = new BitmapDrawable(bmplist1[k]);
//            photo1[k]=drawable;

                //0310同上
                List<sop_detailVo>sopdetaillist=null;
                sopdetaillist = msop_detailDao.selectRaw(mDatabaseHelper,"Step_number ="+"'"+casemasterlist.get(0).getStep_number()+"'");

                steporder1[k] = sopdetaillist.get(0).getStep_order();
                //steptotal1[k]=productsList1.get(i).get(TAG_TATOL);
                List<sop_detailVo> listforcount1 = null;
                //listforcount = msop_detailDao.selectRaw(mDatabaseHelper, "Sop_number IN(SELECT Sop_number FROM case_masterVo WHERE Account='" + TAG_ACCOUNT + "')");
                listforcount1 = msop_detailDao.selectRawByNest(mDatabaseHelper, "Account", TAG_ACCOUNT, "Sop_number");
                steptotal1[k] = String.valueOf(listforcount1.size());
                switch (sopdetaillist.get(0).getStart_rule()) {
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
                            now = df.parse(sopdetaillist.get(0).getStart_value1());
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

            adapter = new MyAdapter(DynamicAction.this);
            adapter1 = new MyAdapter1(DynamicAction.this);
            listInput.setAdapter(adapter);
            listInput1.setAdapter(adapter1);

            listInput.setOnItemClickListener(listener);
            listInput1.setOnItemClickListener(listener1);
        }else{

        }

        dynamicscroll.fullScroll(ScrollView.FOCUS_UP);
        dynamicscroll.smoothScrollTo(0, 0);
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
        getMenuInflater().inflate(R.menu.menu_dynamic_action, menu);
        return true;
    }


    private ListView.OnItemClickListener listener = new ListView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(getApplicationContext(), "你選擇的是" + list[position], Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putString("TAG_CASE_NUMBER", list[position]);

            if(steporder[position].equals("1")){

                Intent it = new Intent(DynamicAction.this,Start.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }else{
                Intent it = new Intent(DynamicAction.this,StepActionControl.class);
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

                Intent it = new Intent(DynamicAction.this,Start.class);
                it.putExtras(bundle);//將參數放入intent
                startActivity(it);
            }else{
                Intent it = new Intent(DynamicAction.this,StepActionControl.class);
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
        switch(id){
            case R.id.title_section1:
                TAG_RULE = "1";
                Intent intent = new Intent(this, DynamicAction.class);
                intent.putExtra("TAG_RULE",TAG_RULE);
                intent.putExtra("TAG_ACCOUNT",TAG_ACCOUNT);
                startActivity(intent);
                finish();
                break;
            case R.id.title_section2:
                TAG_RULE = "2";
                Intent intent1 = new Intent(this, DynamicAction.class);
                intent1.putExtra("TAG_RULE", TAG_RULE);
                intent1.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent1);
                finish();
                break;
            case R.id.title_section3:
                TAG_RULE = "4";
                Intent intent2 = new Intent(this, DynamicAction.class);
                intent2.putExtra("TAG_RULE",TAG_RULE);
                intent2.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent2);
                finish();
                break;
            case R.id.title_section4:
                TAG_RULE = "5";
                Intent intent3 = new Intent(this, DynamicAction.class);
                intent3.putExtra("TAG_RULE",TAG_RULE);
                intent3.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent3);
                finish();
                break;
            case R.id.title_section5:
                TAG_RULE = "3";
                Intent intent4 = new Intent(this, DynamicAction.class);
                intent4.putExtra("TAG_RULE",TAG_RULE);
                intent4.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent4);
                finish();
                break;
            case R.id.title_section6:
                TAG_RULE = "6";
                Intent intent5 = new Intent(this, DynamicAction.class);
                intent5.putExtra("TAG_RULE",TAG_RULE);
                intent5.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent5);
                finish();
                break;
            case R.id.title_section7:
                TAG_RULE = "7";
                Intent intent6 = new Intent(this, DynamicAction.class);
                intent6.putExtra("TAG_RULE",TAG_RULE);
                intent6.putExtra("TAG_ACCOUNT", TAG_ACCOUNT);
                startActivity(intent6);
                finish();
                return super.onOptionsItemSelected(item);
        }
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

    //讀取SDCard圖片，型態為Bitmap
    private static Bitmap getBitmapFromSDCard(String file)
    {
        try
        {
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


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
            String sd = Environment.getExternalStorageDirectory().toString();
            File file = new File(URI.create(sd + "/" + photo[position]).getPath());
            if (file.exists()) {
                MysopLogo.setImageBitmap(getBitmapFromSDCard(sd+"/"+photo[position]));
            }
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
            String sd = Environment.getExternalStorageDirectory().toString();
            File file = new File(URI.create(sd+"/"+photo1[position]).getPath());
            if (file.exists()) {
                MysopLogo1.setImageBitmap(getBitmapFromSDCard(sd+"/"+photo1[position]));
            }
            Name1.setText(" " + name1[position]);
            number1.setText(list1[position]);
            time1.setText(timesee1[position]);
            progressBar1.setMax(Integer.valueOf(steptotal1[position]));
            progressBar1.setProgress(Integer.valueOf(steporder1[position]) - 1);

            return convertView;
        }

    }
}
