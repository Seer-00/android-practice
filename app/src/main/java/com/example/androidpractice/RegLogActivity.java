package com.example.androidpractice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.android.AndroidSmackInitializer;


public class RegLogActivity extends Activity {

    private static final String TAG = RegLogActivity.class.getName();
    private static final int TYPE_LOG = 0;
    private static final int TYPE_REG = 1;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private EditText edtUsr;
    private EditText edtPwd;
    private EditText edtDom;
    private EditText edtIP;
    private CheckBox checkBox;
    private Button btnLogin;
    private Button btnRegister;

    private static User user;

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

    private void initViews() {
        edtUsr = findViewById(R.id.edit_usr);
        edtPwd = findViewById(R.id.edit_pwd);
        edtDom = findViewById(R.id.edit_domain);
        edtIP = findViewById(R.id.edit_ip);
        checkBox = findViewById(R.id.ckb_remember);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        loadInfo();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = getInput();

                if (user == null) {
                    return;
                }

                showProgressDialog();

                ConnThread thread = new ConnThread(TYPE_LOG);
                thread.start();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = getInput();

                if (user == null) {
                    return;
                }

                showProgressDialog();

                ConnThread thread = new ConnThread(TYPE_REG);
                thread.start();
            }
        });
    }

    private static class ConnThread extends Thread {
        private int type;

        public ConnThread(int type) {
            this.type = type;
        }

        @Override
        public void run() {
            String ret = null;
            switch (type) {
                case TYPE_LOG: {
                    /*-- DEBUG --*/
//                    ret = XConnectionHelp.RET_SUCC;
                    ret = conn.login2Server(user);
                    break;
                }
                case TYPE_REG: {
                    /*-- DEBUG --*/
//                    ret = XConnectionHelp.RET_SUCC;
                    ret = conn.register2Server(user);
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
                        conn.getConnection().disconnect();
                        regLogActivity.storeInfo();
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

    private User getInput() {
        String usr = edtUsr.getText().toString().trim();
        String pwd = edtPwd.getText().toString().trim();
        String dom = edtDom.getText().toString().trim();
        String ip = edtIP.getText().toString().trim();
        if (usr.contains("@")) {
            return null;
        }
        return new User(usr, pwd, dom, ip);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("User", user);
        startActivity(intent);
    }

    private void loadInfo() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean remember = pref.getBoolean("remember_info", false);
        if (remember) {
            edtUsr.setText(pref.getString("username", ""));
            edtPwd.setText(pref.getString("password", ""));
            edtDom.setText(pref.getString("domain", ""));
            edtIP.setText(pref.getString("ip_address", ""));
            checkBox.setChecked(true);
        }
    }

    private void storeInfo() {
        editor = pref.edit();
        if (checkBox.isChecked()) {
            editor.putBoolean("remember_info", true);
            editor.putString("username", user.getName());
            editor.putString("password", user.getPwd());
            editor.putString("domain", user.getDom());
            editor.putString("ip_address", user.getIp());
        } else {
            editor.clear();
        }
        editor.apply();
    }
}
