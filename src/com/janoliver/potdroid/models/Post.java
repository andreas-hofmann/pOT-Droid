/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.janoliver.potdroid.models;

import java.io.UnsupportedEncodingException;

/**
 * Post model.
 */
public class Post {

    private Integer mId;
    private Topic mThread;
    private String mText;
    private String mDate;
    private String mTitle;
    private User mAuthor;

    public String mBookmarktoken = "";
    public String mEdittoken = "";

    public Post(Integer id) {
        mId = id;
    }

    public void setText(String text) {
        String utf8Text = text;
        try {
            utf8Text = new String(text.getBytes(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            mText = "";
        }
        mText = utf8Text;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public void setThread(Topic thread) {
        mThread = thread;
    }

    public void setAuthor(User author) {
        mAuthor = author;
    }

    public void setBookmarktoken(String bookmarktoken) {
        mBookmarktoken = bookmarktoken;
    }

    public void setEdittoken(String edittoken) {
        mEdittoken = edittoken;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Integer getId() {
        return mId;
    }

    public String getEdittoken() {
        return mEdittoken;
    }

    public String getDate() {
        return mDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public User getAuthor() {
        return mAuthor;
    }

    public String getBookmarktoken() {
        return mBookmarktoken;
    }

    public String getText() {
        return mText;
    }

    public Topic getThread() {
        return mThread;
    }

}