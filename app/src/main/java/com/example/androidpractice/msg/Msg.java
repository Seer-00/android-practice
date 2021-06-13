package com.example.androidpractice.msg;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Msg implements Serializable {
    public final static int TYPE_RECV = 0;
    public final static int TYPE_SEND = 1;

    private int type;
    private String content;

    public Msg(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public static void saveMsgToFile(Context context, String peerJID, List<Msg> msgList) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        ContextWrapper contextWrapper = new ContextWrapper(context);
        try {
            // 文件名：peerJID，方式：覆写
            fos = contextWrapper.openFileOutput(peerJID, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(msgList);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Msg> loadMsgfromFile(Context context, String peerJID) {
        List<Msg> msgList = null;
        // 从文件中加载消息记录
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        ContextWrapper contextWrapper = new ContextWrapper(context);
        try {
            // 文件名即peerJID
            fis = contextWrapper.openFileInput(peerJID);
            ois = new ObjectInputStream(fis);
            msgList = (List<Msg>) ois.readObject();
//            for (Msg msg : msgList) {
//                Log.i("tag", msg.getType() + " " + msg.getContent());
//            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 若加载消息记录失败（如消息文件被损坏），返回空列表，不影响本次聊天
        return msgList == null ? new LinkedList<Msg>() : msgList;
    }
}
