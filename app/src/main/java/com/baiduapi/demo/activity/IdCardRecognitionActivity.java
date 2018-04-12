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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baiduapi.demo.R;
import com.baiduapi.demo.utils.FileUtils;
import com.baiduapi.demo.utils.MatisseUtils;
import com.baiduapi.demo.utils.OCRHttpRequest;
import com.soundcloud.android.crop.Crop;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.IOException;

/**
 * 身份证识别（包含正反面）
 *
 * @author Android(JiaWei)
 * @date 2017/10/26.
 */

public class IdCardRecognitionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int FACE_IMG_CODE = 1;
    private static final int FACE_CROP_IMG_CODE = 2;
    private static final int BACK_IMG_CODE = 3;
    private static final int BACK_CROP_IMG_CODE = 4;
    private static final String TAG = IdCardRecognitionActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;
    private TextView nameTv;
    private TextView genderTv;
    private TextView birthTv;
    private TextView addressTv;
    private TextView numTv;
    private TextView issueTv;
    private TextView timeTv;

    private String name;
    private String gender;
    private String birth;
    private String address;
    private String num;
    private String issue;
    private String time;
    private Uri faceImgUri;
    private Uri backImgUri;

    /**
     * 身份识别的正面信息
     */
    private IDCardResult faceIdCardInfo;
    /**
     * 身份识别的反面信息
     */
    private IDCardResult backIdCardInfo;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //身份证正面识别完成
                    name = faceIdCardInfo.getName().toString();
                    gender = faceIdCardInfo.getGender().toString();
                    birth = faceIdCardInfo.getBirthday().toString();
                    address = faceIdCardInfo.getAddress().toString();
                    num = faceIdCardInfo.getIdNumber().toString();
                    nameTv.setText(String.format("姓名：%s" , name));
                    genderTv.setText(String.format("性别：%s" , gender));
                    birthTv.setText(String.format("出生：%s" , birth));
                    addressTv.setText(String.format("住址：%s" , address));
                    numTv.setText(String.format("身份证号码：%s" , num));
                    dismissLoadingDialog();
                    Snackbar.make(nameTv, "识别完成", Snackbar.LENGTH_SHORT).show();
                    break;
                case 1:
                    dismissLoadingDialog();
                    if (TextUtils.isEmpty((CharSequence) msg.obj)) {
                        return;
                    }
                    Snackbar.make(nameTv, (String) msg.obj, Snackbar.LENGTH_SHORT).show();
                    break;
                case 2:
                    //身份证反面识别完成
                    issue = backIdCardInfo.getIssueAuthority().toString();
                    issueTv.setText(String.format("发证机关：%s", issue));
                    timeTv.setText(String.format("有效期限：%s", time));
                    dismissLoadingDialog();
                    Snackbar.make(nameTv, "识别完成", Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);
        initViews();

    }

    private void initViews() {
        mProgressDialog = new ProgressDialog(this);
        nameTv = (TextView) findViewById(R.id.idcard_name_tv);
        genderTv = (TextView) findViewById(R.id.idcard_gender_tv);
        birthTv = (TextView) findViewById(R.id.idcard_birth_tv);
        addressTv = (TextView) findViewById(R.id.idcard_address_tv);
        numTv = (TextView) findViewById(R.id.idcard_num_tv);
        issueTv = (TextView) findViewById(R.id.idcard_issue_tv);
        timeTv = (TextView) findViewById(R.id.idcard_time_tv);

        findViewById(R.id.idcard_face_img_btn).setOnClickListener(this);
        findViewById(R.id.idcard_back_img_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.idcard_face_img_btn:
                //身份证正面
                MatisseUtils.selectImg(IdCardRecognitionActivity.this, FACE_IMG_CODE);
                break;
            case R.id.idcard_back_img_btn:
                //身份证反面
                MatisseUtils.selectImg(IdCardRecognitionActivity.this, BACK_IMG_CODE);
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
                case FACE_IMG_CODE:
                    //选择的正面照片
                    Uri mUri1 = Matisse.obtainResult(data).get(0);
                    try {
                        Uri outputUri = Uri.fromFile(File.createTempFile("corp1", ".jpg"));
                        Crop.of(mUri1, outputUri).withAspect(18, 11).start(IdCardRecognitionActivity.this, FACE_CROP_IMG_CODE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case FACE_CROP_IMG_CODE:
                    //裁剪后的正面照片
                    showLodingDialog("识别中...");
                    readFaceIdCardImg(Crop.getOutput(data));
                    break;
                case BACK_IMG_CODE:
                    //选择的反面照片
                    Uri mUri = Matisse.obtainResult(data).get(0);
                    try {
                        Uri outputUri = Uri.fromFile(File.createTempFile("corp1", ".jpg"));
                        Crop.of(mUri, outputUri).withAspect(18, 11).start(IdCardRecognitionActivity.this, BACK_CROP_IMG_CODE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case BACK_CROP_IMG_CODE:
                    //裁剪后的反面照片
                    showLodingDialog("识别中...");
                    readBackIdCardImg(Crop.getOutput(data));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 识别身份证正面
     *
     * @param uri
     */
    private void readFaceIdCardImg(Uri uri) {
        faceImgUri = uri;
        OCRHttpRequest.readIdCard(FileUtils.getRealPathFromURI(this, uri),
                IDCardParams.ID_CARD_SIDE_FRONT,
                new OCRHttpRequest.OCRCallBack<IDCardResult>() {
                    @Override
                    public void onSuccess(IDCardResult idCardResult) {
                        faceIdCardInfo = idCardResult;
                        Log.d(TAG, "姓名：" + faceIdCardInfo.getName()
                                + "\n" + "性别：" + faceIdCardInfo.getGender()
                                + "\n" + "生日：" + faceIdCardInfo.getBirthday()
                                + "\n" + "住址：" + faceIdCardInfo.getAddress()
                                + "\n" + "身份证号码：" + faceIdCardInfo.getIdNumber());
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

    /**
     * 识别身份证反面
     */
    private void readBackIdCardImg(Uri uri) {
        backImgUri = uri;
        OCRHttpRequest.readIdCard(FileUtils.getRealPathFromURI(this, uri),
                IDCardParams.ID_CARD_SIDE_BACK,
                new OCRHttpRequest.OCRCallBack<IDCardResult>() {
                    @Override
                    public void onSuccess(IDCardResult idCardResult) {
                        backIdCardInfo = idCardResult;
                        StringBuilder stringBuilder = new StringBuilder(backIdCardInfo.getSignDate().toString());
                        stringBuilder.insert(4, ".");
                        stringBuilder.insert(7, ".");
                        stringBuilder.insert(10, "\t-\t");
                        time = stringBuilder.toString();
                        if (!"长期".equals(backIdCardInfo.getExpiryDate().toString())) {
                            StringBuilder stringBuilder1 = new StringBuilder(backIdCardInfo.getExpiryDate().toString());
                            stringBuilder1.insert(4, ".");
                            stringBuilder1.insert(7, ".");
                            stringBuilder1.insert(0, time);
                            time = stringBuilder1.toString();
                        } else {
                            time += backIdCardInfo.getExpiryDate().toString();
                        }
                        Log.d(TAG, "签发机关：" + backIdCardInfo.getIssueAuthority()
                                + "\n" + "有效期限：" + time);
                        mHandler.sendEmptyMessage(2);
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

    private void showLodingDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
