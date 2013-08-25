package com.mde.potdroid3.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid3.helpers.Utils;
import com.mde.potdroid3.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

public class BoardParser extends DefaultHandler {

    private Post mCurrentPost;
    private User mCurrentUser;
    private Topic mCurrentThread;
    private Board mBoard;

    public BoardParser() {
        mBoard = new Board();
    }

    public Board parse(InputStream instream) {
        RootElement board = new RootElement(Board.Xml.TAG);

        // find the board information
        board.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setId(Integer.parseInt(attributes.getValue(Board.Xml.ID_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setName(body);
            }
        });
        board.requireChild(Board.Xml.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setDescription(body);
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_THREADS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfThreads(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_THREADS_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfReplies(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.IN_CATEGORY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                Category c = new Category(Integer.parseInt(attributes.getValue(Board.Xml.IN_CATEGORY_ID_ATTRIBUTE)));
                mBoard.setCategory(c);
            }
        });

        Element thread = board.getChild(Board.Xml.THREADS_TAG).getChild(Topic.Xml.TAG);
        thread.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mBoard.addTopic(mCurrentThread);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(Topic.Xml.ID_ATTRIBUTE)));
                mCurrentThread.setBoard(mBoard);
            }
        });
        thread.requireChild(Topic.Xml.TITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.SUBTITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setSubTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_HITS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfHits(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_HITS_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPosts(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_PAGES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPages(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_PAGES_ATTRIBUTE)));
            }
        });
        Element first_post = thread.getChild(Topic.Xml.FIRSTPOST_TAG).getChild(Post.Xml.TAG);
        first_post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentThread.setFirstPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mBoard);
                mCurrentPost.setTopic(mCurrentThread);
            }
        });
        first_post.requireChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        first_post.requireChild(User.Xml.TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(User.Xml.ID_ATTRIBUTE)));
            }
        });

        Element flags = thread.getChild(Topic.Xml.FLAGS_TAG);

        flags.requireChild(Topic.Xml.IS_CLOSED_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsClosed(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_CLOSED_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_STICKY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsSticky(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_STICKY_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_ANNOUNCEMENT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsAnnouncement(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_ANNOUNCEMENT_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_IMPORTANT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsImportant(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_IMPORTANT_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_GLOBAL_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsGlobal(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_GLOBAL_ATTRIBUTE)));
            }
        });

        try {
            Xml.parse(instream, Xml.Encoding.UTF_8, board.getContentHandler());
        } catch (IOException e) {
            Utils.log(e.getMessage());
            return null;
        } catch (SAXException e) {
            Utils.log(e.getMessage());
            return null;
        }

        return mBoard;
    }
}