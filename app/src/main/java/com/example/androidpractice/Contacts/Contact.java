package com.example.androidpractice.Contacts;

public class Contact {
    private static final int PIC_NUMBER = 10;

    private String name;
    private int pictureIdx;

    public Contact(String name) {
        this.name = name;
        this.pictureIdx = name.hashCode() % PIC_NUMBER;
    }

    public String getName() {
        return name;
    }

    public int getPictureIdx() {
        return pictureIdx;
    }

    public static int getPicNumber() {
        return PIC_NUMBER;
    }
}
