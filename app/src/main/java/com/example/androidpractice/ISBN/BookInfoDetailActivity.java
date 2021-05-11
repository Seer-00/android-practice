package com.example.androidpractice.ISBN;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidpractice.R;

public class BookInfoDetailActivity extends Activity {

    private static final String TAG = BookInfoDetailActivity.class.getName();

    private ImageView bCover;
    private TextView bName;
    private TextView bAuthor;
    private TextView bPublisher;
    private TextView bPubDate;
    private TextView bISBN;
    private TextView bPages;
    private TextView bPrice;
    private TextView bScore;
    private TextView bAuthorIntro;
    private TextView bSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏和信息栏
        /*
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

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
        bPages = findViewById(R.id.book_pages);
        bPrice = findViewById(R.id.book_price);
        bScore = findViewById(R.id.book_dbscore);
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
        String str_pages = "页数: " + checkNull(bookInfo.getBookPages());
        String str_price = "价格: " + checkNull(bookInfo.getBookPrice());
        String str_score = "豆瓣分数: " + checkNull(bookInfo.getBookDoubanScore());
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
        bPages.setText(str_pages);
        bPrice.setText(str_price);
        bScore.setText(str_score);
        bAuthorIntro.setText(str_intro);
        bSummary.setText(str_sum);
    }

    private String checkNull(String str) {
        return ("null".equals(str) ? "暂无信息" : str);
    }

}
