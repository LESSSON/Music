package com.leson.music;

import java.util.List;

//创建一个音乐播放接口
public interface MusicInterface {

    //播放音乐
    void play(List mp3list, int position);

    //暂停播放音乐
    void pausePlay();

    //继续播放音乐
    void continuePlay();

    //修改音乐播放位置
    void seekTo(int progress);
    
}
