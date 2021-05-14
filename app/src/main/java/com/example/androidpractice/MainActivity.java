package com.example.androidpractice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.androidpractice.isbn.BookAPI;
import com.example.androidpractice.isbn.BookInfo;
import com.example.androidpractice.isbn.BookInfoDetailActivity;
import com.example.androidpractice.isbn.DownloadUtils;
import com.example.androidpractice.isbn.Response;
import com.example.androidpractice.isbn.ScanISBNActivity;
import com.example.androidpractice.ui.others.OthersFragment;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.EntityBareJid;

import java.util.Collection;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    private static User user;

    private ProgressDialog progressDialog;
    private static IntentIntegrator intentIntegrator;
    private static DownloadHandler downloadHandler;
    private static ConnHandler connHandler;
    private static RecvHandler recvHandler;
    private static XConnectionHelp conn;
    private static ChatManager chatManager;
    private static Roster roster;

    /* Getter */
    public User getUser() {
        return user;
    }

    public XConnectionHelp getConn() {
        return conn;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public Roster getRoster() {
        return roster;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fresco.initialize(this);
        // initialize layout (in ConnHandler) after connect to server successfully
        initConnection();
        // Handle thread of downloading book info
        downloadHandler = new DownloadHandler(this);
        intentIntegrator = new IntentIntegrator(this);
    }

    @Override
    protected void onDestroy() {
        conn.getConnection().disconnect();
        super.onDestroy();
    }

    /* ProgressDialog */
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

    /* Handler */
    private static class ConnHandler extends Handler {
        private MainActivity mainActivity;

        public ConnHandler(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            if (msg.obj == null || mainActivity.progressDialog == null
                    || !(mainActivity.progressDialog.isShowing())) {
                return;
            }

            mainActivity.progressDialog.dismiss();
            String res = (String) msg.obj;

            if (!(res.equals(XConnectionHelp.RET_SUCC))) {
                Toast.makeText(mainActivity, "连接服务器出错...", Toast.LENGTH_SHORT).show();
            }

            /*-- DEBUG --*/
            mainActivity.initRoster();
            mainActivity.initChatManager();
            mainActivity.initLayout();
        }
    }

    private static class RecvHandler extends Handler {
        private MainActivity mainActivity;

        public RecvHandler(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            if (msg.obj == null) {
                return;
            }

            Message recvMsg = (Message) msg.obj;
            String from = ChatActivity.getPeerName(recvMsg.getFrom().toString());

            Toast.makeText(mainActivity,  "[新消息] " + from + ": " + recvMsg.getBody(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /* initialize */
    private void initLayout() {
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_message,
                R.id.navigation_others
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void initConnection() {
        Intent intent = getIntent();
        user = intent.getParcelableExtra("User");

        conn = new XConnectionHelp();
        connHandler = new ConnHandler(this);
        recvHandler = new RecvHandler(this);

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*-- DEBUG --*/
//                String res = XConnectionHelp.RET_SUCC;
                String res = conn.login2Server(user);

                android.os.Message msg = android.os.Message.obtain();
                msg.obj = res;
                connHandler.sendMessage(msg);
            }
        }).start();
    }

    private void initChatManager() {
        chatManager = ChatManager.getInstanceFor(conn.getConnection());
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                android.os.Message msg = android.os.Message.obtain();
                msg.obj = message;
                recvHandler.sendMessage(msg);
            }
        });
        Log.i(TAG, "init chatManager");
    }

    private void initRoster() {
        roster = Roster.getInstanceFor(conn.getConnection());
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            Log.i(TAG, "JID: " + entry.getJid() + " Group: " + entry.getGroups()
                    + " Type: " + entry.getType() + " Name: " + entry.getName());
        }
        // JID: test1@ubuntu
        // Group: [org.jivesoftware.smack.roster.RosterGroup@2d68d6d3]
        // Type: both
        // Name: test1
        Log.i(TAG, "init Roster");
    }

    /* For scanning ISBN */
    public void startScanner() {
        // 原始扫描
        /*
        IntentIntegrator integrator = new IntentIntegrator(mainActivity.this);
        integrator.initiateScan();
        */

        // 自定义样式扫描
        // new IntentIntegrator(this)
        intentIntegrator
                // 扫码的类型
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                // 选择摄像头，可使用前置或者后置
                .setCameraId(0)
                // 是否开启声音，扫完码之后会"哔"的一声
                .setBeepEnabled(true)
                //自定义扫码界面
                .setCaptureActivity(ScanISBNActivity.class)
                // 初始化扫码
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:{
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if ((result == null) || (result.getContents() == null)) {
                    Toast.makeText(this, "扫描取消", Toast.LENGTH_SHORT).show();
                    return;
                }

                /* ISBN for debugging */
                // String contents = "0000000000000";      // book not found
                // String contents = "9787121402180";      // no cover
                // String contents = "9787115209306";      // valid

                String contents = result.getContents();
                Toast.makeText(this, "扫描结果: " + contents, Toast.LENGTH_LONG).show();
                startDownloadBookInfo(contents);
                break;
            }
            default: break;
        }
    }

    private class DownloadThread extends Thread {
        private String url;

        public DownloadThread(String url) {
            super();
            this.url = url;
            Log.i(TAG, "Success: create download thread");
        }

        @Override
        public void run() {
            Log.i(TAG, "Success: run download thread");
            android.os.Message msg = android.os.Message.obtain();
            msg.obj = DownloadUtils.download(url);
            downloadHandler.sendMessage(msg);
        }
    }

    private static class DownloadHandler extends Handler {

        private MainActivity mainActivity;

        public DownloadHandler(MainActivity mainActivity) {
            super();
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            if ((msg.obj == null) || (mainActivity.progressDialog == null)
                    || (!mainActivity.progressDialog.isShowing())) {
                return;
            }

            mainActivity.progressDialog.dismiss();

            Response response = (Response) msg.obj;

            if (response.getResCode() != BookAPI.RESPONSE_CODE_SUCCEED) {
                // 通信异常处理
                Log.i(TAG, "Failure: Communicating ["
                        + response.getResCode() + "]: " + response.getResMessage());

                Toast.makeText(mainActivity, "状态码:" + response.getResCode() + " "
                        + response.getResMessage(), Toast.LENGTH_LONG).show();
            } else {
                // 通信正常，转到图书信息界面
                Log.i(TAG, "Success: Communicating");
                BookInfo bookInfo = (BookInfo) response.getResMessage();
                if (bookInfo == null) {
                    return;
                }

                Intent intent = new Intent(mainActivity, BookInfoDetailActivity.class);
                intent.putExtra(BookInfo.class.getName(), bookInfo);
                mainActivity.startActivity(intent);
            }
        }
    }

    public void startDownloadBookInfo(String isbn) {
        if (isbn == null) {
            return;
        }
        isbn = isbn.replace("-", "");
        if (isbn.isEmpty()) {
            return;
        }

        // 下载耗时，显示进度条
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();

        DownloadThread thread = new DownloadThread(BookAPI.URL_ISBN_BASE + isbn);
        thread.start();
    }
}
