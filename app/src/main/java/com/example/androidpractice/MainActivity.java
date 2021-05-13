package com.example.androidpractice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    private String usr;
    private String pwd;
    private String ip;

    private ProgressDialog progressDialog;
    private static ConnHandler connHandler;
    private static RecvHandler recvHandler;
    private static XConnectionHelp conn;
    private static ChatManager chatManager = null;

    public XConnectionHelp getConn() {
        return conn;
    }

    public ChatManager getChatManager() {
        return chatManager;
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

        Fresco.initialize(this);

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
        recvHandler = new RecvHandler(this);

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*-- DEBUG --*/
                // String res = conn.login2Server(usr, pwd, ip);
//                String res = conn.login2Server("test3", "123", "192.168.81.135");
                String res = XConnectionHelp.RET_SUCC;
                android.os.Message msg = android.os.Message.obtain();
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
        public void handleMessage(@NonNull android.os.Message msg) {
            if (msg.obj == null || mainActivity.progressDialog == null
                    || !(mainActivity.progressDialog.isShowing())) {
                return;
            }

            mainActivity.progressDialog.dismiss();
            String res = (String) msg.obj;

            if (!(res.equals(XConnectionHelp.RET_SUCC))) {
                Toast.makeText(mainActivity, "连接服务器出错...", Toast.LENGTH_SHORT).show();
                return;
            }

            /*-- DEBUG --*/
//            initRoster();
//            initChatManager();
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

            String recvMsg = (String) msg.obj;

            Log.i(TAG, recvMsg);
            Toast.makeText(mainActivity, recvMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

    private static void initChatManager() {
        // init chatManager
        chatManager = ChatManager.getInstanceFor(conn.getConnection());
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                String recvMsg = "New message from " + from + ": " + message.getBody();
                android.os.Message msg = android.os.Message.obtain();
                msg.obj = recvMsg;
                recvHandler.sendMessage(msg);
            }
        });
        Log.i(TAG, "init charManager");
    }

    private static void initRoster() {
        Roster roster = Roster.getInstanceFor(conn.getConnection());
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            Log.i(TAG, "JID: " + entry.getJid() + " Group: " + entry.getGroups()
            + " Type: " + entry.getType() + " Name: " + entry.getName());
        }
        // JID: test1@ubuntu
        // Group: [org.jivesoftware.smack.roster.RosterGroup@2d68d6d3]
        // Type: both
        // Name: test1
    }
}
