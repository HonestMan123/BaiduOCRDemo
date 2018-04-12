package com.baiduapi.demo.utils;

import android.os.Environment;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 存储相关常量
 *
 * @author Android
 * @date 2017/5/22
 */

public class MemoryConstants {
    public static final String SAVE_IMG_PATH = Environment.getExternalStorageDirectory().getPath() + "/JYAPP/image_cache/";

    /**
     * Byte与Byte的倍数
     */
    public static final int BYTE = 1;
    /**
     * KB与Byte的倍数
     */
    public static final int KB   = 1024;
    /**
     * MB与Byte的倍数
     */
    public static final int MB   = 1048576;
    /**
     * GB与Byte的倍数
     */
    public static final int GB   = 1073741824;

    @IntDef({BYTE, KB, MB, GB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Unit {
    }
}
