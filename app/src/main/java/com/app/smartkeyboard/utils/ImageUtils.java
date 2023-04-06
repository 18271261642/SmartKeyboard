package com.app.smartkeyboard.utils;

import android.content.Context;
import android.graphics.Bitmap;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import timber.log.Timber;

public class ImageUtils {


    public static List<Bitmap> getGifData(File gifFile){
        com.bumptech.glide.load.resource.gif.GifDrawable g ;
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数="+count);
            for(int i = 0;i<count;i++){
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);
                lt.add(bt);
            }
            return lt;
        }catch (Exception e){
            e.printStackTrace();
            lt.clear();
            return lt;
        }


    }
}
