package com.example.androidpractice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.androidpractice.HttpURLHelp.downloadStringFromURL;

public class Utils {

    private static final String TAG = Utils.class.getName();

    public static Response download(String url) {
        Log.i(TAG, "Success: downloading from: " + url);

        Response res = downloadFromAPI(url);

        JSONObject json = null;

        try {
            json = new JSONObject(String.valueOf(res.getResMessage()));
            Log.i(TAG, "Success: create JSONObject");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (res.getResCode()) {
            // 连接成功. ResCode == 200
            case BookAPI.RESPONSE_CODE_SUCCEED:
                // 解析JSONObject对象
                res.setResMessage(parseBookInfo(json));
                // 数据无效（如书本未找到），ResCode == 404
                if (res.getResMessage() == null) {
                    res.setResCode(BookAPI.RESPONSE_CODE_ERROE_BOOK_NOT_FOUND);
                    res.setResMessage("没有找到这本书...");
                }
                break;

            // 连接异常（如网络错误或连接超时），设置错误信息，ResCode == 408
            default:
                res.setResCode(BookAPI.RESPONSE_CODE_ERROR_TIME_OUT);
                res.setResMessage("通信出现错误...");
                break;
        }

        return res;
    }

    private static Response downloadFromAPI(String url) {
        String data = downloadStringFromURL(url);

        if (data.isEmpty()) {
            // downloadStringFromURL()返回空串，说明连接失败，没有得到数据
            return new Response(BookAPI.RESPONSE_CODE_ERROR_TIME_OUT, null);
        } else {
            // 否则连接成功，包含2种情况：1.找到书本 2.没有找到书本
            return new Response(BookAPI.RESPONSE_CODE_SUCCEED, data);
        }

        /*
        // 默认状态码为超时：408
        int resCode = BookAPI.RESPONSE_CODE_ERROR_TIME_OUT;



        StringBuffer buffer = new StringBuffer();
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(5000);

            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            // 通信成功，设置状态码：200
            resCode = BookAPI.RESPONSE_CODE_SUCCEED;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new Response(resCode, buffer.toString());

         */
    }

    private static BookInfo parseBookInfo(JSONObject json) {

        try {
            json = json.getJSONObject("data");
            Log.i(TAG, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Invalid json.");
            Log.i(TAG, json.toString());
            return null;
        }

        BookInfo bookInfo = null;

        try {
            bookInfo = new BookInfo();

            if (!("null".equals(json.getString(BookAPI.TAG_COVER)))) {
                bookInfo.setBookCover(BitmapUtils.downloadBitmap(json.getString(BookAPI.TAG_COVER)));
            }

            bookInfo.setBookName(json.getString(BookAPI.TAG_TITLE));
            bookInfo.setBookAuthor(json.getString(BookAPI.TAG_AUTHOR));
            bookInfo.setBookPublisher(json.getString(BookAPI.TAG_PUBLISHER));
            bookInfo.setBookPublishDate(json.getString(BookAPI.TAG_PUBLISH_DATE));
            bookInfo.setBookISBN(json.getString(BookAPI.TAG_ISBN));
            bookInfo.setBookAuthorIntro(json.getString(BookAPI.TAG_AUTHOR_INTRO).replace("\n", "\n\n"));
            bookInfo.setBookSummary(json.getString(BookAPI.TAG_SUMMARY).replace("\n", "\n\n"));
            Log.i(TAG, "Success: parseBookInfo");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Failure: parseBookInfo");
            return null;
        }

        return bookInfo;
    }

}
