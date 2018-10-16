package com.baiyuliang.listviewrefreshdemo;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by baiyuliang on 15/12/2.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final int REFRESH_DONE = 0;//下拉刷新完成
    private static final int PULL_TO_REFRESH = 1;//下拉中（下拉高度未超出headview高度）
    private static final int RELEASE_TO_REFRESH = 2;//准备刷新（下拉高度超出headview高度）
    private static final int REFRESHING = 3;//刷新中
    private static final float REFRESH_RATIO = 3.0f;//下拉系数,越大下拉灵敏度越低
    private LinearLayout headerView;//headerView布局
    private int headerViewHeight;//headerView高度
    private int refreshstate;//下拉刷新状态
    private boolean isScrollFirst;//是否滑动到顶部
    private boolean isRefreshable;//是否启用下拉刷新
    private RefreshAnimView refreshAnimView;
    private ProgressBar pb;
    //新增动画imageview对象
    private ImageView donghua_image;

    private static final int LOAD_DONE = 4;//上拉加载完成
    private static final int PULL_TO_LOAD = 5;//上拉中（上拉高度未超出footerview高度）
    private static final int RELEASE_TO_LOAD = 6;//上拉中（上拉高度超出footerview高度）
    private static final int LOADING = 7;//加载中
    private static final float LOAD_RATIO = 1;//上拉系数
    private LinearLayout footerView;//footerView布局
    private int footerViewHeight;//footerView高度
    private int loadstate;//上拉加载状态
    private boolean isScrollLast;//是否滑动到底部
    private int totalcount;//item总数量
    private boolean isLoadable;//是否启用上拉加载
    private TextView tv_load;//footview布局中显示的文字
    private ProgressBar load_progress;//加载更多的进度条



    private float startY,//手指落点
            offsetY;//手指滑动的距离

    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    //监听接口
    private OnRefreshListener mOnRefreshListener;
    private OnLoadMoreListener mOnLoadMoreListener;

    public RefreshListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化view
     *
     * @param context
     */
    private void init(Context context) {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setOnScrollListener(this);
        headerView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.listview_head_view, null, false);
        refreshAnimView = (RefreshAnimView) headerView.findViewById(R.id.refreshAnimView);
        pb= (ProgressBar) headerView.findViewById(R.id.pb);
        donghua_image = (ImageView)headerView.findViewById(R.id.donghua_image);
        footerView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.listview_foot_view, null, false);
        tv_load = (TextView) footerView.findViewById(R.id.tv_load);
        load_progress = (ProgressBar)footerView.findViewById(R.id.load_progress);
        measureView(headerView);
        measureView(footerView);
        addHeaderView(headerView);
        addFooterView(footerView);

        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);

        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, 0, 0, -footerViewHeight);

        //初始化刷新状态
        refreshstate = REFRESH_DONE;
        //初始化加载状态
        loadstate = LOAD_DONE;

        //默认启用
        isRefreshable = true;
        isLoadable = true;
    }


    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        totalcount = totalItemCount;
        if (firstVisibleItem == 0) {
            isScrollFirst = true;//滑动到顶部
        } else {
            isScrollFirst = false;
        }
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            isScrollLast = true;//滑动到底部
        } else {
            isScrollLast = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetY = ev.getY() - startY;
                /**
                 * 下拉刷新
                 */
                if (isRefreshable &&offsetY > 0 && loadstate == LOAD_DONE && isScrollFirst && refreshstate != REFRESHING) {
                    float headerViewShowHeight = offsetY / REFRESH_RATIO;
                    float scaleProgress=headerViewShowHeight/headerViewHeight;//图片放大缩小比例
                    if(scaleProgress>1){
                        scaleProgress=1;
                    }
                    switch (refreshstate) {
                        case REFRESH_DONE:
                            refreshstate = PULL_TO_REFRESH;
                            break;
                        case PULL_TO_REFRESH:
                            setSelection(0);
                            if (headerViewShowHeight - headerViewHeight >= 0) {
                                refreshstate = RELEASE_TO_REFRESH;
                                changeHeaderByState(refreshstate);
                            }
                            break;
                        case RELEASE_TO_REFRESH:
                            setSelection(0);
                            if (headerViewShowHeight - headerViewHeight < 0) {
                                refreshstate = PULL_TO_REFRESH;
                                changeHeaderByState(refreshstate);
                            }
                            break;
                    }

                    if (refreshstate == PULL_TO_REFRESH || refreshstate == RELEASE_TO_REFRESH) {
                        headerView.setPadding(0, (int) (headerViewShowHeight - headerViewHeight), 0, 0);
                        refreshAnimView.setCurrentProgress(scaleProgress);//设置refreshAnimView的缩放进度
                        refreshAnimView.invalidate();//重绘
                    }
                }
                /**
                 * 上拉加载更多
                 */
                if (isLoadable&&offsetY < 0 && refreshstate == REFRESH_DONE && isScrollLast && loadstate != LOADING) {
                    float footerViewShowHeight = -offsetY / LOAD_RATIO;
                    switch (loadstate) {
                        case LOAD_DONE:
                            loadstate = PULL_TO_LOAD;
                            break;
                        case PULL_TO_LOAD:
                            setSelection(totalcount);
                            if (footerViewShowHeight - footerViewHeight >= 0) {
                                loadstate = RELEASE_TO_LOAD;
                                changeFooterByState(loadstate);
                            }
                            break;
                        case RELEASE_TO_LOAD:
                            setSelection(totalcount);
                            if (footerViewShowHeight - footerViewHeight < 0) {
                                loadstate = PULL_TO_LOAD;
                                changeFooterByState(loadstate);
                            }
                            break;
                    }

                    if (loadstate == PULL_TO_LOAD || loadstate == RELEASE_TO_LOAD) {
                        footerView.setPadding(0, 0, 0, (int) (footerViewShowHeight - footerViewHeight));
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 下拉刷新
                 */
                if (isRefreshable){
                    if (refreshstate == PULL_TO_REFRESH) {
                        refreshstate = REFRESH_DONE;
                        changeHeaderByState(refreshstate);
                    }
                    if (refreshstate == RELEASE_TO_REFRESH) {
                        refreshstate = REFRESHING;
                        changeHeaderByState(refreshstate);
                        mOnRefreshListener.onRefresh();
                    }
                }

                /**
                 * 上拉加载
                 */
                if (isLoadable){
                    if (loadstate == PULL_TO_LOAD) {
                        loadstate = LOAD_DONE;
                        changeFooterByState(loadstate);
                    }
                    if (loadstate == RELEASE_TO_LOAD) {
                        loadstate = LOADING;
                        changeFooterByState(loadstate);
                        mOnLoadMoreListener.onLoadMore();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 改变headview状态
     *
     * @param state
     */
    private void changeHeaderByState(int state) {
        switch (state) {
            case REFRESH_DONE:
                headerView.setPadding(0, -headerViewHeight, 0, 0);
                refreshAnimView.setVisibility(View.VISIBLE);
                AnimationDrawable mAnimationDrawable2= (AnimationDrawable) donghua_image.getDrawable();
                if(null!=mAnimationDrawable2
                        &&mAnimationDrawable2.isRunning()){
                    //停止播放下拉刷新动画
                    mAnimationDrawable2.stop();
                }
                donghua_image.setVisibility(View.GONE);
                pb.setVisibility(View.GONE);
                break;
            case RELEASE_TO_REFRESH:
                break;
            case PULL_TO_REFRESH:
                break;
            case REFRESHING:
                headerView.setPadding(0, 0, 0, 0);
                refreshAnimView.setVisibility(View.GONE);
                donghua_image.setImageResource(R.drawable.mei_tuan_loading);
                donghua_image.setVisibility(View.VISIBLE);
                AnimationDrawable mAnimationDrawable= (AnimationDrawable) donghua_image.getDrawable();
                mAnimationDrawable.start();
                pb.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 改变footerview状态
     *
     * @param loadstate
     */
    private void changeFooterByState(int loadstate) {
        switch (loadstate) {
            case LOAD_DONE:
                footerView.setPadding(0, 0, 0, -footerViewHeight);
                tv_load.setText("上拉加载更多");
                load_progress.setVisibility(View.GONE);
                break;
            case RELEASE_TO_LOAD:
                tv_load.setText("松开加载更多");
                load_progress.setVisibility(View.GONE);
                break;
            case PULL_TO_LOAD:
                tv_load.setText("上拉加载更多");
                load_progress.setVisibility(View.GONE);
                break;
            case LOADING:
                tv_load.setText("正在加载...");
                footerView.setPadding(0, 0, 0, 0);
                load_progress.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    /**
     * 下拉刷新监听
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * 设置下拉刷新
     *
     * @param onRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    /**
     * 下拉刷新完成
     */
    public void setOnRefreshComplete() {
        refreshstate = REFRESH_DONE;
        changeHeaderByState(refreshstate);
    }

    /**
     * 加载更多监听
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    /**
     * 设置加载更多监听
     *
     * @param onLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    /**
     * 加载更多完成
     */
    public void setOnLoadMoreComplete() {
        loadstate = LOAD_DONE;
        changeFooterByState(loadstate);
    }

    /**
     * 设置是否启用下拉刷新
     *
     * @param isRefreshable
     */
    public void setIsRefreshable(boolean isRefreshable) {
        this.isRefreshable = isRefreshable;
    }

    /**
     * 设置是否启用加载更多
     *
     * @param isLoadable
     */
    public void setIsLoadable(boolean isLoadable) {
        this.isLoadable = isLoadable;
    }

    /**
     * 计算控件宽高
     *
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

}
