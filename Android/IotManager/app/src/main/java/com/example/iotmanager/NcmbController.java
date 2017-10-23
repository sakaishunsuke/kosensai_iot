package com.example.iotmanager;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.nifty.cloud.mb.core.FetchFileCallback;
import com.nifty.cloud.mb.core.FindCallback;
import com.nifty.cloud.mb.core.NCMB;
import com.nifty.cloud.mb.core.NCMBException;
import com.nifty.cloud.mb.core.NCMBFile;
import com.nifty.cloud.mb.core.NCMBObject;
import com.nifty.cloud.mb.core.NCMBQuery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/*
 * Created by eshia on 2017/10/21.
 */

public class NcmbController {
    //メンバ変数
    //状態を保持する変数
    Boolean refrigeratorLoding = false, doorLoding = false,curtainLoding = false,doorProcessing = false,curtainProcessing = false;
    //呼び出し元のcontext
    Context context;
    //冷蔵庫系
    String  refrigerator_date="--/--/-- --:--";
    //ドア系　
    ArrayList<ArrayList<String>>doorList = new ArrayList<ArrayList<String>>();//リストは順番、種類、それぞれの値となっている
    public static final int STATE = 0,USER_NAME = 1,REQUEST=2;

    NcmbController(Application application){
        // APIキーの設定とSDK初期化
        NCMB.initialize(application.getApplicationContext(),
                "4dd8d0b46a5b342c8c91d16326eeefdc32279ddbd8d26fa227ad062863318987",
                "a869d2c96f84bb2b24a3a45df8f7731b6f365b6390bf6addff606f643747012b");
        context = application.getApplicationContext();
    }

    public void onStop(){
        //onStopの時にすべき処理
        doorProcessing = curtainProcessing = false;
    }
    public boolean getRefrigeratorImage(){
        final boolean toast = false;
        if(refrigeratorLoding)return false;
        else refrigeratorLoding = true;
        NCMBQuery<NCMBFile> query = NCMBFile.getQuery();
        //query.whereEqualTo("tmimeType", "image/jpeg");//画像のみ
        query.setLimit(5);//5個まで
        query.addOrderByDescending("createDate");//データを降順で取得するためのフィールドを設定
        //query.addOrderByAscending("createDate"); //データを昇順で取得するためのフィールドを設定
        query.findInBackground(new FindCallback<NCMBFile>() {
            @Override
            public void done(final List<NCMBFile> results, NCMBException e) {
                if (e != null) {
                    //検索失敗
                    //たぶん0個の時もここかな？
                    refrigeratorLoding = false;
                    if(toast)Toast.makeText(context,"クラウドにデータがない",Toast.LENGTH_SHORT).show();
                } else {
                    //検索成功
                    for (int i = 0; i < results.size() && i < 1; i++) {
                        final int finalI = i;
                        results.get(i).fetchInBackground(new FetchFileCallback() {
                            @Override
                            public void done(byte[] obj, NCMBException e) {
                                if (e != null) {
                                    //取得失敗
                                    if(toast)Toast.makeText(context,"クラウドにあるのに取れない",Toast.LENGTH_SHORT).show();
                                    refrigeratorLoding = false;
                                } else {
                                    //取得成功
                                    try {
                                        //FileOutputStream fileImage = new File(context.getFilesDir(),"refrigeratorImage.jpeg");
                                        FileOutputStream fileImage = context.openFileOutput("refrigeratorImage.jpeg", Context.MODE_APPEND);
                                        //new openFileOutput("refrigeratorImage.jpeg", Context.MODE_PRIVATE);
                                        //FileOutputStream fileImage = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/refrigeratorImage.jpeg");
                                        fileImage.write(results.get(finalI).getFileData());//データを書き込む
                                        fileImage.close();//最後に閉じる
                                        refrigerator_date = results.get(finalI).getFileName();//ファイルの名前(日時の取得)
                                        String s[] = refrigerator_date.split("_");
                                        if(s.length > 4)  refrigerator_date = (s[0]+"/"+s[1]+"/"+s[2]+" "+s[3]+":"+String.format("%02d",Integer.parseInt(s[4])));
                                        if(toast)Toast.makeText(context,"クラウドからデータが取れました",Toast.LENGTH_SHORT).show();
                                    } catch (FileNotFoundException e1) {
                                        //ファイルが作れなかったとき？
                                        if(toast)Toast.makeText(context,"ファイルを作れません",Toast.LENGTH_SHORT).show();
                                        e1.printStackTrace();
                                    } catch (IOException e1) {
                                        //データが書き込めなかったとき？
                                        if(toast)Toast.makeText(context,"データが書き込めない",Toast.LENGTH_SHORT).show();
                                        e1.printStackTrace();
                                    }
                                    refrigeratorLoding = false;

                                    results.get(finalI);
                                }
                            }
                        });
                    }
                }
            }
        });
        return true;
    }
    public boolean getDoorState() {
        final boolean toast = true;
        if(doorLoding || doorProcessing)return false;
        else doorLoding = true;
        //TestClassを検索するためのNCMBQueryインスタンスを作成
        NCMBQuery<NCMBObject> query = new NCMBQuery<>("DoorLog");
        query.setLimit(10);//10個まで
        query.addOrderByDescending("createDate");//データを降順で取得するためのフィールドを設定
        //keyというフィールドがvalueとなっているデータを検索する条件を設定
        //query.whereEqualTo("key", "value");

        //データストアからデータを検索
        query.findInBackground(new FindCallback<NCMBObject>() {
            @Override
            public void done(List<NCMBObject> results, NCMBException e) {
                if (e != null) {
                    if(toast)Toast.makeText(context,"クラウドにデータがない",Toast.LENGTH_SHORT).show();
                    doorLoding = false;
                    //検索失敗時の処理
                } else {
                    //if(results.size() > 0) doorList.clear();//入れるデータがあれば中身を消す
                    for(int i=0;i<results.size() ; i++) {
                        ArrayList<String> data = new ArrayList<String>();
                        Collections.addAll( data,results.get(i).getString("state") ,
                                            results.get(i).getString("user"));
                                            //results.get(i).getString("request"));
                        doorList.add(data);
                    }
                    if(toast)Toast.makeText(context,"ドアlog取得完了",Toast.LENGTH_SHORT).show();
                    doorLoding = false;
                    //検索成功時の処理
                }
            }
        });
        return true;

    }
    public boolean setDoorRequest(){
        return false;
    }
}
