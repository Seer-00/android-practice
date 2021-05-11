package com.example.androidpractice.ISBN;

public class Response {

    private int resCode;        // 响应码
    private Object resMessage;  // 响应详情

    public Response(int resCode, Object resMessage) {
        this.resCode = resCode;
        this.resMessage = resMessage;
    }

    public int getResCode() {
        return resCode;
    }

    public void setResCode(int resCode) {
        this.resCode = resCode;
    }

    public Object getResMessage() {
        return resMessage;
    }

    public void setResMessage(Object resMessage) {
        this.resMessage = resMessage;
    }
}
