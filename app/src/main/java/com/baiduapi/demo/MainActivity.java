package com.baiduapi.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.baiduapi.demo.activity.BankCardRecognitionActivity;
import com.baiduapi.demo.activity.IdCardRecognitionActivity;
import com.baiduapi.demo.adapter.MainAdapter;
import com.baiduapi.demo.utils.OCRHttpRequest;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnRecyclerViewItemClickListener {
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private ArrayList<String> contentList;
    //判断是否已经获取token，
    private boolean hasGotToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new ProgressDialog(this);
        initDatas();
        initViews();
    }

    private void initDatas() {
        contentList = new ArrayList<>();
        contentList.add("身份证识别");
        contentList.add("银行卡识别");
        getBaiduToken();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MainAdapter(this,contentList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnClickListener(this);

    }

    @Override
    public void onItemClick(int position) {
        if (!hasGotToken){
            //如果不先获取token，后面识别会报异常的
            getBaiduToken();
            Snackbar.make(mRecyclerView,"百度Token获取失败，请稍候重试...",Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent intent = null;
        switch (position){
            case 0:
                //身份证识别
                intent = new Intent(MainActivity.this,IdCardRecognitionActivity.class);
                break;
            case 1:
                //银行卡识别
                intent = new Intent(MainActivity.this,BankCardRecognitionActivity.class);
                break;
            default:
                break;
        }
        if (intent!=null){
            startActivity(intent);
        }
    }

    /**
     * 获取百度云 token
     */
    private void getBaiduToken() {
        showLodingDialog("正在获取token，请稍候...");
        OCRHttpRequest.getBaiduToken(new OCRHttpRequest.OCRCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                hasGotToken = true;
            }

            @Override
            public void onFail(String s) {
                Snackbar.make(mRecyclerView,s,Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onComplete() {
                dismissLoadingDialog();
            }
        });
    }

    private void showLodingDialog(String message) {
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
