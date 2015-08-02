package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.*;
import android.widget.TextView;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.AbstractViewHolder;
import com.mde.potdroid.helpers.AsyncHttpLoader;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.parsers.BoardParser;
import com.mde.potdroid.views.IconDrawable;
import org.apache.http.Header;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The Board Fragment, which contains a list of Topics.
 */
public class BoardFragment extends PaginateFragment implements LoaderManager.LoaderCallbacks<Board> {

    // the tags of the fragment arguments
    public static final String ARG_ID = "board_id";
    public static final String ARG_PAGE = "page";
    // the board object
    private Board mBoard;
    // the topic list adapter
    private BoardListAdapter mListAdapter;
    private RecyclerView mListView;
    // bookmark database handler
    private DatabaseWrapper mDatabase;

    private LinearLayoutManager mLayoutManager;

    private FloatingActionButton mFab;

    /**
     * Returns an instance of the BoardFragment and sets required parameters as Arguments
     *
     * @param board_id the id of the board
     * @param page     the currently visible page
     * @return BoardFragment object
     */
    public static BoardFragment newInstance(int board_id, int page) {
        BoardFragment f = new BoardFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_ID, board_id);
        args.putInt(ARG_PAGE, page);

        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_board, container, false);

        mListAdapter = new BoardListAdapter();
        mListView = (RecyclerView) v.findViewById(R.id.forum_list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getBaseActivity());
        mListView.setLayoutManager(mLayoutManager);

        mFab = (FloatingActionButton) v.findViewById(R.id.fab);
        mFab.setImageDrawable(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));

        if(Utils.isLoggedIn() && mSettings.isShowFAB()) {
            //mFab.attachToListView(mListView);

            mFab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    newThread();
                }
            });
        } else {
            mFab.setVisibility(View.GONE);
        }

        mDatabase = new DatabaseWrapper(getActivity());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        if (mBoard == null)
            startLoader(this);

        if(mSettings.isBottomToolbar()) {
            getWriteButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newThread();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_thread:
                // reload content
                newThread();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_board, menu);
        if(!Utils.isLoggedIn() || mSettings.isBottomToolbar())
            menu.findItem(R.id.new_thread).setVisible(false);
        else
            menu.findItem(R.id.new_thread).setIcon(IconDrawable.getIconDrawable(getActivity(), R.string.icon_pencil));
    }

    /**
     * Open the form for a new thread
     */
    public void newThread() {
        if(mBoard == null)
            return;

        Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
        intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_THREAD);
        intent.putExtra(EditorFragment.ARG_BOARD_ID, mBoard.getId());
        intent.putExtra(EditorFragment.ARG_TOKEN, mBoard.getNewthreadtoken());

        startActivityForResult(intent, EditorFragment.MODE_THREAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EditorFragment.MODE_THREAD) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                intent.putExtra(TopicFragment.ARG_TOPIC_ID, data.getExtras().getInt(EditorFragment.ARG_TOPIC_ID));
                intent.putExtra(TopicFragment.ARG_PAGE, 1);
                startActivity(intent);
            }
        }
    }

    @Override
    public Loader<Board> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt(ARG_PAGE, 1);
        int bid = getArguments().getInt(ARG_ID, 0);

        showLoadingAnimation();
        setSwipeEnabled(false);

        return new AsyncContentLoader(getBaseActivity(), page, bid);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        restartLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Board> loader, Board data) {
        hideLoadingAnimation();

        if (data != null) {
            mBoard = data;

            // refresh the list
            mListAdapter.notifyDataSetChanged();

            // refresh the OptionsMenu, because of new pagination possibilities
            //getBaseActivity().supportInvalidateOptionsMenu();
            refreshPaginateLayout();

            // generate subtitle and set title and subtitle of the actionbar
            Spanned subtitle = Html.fromHtml(String.format(getString(
                    R.string.subtitle_paginate), mBoard.getPage(),
                    mBoard.getNumberOfPages()));

            getActionbar().setTitle(mBoard.getName());
            getActionbar().setSubtitle(subtitle);
            setSwipeEnabled(true);

            // scroll to top
            mLayoutManager.scrollToPositionWithOffset(0, 0);

        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<Board> loader) {
        hideLoadingAnimation();
    }

    public void goToNextPage() {
        // whether there is a next page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getPage() + 1);
        restartLoader(this);
    }

    @Override
    public void nextButtonLongClick() {
        goToLastPage();
    }

    public void goToPrevPage() {
        // whether there is a previous page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getPage() - 1);
        restartLoader(this);
    }

    public void goToLastPage() {
        // whether there is a previous page was checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, mBoard.getNumberOfPages());
        restartLoader(this);
    }

    public void goToFirstPage() {
        // whether there is a previous page was already checked in onCreateOptionsMenu
        getArguments().putInt(ARG_PAGE, 1);
        restartLoader(this);
    }

    @Override
    public boolean isLastPage() {
        return mBoard == null || mBoard.isLastPage();
    }

    @Override
    public ViewGroup getSwipeView() {
        return mListView;
    }

    @Override
    public boolean isFirstPage() {
        return mBoard == null || mBoard.getPage() == 1;
    }

    public void refreshPage() {
        restartLoader(this);
    }

    /**
     * The content loader
     */
    static class AsyncContentLoader extends AsyncHttpLoader<Board> {

        AsyncContentLoader(Context cx, int page, int board_id) {
            super(cx, BoardParser.getUrl(board_id, page));
        }

        @Override
        public Board processNetworkResponse(String response) {
            try {
                BoardParser parser = new BoardParser();
                return parser.parse(response);
            } catch (Exception e) {
                Utils.printException(e);
                return null;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }

    }

    public static class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
        public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
            super();
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                           FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                            nestedScrollAxes);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                    dyUnconsumed);

            if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
                child.hide();
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                child.show();
            }
        }

    }

    public class BoardListAdapter extends RecyclerView.Adapter<TopicViewHolder> {

        public int getItemCount() {
            if (mBoard == null)
                return 0;
            return mBoard.getFilteredTopics(getActivity()).size();
        }

        @Override
        public void onBindViewHolder(TopicViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            View row = holder.getView();

            Topic t = mBoard.getFilteredTopics(getActivity()).get(position);

            holder.bindModel(t);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(t.getTitle());
            if (t.isClosed())
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            // set the subtitle
            TextView subtitle = (TextView) row.findViewById(R.id.subtitle);
            subtitle.setText(t.getSubTitle());

            // pages information
            TextView pages = (TextView) row.findViewById(R.id.pages);
            Spanned pages_content = Html.fromHtml(String.format(getString(
                            R.string.topic_additional_information),
                    t.getNumberOfPosts(), t.getNumberOfPages()));
            pages.setText(pages_content);

            // lastpost
            Post displayPost;

            if(t.getLastPost() != null) {
                displayPost = t.getLastPost();
            } else {
                displayPost = t.getFirstPost();
            }

            // some smarter date formatting
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(displayPost.getDate());
            Calendar today = Calendar.getInstance();
            String fmt = "dd.MM.yyyy, HH:mm";
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                fmt = "HH:mm";
            } else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                fmt = "dd.MM., HH:mm";
            }

            TextView lastpost = (TextView) row.findViewById(R.id.author);
            String time = new SimpleDateFormat(fmt).format(displayPost.getDate());
            lastpost.setText(Html.fromHtml(String.format(
                    getString(R.string.thread_lastpost), displayPost.getAuthor().getNick(), time)));

            // icon
            if (t.getIconId() != null) {
                try {
                    Drawable d = Utils.getIcon(getActivity(), t.getIconId());
                    d.setBounds(0, 0, (int)title.getTextSize(), (int)title.getTextSize());
                    title.setCompoundDrawables(d, null, null, null);
                } catch (IOException e) {
                    Utils.printException(e);
                }
            }

            // all important topics get a different background.
            // the padding stuff is apparently an android bug...
            // see http://stackoverflow.com/questions/5890379
            if (t.isSticky() || t.isImportant() || t.isAnnouncement() || t.isGlobal()) {
                View v = row.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(Utils.getDrawableResourceIdByAttr(getActivity(), R.attr.bbBackgroundListActive));
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            } else {
                View v = row.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(Utils.getDrawableResourceIdByAttr(getActivity(), R.attr.bbBackgroundList));
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            }

            if (!t.isSticky()) {
                row.findViewById(R.id.icon_pinned).setVisibility(View.GONE);
            } else {
                row.findViewById(R.id.icon_pinned).setVisibility(View.VISIBLE);
            }

            if (!t.isClosed()) {
                row.findViewById(R.id.icon_locked).setVisibility(View.GONE);
            } else {
                row.findViewById(R.id.icon_locked).setVisibility(View.VISIBLE);
            }

            if (Utils.isLoggedIn() && !mDatabase.isBookmark(t)) {
                row.findViewById(R.id.icon_bookmarked).setVisibility(View.GONE);
            } else {
                row.findViewById(R.id.icon_bookmarked).setVisibility(View.VISIBLE);
            }


        }


        @Override
        public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View row = getInflater().inflate(R.layout.listitem_thread, null);
            return new TopicViewHolder(row, getBaseActivity());
        }
    }

    public class TopicViewHolder extends AbstractViewHolder<Topic> {

        public TopicViewHolder(View v, Context c) {
            super(v, c, true, true);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), TopicActivity.class);
            Topic t = getModel();
            Bookmark b = BoardFragment.this.mDatabase.getBookmarkByTopic(t);

            if(b != null) {
                intent.putExtra(TopicFragment.ARG_POST_ID, b.getLastPost().getId());
            } else {
                intent.putExtra(TopicFragment.ARG_PAGE, t.getNumberOfPages());
            }

            intent.putExtra(TopicFragment.ARG_TOPIC_ID, t.getId());
            getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(getContext(), TopicActivity.class);
            intent.putExtra(TopicFragment.ARG_TOPIC_ID, getModel().getId());
            intent.putExtra(TopicFragment.ARG_PAGE, 1);
            getContext().startActivity(intent);
            return true;
        }
    }

}
