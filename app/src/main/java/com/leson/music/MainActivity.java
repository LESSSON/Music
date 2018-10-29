package com.leson.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    MyServiceConn conn;
    Intent intent;
    MusicInterface mi;
    int number = 0;

    //音乐播放器列表路径及权限获取
    private String music_path = Environment.getExternalStorageDirectory().getAbsolutePath().replace("0","") + "mp3/";//音乐文件的路径
    private static int REQUEST_PERMISSION_CODE = 2;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STOPAGE"
    };

    private List<String>mp3list = new ArrayList<String>();//歌曲列表
    private static ListView lv = null;//listview控件


    //用于设置音乐播放器的播放进度
    private static SeekBar sb;
    private static TextView tv_progress;
    private static TextView tv_total;
    //控制按钮
    private Button t1,t2,t3;

    private static MyAdapter adapter1;
    class ViewHolder{
        public TextView song;
    }
    public class MyAdapter extends BaseAdapter{

        private LayoutInflater mInflater = null;
        private int selectedItem = -1;
        private MyAdapter(Context context)
        {
            //根据context上下文加载布局，这里的是this
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            //在此适配器中所代表的数据集中的条目数
            return mp3list.size();
        }

        @Override
        public Object getItem(int position) {
            //获取数据集中与指定索引对应的数据项
            return position;
        }

        public void setSelectedItem(int selectedItem){
            this.selectedItem = selectedItem;
        }

        @Override
        public long getItemId(int position) {
            //获取在列表中与指定索引对应的行id
            return position;
        }

        //获取一个在数据集中指定索引的视图来显示数据
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //如果缓存converView为空，则创建View
            if(convertView == null){
                holder = new ViewHolder();
                //根据自定义的Item布局加载布局
                convertView = mInflater.inflate(R.layout.list_items,null);
                holder.song = (TextView) convertView.findViewById(R.id.mp3Name);
                //将设置好的布局保存到缓存中，并将其设置在Tag里，以便以后方便取出Tag
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.song.setText((String)mp3list.get(position));
            if(position == selectedItem){
                convertView.setBackgroundColor(Color.RED);
                convertView.setSelected(true);

            }else {
                convertView.setBackgroundColor(Color.WHITE);
                convertView.setSelected(false);
            }
            return convertView;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //启动时查看SD卡权限状态
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,REQUEST_PERMISSION_CODE);
            }
        }



        lv = (ListView) findViewById(R.id.mp3list);
        loadMp3();//调用加载音乐文件函数

        //数据适配器用于把list的数据显示在ListView控件中
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_items, R.id.mp3Name, mp3list);
        adapter1 = new MyAdapter(this);
        lv.setAdapter(adapter1);

        //设置数据适配器

//        lv.setAdapter(adapter);

        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                adapter1.setSelectedItem(position);
                adapter1.notifyDataSetInvalidated();
                number = position;
                mi.play(mp3list,number);
                t1.setText("PAUSE");



            }
        });


        //按钮响应
        t1 = (Button) findViewById(R.id.touch1);
        t2 = (Button) findViewById(R.id.touch2);
        t3 = (Button) findViewById(R.id.touch3);


        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t1.getText() == "PLAY"){
                    t1.setText("PAUSE");
                    mi.continuePlay();
                }else{
                    if(t1.getText() == "PAUSE"){
                        t1.setText("PLAY");
                        mi.pausePlay();
                    }
                }

            }
        });

        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.setText("PAUSE");
                mi.forward(mp3list);
            }
        });

        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.setText("PAUSE");
                mi.next(mp3list);
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



    //扫面SD卡歌曲放入list中
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

            //当前歌曲播放位置
            int position = bundle.getInt("position");


            MyAdapter adapter2 = null;
            adapter2 =(MyAdapter) lv.getAdapter();
            adapter2.setSelectedItem(position);
            lv.setAdapter(adapter2);


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

