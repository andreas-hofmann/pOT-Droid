package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.EncodingRequestParams;
import com.mde.potdroid.helpers.Network;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.parsers.MessageParser;
import com.mde.potdroid.views.IconDrawable;
import com.mde.potdroid.views.IconSelectionDialog;
import org.apache.http.Header;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the fragment responsible for the Forms, both the post editing/writing and the
 * PM message form. Activities must implement the FormListener, to be notified on
 * finishing, success and failure of the form.
 */
public class EditorFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Bundle>
{

    protected static final String ARG_MODE = "mode";
    protected static final String ARG_TOPIC_ID = "topic_id";
    protected static final String ARG_POST_ID = "post_id";
    protected static final String ARG_TOKEN = "token";
    protected static final String ARG_ICON = "icon";
    protected static final String ARG_RCPT = "rcpt";
    protected static final String ARG_TEXT = "text";
    protected static final String ARG_TITLE = "title";
    protected static final String ARG_STATUS = "status";

    // the input title and text views
    protected EditText mEditRcpt;
    protected EditText mEditTitle;
    protected EditText mEditText;
    protected ImageButton mIconButton;

    // this holds the kind of form this is. The static fields are defined below.
    public static int MODE_EDIT = 1;
    public static int MODE_REPLY = 2;
    public static int MODE_MESSAGE = 3;

    // the array of icons
    private int mIconId;

    /**
     * * Return new instance of FormFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return FormFragment
     */
    public static EditorFragment newInstance(Bundle args) {
        EditorFragment ef = new EditorFragment();
        ef.setArguments(args);
        return ef;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_form, container, false);

        // set the image button listener
        ImageButton icon = (ImageButton) v.findViewById(R.id.button_icon);
        icon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                IconSelectionDialog id = new IconSelectionDialog();
                id.setTargetFragment(EditorFragment.this, 0);
                id.show(getBaseActivity().getSupportFragmentManager(), "icondialog");
            }
        });

        // assign some views
        mEditText = (EditText) v.findViewById(R.id.edit_content);
        mEditTitle = (EditText) v.findViewById(R.id.edit_title);
        mEditRcpt = (EditText) v.findViewById(R.id.edit_rcpt);
        mIconButton = (ImageButton) v.findViewById(R.id.button_icon);

        // fill the form
        if(getArguments().getString(ARG_TEXT) != null)
            mEditText.setText(getArguments().getString(ARG_TEXT));

        if(getArguments().getString(ARG_TITLE) != null)
            mEditTitle.setText(getArguments().getString(ARG_TITLE));

        if(getArguments().getInt(ARG_ICON, 0) > 0)
            setIconById(getArguments().getInt(ARG_ICON));

        if(getArguments().getString(ARG_RCPT) != null)
            mEditRcpt.setText(getArguments().getString(ARG_RCPT));

        // set the title
        if(getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_REPLY)
            getActionbar().setTitle("Antwort verfassen");
        else if(getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_EDIT)
            getActionbar().setTitle("Post bearbeiten");
        else if(getArguments().getInt(ARG_MODE, MODE_REPLY) == MODE_MESSAGE) {
            mIconButton.setVisibility(View.GONE);
            mEditRcpt.setVisibility(View.VISIBLE);
            getActionbar().setTitle("Nachricht verfassen");
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_editor, menu);

        MenuItem send = menu.findItem(R.id.send);
        send.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_ok));

        MenuItem cancel = menu.findItem(R.id.cancel);
        cancel.setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_remove));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.send:
                hideKeyboard();

                Bundle args = new Bundle(getArguments());
                args.putString(ARG_RCPT, mEditRcpt.getText().toString());
                args.putString(ARG_TEXT, mEditText.getText().toString());
                args.putString(ARG_TITLE, mEditTitle.getText().toString());
                args.putInt(ARG_ICON, mIconId);

                startLoader(EditorFragment.this, args);
                return true;
            case R.id.cancel:
                stopLoader();
                hideKeyboard();
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Bundle> onCreateLoader(int i, Bundle args) {

        showLoadingAnimation();

        if (getArguments().getInt(ARG_MODE) == MODE_MESSAGE)
            return new AsyncMessageSubmitter(getBaseActivity(), args);
        else
            return new AsyncPostSubmitter(getBaseActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<Bundle> sender, Bundle result) {
        hideLoadingAnimation();

        if (result != null) {
            Intent intent = new Intent();
            intent.putExtras(result);
            getActivity().setResult(result.getInt(ARG_STATUS, Activity.RESULT_CANCELED), intent);
            getActivity().finish();
        } else {
            showError(getString(R.string.loading_error));
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @Override
    public void onLoaderReset(Loader<Bundle> sender) {
        hideLoadingAnimation();
    }

    /**
     * Set the icon by providing its id
     * @param iconId the icon id
     */
    public void setIconById(Integer iconId) {

        Bitmap icon;
        try {
            icon = Utils.getBitmapIcon(getActivity(), iconId);
            Bitmap bm = Bitmap.createScaledBitmap(icon, 80, 80, true);
            mIconButton.setImageBitmap(bm);
            mIconId = iconId;
        } catch (IOException e) {}
    }

    static class AsyncPostSubmitter extends AsyncHttpLoader<Bundle>
    {

        protected int mMode;

        AsyncPostSubmitter(Context cx, Bundle args) {
            super(cx, Network.BOARD_URL_POST, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            EncodingRequestParams r = new EncodingRequestParams();

            r.setEncoding(Network.ENCODING_ISO);

            // this must resemble the same form on the website
            r.add("message", args.getString(ARG_TEXT));
            r.add("submit", "Eintragen");
            r.add("TID", "" + args.getInt(ARG_TOPIC_ID));
            r.add("token", args.getString(ARG_TOKEN));

            if (mMode == MODE_EDIT) {
                r.add("PID", "" + args.getInt(ARG_POST_ID));
                r.add("edit_title", args.getString(ARG_TITLE));
                r.add("edit_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("edit_converturls", "1");
                setUrl(Network.BOARD_URL_EDITPOST);
            } else if (mMode == MODE_REPLY) {
                r.add("SID", "");
                r.add("PID", "");
                r.add("post_title", args.getString(ARG_TITLE));
                r.add("post_icon", new Integer(args.getInt(ARG_ICON)).toString());
                r.add("post_converturls", "1");
                setUrl(Network.BOARD_URL_POST);
            }

            setParams(r);

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("thread.php\\?TID=([0-9]+)&temp=[0-9]+&PID=([0-9]+)");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(ARG_STATUS, Activity.RESULT_OK);
                result.putInt(ARG_POST_ID, Integer.parseInt(m.group(2)));
            } else {
                result.putInt(ARG_STATUS, Activity.RESULT_CANCELED);
            }

            return result;
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }

    static class AsyncMessageSubmitter extends AsyncHttpLoader<Bundle>
    {

        protected int mMode;

        AsyncMessageSubmitter(Context cx, Bundle args) {
            super(cx, MessageParser.SEND_URL, AsyncHttpLoader.POST);

            mMode = args.getInt(ARG_MODE);

            EncodingRequestParams r = new EncodingRequestParams();

            r.setEncoding(Network.ENCODING_ISO);

            r.add("rcpts", "0");
            r.add("rcpt", args.getString("rcpt"));
            r.add("subj", args.getString("title"));
            r.add("msg", args.getString("text"));
            r.add("mf_sc", "1");
            r.add("submit", "Senden");

            setParams(r);

        }

        @Override
        public Bundle processNetworkResponse(String response) {
            // check if the correct success website is displayed

            Pattern pattern = Pattern.compile("Nachricht wird an");
            Matcher m = pattern.matcher(response);

            Bundle result = new Bundle();
            result.putInt(ARG_MODE, mMode);

            if (m.find()) {
                result.putInt(ARG_STATUS, Activity.RESULT_OK);
            } else {
                result.putInt(ARG_STATUS, Activity.RESULT_CANCELED);
            }

            return result;
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }
}