package com.example.androidpractice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    private String usr;
    private String pwd;
    private String ip;

    private ProgressDialog progressDialog;
    private static ConnHandler connHandler;
    private static XConnectionHelp conn;

    public XConnectionHelp getConn() {
        return conn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_message, R.id.navigation_others
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        initConnection();
    }

    @Override
    protected void onDestroy() {
        conn.getConnection().disconnect();
        super.onDestroy();
    }

    private void initConnection() {
        Intent intent = getIntent();
        usr = intent.getStringExtra("Usr");
        pwd = intent.getStringExtra("Pwd");
        ip = intent.getStringExtra("IP");

        conn = new XConnectionHelp();
        connHandler = new ConnHandler(this);

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = conn.login2Server("test3", "123", "192.168.81.135");
                Message msg = Message.obtain();
                msg.obj = res;
                connHandler.sendMessage(msg);
            }
        }).start();
    }

    private static class ConnHandler extends Handler {
        private MainActivity mainActivity;

        public ConnHandler(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.obj == null || mainActivity.progressDialog == null
                    || !(mainActivity.progressDialog.isShowing())) {
                return;
            }

            mainActivity.progressDialog.dismiss();
            String res = (String) msg.obj;

            if (!(res.equals(XConnectionHelp.RET_SUCC))) {
                Toast.makeText(mainActivity, "连接服务器出错...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

}
