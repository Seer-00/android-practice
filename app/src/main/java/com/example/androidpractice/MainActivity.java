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
import com.example.androidpractice.msg.Msg;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Collection;
import java.util.List;

import static org.jivesoftware.smack.roster.Roster.SubscriptionMode.accept_all;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    public static final int SCAN_TYPE_ISBN = 1000;
    public static final int SCAN_TYPE_QRCODE = 1001;

    public static final int ROSTER_MSG_UPD = 0;
    public static final int ROSTER_MSG_ADD = 1;
    public static final int ROSTER_MSG_DEL = 2;

    private static User user;

    private ProgressDialog progressDialog;
    private static DownloadHandler downloadHandler;
    private static ConnHandler connHandler;
    private static RecvHandler recvHandler;
    private static RosterHandler rosterHandler;
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

        // Handle status of connection to server
        connHandler = new ConnHandler(this);
        // Handle messages from other contacts
        recvHandler = new RecvHandler(this);
        // Handle messages from roster
        rosterHandler = new RosterHandler(this);
        // Handle thread of downloading book info
        downloadHandler = new DownloadHandler(this);
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
            // example: abc@ubuntu/9qdohi7qui
            String from = recvMsg.getFrom().toString();
            // example: abc@ubuntu
            String fromJID = from.substring(0, from.indexOf('/'));
            // example: abc
            String fromName = from.substring(0, from.indexOf('@'));

            // 读取更新前的消息记录列表
            List<Msg> msgList = Msg.loadMsgfromFile(mainActivity, user.getName(), fromJID);
            // 向List添加该条记录
            msgList.add(new Msg(Msg.TYPE_RECV, recvMsg.getBody()));
            // 将更新后的消息记录列表保存
            Msg.saveMsgToFile(mainActivity, user.getName(), fromJID, msgList);

            Toast.makeText(mainActivity, "[新消息] " + fromName + ": " + recvMsg.getBody(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static class RosterHandler extends Handler {
        private MainActivity mainActivity;

        public RosterHandler(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            if (msg.obj == null) {
                return;
            }
            String s = (String) msg.obj;

            switch (msg.what) {
                case ROSTER_MSG_UPD: {
                    //Toast.makeText(mainActivity, "好友列表已更新", Toast.LENGTH_LONG).show();
                    break;
                }
                case ROSTER_MSG_ADD: {
                    Toast.makeText(mainActivity, s + "已添加您为好友", Toast.LENGTH_LONG).show();
                    break;
                }
                case ROSTER_MSG_DEL: {
                    Toast.makeText(mainActivity, s + "已从好友列表删除", Toast.LENGTH_LONG).show();
                    break;
                }
                default:
                    break;
            }
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
        // 自动接收所有好友请求
        roster.setSubscriptionMode(accept_all);

        roster.addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = ROSTER_MSG_ADD;
                    msg.obj = jid.toString();
                    rosterHandler.sendMessage(msg);
                }
            }

            public void entriesDeleted(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = ROSTER_MSG_DEL;
                    msg.obj = jid.toString();
                    rosterHandler.sendMessage(msg);
                }
            }

            public void entriesUpdated(Collection<Jid> addresses) {
                for (Jid jid : addresses) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = ROSTER_MSG_UPD;
                    msg.obj = jid.toString();
                    rosterHandler.sendMessage(msg);
                }
            }

            public void presenceChanged(Presence presence) {
            }
        });

        /*Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            Log.i(TAG, "JID: " + entry.getJid() + " Group: " + entry.getGroups()
                    + " Type: " + entry.getType() + " Name: " + entry.getName());
        }
        // e.g.
        // JID: test1@ubuntu
        // Group: [org.jivesoftware.smack.roster.RosterGroup@2d68d6d3]
        // Type: both
        // Name: test1*/
        Log.i(TAG, "init Roster");
    }

    /* For scanning ISBN */
    public void startScanner(int requestCode) {
        // 原始扫描
        /*
        IntentIntegrator integrator = new IntentIntegrator(mainActivity.this);
        integrator.initiateScan();
        */

        // 自定义样式扫描
        new IntentIntegrator(this)
                // 扫码的类型
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                // 选择摄像头，可使用前置或者后置
                .setCameraId(0)
                // 是否开启声音，扫完码之后会"哔"的一声
                .setBeepEnabled(true)
                // 自定义扫码界面
                .setCaptureActivity(ScanActivity.class)
                // 设置MainActivity的回调码
                .setRequestCode(requestCode)
                // 初始化扫码
                .initiateScan();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result.getContents() == null) {
            Toast.makeText(this, "扫描取消", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case SCAN_TYPE_ISBN: {
                /* ISBN for debugging */
                // String contents = "0000000000000";      // book not found
                // String contents = "9787121402180";      // no cover
                // String contents = "9787115209306";      // valid

                String contents = result.getContents();
                startDownloadBookInfo(contents);
                break;
            }
            case SCAN_TYPE_QRCODE: {
                String peerName = result.getContents();
                if (peerName.isEmpty()) {
                    Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
                }
                BareJid peerJID;
                try {
                    if (!conn.isUserExistInServer(peerName)) {
                        Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    peerJID = JidCreate.bareFrom(peerName + "@" + user.getDom());
                    roster.createEntry(peerJID, peerName, null);
                    Toast.makeText(this, "好友请求已发送", Toast.LENGTH_SHORT).show();
                } catch (XmppStringprepException
                        | InterruptedException
                        | SmackException.NotLoggedInException
                        | XMPPException.XMPPErrorException
                        | SmackException.NotConnectedException
                        | SmackException.NoResponseException e) {
                    Toast.makeText(this, "添加好友出现错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }
}
