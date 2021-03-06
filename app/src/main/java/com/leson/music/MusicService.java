package com.leson.music;


//创建一个继承自服务的音乐服务类

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service{

    private MediaPlayer player;
    private Timer timer;
    private int po;

    //绑定服务时，调用此方法
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return new MusicControl();
    }

    //创建播放音乐的服务
    @Override
    public void onCreate() {
        super.onCreate();

        //创建音乐播放器对象
        player = new MediaPlayer();

    }


    //销毁播放音乐服务
    @Override
    public void onDestroy() {
        super.onDestroy();

        //停止播放音乐
        player.stop();

        //释放占用的资源
        player.release();

        //将player置为空
        player = null;

    }




        //播放音乐
        public void play(final List mp3list, final int position){

        po = position;

            try {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath().replace("0","") + "mp3/";
                if(player == null){
                    player = new MediaPlayer();
                }

                //重置
                player.reset();

                //加载多媒体文件
                player.setDataSource(path+ mp3list.get(position));

                //准备播放音乐
                player.prepare();

                //播放音乐
                player.start();

                //添加计时器
                addTimer();

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(position == mp3list.size() - 1){
                            MusicService.this.play(mp3list,0);
                        }else{
                            po++;
                            MusicService.this.play(mp3list,po);
                        }

                    }
                });



                //


            } catch (IOException e){
                e.printStackTrace();
            }
        }


        //暂停播放音乐
        public void pausePlay(){

            player.pause();
        }

        //继续播放音乐
        public void continuePlay(){

            player.start();
        }



        //创建一个实现音乐接口的音乐控制类
        class MusicControl extends Binder implements MusicInterface {

            @Override
            public void play(List mp3list,int position) {

                MusicService.this.play(mp3list, position);
            }

            @Override
            public void pausePlay(){

                MusicService.this.pausePlay();
            }

            @Override
            public void continuePlay(){

                MusicService.this.continuePlay();
            }

            @Override
            public void seekTo(int progress) {

                MusicService.this.seekTo(progress);
            }

            @Override
            public void next(List mp3list){
                if(po == mp3list.size() - 1){
                    MusicService.this.play(mp3list,0);
                }else{
                    po++;
                    MusicService.this.play(mp3list,po);
                }

            }

            @Override
            public void forward(List mp3list){
                if(po == 0){
                    MusicService.this.play(mp3list,mp3list.size()-1);
                }else {
                    po--;
                    MusicService.this.play(mp3list,po);
                }
            }


        }

        //设置音乐的播放位置
        public void seekTo(int progress) {

            player.seekTo(progress);
        }



        //添加计时器用于设置音乐播放器中的播放进度
    public void addTimer(){

        //如果没有创建计时器对象
         if(timer == null) {

            //创建计时器对象
            timer = new Timer();

            timer.schedule(new TimerTask(){

                    //执行计时任务
                    @Override
                    public void run(){

                  //获得歌曲总时长
                  int duration = player.getDuration();

                  //获取歌曲的当前播放进度
                  int currentPosition = player.getCurrentPosition();

                  //创建消息对象
                  Message msg = MainActivity.handler.obtainMessage();

                  //将音乐的播放进度封装至消息对象中
                  Bundle bundle = new Bundle();
                  bundle.putInt("duration", duration);
                  bundle.putInt("currentPosition", currentPosition);
                  bundle.putInt("position",po);
                  msg.setData(bundle);

                  //将消息发送到主线程的消息队列
                  MainActivity.handler.sendMessage(msg);
                    }

                    },
                    //开始计时任务后的5毫秒，第一次执行run方法，以后每500毫秒执行一次
                    5,500);
            }
        }
        }
