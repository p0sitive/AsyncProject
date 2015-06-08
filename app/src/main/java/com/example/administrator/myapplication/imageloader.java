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
        //��ȡ�����ڴ�
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheSize=maxMemory/4;
        mCaches=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //��ÿ�δ��뻺��ʱ��ʹ��
                return value.getByteCount();
            }
        };
    }

    //���ӵ�����
    public void addBitmap2Cache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null) {
            mCaches.put(url, bitmap);
        }

    }

    //�ӻ����л�ȡ����
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }

    /*
    �������ش�start��end������ͼƬ
     */
    public void loadImages(int start,int end){
        for(int i=start;i<end;i++){
            String url=ListAdapter.URLS[i];
            //��ȡ�����е�ͼƬ
            Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap == null) {
                //�����в����ڣ�������
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
    ʹ�ö��̼߳���ͼƬ�����Handlerʹ��
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

    //�Ӹ�����url��ȡͼƬ
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

    //�첽��ȡͼƬ
    public void showImageByAsyncTask(ImageView imageView,String url) {
        //��ȡ�����е�ͼƬ
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
        //�����в����ڣ�������
            //new NewAsyncTask(imageView,url).execute(url);
            //����loadImages�����������첽���ط����ݲ�ʹ��
            imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /*
    ȡ������
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
                addBitmap2Cache(mURL, bitmap);//�����ص�ͼƬ���뻺��
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
