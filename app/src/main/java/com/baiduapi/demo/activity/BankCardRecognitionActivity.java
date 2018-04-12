package com.baiduapi.demo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.baidu.ocr.sdk.model.BankCardResult;
import com.baiduapi.demo.R;
import com.baiduapi.demo.utils.BankCardNumUtils;
import com.baiduapi.demo.utils.FileUtils;
import com.baiduapi.demo.utils.MatisseUtils;
import com.baiduapi.demo.utils.OCRHttpRequest;
import com.soundcloud.android.crop.Crop;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;

/**
 * 识别银行卡
 *
 * @author Android(JiaWei)
 * @date 2017/10/26.
 */

public class BankCardRecognitionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int IMG_CODE = 1;
    private static final int CROP_IMG_CODE = 2;
    private static final String TAG = BankCardRecognitionActivity.class.getSimpleName();
    private TextView resultv;
    private ProgressDialog mProgressDialog;
    //识别银行卡信息
    private BankCardResult bankCardInfo;
    private String bankCardNum;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //银行卡识别完成
                    bankCardNum = bankCardInfo.getBankCardNumber();
                    resultv.setText(String.format("银行卡账号：%s", bankCardNum));
                    dismissLoadingDialog();
                    try {
                        String cardInfo = new BankCardNumUtils(bankCardNum.replace(" ","")).getBankName();
                        resultv.append("\n发卡行及卡种：" + cardInfo);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(resultv, "识别完成", Snackbar.LENGTH_SHORT).show();
                    break;
                case 1:
                    dismissLoadingDialog();
                    bankCardNum = "";
                    resultv.setText("");
                    if (TextUtils.isEmpty((CharSequence) msg.obj)) {
                        return;
                    }
                    Snackbar.make(resultv, (String) msg.obj, Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bankcard);
        initViews();
        initProgressDialog();
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
    }

    private void initViews() {
        resultv = (TextView) findViewById(R.id.bankcard_result_tv);
        findViewById(R.id.bankcard_img_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bankcard_img_btn:
                //选择银行卡照片
                MatisseUtils.selectImg(BankCardRecognitionActivity.this, IMG_CODE);

                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMG_CODE:
                    //选择完的银行卡照片

                    Uri mUri1 = Matisse.obtainResult(data).get(0);
                    try {
                        Uri outputUri = Uri.fromFile(File.createTempFile("corp1", ".jpg"));
                        Crop.of(mUri1, outputUri).withAspect(18, 11).start(BankCardRecognitionActivity.this, CROP_IMG_CODE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case CROP_IMG_CODE:
                    //裁剪完的照片
                    showLodingDialog("识别中...");
                    readBankCard(Crop.getOutput(data));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 识别银行卡
     *
     * @param uri
     */
    private void readBankCard(Uri uri) {
        OCRHttpRequest.readBankCard(FileUtils.getRealPathFromURI(this, uri),
                new OCRHttpRequest.OCRCallBack<BankCardResult>() {
                    @Override
                    public void onSuccess(BankCardResult bankCardResult) {
                        bankCardInfo = bankCardResult;
                        mHandler.sendEmptyMessage(0);
                    }

                    @Override
                    public void onFail(String errorMessage) {
                        Message message = new Message();
                        message.what = 1;
                        message.obj = errorMessage;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    private void showLodingDialog(String message) {
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
