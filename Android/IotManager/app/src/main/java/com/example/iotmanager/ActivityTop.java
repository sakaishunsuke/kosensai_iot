package com.example.iotmanager;

import com.nifty.cloud.mb.core.NCMB;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

public class ActivityTop extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //メンバ変数
    NcmbController ncmbController;//NCMBを使用するため
    ImageView img_refrigrator;//冷蔵庫の画像部分
    TextView  text_refrigrator_time;//冷蔵庫の写真の日時
    ImageButton     img_but_door; //ドアのボタン
    ImageButton     img_but_curtain;//カーテンボタン
    ImageButton     img_but_refrigerator;//冷蔵庫ボタン
    ImageButton     img_but_light;//ライトボタン
    ImageButton     img_but_myfavorite1;//お気に入り１ボタン
    ImageButton     img_but_myfavorite2;//お気に入り２ボタン

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //レイアウトのセット
        setContentView(R.layout.activity_top);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Viewセット＆関連付け
        img_but_door = (ImageButton) findViewById(R.id.imgButTopActivityDoor);
        img_but_curtain = (ImageButton) findViewById(R.id.imgButTopActivityCurtain);
        img_but_refrigerator = (ImageButton) findViewById(R.id.imgButTopActivityRefrigerator);
        img_but_light = (ImageButton) findViewById(R.id.imgButTopActivityLight);
        img_but_myfavorite1 = (ImageButton) findViewById(R.id.imgButTopActivityMyfavorite01);
        img_but_myfavorite2 = (ImageButton) findViewById(R.id.imgButTopActivityMyfavorite02);
                        //img_refrigrator = (ImageView) findViewById(R.id.imgRefrigrator);
                        //text_refrigrator_time = (TextView) findViewById(R.id.textRefrigratorTime);

        //FCM
        Intent intent = new Intent(this.getApplicationContext(),MyFirebaseInstanceIDService.class);
        startService(intent);

        //NCMB系
        ncmbController = new NcmbController(this.getApplication());


        img_but_door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoorRequest();//ドアの処理開始
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
                //setDoorRequest();//カーテンの処理開始
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
                //getRefrigeratorImage();//ライトの処理開始
            }
        });



        //fadのボタンの内容
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //以下ナビゲーションバーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //ドアの状態の取得＆画像に差し替え開始
    private void getDoorState(){
        final boolean toast = false;
        if(ncmbController.getDoorState()){
            if(toast)Toast.makeText(this.getApplicationContext(),"ドア状態取得開始",Toast.LENGTH_SHORT).show();
            // 定期処理
            final Context context = this.getApplicationContext();
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    // UIスレッド部
                    if(!ncmbController.doorLoding){//取得完了
                        if (ncmbController.doorList.size() > 0 && ncmbController.doorList.get(0).get(ncmbController.STATE).matches("open")) {
                            //開いている
                            img_but_door.setImageResource(R.drawable.door_open);
                        }else if(ncmbController.doorList.size() > 0 && ncmbController.doorList.get(0).get(ncmbController.STATE).matches("close")) {
                            //閉まってる
                            img_but_door.setImageResource(R.drawable.door_close);
                        }else {
                            //どちらでもない
                        }
                        return;//定期処理終了
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }else{
            Toast.makeText(this.getApplicationContext(),"取得開始できません",Toast.LENGTH_SHORT).show();
        }
    }
    //ドアの状態の取得＆画像に差し替え開始
    private void setDoorRequest(){
        final boolean toast = false;
        if(ncmbController.setDoorRequest()){
            if(toast)Toast.makeText(this.getApplicationContext(),"ドア開閉開始",Toast.LENGTH_SHORT).show();
            // 定期処理
            final Context context = this.getApplicationContext();
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    // UIスレッド部
                    if(!ncmbController.doorProcessing){//取得完了
                        if (ncmbController.doorList.size() > 0 && ncmbController.doorList.get(0).get(ncmbController.REQUEST).matches("open")) {
                            //開いている
                            img_but_door.setImageResource(R.drawable.door_open);
                        }else if(ncmbController.doorList.size() > 0 && ncmbController.doorList.get(0).get(ncmbController.REQUEST).matches("close")) {
                            //閉まってる
                            img_but_door.setImageResource(R.drawable.door_close);
                        }else {
                            //どちらでもない
                        }
                        return;//定期処理終了
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }else{
            Toast.makeText(this.getApplicationContext(),"取得開始できません",Toast.LENGTH_SHORT).show();
        }
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
                        TextView dialog_drink1 = (TextView)dialog.findViewById(R.id.dialog_drink1); dialog_drink1.setText(String.format("飲み物1　%3dml 　　　　",drink1));
                        if(drink1 != 0 && drink1 < 100){dialog_drink1.setBackgroundColor(Color.parseColor("#f0ff4500")); dialog_drink1.setText(String.format("飲み物1　%3dml 残り少し",drink1));}
                        TextView dialog_drink2 = (TextView)dialog.findViewById(R.id.dialog_drink2); dialog_drink2.setText(String.format("飲み物2　%3dml 　　　　",drink2));
                        if(drink2 != 0 && drink2 < 100){dialog_drink2.setBackgroundColor(Color.parseColor("#f0ff4500")); dialog_drink2.setText(String.format("飲み物2　%3dml 残り少し",drink1)); }
                        // OK ボタンのリスナ
                        dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(new View.OnClickListener() {
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
    }
    @Override
    protected void onStart(){
        super.onStart();

    }
    @Deprecated
    protected void onStop(){
        super.onStop();
        ncmbController.onStop();
    }

    /*
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/

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
