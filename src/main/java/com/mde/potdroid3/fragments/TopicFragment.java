package com.mde.potdroid3.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mde.potdroid3.ForumActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.SettingsActivity;
import com.mde.potdroid3.helpers.TopicBuilder;
import com.mde.potdroid3.helpers.TopicJSInterface;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.parsers.TopicParser;

import java.io.*;

public class TopicFragment extends BaseFragment {

    private Topic mTopic = null;
    private WebView mWebView;
    private TopicJSInterface mJsInterface;

    public static TopicFragment newInstance(int thread_id, int page, int post_id) {
        TopicFragment f = new TopicFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("thread_id", thread_id);
        args.putInt("page", page);
        args.putInt("post_id", post_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWebView = (WebView)getView().findViewById(R.id.topic_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.addJavascriptInterface(new TopicJSInterface(mWebView, getActivity()), "api");
        mWebView.loadData("", "text/html", "utf-8");

        new BaseLoaderTask().execute((Void[]) null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_topic, menu);
        //menu.setGroupVisible(R.id.loggedin, mObjectManager.isLoggedIn());
    }

    public void loadHtml() {
        // generate topic html
        TopicBuilder t = new TopicBuilder(getActivity());
        String html = t.parse(mTopic);

        BufferedWriter output = null;
        try {
            File logFile = new File(Environment.getExternalStorageDirectory().toString(), "topic.html");
            if(!logFile.exists()) {
                logFile.createNewFile();
            }
            output = new BufferedWriter(new FileWriter(logFile));

            output.write(html);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                new BaseLoaderTask().execute((Void[]) null);
                return true;
            case R.id.bookmarks:
                return true;
            case R.id.preferences:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.forumact:
                intent = new Intent(getActivity(), ForumActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected int getLayout() {
        return R.layout.layout_topic;
    }

    class BaseLoaderTask extends AsyncTask<Void, Void, Topic> {

        @Override
        protected void onPreExecute() {
            showLoader();
        }

        @Override
        protected Topic doInBackground(Void... params) {
            int page = getArguments().getInt("page", 1);
            int thread_id = getArguments().getInt("thread_id", 0);
            int post_id = getArguments().getInt("post_id", 0);

            try {
                InputStream xml = mNetwork.getDocument(Topic.Xml.getUrl(thread_id, page, post_id));
                TopicParser parser = new TopicParser();
                return parser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Topic topic) {
            if(topic != null) {
                mTopic = topic;
                loadHtml();
            }
            hideLoader();
        }
    }

    public String mCurrentText = "";


}