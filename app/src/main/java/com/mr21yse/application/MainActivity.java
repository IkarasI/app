package com.mr21yse.application;


/*
基本的な参考サイト
androidstudioのインストール方法　https://akira-watson.com/android/adt-windows.html
ドットインストール等開発参考サイトまとめ（旧バージョンに注意）　https://qiita.com/morizyun/items/ec78167ba1b66f4dad6d
デバッグのためにNOXとandroidstudioを接続する方法　https://www.bignox.com/blog/how-to-connect-android-studio-with-nox-app-player-for-android-development-and-debug/
 */
/*
参考サイト
ボタンタップ時のイベント起動　https://qiita.com/HideMatsu/items/2e6caec8265bcf2a2dcb
アクティビティ間の移動　https://akira-watson.com/android/activity-1.html
アクティビティ間のデータの受け渡し　https://akira-watson.com/android/global-val.html
レイアウトのフォント等　https://qiita.com/matsujun/items/b03556ceab5cec258c2c
プログレスバーによる進捗状況の表示　https://techbooster.org/android/ui/659/


 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.SharedPreferences;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.app.AlertDialog;

// ,  View.OnClickListener
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Runnable{

    /* tag */
    private static final String TAG = "BluetoothSample";

    /* Bluetooth Adapter */
    private BluetoothAdapter mAdapter;

    /* Bluetoothデバイス */
    private BluetoothDevice mDevice;

    /* Bluetooth UUID */
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /* デバイス名 */
    private final String DEVICE_NAME = "HC-06";

    /* Soket */
    private BluetoothSocket mSocket;

    /* Thread */
    private Thread mThread;

    /* Threadの状態を表す */
    private boolean isRunning;

    /** 接続ボタン. */
   // private Button connectButton;

    /** 書込みボタン. */
    // private Button writeButton;

    /** ステータス. */
    private TextView mStatusTextView;

    /** Bluetoothから受信した値. */
    private TextView mInputTextView;

    /** Action(ステータス表示). */
    private static final int VIEW_STATUS = 0;

    /** Action(取得文字列). */
    private static final int VIEW_INPUT = 1;

    /** Connect確認用フラグ */
    private boolean connectFlg = false;

    /** BluetoothのOutputStream. */
    OutputStream mmOutputStream = null;


    private String strcont="0";
    private String savestr="";
    //カウント用
    public int numcont=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //↓カウントの生成と初期化
       final TextView cont = findViewById(R.id.txt_cout);
       cont.setText("現在"+strcont+"歩");      //アプリ立ち上げ時に表示される現在歩数

        Button btncont = findViewById(R.id.cont_btn);
        btncont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cont.setText("test");
                contadd();
            }
        });

        //↓参考→　https://akira-watson.com/android/global-val.html　数値の受け渡し
        numchangerActivity numcont = (numchangerActivity) this.getApplication();
        strcont = numcont.getZerocount();

        TextView textViewSub = (TextView)findViewById(R.id.txt_tgt);
        textViewSub.setText("目標"+strcont+"歩");

        ProgressBar progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
        progressBar1.setMax(Integer.parseInt(strcont)); // 水平プログレスバーの最大値を設定
        progressBar1.setProgress(0); // 水平プログレスバーの値を設定
        progressBar1.setSecondaryProgress(0); // 水平プログレスバーのセカンダリ値を設定
        if (Integer.parseInt(strcont)==0){
            progressBar1.setMax(1000); // 水平プログレスバーの最大値を設定
        }


        Button revcont = findViewById(R.id.rev_btn);    //revcontをOnClickListener化
        revcont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refrevcont();
            }
        });

        Button resetcont = findViewById(R.id.reset);    //resetcontをOnClickListener化
        resetcont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetrevcont();
            }
        });







       // mInputTextView = (TextView)findViewById(R.id.inputValue);
        mStatusTextView = (TextView)findViewById(R.id.statusValue);

       // connectButton = (Button)findViewById(R.id.connectButton);
       // writeButton = (Button)findViewById(R.id.writeButton);

        // connectButton.setOnClickListener(this);
        // writeButton.setOnClickListener(this);

        // Bluetoothのデバイス名を取得
        // デバイス名は、RNBT-XXXXになるため、
        // DVICE_NAMEでデバイス名を定義
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        //mStatusTextView.setText("SearchDevice");
        Set< BluetoothDevice > devices = mAdapter.getBondedDevices();
        for ( BluetoothDevice device : devices) {

            if(device.getName().equals(DEVICE_NAME)) {
               // mStatusTextView.setText("find: " + device.getName());
                mDevice = device;
            }
        }
    }





    @Override
    protected void onPause() {
        super.onPause();

        isRunning = false;
        try {
            mSocket.close();
        }
        catch(Exception e){}
    }

    @Override
    public void run() {
        InputStream mmInStream = null;

        Message valueMsg = new Message();
        valueMsg.what = VIEW_STATUS;
        valueMsg.obj = "connecting...";
        mHandler.sendMessage(valueMsg);

        try {

            // 取得したデバイス名を使ってBluetoothでSocket接続
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mSocket.connect();
            mmInStream = mSocket.getInputStream();
            mmOutputStream = mSocket.getOutputStream();

            // InputStreamのバッファを格納
            byte[] buffer = new byte[1024];

            // 取得したバッファのサイズを格納
            int bytes;
            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "connected.";
            mHandler.sendMessage(valueMsg);

            connectFlg = true;

            while(isRunning) {

                // InputStreamの読み込み
                bytes = mmInStream.read(buffer);
                Log.i(TAG,"bytes="+bytes);
                // String型に変換
                String readMsg = new String(buffer, 0, bytes);

                // null以外なら表示
                if(readMsg.trim() != null && !readMsg.trim().equals("")) {
                    Log.i(TAG,"value="+readMsg.trim());

                    valueMsg = new Message();
                    valueMsg.what = VIEW_INPUT;
                    valueMsg.obj = readMsg;
                    mHandler.sendMessage(valueMsg);
                } else {
                    // Log.i(TAG,"value=nodata");
                }

            }
        }catch(Exception e){

            valueMsg = new Message();
            valueMsg.what = VIEW_STATUS;
            valueMsg.obj = "Error1:" + e;
            mHandler.sendMessage(valueMsg);

            try{
                mSocket.close();
            }catch(Exception ee){}
            isRunning = false;
            connectFlg = false;
        }
    }
/*
    @Override
    public void onClick(View v) {
        if(v.equals(connectButton)) {
            // 接続されていない場合のみ
            if (!connectFlg) {
                mStatusTextView.setText("try connect");

                mThread = new Thread(this);
                // Threadを起動し、Bluetooth接続
                isRunning = true;
                mThread.start();
            }
        } else if(v.equals(writeButton)) {
            // 接続中のみ書込みを行う
            if (connectFlg) {
                try {
                    mmOutputStream.write("2".getBytes());
                    mStatusTextView.setText("Write:");
                } catch (IOException e) {
                    Message valueMsg = new Message();
                    valueMsg.what = VIEW_STATUS;
                    valueMsg.obj = "Error3:" + e;
                    mHandler.sendMessage(valueMsg);
                }
            } else {
                mStatusTextView.setText("Please push the connect button");
            }
        }

    }
*/
    /**
     * 描画処理はHandlerでおこなう
     */
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String)msg.obj;
            if(action == VIEW_INPUT){
                mInputTextView.setText(msgStr);

                TextView cont = findViewById(R.id.txt_cout);
                cont.setText("現在"+msgStr+"歩");
                ProgressBar progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
                progressBar1.setProgress(numcont);
                /*
                int tgtintB = Integer.parseInt(strcont);
                if (tgtintB==numcont){
                    AlertDialog.Builder alertB = new AlertDialog.Builder(this);
                    alertB.setTitle("やったぜ");
                    alertB.setMessage("I did it　TGTcount=>"+tgtintB);
                    alertB.setPositiveButton("OK", null);
                    alertB.show();
                }
                */
            }
            else if(action == VIEW_STATUS){

                String save= String.valueOf(numcont);
                SharedPreferences preferences = getSharedPreferences("game_data",MODE_PRIVATE);
                // データの保存
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("count", savestr+"\n"+save+"歩");
                editor.commit();


                mStatusTextView.setText(msgStr);
            }
        }
    };












    //↓カウントの加算
    private void contadd(){
        TextView cont = findViewById(R.id.txt_cout);
        numcont++;
        String strnumcont = String.valueOf(numcont);
        cont.setText("現在"+strnumcont+"歩");

        ProgressBar progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
        progressBar1.setProgress(numcont);
        // progressBar1.setSecondaryProgress(numcont*2);


        int tgtint = Integer.parseInt(strcont);
        if (tgtint==numcont){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("やったぜ");
            alert.setMessage("I did it　TGTcount=>"+tgtint);
            alert.setPositiveButton("OK", null);
            alert.show();
        }



        String save= String.valueOf(numcont);
        SharedPreferences preferences = getSharedPreferences("game_data",MODE_PRIVATE);
        // データの保存
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("count", savestr+"\n"+save+"歩");
        editor.commit();
    }

    private void refrevcont(){
        // データの読込
        SharedPreferences preferences = getSharedPreferences("game_data",MODE_PRIVATE);
        String revcount = preferences.getString("count", "");


        TextView rev = findViewById(R.id.revnum);
        rev.setText(String.valueOf(revcount));

        savestr=revcount;
    }
    private void resetrevcont(){
        SharedPreferences preferences = getSharedPreferences("game_data",MODE_PRIVATE);
        // データの保存
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("count", "");
        editor.commit();
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // 下はメニューボタンの動作設定
        if (id == R.id.nav_gallery) {

            Intent intent = new Intent(getApplication(), subActivity.class); //subへ
            startActivity(intent);

        } else if (id == R.id.nav_manage) {

            Intent intent = new Intent(getApplication(), ManageActivity.class); //Manageへ
            startActivity(intent);

        } else if (id == R.id.nav_reset) {

            android.support.v7.app.AlertDialog.Builder resetalert = new android.support.v7.app.AlertDialog.Builder(this);
//ダイアログタイトルをセット
            resetalert.setTitle("歩数データが初期化されます");
//ダイアログメッセージをセット
// アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
            resetalert.setPositiveButton("OK!", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    //OKボタンが押された時の処理
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                }});
// アラートダイアログのボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
            resetalert.setNegativeButton("YES!!", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    //NOボタンが押された時の処理
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                }});
            //ダイアログ表示
            resetalert.show();

        } else if (id == R.id.connect) {

            // 接続されていない場合のみ
            if (!connectFlg) {
                mStatusTextView.setText("try connect");

                mThread = new Thread(this);
                // Threadを起動し、Bluetooth接続
                isRunning = true;
                mThread.start();
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
