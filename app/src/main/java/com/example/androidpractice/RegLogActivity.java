package com.example.androidpractice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jivesoftware.smack.android.AndroidSmackInitializer;


public class RegLogActivity extends AppCompatActivity {

    private static final String TAG = RegLogActivity.class.getName();
    private static final int TYPE_LOG = 0;
    private static final int TYPE_REG = 1;

    private EditText edtUsr;
    private EditText edtPwd;
    private EditText edtIP;
    private Button btnLogin;
    private Button btnRegister;

    private String usr;
    private String pwd;
    private String ip;

    private ProgressDialog progressDialog;
    private static XConnectionHelp conn;
    private static ConnHandler connHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_log);

        AndroidSmackInitializer.initialize(this);

        initViews();

        conn = new XConnectionHelp();
        connHandler = new ConnHandler(this);
    }

    @Override
    protected void onDestroy() {
//        conn.getConnection().disconnect();
        super.onDestroy();
    }

    private void initViews() {
        edtUsr = findViewById(R.id.edit_usr);
        edtPwd = findViewById(R.id.edit_pwd);
        edtIP = findViewById(R.id.edit_ip);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] inputs = getInput();

                showProgressDialog();

                ConnThread thread = new ConnThread(inputs, TYPE_LOG);
                thread.start();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] inputs = getInput();

                showProgressDialog();

                ConnThread thread = new ConnThread(inputs, TYPE_REG);
                thread.start();
            }
        });
    }

    private static class ConnThread extends Thread {
        private String usr;
        private String pwd;
        private String ip;
        private int type;

        public ConnThread(String[] inputs, int type) {
            this.usr = inputs[0];
            this.pwd = inputs[1];
            this.ip = inputs[2];
            this.type = type;
        }

        @Override
        public void run() {
            String ret = null;
            switch (type) {
                case TYPE_LOG: {
                    /*-- DEBUG --*/
                    // ret = conn.login2Server(usr, pwd, ip);
//                    ret = conn.login2Server("test3", "123", "192.168.81.135");
                    ret = XConnectionHelp.RET_SUCC;
                    break;
                }
                case TYPE_REG: {
                    /*-- DEBUG --*/
                    // ret = conn.register2Server(usr, pwd, ip);
                    ret = conn.register2Server("test111", "123", "192.168.81.135");
                    break;
                }
                default:
                    break;
            }

            Message msg = Message.obtain();
            msg.what = type;
            msg.obj = ret;
            connHandler.sendMessage(msg);
        }
    }

    private static class ConnHandler extends Handler {
        private RegLogActivity regLogActivity;

        public ConnHandler(RegLogActivity regLogActivity) {
            super();
            this.regLogActivity = regLogActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.obj == null || regLogActivity.progressDialog == null
                    || !(regLogActivity.progressDialog.isShowing())) {
                return;
            }

            regLogActivity.progressDialog.dismiss();
            String res = (String) msg.obj;

            switch (msg.what) {
                case TYPE_LOG: {
                    Log.i(TAG, "Login message");
                    Toast.makeText(regLogActivity, res, Toast.LENGTH_SHORT).show();

                    if (res.equals(XConnectionHelp.RET_SUCC)) {
                        regLogActivity.startMainActivity();
                        regLogActivity.finish();
                    }
                    break;
                }
                case TYPE_REG: {
                    Log.i(TAG, "Register message");
                    Toast.makeText(regLogActivity, res, Toast.LENGTH_SHORT).show();

                    if (res.equals(XConnectionHelp.RET_SUCC)) {
                        regLogActivity.btnRegister.setVisibility(View.GONE);
                    }
                    break;
                }
                default:
                    break;
            }

        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

    private String[] getInput() {
        this.usr = edtUsr.getText().toString().trim();
        this.pwd = edtPwd.getText().toString().trim();
        this.ip = edtIP.getText().toString().trim();
        return new String[] { this.usr, this.pwd, this.ip};
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Usr", this.usr);
        intent.putExtra("Pwd", this.pwd);
        intent.putExtra("IP", this.ip);
        startActivity(intent);
    }
}
