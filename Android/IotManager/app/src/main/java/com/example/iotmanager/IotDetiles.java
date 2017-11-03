package com.example.iotmanager;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nifty.cloud.mb.core.NCMBObject;

import static com.example.iotmanager.R.styleable.View;

/**
 * Created by eshia on 2017/10/27.
 */

public class IotDetiles {
    Application application;
    Context context;
    NcmbController ncmbController;
    boolean door_request_finish,curtain_request_finish,light_request_finish;//すまん！
    int time_out = 30000,cool_time=2000;

    IotDetiles(Application a){
        application = a;
        context = a.getApplicationContext();
        //NCMB系
        ncmbController = new NcmbController(application);
        door_request_finish=curtain_request_finish=light_request_finish=false;//ドアのリクエストの為
    }

    public void getDoorState(final ImageButton img_but_door, final TextView text_view, final ProgressBar progressBar){
        if(ncmbController.getDoorState() == false){ Toast.makeText(context,"開閉中",Toast.LENGTH_SHORT).show();return; }
        else {
            setProText(text_view, "確認中", progressBar);//表示
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.doorLoding) {//取得完了
                        if (ncmbController.doorList.get(0).get(ncmbController.STATE) != null && ncmbController.doorList.get(0).get(ncmbController.STATE).matches("open")) {
                            //開いている
                            img_but_door.setImageResource(R.drawable.door_open);
                        } else if (ncmbController.doorList.get(0).get(ncmbController.STATE) != null && ncmbController.doorList.get(0).get(ncmbController.STATE).matches("close")) {
                            //閉まってる
                            img_but_door.setImageResource(R.drawable.door_close);
                        } else {/*どちらでもない*/ }
                        deleteProText(text_view, progressBar);//消去
                        return;//定期処理終了
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }
    }
    public void setDoorRequest(final ImageButton img_but_door, final TextView text_view, final ProgressBar progressBar,String order) {
        //ここからリクエスト
        if (door_request_finish || ncmbController.setDoorRequest(order) == false ) { Toast.makeText(context,"確認中",Toast.LENGTH_SHORT).show(); }
        else {
            setProText(text_view, "リクエスト中", progressBar);
            final boolean toast = false;
            // 定期処理
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                //各種変数セット
                long request_finish_time = 0;
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.doorProcessing && door_request_finish == false) {//取得完了
                        setProText(text_view, "RE待機中", progressBar);
                        door_request_finish = true;
                        request_finish_time = System.currentTimeMillis();//リクエストの完了した時間を記録
                    } else if (door_request_finish) {
                        if (new MyPreferences().getboolen(context, MyPreferences.DOOR_REQUEST_RE) && (System.currentTimeMillis() - request_finish_time) > cool_time ) {
                            if (toast)
                                Toast.makeText(context, "レスポンスがありました\n" + (System.currentTimeMillis() - request_finish_time) + "ミリ秒でした", Toast.LENGTH_SHORT).show();
                            new MyPreferences(context, MyPreferences.DOOR_REQUEST_RE, false);
                            door_request_finish = false;
                            getDoorState(img_but_door, text_view, progressBar);//確認へ飛ばす
                            return;//定期処理終了
                        } else if ((System.currentTimeMillis() - request_finish_time) > time_out) {
                            Toast.makeText(context, "タイムアウトしました", Toast.LENGTH_SHORT).show();
                            deleteProText(text_view, progressBar);//消去
                            door_request_finish = false;
                            return;//定期処理終了
                        }

                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }

    }

    public void getCurtainState(final ImageButton img_but, final TextView text_view, final ProgressBar progressBar){
        if(ncmbController.getCurtainState() == false){ Toast.makeText(context,"開閉中",Toast.LENGTH_SHORT).show();return; }
        else {
            setProText(text_view, "確認中", progressBar);//表示
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.curtainLoding) {//取得完了
                        if (ncmbController.curtainList.get(0).get(ncmbController.STATE) != null && ncmbController.curtainList.get(0).get(ncmbController.STATE).matches("open")) {
                            //開いている
                            img_but.setImageResource(R.drawable.curtain_open);
                        } else if (ncmbController.curtainList.get(0).get(ncmbController.STATE) != null && ncmbController.curtainList.get(0).get(ncmbController.STATE).matches("close")) {
                            //閉まってる
                            img_but.setImageResource(R.drawable.curtain_close);
                        } else {/*どちらでもない*/ }
                        deleteProText(text_view, progressBar);//消去
                        return;//定期処理終了
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }
    }
    public void setCurtainRequest(final ImageButton img_but_curtain, final TextView text_view, final ProgressBar progressBar ,String order) {
        //ここからリクエスト
        if (curtain_request_finish || ncmbController.setCurtainRequest(order) == false ) { Toast.makeText(context,"確認中",Toast.LENGTH_SHORT).show(); }
        else {
            setProText(text_view, "リクエスト中", progressBar);
            final boolean toast = false;
            // 定期処理
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                //各種変数セット
                long request_finish_time = 0;
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.curtainProcessing && curtain_request_finish == false) {//取得完了
                        setProText(text_view, "RE待機中", progressBar);
                        curtain_request_finish = true;
                        request_finish_time = System.currentTimeMillis();//リクエストの完了した時間を記録
                    } else if (curtain_request_finish) {
                        if (new MyPreferences().getboolen(context, MyPreferences.CURTAIN_REQUEST_RE)  && (System.currentTimeMillis() - request_finish_time) > cool_time) {
                            if (toast)
                                Toast.makeText(context, "レスポンスがありました\n" + (System.currentTimeMillis() - request_finish_time) + "ミリ秒でした", Toast.LENGTH_SHORT).show();
                            new MyPreferences(context, MyPreferences.CURTAIN_REQUEST_RE, false);
                            curtain_request_finish = false;
                            getCurtainState(img_but_curtain, text_view, progressBar);//確認へ飛ばす
                            return;//定期処理終了
                        } else if ((System.currentTimeMillis() - request_finish_time) > time_out) {
                            Toast.makeText(context, "タイムアウトしました", Toast.LENGTH_SHORT).show();
                            deleteProText(text_view, progressBar);//消去
                            curtain_request_finish = false;
                            return;//定期処理終了
                        }

                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }

    }

    public void getLightState(final ImageButton img_but, final TextView text_view, final ProgressBar progressBar){
        if(ncmbController.getLightState() == false){ Toast.makeText(context,"処理中",Toast.LENGTH_SHORT).show();return; }
        else {
            setProText(text_view, "確認中", progressBar);//表示
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.lightLoding) {//取得完了
                        if (ncmbController.lightList.get(0).get(ncmbController.STATE) != null && ncmbController.lightList.get(0).get(ncmbController.STATE).matches("on")) {
                            //開いている
                            img_but.setImageResource(R.drawable.light_on);
                        } else if (ncmbController.lightList.get(0).get(ncmbController.STATE) != null && ncmbController.lightList.get(0).get(ncmbController.STATE).matches("off")) {
                            //閉まってる
                            img_but.setImageResource(R.drawable.light_off);
                        }  else if (ncmbController.lightList.get(0).get(ncmbController.STATE) != null && ncmbController.lightList.get(0).get(ncmbController.STATE).matches("sleep")) {
                            //閉まってる
                            img_but.setImageResource(R.drawable.light_sleep);
                        }  else if (ncmbController.lightList.get(0).get(ncmbController.STATE) != null && ncmbController.lightList.get(0).get(ncmbController.STATE).matches("wether")) {
                            //閉まってる
                            img_but.setImageResource(R.drawable.light_wether);
                        } else {/*どちらでもない*/ }
                        deleteProText(text_view, progressBar);//消去
                        return;//定期処理終了
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }
    }
    public void setLightRequest(final ImageButton img_but_light, final TextView text_view, final ProgressBar progressBar,String order) {
        //ここからリクエスト
        if (light_request_finish || ncmbController.setLightRequest(order) == false ) { Toast.makeText(context,"確認中",Toast.LENGTH_SHORT).show(); }
        else {
            setProText(text_view, "リクエスト中", progressBar);
            final boolean toast = false;
            // 定期処理
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                //各種変数セット
                long request_finish_time = 0, time_out = 5000;
                @Override
                public void run() {
                    // UIスレッド部
                    if (!ncmbController.lightProcessing && light_request_finish == false) {//取得完了
                        setProText(text_view, "RE待機中", progressBar);
                        light_request_finish = true;
                        request_finish_time = System.currentTimeMillis();//リクエストの完了した時間を記録
                    } else if (light_request_finish) {
                        if (new MyPreferences().getboolen(context, MyPreferences.LIGHT_REQUEST_RE) && (System.currentTimeMillis() - request_finish_time) > cool_time) {
                            if (toast)
                                Toast.makeText(context, "レスポンスがありました\n" + (System.currentTimeMillis() - request_finish_time) + "ミリ秒でした", Toast.LENGTH_SHORT).show();
                            new MyPreferences(context, MyPreferences.LIGHT_REQUEST_RE, false);
                            light_request_finish = false;
                            getLightState(img_but_light, text_view, progressBar);//確認へ飛ばす
                            return;//定期処理終了
                        } else if ((System.currentTimeMillis() - request_finish_time) > time_out) {
                            Toast.makeText(context, "タイムアウトしました", Toast.LENGTH_SHORT).show();
                            deleteProText(text_view, progressBar);//消去
                            light_request_finish = false;
                            return;//定期処理終了
                        }

                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }

    }


    private void setProText(TextView text_view,String messe,ProgressBar progressBar){
        text_view.setText(messe);//テキストセット
        progressBar.setVisibility(android.view.View.VISIBLE);//プログレス表示
    }
    private void deleteProText(TextView text_view,ProgressBar progressBar){
        text_view.setText("");//テキスト消去
        progressBar.setVisibility(android.view.View.GONE);//プログレス消去
    }
    public void onStop() {
        ncmbController.onStop();
    }

    public void setUserName(String name){
        //final ProgressDialog progressDialog = new ProgressDialog(context);//処理中ダイアログの準備
        //progressDialog.setMessage("保存中");
        //progressDialog.show();

        ncmbController.setUserName(name);
        // 定期処理
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                // UIスレッド部
                if (!ncmbController.nameSeting) {//取得完了
                    //画像をダイアログで表示
                    //if(progressDialog.isShowing())progressDialog.dismiss();//プレグレスダイアログを消去
                }
            }
        };
        handler.post(r);

    }
}
