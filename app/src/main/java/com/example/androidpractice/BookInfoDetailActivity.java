package com.example.androidpractice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BookInfoDetailActivity extends AppCompatActivity {

    private static final String TAG = BookInfoDetailActivity.class.getName();

    private ImageView bCover;
    private TextView bName;
    private TextView bAuthor;
    private TextView bPublisher;
    private TextView bPubDate;
    private TextView bISBN;
    private TextView bAuthorIntro;
    private TextView bSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info_detail);

        initViews();
        initBookInfo(getIntent().getParcelableExtra(BookInfo.class.getName()));
    }

    private void initViews() {
        bCover = findViewById(R.id.book_cover);
        bName = findViewById(R.id.book_name);
        bAuthor = findViewById(R.id.book_author);
        bPublisher = findViewById(R.id.book_publisher);
        bPubDate = findViewById(R.id.book_pub_date);
        bISBN = findViewById(R.id.book_isbn);
        bAuthorIntro = findViewById(R.id.book_author_intro);
        bSummary = findViewById(R.id.book_summary);
    }

    private void initBookInfo(Parcelable data) {
        if (data == null) return;

        BookInfo bookInfo = (BookInfo) data;

        String str_author = "作者: " + checkNull(bookInfo.getBookAuthor());
        String str_pub = "出版社: " + checkNull(bookInfo.getBookPublisher());
        String str_pubd = "出版日期: " + checkNull(bookInfo.getBookPublishDate());
        String str_isbn = "ISBN: " + checkNull(bookInfo.getBookISBN());
        String str_intro = "\u3000\u3000" + checkNull(bookInfo.getBookAuthorIntro());
        String str_sum = "\u3000\u3000" + checkNull(bookInfo.getBookSummary());

        if (bookInfo.getBookCover() == null) {
            Bitmap cover = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
            cover = BitmapUtils.compress(cover);
            bCover.setImageBitmap(cover);
        } else {
            bCover.setImageBitmap(bookInfo.getBookCover());
        }

        bName.setText(bookInfo.getBookName());
        bAuthor.setText(str_author);
        bPublisher.setText(str_pub);
        bPubDate.setText(str_pubd);
        bISBN.setText(str_isbn);
        bAuthorIntro.setText(str_intro);
        bSummary.setText(str_sum);
    }

    private String checkNull(String str) {
        return ("null".equals(str) ? "暂无信息" : str);
    }

}
