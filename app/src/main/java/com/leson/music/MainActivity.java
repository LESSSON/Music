package com.leson.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    MyServiceConn conn;
    Intent intent;
    MusicInterface mi;
    int number = 0;




    //音乐播放器列表
    private String music_path = Environment.getExternalStorageDirectory().getAbsolutePath().replace("0","") + "mp3/";//音乐文件的路径
//    private String path1 = Environment.getExternalStorageDirectory().getAbsolutePath().replace("0","") + "/mp3";//音乐文件的路径

    private List<String>mp3list = new ArrayList<String>();//歌曲列表
    private ListView lv = null;


    //用于设置音乐播放器的播放进度
    private static SeekBar sb;

    private static TextView tv_progress;
    private static TextView tv_total;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.mp3list);
        loadMp3();//调用加载音乐文件函数

        //数据适配器用于把list的数据显示在ListView控件中
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_items, R.id.mp3Name, mp3list);
        //设置数据适配器
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                number = position;
                mi.play(mp3list,number);
            }
        });





        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_total = (TextView) findViewById(R.id.tv_total);

        //创建意图对象
        intent = new Intent(this, MusicService.class);

        //启动服务
        startService(intent);

        //创建服务连接对象
        conn = new MyServiceConn();

        //绑定服务
        bindService(intent, conn, BIND_AUTO_CREATE);

        //获取布局文件上的滑动条
        sb = (SeekBar) findViewById(R.id.sb);

        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            //当滑动条中的进度改变后，此方法被调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            //滑动条刚开始滑动，此方法被调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //当滑动条停止滑动，此方法被调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //根据拖到的进度改变音乐播放进度
                int progress = seekBar.getProgress();

                //改变播放进度
                mi.seekTo(progress);

            }
        });
    }

    public void loadMp3() {
        File file = new File(music_path);//实例化文件
        Log.i("ll", "path" + music_path);
        File[] fileNames = file.listFiles();//返回指定目录下所有的文件名
        for (File name : fileNames) {
            //需要对文件名进行过滤
            Log.i("hxx", ".." + name.getName());
            mp3list.add(name.getName());
        }
    }



    public void requestPerm(View view) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //创建消息处理器对象
    public static Handler handler = new Handler() {

        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg) {

            //获取从子线程发送过来的音乐播放的进度
            Bundle bundle = msg.getData();

            //歌曲的总时长（毫秒）
            int duration = bundle.getInt("duration");

            //歌曲的当前进度（毫秒）
            int currentPostition = bundle.getInt("currentPosition");

            if(duration == currentPostition){

            }

            //刷新滑块的进度
            sb.setMax(duration);
            sb.setProgress(currentPostition);

            //歌曲的总时长
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;

            String strMinute = null;
            String strSecond = null;

            //如果歌曲的时间中的分钟小于10
            if (minute < 10) {

                //在分钟的前面加一个0
                strMinute = "0" + minute;
            } else {

                strMinute = minute + "";
            }

            //如果歌曲的时间中的秒钟小于10
            if (second < 10) {
                //在秒钟前面加一个0
                strSecond = "0" + second;
            } else {

                strSecond = second + "";
            }

            tv_total.setText(strMinute + ":" + strSecond);

            //歌曲当前播放时长
            minute = currentPostition / 1000 / 60;
            second = currentPostition / 1000 % 60;

            //如果歌曲的时间中的分钟小于10
            if (minute < 10) {

                //在分钟的前面加一个0
                strMinute = "0" + minute;
            } else {

                strMinute = minute + "";
            }

            //如果歌曲的时间中的秒钟小于10
            if (second < 10) {

                //在秒钟前面加一个0
                strSecond = "0" + second;
            } else {

                strSecond = second + "";
            }

            tv_progress.setText(strMinute + ":" + strSecond);

        }
    };

    //播放音乐按钮响应函数
    public void play(View view) {

        //播放音乐
        mi.play(mp3list,0);
    }



    //暂停播放音乐按钮相应函数
    public void pausePlay(View view) {

        //暂停播放音乐
        mi.pausePlay();
    }

    //继续播放音乐按钮相应函数
    public void continuePlay(View view) {

        //继续播放音乐
        mi.continuePlay();
    }

    //退出音乐播放器按钮响应函数
    public void exit(View view) {

        //解绑服务
        unbindService(conn);

        //停止服务
        stopService(intent);

        //结束这个activity
        finish();
    }

    //实现服务器连接接口
    class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //获取中间人对象
            mi = (MusicInterface) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }




}

