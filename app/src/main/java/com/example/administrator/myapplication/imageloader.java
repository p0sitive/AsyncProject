package com.example.administrator.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2015/6/7.
 */
public class imageloader {

    ImageView mImageView;
    String mURL;

    private LruCache<String,Bitmap>mCaches;

    private ListView mListView;
    private Set<NewAsyncTask>mTask;

    public imageloader(ListView listView){
        mListView=listView;
        mTask=new HashSet<>();
        //获取运行内存
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/4;
        mCaches=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存时候使用
                return value.getByteCount();
            }
        };
    }

    //增加到缓存
    public void addBitmap2Cache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null) {
            mCaches.put(url, bitmap);
        }

    }

    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }

    /*
    用来加载从start到end的所有图片
     */
    public void loadImages(int start,int end){
        for(int i=start;i<end;i++){
            String url=ListAdapter.URLS[i];
            //获取缓存中的图片
            Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap == null) {
                //缓存中不存在，则下载
                NewAsyncTask task=new NewAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            } else {
                ImageView imageView= (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mURL)){
            mImageView.setImageBitmap((Bitmap) msg.obj);}
        }
    };

    /*
    使用多线程加载图片，配合Handler使用
     */
    public  void showImageByThread(ImageView imageView,final String url){
        mImageView=imageView;
        mURL=url;
        new Thread(){

            public void run(){
                super.run();
                Bitmap bitmap=getBitmap(url);
                Message message=Message.obtain();
                message.obj=bitmap;
                handler.sendMessage(message);
            }
        }.start();
    }

    //从给定的url获取图片
    public Bitmap getBitmap(String url_){
        Bitmap bitmap;
        InputStream is=null;
        try{
            URL url=new URL(url_);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            is=new BufferedInputStream(connection.getInputStream());
            bitmap= BitmapFactory.decodeStream(is);
            connection.disconnect();
            return  bitmap;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if(is!=null){is.close();}
            }
            catch (IOException e2){
                e2.printStackTrace();
            }
        }
        return  null ;
    }

    //异步获取图片
    public void showImageByAsyncTask(ImageView imageView,String url) {
        //获取缓存中的图片
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
        //缓存中不存在，则下载
            //new NewAsyncTask(imageView,url).execute(url);
            //采用loadImages方法，所以异步下载方法暂不使用
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /*
    取消任务
     */
    public void cancelAllTask(){
        if(mTask!=null){
            for(NewAsyncTask newAsyncTask:mTask){
                newAsyncTask.cancel(false);
            }
        }
    }
    public class NewAsyncTask extends AsyncTask<String ,Void,Bitmap>{

        ImageView mImageView;
        String mURL;
        public NewAsyncTask(ImageView imageView,String url){
            mImageView=imageView;
            mURL=url;
        }
        public NewAsyncTask(String url){
            mURL=url;
        }
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap=getBitmap(strings[0]);
            if(bitmap!=null) {
                addBitmap2Cache(mURL, bitmap);//将下载的图片存入缓存
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(mImageView==null) {
                mImageView = (ImageView) mListView.findViewWithTag(mURL);
            }
            if (mImageView.getTag().equals(mURL) && bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
