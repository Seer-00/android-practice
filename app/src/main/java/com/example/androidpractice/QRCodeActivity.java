package com.example.androidpractice;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.contacts.ContactAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;


public class QRCodeActivity extends Activity {
    private static final int HEIGHT = 300;
    private static final int WIDTH = 300;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        Intent intent = getIntent();
        name = intent.getStringExtra("Name");

        setViews();
    }

    private void setViews() {
        SimpleDraweeView user_img = findViewById(R.id.qr_user_img);
        TextView user_name = findViewById(R.id.qr_user_name);
        ImageView user_qrcode = findViewById(R.id.qr_code);

        user_name.setText(name);
        String imageUri = "res://" + this.getPackageName() + "/"
                + ContactAdapter.getResID().get(getPicIdx(name));

        user_img.setImageURI(imageUri);

        user_qrcode.setImageBitmap(generateQRCode(name, WIDTH, HEIGHT));
    }

    /**
     *
     * @param content   生成的二维码所包含的字符串内容
     * @param width     二维码宽度
     * @param height    二维码高度
     * @return 生成的二维码 Bitmap
     */
    private Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getPicIdx(String name) {
        return Math.abs(name.hashCode() % Contact.getPicNumber());
    }

}
