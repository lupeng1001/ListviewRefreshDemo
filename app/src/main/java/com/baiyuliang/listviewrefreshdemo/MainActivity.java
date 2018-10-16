package com.baiyuliang.listviewrefreshdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements RefreshListView.OnRefreshListener,RefreshListView.OnLoadMoreListener{

    private RefreshListView mListView;
    private List<String> mDatas;
    private ArrayAdapter<String> mAdapter;
    private final static int REFRESH_COMPLETE = 0;
    private final static int LOAD_COMPLETE = 1;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    mListView.setOnRefreshComplete();
                    mAdapter.notifyDataSetChanged();
                    break;
                case LOAD_COMPLETE:
                    mListView.setOnLoadMoreComplete();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (RefreshListView) findViewById(R.id.listview);
        String[] data = new String[]{"下拉刷新", "下拉刷新", "下拉刷新", "下拉刷新",
                "下拉刷新", "下拉刷新", "下拉刷新", "下拉刷新", "下拉刷新",
                "下拉刷新", "下拉刷新", "下拉刷新", "下拉刷新", "下拉刷新",};
        mDatas = new ArrayList<>(Arrays.asList(data));
        mAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, mDatas);
        View view = LayoutInflater.from(this).inflate(R.layout.list_headview_layout,null);
        mListView.addHeaderView(view);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadMoreListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    mDatas.add(0, "new下拉刷新");
                    mHandler.sendEmptyMessage(REFRESH_COMPLETE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onLoadMore() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    mDatas.add("more下拉刷新");
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
