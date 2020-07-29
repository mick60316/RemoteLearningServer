package com.example.remotecontroller.Component;

import android.icu.text.Collator;
import android.media.MediaPlayer;
import android.net.Uri;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.remotecontroller.ExtraTools;
import com.example.remotecontroller.Resource;


import java.util.Timer;
import java.util.TimerTask;

public class CustomVideoView implements MediaPlayer.OnCompletionListener {


    private String TAG = "Video view";
    private String url;
    private VideoView videoView;
    private ImageView imageView;

    private int currentSession= 0;

    private boolean isLoop =false;
    private boolean isVideoReady =false;


    private int [] currentCheckPoint;
    private int [] s2CheckPoint= new int []{-1,4000,-1};
    private int [] s3CheckPoint =new int []{3000,-1,-1};
    private int [] s4CheckPoint=new int []{-1,-1,-1,-1,-1,-1};
    private int [] s5CheckPoint =new int []{3200,-1,-1};
    private int [] checkPoint  =new int []{-1,2000,3000,4000,5000};
    private int currentCheckPointIndex =0 ;

    public CustomVideoView (VideoView videoView, ImageView imageView)
    {

        this.videoView=videoView;
        this.imageView=imageView;

        videoView.setOnCompletionListener(this);
        detectorVideoFrame();
    }

    public void setUrl(String url)
    {
        this.url=url;
        videoView.setVideoURI(Uri.parse(url));
    }
    public void playVideo ()
    {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                videoView.start();
                isVideoReady =true;
            }
        });

    }
    public void nextClick ()
    {
        /*
            Next button click event
         */
        if (currentSession ==ExtraTools.S3) {
            videoView.start();
            currentCheckPointIndex ++;
            if ( currentCheckPointIndex==checkPoint.length) currentCheckPointIndex =checkPoint.length-1;
        }
        else if (currentSession==ExtraTools.S4){
            imageView.setImageResource(Resource.s4ImageId[currentCheckPointIndex]);
            currentCheckPointIndex++;
            currentCheckPointIndex%=Resource.s4ImageId.length;
        }

    }

    public void changeSession (int session)
    {
        /*
            change video and image source by session

            argv :
                session : index from session
        */

        Log.e(TAG,"Change Session");
        currentSession=session;
        currentCheckPointIndex= 0;
        String videoReource="";
        imageView.setVisibility(View.INVISIBLE);
        isVideoReady =false;
        switch (session)
        {
            case ExtraTools.S1:
                //videoReource=Resource.s1VideoPath;
                isLoop =false;
                break;
            case ExtraTools.S2:
                //videoReource=Resource.s2VideoPath;
                checkPoint =s2CheckPoint;
                isLoop=true;
                break;
            case ExtraTools.S3:
                videoReource= Resource.s3VideoPath;
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                checkPoint =s3CheckPoint;
                isLoop =false;
                break;
            case ExtraTools.S4:
               // videoReource=Resource.s4VideoPath;

                checkPoint =s4CheckPoint;
                isLoop=true;
                break;
            case ExtraTools.S5:
                checkPoint=s5CheckPoint;
                //videoReource=Resource.s5VideoPath;
                isLoop=false;

                break;
            default:
                break;

        }
        if (!videoReource.equals(""))
        {
            setUrl(videoReource);
            playVideo();
        }




    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        /*
            check video is finish and loop or not
        */
        if (isLoop)
        {
            //videoView.seekTo(0);
            videoView.start();
        }
    }

    private void detectorVideoFrame ()
    {
        /*
            make a timer to detect frame position

        */
        Timer clockTimer =new Timer(true);
        clockTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (isVideoReady&& videoView.isPlaying() ) {
                    int position = videoView.getCurrentPosition();
                    //Log.i(TAG,"position"+position);
                    if (checkPoint[currentCheckPointIndex] != -1 && position > checkPoint[currentCheckPointIndex])
                        videoView.pause();
                }
            }
        },0,100);
    }
    public void setSessionImage (int imageIndex)
    {
        /*
            set imageview 's image  by image index
            argv:
                imageIndex : image's index
         */
        Log.e(TAG,"setSessionImage" +imageIndex);
        currentCheckPointIndex =  imageIndex;
        if (currentCheckPointIndex ==-1){
            imageView.setImageResource(Resource.resourceImageId[0]);
        }else {
            imageView.setImageResource(Resource.s4ImageId[currentCheckPointIndex]);
        }
        videoView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        currentCheckPointIndex+=1;
        currentCheckPointIndex%=4;

    }





}
