package com.example.kelly.mysop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import Ormlite.DatabaseHelper;
import Ormlite.member_accountDao;
import Ormlite.member_accountVo;

public class Logout extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DatabaseHelper mDatabaseHelper = DatabaseHelper.getHelper(this);
        member_accountDao mmember_accountDao = new member_accountDao();
        member_accountVo mmember_accountVo = new member_accountVo();
        mmember_accountDao.delete(mDatabaseHelper,mmember_accountVo);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        Intent i = new Intent(this, Home.class);
        Bundle bundle = new Bundle();
        bundle.putString("TAG_Key", "");
        i.putExtras(bundle);	//將參數放入intent
        startActivity(i);


    }

}