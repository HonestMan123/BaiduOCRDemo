package com.baiduapi.demo.utils;

import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baiduapi.demo.APP;

import java.io.File;

/**
 * @author Android(JiaWei)
 * @date 2018/4/12.
 */

public class OCRHttpRequest {
    public static final String SIDE_FACE = "face";
    public static final String SIDE_BACK = "back";
    /**
     * 获取百度token
     *
     * @param ocrCallBack
     */
    public static void getBaiduToken(final OCRCallBack<String> ocrCallBack) {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                ocrCallBack.onSuccess(token);
                ocrCallBack.onComplete();
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                ocrCallBack.onFail("");
                ocrCallBack.onComplete();
                Log.e("Baidu OCR", "AK，SK方式获取token失败\n" + error.getMessage());
            }
        }, APP.getContext(), APPConstants.BAIDU_OCR_KEY, APPConstants.BAIDU_OCR_SECRET);
    }

    /**
     * 身份证识别（百度云）
     *
     * @param imgPath 图片文件路径
     * @param side    身份证正反面
     */
    public static void readIdCard(String imgPath, String side, final OCRCallBack<IDCardResult> ocrCallBack) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(imgPath));
        // 设置身份证正反面
        param.setIdCardSide(side);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                ocrCallBack.onSuccess(result);
                ocrCallBack.onComplete();
            }

            @Override
            public void onError(OCRError error) {
                ocrCallBack.onFail("请选择清晰的身份证照片");
                ocrCallBack.onComplete();
            }
        });
    }

    /**
     * 识别银行卡（百度云）
     *
     * @param imgPath         银行卡图片路径
     * @param ocrCallBack
     */
    public static void readBankCard(String imgPath, final OCRCallBack<BankCardResult> ocrCallBack) {
        BankCardParams param = new BankCardParams();
        param.setImageFile(new File(imgPath));
        OCR.getInstance().recognizeBankCard(param, new OnResultListener<BankCardResult>() {
            @Override
            public void onResult(BankCardResult result) {
                ocrCallBack.onSuccess(result);
                String res = String.format("卡号：%s\n类型：%s\n发卡行：%s",
                        result.getBankCardNumber(),
                        result.getBankCardType().name(),
                        result.getBankName());
                ocrCallBack.onComplete();
            }

            @Override
            public void onError(OCRError error) {
                ocrCallBack.onFail("请选择清晰的银行卡照片");
                ocrCallBack.onComplete();
            }
        });
    }

    public interface OCRCallBack<T> {
        void onSuccess(T t);

        void onFail(String s);

        void onComplete();
    }

}
