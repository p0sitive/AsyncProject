package com.example.administrator.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Administrator on 2015/6/7.
 */
public class ListAdapter extends BaseAdapter implements AbsListView.OnScrollListener{
    List<NewsModel> mList;
    Context context;
    private  imageloader imageloader;
    private int iStart,iEnd;
    public static String [] URLS;

    private boolean mIsFirst;

    public ListAdapter(Context context,List<NewsModel>list,ListView listView){
        this.context=context;
        this.mList=list;
        imageloader=new imageloader(listView);
        URLS=new String[list.size()];
        for(int i=0;i<list.size();i++){
            URLS[i]=list.get(i).imgUrl;
        }
        //注册相对应的事件
        listView.setOnScrollListener(this);
        mIsFirst=true;
    }



    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder=null;
        if(view==null){
            viewHolder=new ViewHolder();
            view= LayoutInflater.from(context).inflate(R.layout.item_layout, null);
            viewHolder.context=(TextView)view.findViewById(R.id.tv_context);
            viewHolder.icon=(ImageView)view.findViewById(R.id.iv_icon);
            viewHolder.title=(TextView)view.findViewById(R.id.tv_title);
            view.setTag(viewHolder);
        }
        else {
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.icon.setImageResource(R.mipmap.ic_launcher);
//        new imageloader().showImageByThread(viewHolder.icon,
//                mList.get(i).imgUrl);
        imageloader.showImageByAsyncTask(viewHolder.icon,
                mList.get(i).imgUrl);
        viewHolder.icon.setTag(mList.get(i).imgUrl);
        viewHolder.title.setText(mList.get(i).title);
        viewHolder.context.setText(mList.get(i).content);
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if(i==SCROLL_STATE_IDLE){
            //加载可见项
            imageloader.loadImages(iStart,iEnd);
        }else {
            //停止可见项
            imageloader.cancelAllTask();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        iStart=i;
        iEnd=i+i1;
        if(mIsFirst&&i2>0){//第一次调用，初始第一屏count大于0
            imageloader.loadImages(iStart,iEnd);
            mIsFirst=false;
        }
    }

    class ViewHolder{
        public TextView title,context;
        public ImageView icon;
    }
}
