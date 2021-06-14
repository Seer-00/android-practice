package com.example.androidpractice.isbn;

public class BookAPI {

    public static final int RESPONSE_CODE_SUCCEED = 200;
    public static final int RESPONSE_CODE_ERROR_TIME_OUT = 408;
    public static final int RESPONSE_CODE_ERROR_BOOK_NOT_FOUND = 404;

    // public static String URL_ISBN_BASE = "https://api.zuk.pw/situ/book/isbn/";
    public static String URL_ISBN_BASE = "https://book.zuk.pw/?s=Index.V1_Open.Book&isbn=";

    public static final String TAG_COVER = "photoUrl";
    public static final String TAG_TITLE = "name";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_PUBLISHER = "publishing";
    public static final String TAG_PUBLISH_DATE = "published";
    public static final String TAG_ISBN = "id";
    public static final String TAG_SUMMARY = "description";
    public static final String TAG_AUTHOR_INTRO = "authorIntro";
    public static final String TAG_PRICE = "price";
    public static final String TAG_PAGES = "pages";
    public static final String TAG_DOUBANSCORE = "doubanScore";
}
