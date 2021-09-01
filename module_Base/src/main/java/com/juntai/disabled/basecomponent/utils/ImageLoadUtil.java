package com.juntai.disabled.basecomponent.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.juntai.disabled.basecomponent.R;

/**
 * 图片加载工具
 *
 * @aouther Ma
 * @date 2019/3/5
 */
public class ImageLoadUtil {

    /**
     * 加载本地图片
     *
     * @param context
     * @param recouse
     * @param view
     */
    public static void loadImage(Context context, int recouse, ImageView view) {
        Glide.with(context).load(recouse).into(view);
    }

    /**
     * 加载图片
     */
    public static void loadImage(Context context, Bitmap bitmap, ImageView view) {
        Glide.with(context).load(bitmap).into(view);
    }
    /**
     * @param context
     * @param url
     * @param view
     */
    public static void loadImageWithCache(Context context, String url, int replacePic,ImageView view) {
        Glide.with(context).load(url).skipMemoryCache(false)
                .apply(new RequestOptions().error(replacePic).placeholder(replacePic))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view);
    }
    /**
     * @param context
     * @param url     内存缓存和硬盘缓存
     * @param view
     */
    public static void loadImageCache(Context context, String url, ImageView view) {
        try {
            int urlInt = Integer.parseInt(url);
            Glide.with(context).load(urlInt).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.nopicture).into(view);
        } catch (NumberFormatException ex) {
            Glide.with(context).load(url).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.nopicture).into(view);
        }
    }

    /**
     * @param context
     * @param url     内存缓存和硬盘缓存
     * @param view
     */
    public static void loadImageCache(Context context, int url, ImageView view) {
        Glide.with(context).load(url).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.drawable.nopicture).into(view);
    }

    /**
     * @param context
     * @param url     加载网络视频的时候 不能使用硬盘缓存
     * @param view
     */
    public static void loadImageNoCache(Context context, String url, ImageView view) {
        Glide.with(context).load(url).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(view);
    }

    /**
     * @param context
     * @param url     加载网络视频的时候 不能使用硬盘缓存
     * @param view
     */
    public static void loadImageNoCache(Context context, String url, ImageView view, int error) {
        Glide.with(context).load(url).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).error(error).skipMemoryCache(true)).into(view);
    }



    /**
     * @param context
     * @param url
     * @param view
     */
    public static void loadImageNoCrash(Context context, String url, ImageView view, int loading, int error) {
        Glide.with(context).load(url).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).placeholder(loading).dontAnimate().error(error).into(view);
    }


    /**
     * @param context
     * @param url
     * @param error
     * @param placeholder
     * @param view
     */
    public static void loadImage(Context context, String url, int error, int placeholder, ImageView view) {
        Glide.with(context).load(url).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESOURCE).apply(new RequestOptions().error(error).placeholder(placeholder)).into(view);
    }

    /**
     * @param context
     * @param url
     * @param view
     */
    public static void loadImage(Context context, String url, ImageView view) {
        Glide.with(context).load(url).skipMemoryCache(false)
                .apply(new RequestOptions().error(R.drawable.nopicture).placeholder(R.drawable.nopicture))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view);
    }

    /**
     * 加载圆形图片
     *
     * @param context
     * @param url
     * @param error
     * @param placeholder
     * @param view
     */
    public static void loadCircularImage(Context context, String url, int error, int placeholder, ImageView view) {
        Glide.with(context).load(url).apply(new RequestOptions().error(error).placeholder(placeholder).circleCrop()).into(view);
    }

    public static void loadCentercropImage(Context context, int url, ImageView view) {
        Glide.with(context).load(url).apply(new RequestOptions().optionalCenterCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(view);
    }

    public static void loadCentercropImage(Context context, String url, ImageView view) {
        Glide.with(context).load(url).apply(new RequestOptions().optionalCenterCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(view);
    }

    /**
     * 加载圆形图片,无缓存
     *
     * @param context
     * @param url
     * @param view
     * @param placeholder
     * @param error
     */
    public static void loadCirImgNoCrash(Context context, String url, ImageView view, int placeholder, int error) {
        Glide.with(context).load(url).apply(new RequestOptions().error(error).placeholder(placeholder).circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(view);
    }

    /**
     * 加载圆形图
     *
     */
    public static void loadCirImgWithCrash(Context context, String url, ImageView view, int placeholder, int error) {
        Glide.with(context).load(url).apply(new RequestOptions().error(error).placeholder(placeholder).circleCrop()).into(view);
    }

    /**
     * 加载圆形图片,无缓存
     *
     * @param context
     * @param view
     */
    public static void loadCirImgNoCrash(Context context, int resId, ImageView view) {
        Glide.with(context).load(resId).apply(new RequestOptions().circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(view);
    }

    public interface BitmapCallBack {
        void getBitmap(Bitmap bitmap);
    }

    /**
     * 获取bitmap
     *
     * @param context
     * @param path
     * @param error
     * @param callback
     */
    public static void getBitmap(Context context, String path, int error, BitmapCallBack callback) {
        Glide.with(context).asBitmap().error(error).load(path).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                LogUtil.e("onResourceReady");
                callback.getBitmap(resource);
            }
        });
    }

    /**
     * 获取bitmap
     * @param context
     * @param path
     * @param error
     * @param width 宽
     * @param height 高
     * @param callback
     */
    public static void getBitmap(Context context, String path, int error, int width, int height, BitmapCallBack callback) {
        Glide.with(context).asBitmap().error(error).load(path).into(new SimpleTarget<Bitmap>(width,height) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                LogUtil.e("onResourceReady");
                callback.getBitmap(resource);
            }
        });
    }

    /**
     * 加载圆角图片
     * 加载网络视频的时候 不能使用硬盘缓存
     * @param context
     * @param url
     * @param view
     * @param placeholder
     * @param type 1视频，2图片
     */
    public static void loadRoundCornerImg(Context context, String url, ImageView view, int placeholder, int type){
        int corners = 10;
        if (type == 1){
            corners = 7;
        }else {
            corners = 15;
        }
        RoundedCorners roundedCorners = new RoundedCorners(corners);//数字为圆角度数
        RequestOptions coverRequestOptions;
        if (type == 1){
            coverRequestOptions = new RequestOptions()
                    .error(R.drawable.nopicture_video)
                    .placeholder(placeholder)
                    .override(350,200)
                    .centerCrop()
                    .transform(new CenterCrop(),roundedCorners)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(true);//不做内存缓存
        }else {
            coverRequestOptions = new RequestOptions()
                    .error(R.drawable.nopicture)
                    .placeholder(placeholder)
                    .override(300,200)
                    .centerCrop()
                    .transform(new CenterCrop(),roundedCorners)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .skipMemoryCache(false);//不做内存缓存
        }
        Glide.with(context)
                .load(url)
                .apply(coverRequestOptions)
//                .thumbnail(0.1f)//缩略图
                .into(view);
    }

    /**
     * 加载列表图片(包括视频),磁盘缓存
     * @param context
     * @param url 图片地址
     * @param view 填充view
     * @param type 类型，1视频，2图片
     * @param width 裁剪宽 120
     * @param height 裁剪高 160
     * @param corners 圆角度数 10
     * @param error 错误占位图
     * @param placeholder 加载占位图
     */
    public static void loadImageForList(Context context, String url, ImageView view, int type, int width, int height, int corners, int error, int placeholder) {
        RoundedCorners roundedCorners = new RoundedCorners(corners);//数字为圆角度数
        RequestOptions coverRequestOptions = new RequestOptions()
                .error(error)//R.drawable.nopicture
                .placeholder(placeholder)
                .override(width,height)
                .centerCrop()
                .transform(new CenterCrop(),roundedCorners)
                .diskCacheStrategy(type == 1? DiskCacheStrategy.AUTOMATIC : DiskCacheStrategy.RESOURCE)
                .skipMemoryCache(false);//内存缓存

        Glide.with(context)
                .load(url)
                .apply(coverRequestOptions)
//                .thumbnail(0.1f)//缩略图
                .into(view);
    }

    public static void loadImageForList(Context context, String url, ImageView view, int error, int placeholder) {
        loadImageForList(context, url, view, 2, 160, 120, 1, error, placeholder);
    }

    public static void loadImageForList(Context context, String url, ImageView view, int type, int width, int height, int corners) {
        loadImageForList(context, url, view, type, width, height, corners, R.drawable.nopicture, R.drawable.nopicture);
    }

    public static void loadImageForList(Context context, String url, ImageView view, int type, int width, int height) {
        loadImageForList(context, url, view, type, width, height, 1);
    }
    public static void loadImageForList(Context context, String url, ImageView view, int type) {
        loadImageForList(context, url, view, type, 160, 120);
    }
    public static void loadCornerImageForList(Context context, String url, ImageView view, int corners) {
        loadImageForList(context, url, view, 2, 160, 120, corners);
    }
    public static void loadImageForList(Context context, String url, ImageView view) {
        loadImageForList(context, url, view, 2);
    }

}
