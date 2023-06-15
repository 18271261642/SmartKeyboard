package com.app.smartkeyboard.utils;

import android.content.Context;
import android.graphics.Bitmap;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import timber.log.Timber;

public class ImageUtils {


    public static List<Bitmap> getGifData(File gifFile) {
        com.bumptech.glide.load.resource.gif.GifDrawable g;
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("--------尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    break;
                }
                lt.add(bt);
            }

            return lt.size() > 10 ? lt.subList(0, 10) : lt;
        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }

    }


    public static List<Bitmap> getGifDataBitmap(File gifFile) {
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("---111-----尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    lt.clear();
                    break;
                }


                if (width == 320 && height == 172) {
                    lt.add(bt);
                } else {
                    //计算偏移量
                    int x = width / 2 - 160;
                    int y = height / 2 - 81;


                    Bitmap newBitmap = Bitmap.createBitmap(bt, x, y, 320, 172);

                    //Timber.e("---222-----尺寸=" + newBitmap.getWidth() + " height=" + newBitmap.getHeight());
                    lt.add(newBitmap);
                }

            }


            int size = lt.size();
            if (size == 0) {
                return lt;
            }
            if (size > 9) {
                List<Bitmap> bigList = new ArrayList<>();
                int number = size / 9;
                Timber.e("-------number=" + number);
                for (int k = 0; k < size; k += number) {
                    if ((k + number) < size) {
                        bigList.add(lt.get(k));
                    }
                }
                return bigList;
            } else {
                return lt;
            }

        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }

    }



    public static int getGifAnimationDuration(File gifFile){
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int duration = gifDrawable.getDuration();
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static List<Bitmap> getGifDataBitmapNoClip(File gifFile) {
        com.bumptech.glide.load.resource.gif.GifDrawable g;
        List<Bitmap> lt = new ArrayList<>();
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            int count = gifDrawable.getNumberOfFrames();
            Timber.e("-------帧数=" + count);
            for (int i = 0; i < count; i++) {
                Bitmap bt = gifDrawable.seekToFrameAndGet(i);

                //判断尺寸，小于320x172的提示不合法
                int width = bt.getWidth();
                int height = bt.getHeight();

                //Timber.e("---111-----尺寸=" + width + " height=" + height);
                if (width < 320 || height < 172) {
                    lt.clear();
                    break;
                }
                lt.add(bt);

            }


            int size = lt.size();
            if (size == 0) {
                return lt;
            }
            if (size > 9) {
                List<Bitmap> bigList = new ArrayList<>();
                int number = size / 9;
                Timber.e("-------number=" + number);
                for (int k = 0; k < size; k += number) {
                    if ((k + number) < size) {
                        bigList.add(lt.get(k));
                    }
                }
                return bigList;
            } else {
                return lt;
            }

        } catch (Exception e) {
            e.printStackTrace();
            lt.clear();
            return lt;
        }


    }

}
