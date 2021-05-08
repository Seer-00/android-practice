package com.example.androidpractice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapUtils {

    private static final String TAG = BitmapUtils.class.getName();

    // 根据ImageView的大小，设置高和宽：280f 280f
    private static final float TARGET_WIDTH = 280f;
    private static final float TARGET_HEIGHT = 280f;

    public static Bitmap compress(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        /*
        //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        Log.i(TAG, "baos.len:" + baos.toByteArray().length);
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();//重置baos即清空baos
            // Bitmap.CompressFormat Type=picType==0?Bitmap.CompressFormat.PNG:Bitmap.CompressFormat.JPEG;
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
            // image.compress(Type, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
         */
        ByteArrayInputStream isbm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，只读入宽和高
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isbm, null, newOpts);

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        Log.i(TAG, "Origin W/H: " + w + " " + h);

        // 缩放比：1表示不缩放。固定比例缩放，只用高或宽其中一个数据进行计算
        int sampleSize = 1;

        if (w >= h && w > TARGET_WIDTH) {
            //如果宽度大，根据宽度固定大小缩放
            sampleSize = (int) (w / TARGET_WIDTH);
        } else if (w < h && h > TARGET_HEIGHT) {
            //如果高度大，根据宽度固定大小缩放
            sampleSize = (int) (h / TARGET_HEIGHT);
        }
        if (sampleSize <= 0) {
            sampleSize = 1;
        }

        Log.i(TAG, "compress sampleSize: " + sampleSize);
        Log.i(TAG, "Compressed W/H: " + w/sampleSize + " " + h/sampleSize);

        // 设置缩放比例
        newOpts.inSampleSize = sampleSize;
        // 重新读入图片
        newOpts.inJustDecodeBounds = false;
        isbm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isbm, null, newOpts);

        return bitmap;
    }

    public static Bitmap downloadBitmap(String url) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        Bitmap bitmap = null;

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(5000);
            is = connection.getInputStream();
            bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);

            Log.i(TAG, "Success: download bitmap H/W/Size" + " "
                    + bitmap.getHeight() + "/" + bitmap.getWidth() + "/" + bitmap.getAllocationByteCount());

            bitmap = compress(bitmap);
            Log.i(TAG, "Success: compress bitmap H/W/Size" + " "
                    + bitmap.getHeight() + "/" + bitmap.getWidth() + "/" + bitmap.getAllocationByteCount());

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Failure: download bitmap");
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

}
