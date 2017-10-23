package com.example.iotmanager;

import com.nifty.cloud.mb.core.NCMB;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
                        img_refrigrator = (ImageView) findViewById(R.id.imgRefrigrator);
                        text_refrigrator_time = (TextView) findViewById(R.id.textRefrigratorTime);

        //NCMB系
        ncmbController = new NcmbController(this.getApplication());


        img_but_door.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ドアの処理開始
                setDoorRequest();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //冷蔵庫の取得＆画像に差し替え開始
    private void getRefrigeratorImage(){
        final boolean toast = false;
        if(ncmbController.getRefrigeratorImage()){
            if(toast)Toast.makeText(this.getApplicationContext(),"取得開始",Toast.LENGTH_SHORT).show();
            // 定期処理
            final Context context = this.getApplicationContext();
            final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                int count = 0;
                @Override
                public void run() {
                    // UIスレッド部
                    if(!ncmbController.refrigeratorLoding){//取得完了
                        //画像の差し替え
                        InputStream istream = null;
                        try {
                            //istream = getResources().getAssets().open(Environment.getExternalStorageDirectory().getPath()+"/refrigeratorImage.jpeg");
                            text_refrigrator_time.setText(ncmbController.refrigerator_date);//日時を書く
                            istream = context.openFileInput("refrigeratorImage.jpeg");//写真を張り付け
                            if(toast)Toast.makeText(context,"画像を入れました",Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            if(toast)Toast.makeText(context,"新しい画像がありませんでした",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(istream);
                        img_refrigrator.setImageBitmap(bitmap);
                        return;//定期処理から抜ける
                    }
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(r);
        }else{
            if(toast)Toast.makeText(this.getApplicationContext(),"取得開始できません",Toast.LENGTH_SHORT).show();
        }
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

    //画面表示時に更新
    @Override
    protected void onResume() {
        super.onResume();
        getRefrigeratorImage();//冷蔵庫の写真後進
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
