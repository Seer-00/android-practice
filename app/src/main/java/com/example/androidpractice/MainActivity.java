package com.example.androidpractice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();
    private ProgressDialog mProgressDialog;
    private DownloadHandler mDownloadHandler = new DownloadHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_start = findViewById(R.id.btn_scan);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });
    }

    private void startScanner() {
        // 原始扫描
        /*
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();
        */

        // 自定义样式扫描
        new IntentIntegrator(this)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// 扫码的类型，可选：一维码，二维码，一/二维码
                //.setPrompt("请对准二维码")// 设置提示语
                .setCameraId(0)// 选择摄像头，可使用前置或者后置
                .setBeepEnabled(true)// 是否开启声音，扫完码之后会"哔"的一声
                .setCaptureActivity(ScanISBNActivity.class)//自定义扫码界面
                .initiateScan();// 初始化扫码

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if ((result == null) || (result.getContents() == null)) {
            Log.i(MainActivity.TAG, "用户取消了扫描");
            Toast.makeText(this, "扫描取消", Toast.LENGTH_SHORT).show();
            return;
        }
        String contents = result.getContents();

        /* ISBN for debugging */
        // String contents = "0000000000000";      // book not found
        // String contents = "9787121402180";      // no cover
        // String contents = "9787115209306";      // valid

        Log.i(MainActivity.TAG, "Scanning result: " + contents);
        Toast.makeText(this, "扫描结果: " + contents, Toast.LENGTH_LONG).show();

        // 下载耗时，显示进度条
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("通信中...");
        mProgressDialog.show();

        DownloadThread thread = new DownloadThread(BookAPI.URL_ISBN_BASE + contents);
        thread.start();
    }

    private void startBookInfoDetailActivity(BookInfo bookInfo) {
        if (bookInfo == null) {
            return;
        }

        Intent intent = new Intent(this, BookInfoDetailActivity.class);
        intent.putExtra(BookInfo.class.getName(), bookInfo);
        startActivity(intent);
    }

    private class DownloadThread extends Thread {
        private String url;

        public DownloadThread(String url) {
            super();
            this.url = url;
            Log.i(MainActivity.TAG, "Success: create download thread");
        }

        @Override
        public void run() {
            Log.i(MainActivity.TAG, "Success: run download thread");
            Message msg = Message.obtain();
            msg.obj = Utils.download(url);
            mDownloadHandler.sendMessage(msg);
        }
    }

    private static class DownloadHandler extends Handler {

        private MainActivity mainActivity;

        public DownloadHandler(MainActivity mainActivity) {
            super();
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if ((msg.obj == null) || (mainActivity.mProgressDialog == null)
                    || (!mainActivity.mProgressDialog.isShowing())) {
                return;
            }

            mainActivity.mProgressDialog.dismiss();

            Response response = (Response) msg.obj;

            if (response.getResCode() != BookAPI.RESPONSE_CODE_SUCCEED) {
                // 通信异常处理
                Log.i(MainActivity.TAG, "Failure: Communicating ["
                        + response.getResCode() + "]: " + response.getResMessage());

                Toast.makeText(mainActivity, "状态码:" + response.getResCode() + " "
                        + response.getResMessage(), Toast.LENGTH_LONG).show();
            } else {
                // 通信正常，转到图书信息界面
                Log.i(MainActivity.TAG, "Success: Communicating");
                mainActivity.startBookInfoDetailActivity((BookInfo) response.getResMessage());
            }
        }
    }

}

// api: https://book.zuk.pw/?s=Index.V1_Open.Book&isbn=9787115209306

