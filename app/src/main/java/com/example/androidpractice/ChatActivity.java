package com.example.androidpractice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.msg.Msg;
import com.example.androidpractice.msg.MsgAdapter;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;

public class ChatActivity extends Activity {
    private static final String TAG = ChatActivity.class.getName();

    private static User self;
    private static String peerJID;
    private static String peer;

    private ProgressDialog progressDialog;
    private static ConnHandler connHandler;
    private static RecvHandler recvHandler;
    private static XConnectionHelp conn;
    private static ChatManager chatManager;

    private List<Msg> msgList;
    private EditText edt_msg;
    private Button btn_send;
    private RecyclerView recyclerView;
    private MsgAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initMsgList();
        initConnection();
    }

    @Override
    protected void onDestroy() {
        // 将消息记录保存到文件中
        Msg.saveMsgToFile(this, peerJID, msgList);
        // 关闭连接
        conn.getConnection().disconnect();
        super.onDestroy();
    }

    private void initMsgList() {
        // 从Intent中获得自己的用户名和对方的用户名
        Intent intent = getIntent();
        self = intent.getParcelableExtra("Self");
        peerJID = intent.getStringExtra("PeerJID");
        peer = Contact.getNameFromJID(peerJID);
        // 从文件中读取消息记录
        msgList = Msg.loadMsgfromFile(this, peerJID);
    }

    private void initConnection() {
        conn = new XConnectionHelp();
        connHandler = new ConnHandler(this);
        recvHandler = new RecvHandler(this);

        showProgressDialog();

        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Message msg = android.os.Message.obtain();
                msg.obj = conn.login2Server(self); // result of connection
                connHandler.sendMessage(msg);
            }
        }).start();
    }

    private void initLayout() {
        edt_msg = findViewById(R.id.edit_msg);
        btn_send = findViewById(R.id.btn_msg_send);
        recyclerView = findViewById(R.id.chat_recycler_view);

        TextView tv_title = findViewById(R.id.chat_title);
        tv_title.setText(peer);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new MsgAdapter(msgList, this, peer, self.getName());
        recyclerView.setAdapter(adapter);
        // 移动到最后一行
        recyclerView.scrollToPosition(msgList.size() - 1);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edt_msg.getText().toString();
                if (!("".equals(content))) {
                    try {
                        EntityBareJid jid = JidCreate.entityBareFrom(peerJID);
                        Chat chat = chatManager.chatWith(jid);
                        chat.send(content);

                        msgList.add(new Msg(Msg.TYPE_SEND, content));
                        // 有新消息时，刷新RecyclerView的显示
                        adapter.notifyItemInserted(msgList.size() - 1);
                        // 移动到最后一行
                        recyclerView.scrollToPosition(msgList.size() - 1);
                        // 清空编辑框
                        edt_msg.setText("");

                    } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
                        e.printStackTrace();
                        Log.i(TAG, e.getMessage());
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("通信中...");
        progressDialog.show();
    }

    private static class ConnHandler extends Handler {
        private ChatActivity chatActivity;

        public ConnHandler(ChatActivity chatActivity) {
            this.chatActivity = chatActivity;
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            if (msg.obj == null || chatActivity.progressDialog == null
                    || !(chatActivity.progressDialog.isShowing())) {
                return;
            }

            chatActivity.progressDialog.dismiss();
            String res = (String) msg.obj;

            if (!(res.equals(XConnectionHelp.RET_SUCC))) {
                Toast.makeText(chatActivity, "连接服务器出错...", Toast.LENGTH_SHORT).show();
            }

            /*-- DEBUG --*/
            chatActivity.initChatManager();
            chatActivity.initLayout();
        }
    }

    private static class RecvHandler extends Handler {
        private ChatActivity chatActivity;

        public RecvHandler(ChatActivity chatActivity) {
            this.chatActivity = chatActivity;
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

            if (peer.equals(fromName)) { // 当前对话的人发来的消息
                chatActivity.msgList.add(new Msg(Msg.TYPE_RECV, recvMsg.getBody()));
                chatActivity.adapter.notifyItemInserted(chatActivity.msgList.size() - 1);
                chatActivity.recyclerView.scrollToPosition(chatActivity.msgList.size() - 1);
            } else { // 其他人发来的消息，将其保存到消息历史记录文件
                // 读取更新前的消息记录列表
                List<Msg> msgList = Msg.loadMsgfromFile(chatActivity, fromJID);
                // 向List添加该条记录
                msgList.add(new Msg(Msg.TYPE_RECV, recvMsg.getBody()));
                // 将更新后的消息记录列表保存
                Msg.saveMsgToFile(chatActivity, fromJID, msgList);

                Toast.makeText(chatActivity, "[新消息] " + fromName + ": " + recvMsg.getBody(),
                        Toast.LENGTH_LONG).show();
            }
        }
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
}
