package com.leson.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    MyServiceConn conn;
    Intent intent;
    MusicInterface mi;

    //用于设置音乐播放器的播放进度
    private static SeekBar sb;

    private static TextView tv_progress;
    private static TextView tv_total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        mi.play();
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

//    ContentResolver contentResolver=getContentResolver();
//
//    Cursor c = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
//
//    if (c!=null){
//        int i = 0;
//        while(c.moveToNext()) {
//            Map<String,Object> map = new HashMap<String, Object>();//存放在map中显示在listview
//
//            //歌曲名
//            name[i] = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//
//            //歌曲id
//            id[i] = c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID));
//
//            //作者
//            artical[i] = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//
//            //路径
//            url[i] = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
//            map.put("SongName",name[i]);
//            map.put("id",id[i]);
//            map.put("Artical",artical[i]);
//            map.put("url",url[i]);
//            list.add(map);
//            i++;
//        }
//    }
//
//}
