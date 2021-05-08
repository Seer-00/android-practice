package com.example.androidpractice;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class BookInfo implements Parcelable {

    private Bitmap bookCover;       // 封面图片
    private String bookName;        // 书名
    private String bookAuthor;      // 作者
    private String bookPublisher;   // 出版社
    private String bookPublishDate; // 出版日期
    private String bookISBN;        // ISBN
    private String bookAuthorIntro; // 作者简介
    private String bookSummary;     // 内容简介

    /* 实现Parcelable接口 */
    public static final Creator<BookInfo> CREATOR = new Creator<BookInfo>() {
        @Override
        public BookInfo createFromParcel(Parcel source) {
            BookInfo bookInfo = new BookInfo();
            bookInfo.bookCover = source.readParcelable(Bitmap.class.getClassLoader());
            bookInfo.bookName = source.readString();
            bookInfo.bookAuthor = source.readString();
            bookInfo.bookPublisher = source.readString();
            bookInfo.bookPublishDate = source.readString();
            bookInfo.bookISBN = source.readString();
            bookInfo.bookAuthorIntro = source.readString();
            bookInfo.bookSummary = source.readString();
            return bookInfo;
        }

        @Override
        public BookInfo[] newArray(int size) {
            return new BookInfo[size];
        }
    };

    /* 实现Parcelable接口 */
    @Override
    public int describeContents() {
        return 0;
    }

    /* 实现Parcelable接口 */
    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeParcelable(bookCover, flag);
        parcel.writeString(bookName);
        parcel.writeString(bookAuthor);
        parcel.writeString(bookPublisher);
        parcel.writeString(bookPublishDate);
        parcel.writeString(bookISBN);
        parcel.writeString(bookAuthorIntro);
        parcel.writeString(bookSummary);
    }

    public Bitmap getBookCover() {
        return bookCover;
    }

    public void setBookCover(Bitmap bookCover) {
        this.bookCover = bookCover;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookPublisher() {
        return bookPublisher;
    }

    public void setBookPublisher(String bookPublisher) {
        this.bookPublisher = bookPublisher;
    }

    public String getBookPublishDate() {
        return bookPublishDate;
    }

    public void setBookPublishDate(String bookPublishDate) {
        this.bookPublishDate = bookPublishDate;
    }

    public String getBookISBN() {
        return bookISBN;
    }

    public void setBookISBN(String bookISBN) {
        this.bookISBN = bookISBN;
    }

    public String getBookSummary() {
        return bookSummary;
    }

    public void setBookSummary(String bookSummary) {
        this.bookSummary = bookSummary;
    }

    public String getBookAuthorIntro() {
        return bookAuthorIntro;
    }

    public void setBookAuthorIntro(String bookAuthorIntro) {
        this.bookAuthorIntro = bookAuthorIntro;
    }
}
