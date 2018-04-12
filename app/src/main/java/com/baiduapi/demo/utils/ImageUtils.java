package com.baiduapi.demo.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 *
 * @author Android(JiaWei)
 * @date 2017/9/11
 */

public class ImageUtils {
    public static final String SAVE_PATH = Environment.getExternalStorageDirectory() + "/Wallets/";

    /**
     * 保存图片
     * @param mBitmap
     * @param fileName
     * @return
     */
    public static boolean saveImg(Bitmap mBitmap,String fileName){
        if (isEmptyBitmap(mBitmap)){
            return false;
        }
        File destDir = new File(SAVE_PATH);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try {
            File f = new File(SAVE_PATH, fileName + ".PNG");
            if (f.exists()) {
                f.delete();
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int options = 100;
            mBitmap.compress(Bitmap.CompressFormat.JPEG, options, os);
            FileOutputStream out = new FileOutputStream(f);
            out.write(os.toByteArray());
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断bitmap对象是否为空
     *
     * @param src 源图片
     * @return {@code true}: 是<br>{@code false}: 否
     */
    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
