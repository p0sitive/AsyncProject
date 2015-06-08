package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/6/7.
 */
public class MainActivity extends Activity {

    ListView listView;
    String URL="http://www.imooc.com/api/teacher?type=4&num=30";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.lv_main);
        new NewAsyncTask().execute(URL);
    }

    private List<NewsModel> getJsonData(String url){

        List<NewsModel>list=new ArrayList<>();
        try {
            String jsonString = readStream(new URL(url).openStream());
            Log.d("aaa",jsonString);
            JSONObject jsonObject;
            NewsModel newsModel;
            try{
                jsonObject=new JSONObject(jsonString);
                JSONArray jsonArray=jsonObject.getJSONArray("data");
                for(int i=0;i<jsonArray.length();i++){
                    jsonObject=jsonArray.getJSONObject(i);
                    newsModel=new NewsModel();
                    newsModel.content=jsonObject.getString("description");
                    newsModel.imgUrl=jsonObject.getString("picBig");
                    newsModel.title=jsonObject.getString("name");
                    list.add(newsModel);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  list;
    }

    private String readStream(InputStream is){
        InputStreamReader isr;
        String result="";
        try {
            String line="";
            isr = new InputStreamReader(is, "UTF-8");
            BufferedReader bf=new BufferedReader(isr);
            while ((line=bf.readLine())!=null){
                result+=line;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  result;
    }

    class NewAsyncTask extends AsyncTask<String,Void,List<NewsModel>>{
        @Override
        protected List<NewsModel> doInBackground(String... strings) {

            return getJsonData(strings[0]);
        }

        @Override
        protected void onPostExecute(List<NewsModel> newsModels) {
            super.onPostExecute(newsModels);
            ListAdapter listAdapter=new ListAdapter(MainActivity.this,newsModels,listView);
            listView.setAdapter(listAdapter);
        }
    }
}
