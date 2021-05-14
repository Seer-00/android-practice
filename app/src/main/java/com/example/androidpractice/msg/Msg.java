package com.example.androidpractice.msg;

public class Msg {
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
}
