package com.example.iotmanager;

import com.google.firebase.iid.FirebaseInstanceId;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class ActivityTop extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //メンバ変数
    NcmbController ncmbController;//NCMBを使用するため
    ImageView img_refrigrator;//冷蔵庫の画像部分
    TextView  text_refrigrator_time;//冷蔵庫の写真の日時
    ImageButton     img_but_door; TextView  text_view_door; ProgressBar progress_bar_door;  //ドア
    ImageButton     img_but_curtain; TextView  text_view_curtain; ProgressBar progress_bar_curtain;  //カーテンボタン
    ImageButton     img_but_refrigerator;//冷蔵庫ボタン
    ImageButton     img_but_light; TextView  text_view_light; ProgressBar progress_bar_light;  //ライトボタン
    ImageButton     img_but_myfavorite1;//お気に入り１ボタン
    ImageButton     img_but_myfavorite2;//お気に入り２ボタン

    IotDetiles iotDetiles;//IOT機器との通信すべて

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //レイアウトのセット
        setContentView(R.layout.activity_top);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Viewセット＆関連付け
        img_but_door = (ImageButton) findViewById(R.id.imgButTopActivityDoor);
        text_view_door = (TextView)findViewById(R.id.textViewTopActivityDoor);
        progress_bar_door = (ProgressBar)findViewById(R.id.progressBarTopActivityDoor);
        img_but_curtain = (ImageButton) findViewById(R.id.imgButTopActivityCurtain);
        text_view_curtain = (TextView)findViewById(R.id.textViewTopActivityCurtain);
        progress_bar_curtain = (ProgressBar)findViewById(R.id.progressBarTopActivityCurtain);
        img_but_refrigerator = (ImageButton) findViewById(R.id.imgButTopActivityRefrigerator);
        img_but_light = (ImageButton) findViewById(R.id.imgButTopActivityLight);
        text_view_light = (TextView)findViewById(R.id.textViewTopActivityLight);
        progress_bar_light = (ProgressBar)findViewById(R.id.progressBarTopActivityLight);
        img_but_myfavorite1 = (ImageButton) findViewById(R.id.imgButTopActivityMyfavorite01);
        //img_but_myfavorite2 = (ImageButton) findViewById(R.id.imgButTopActivityMyfavorite02);
                        //img_refrigrator = (ImageView) findViewById(R.id.imgRefrigrator);
                        //text_refrigrator_time = (TextView) findViewById(R.id.textRefrigratorTime);

        //名前の設定

        //NCMB系
        ncmbController = new NcmbController(this.getApplication());
        iotDetiles = new IotDetiles(getApplication());

        if(new MyPreferences().getString(this,MyPreferences.USERNAME).matches("-1")){
            getNameDialog("名前を入力してください\n後で変更もできます");
        }

        //FCMから通知が来た時に動く部分

        // 定期処理
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                // スレッド部
                if (new MyPreferences().getboolen(ActivityTop.this, MyPreferences.DOOR_NOTIFICATION)) {
                    new MyPreferences(ActivityTop.this,MyPreferences.DOOR_NOTIFICATION,false);
                    getDoorState();
                    //return;//定期処理終了
                }else if(new MyPreferences().getboolen(ActivityTop.this, MyPreferences.CURTAIN_NOTIFICATION)) {
                    new MyPreferences(ActivityTop.this,MyPreferences.CURTAIN_NOTIFICATION,false);
                    getCurtainState();
                    //return;//定期処理終了
                }else if(new MyPreferences().getboolen(ActivityTop.this, MyPreferences.LIGHT_NOTIFICATION)) {
                    new MyPreferences(ActivityTop.this,MyPreferences.LIGHT_NOTIFICATION,false);
                    getLightState();
                    //return;//定期処理終了
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(r);

        img_but_myfavorite1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMyfavorite01();
            }
        });

        img_but_door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoorRequest("");//ドアの処理開始
            }
        });
        img_but_door.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        img_but_curtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurtainRequest("");//カーテンの処理開始
            }
        });
        img_but_refrigerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRefrigeratorImage();//冷蔵庫の処理開始
            }
        });
        img_but_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLightRequest("reverse");//ライトの処理開始
            }
        });


        img_but_light.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // ダイアログを作成する
                final Dialog dialog = new Dialog(ActivityTop.this);
                // タイトル非表示
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                // フルスクリーン
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                dialog.setContentView(R.layout.dialog_light_mode);
                // 背景を透明にする
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // OK ボタンのリスナ
                dialog.findViewById(R.id.dialog_on_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setLightRequest("on");
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_off_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setLightRequest("off");
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_sleep_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setLightRequest("sleep");
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_weather_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setLightRequest("wether");
                        dialog.dismiss();
                    }
                });
                // ダイアログを表示する
                dialog.show();
                return false;
            }
        });




        //fadのボタンの内容
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        //以下ナビゲーションバーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void startMyfavorite01() {
        setCurtainRequest("close");
        setLightRequest("sleep");
        setDoorRequest("close");
    }

    //文字入力をしてもらうダイアログ
    private void getNameDialog(final String titel){
        //入力された文字を入れる変数
        final String[] in_put_s = {""};
        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(getApplicationContext());
        new AlertDialog.Builder(ActivityTop.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(titel)
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(editView.getText().toString().matches("")){
                            //文字が入っていないので入れてもらうように通知
                            Toast.makeText(getApplicationContext(), "文字を入力してください", Toast.LENGTH_LONG).show();
                            getNameDialog(titel);
                        }else iotDetiles.setUserName(editView.getText().toString());
                    }
                })
                .show();
    }

    //ドアの状態の取得＆画像に差し替え開始
    private void getDoorState(){
        iotDetiles.getDoorState(img_but_door,text_view_door,progress_bar_door);
    }
    //ドアの開閉開始
    private void setDoorRequest(String order){
        iotDetiles.setDoorRequest(img_but_door,text_view_door,progress_bar_door,order);
    }

    //カーテンの状態の取得＆画像に差し替え開始
    private void getCurtainState(){
        iotDetiles.getCurtainState(img_but_curtain,text_view_curtain,progress_bar_curtain);
    }
    //カーテンの開閉開始
    private void setCurtainRequest(String order){
        iotDetiles.setCurtainRequest(img_but_curtain,text_view_curtain,progress_bar_curtain,order);
    }

    //ライトの状態の取得＆画像に差し替え開始
    private void getLightState(){
        iotDetiles.getLightState(img_but_light,text_view_light,progress_bar_light);
    }
    //ライトのの開閉開始
    private void setLightRequest(String order){
        iotDetiles.setLightRequest(img_but_light,text_view_light,progress_bar_light,order);
    }

    //冷蔵庫の取得＆画像に差し替え開始
    private void getRefrigeratorImage(){
        final boolean toast = false;//デバック内容を表示するかどうか
        final ProgressDialog progressDialog = new ProgressDialog(this);//処理中ダイアログの準備
        progressDialog.setMessage("取得中");
        progressDialog.show();
        if(ncmbController.getRefrigeratorImage()){
            if(toast)Toast.makeText(this.getApplicationContext(),"取得開始",Toast.LENGTH_SHORT).show();
            // 定期処理
            final Context context = this;
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // UIスレッド部
                    if(!ncmbController.refrigeratorLoding){//取得完了
                        //画像をダイアログで表示
                        if(progressDialog.isShowing())progressDialog.dismiss();//プレグレスダイアログを消去

                        InputStream istream = null;//画像を入れる変数
                        String time_data = "不明" ;
                        int drink1 = 0,drink2 = 0;
                        try {
                            //istream = getResources().getAssets().open(Environment.getExternalStorageDirectory().getPath()+"/refrigeratorImage.jpeg");
                            time_data = ncmbController.refrigerator_date;//日時を保存
                            istream = context.openFileInput("refrigeratorImage.jpeg");//写真を取得
                            drink1 = ncmbController.drink1; drink2 = ncmbController.drink2; //飲み物データ取得
                            if(toast)Toast.makeText(context,"画像を入れました",Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            if(toast)Toast.makeText(context,"新しい画像がありませんでした",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(istream);
                        // ダイアログを作成する
                        final Dialog dialog = new Dialog(context);
                        // タイトル非表示
                        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        // フルスクリーン
                        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
                        dialog.setContentView(R.layout.dialog_refrigerator);
                        // 背景を透明にする
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        //画像を入れる&日時
                        ImageView i = (ImageView)dialog.findViewById(R.id.dialog_image);i.setImageBitmap(bitmap);
                        TextView dialog_time_data = (TextView)dialog.findViewById(R.id.dialog_time_data); dialog_time_data.setText(time_data);
                        //飲み物のデータセット
                        i = (ImageView)dialog.findViewById(R.id.img_drink1);
                        switch (drink1){
                            case 0:
                                i.setImageResource(R.drawable.drink_0);
                                break;
                            case 1:
                                i.setImageResource(R.drawable.drink_1);
                                break;
                            case 2:
                                i.setImageResource(R.drawable.drink_2);
                                break;
                            case 3:
                                i.setImageResource(R.drawable.drink_3);
                                break;
                            default:
                                i.setImageResource(R.drawable.drink_3);
                                break;
                        }
                        i = (ImageView)dialog.findViewById(R.id.img_drink2);
                        switch (drink2){
                            case 0:
                                i.setImageResource(R.drawable.drink_0);
                                break;
                            case 1:
                                i.setImageResource(R.drawable.drink_1);
                                break;
                            case 2:
                                i.setImageResource(R.drawable.drink_2);
                                break;
                            case 3:
                                i.setImageResource(R.drawable.drink_2);
                                break;
                            default:
                                i.setImageResource(R.drawable.drink_0);
                                break;
                        }

                        TextView dialog_drink1 = (TextView)dialog.findViewById(R.id.dialog_drink1); dialog_drink1.setText(String.format("飲み物1",drink1));
                        TextView dialog_drink2 = (TextView)dialog.findViewById(R.id.dialog_drink2); dialog_drink2.setText(String.format("飲み物2",drink2));
                        // OK ボタンのリスナ
                        dialog.findViewById(R.id.dialog_on_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        // ダイアログを表示する
                        dialog.show();
                        return;//定期処理から抜ける
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }else{
            if(toast)Toast.makeText(this.getApplicationContext(),"取得開始できません",Toast.LENGTH_SHORT).show();
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
                Toast.makeText(this.getApplicationContext(),"取得出来ません",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //画面表示時に更新
    @Override
    protected void onResume() {
        super.onResume();
        getDoorState();//ドアの状態を取得
        getCurtainState();
        getLightState();
        //自分のFCMのカギ番号確認&保存
        new MyPreferences(this,MyPreferences.FCMTOKEN,FirebaseInstanceId.getInstance().getToken());
        //Log.d("FCMkey",FirebaseInstanceId.getInstance().getToken());
    }
    @Override
    protected void onStart(){
        super.onStart();

    }
    @Deprecated
    protected void onStop(){
        super.onStop();
        iotDetiles.onStop();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
