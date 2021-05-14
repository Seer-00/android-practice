package com.example.androidpractice.contacts;

public class Contact {
    private static final int PIC_NUMBER = 10;

    private String name;
    private String JID;
    private int pictureIdx;

    public Contact(String name, String JID) {
        this.name = name;
        this.JID = JID;
        this.pictureIdx = Math.abs(name.hashCode() % PIC_NUMBER);   // %运算可能出现负数
    }

    public String getName() {
        return name;
    }

    public String getJID() {
        return JID;
    }

    public int getPictureIdx() {
        return pictureIdx;
    }

    public static int getPicNumber() {
        return PIC_NUMBER;
    }
}
