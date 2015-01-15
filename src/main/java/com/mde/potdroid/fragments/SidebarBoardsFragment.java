package com.mde.potdroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.*;
import android.widget.*;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mde.potdroid.BoardActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.DatabaseWrapper;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.views.IconButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This is the Sidebar containing a list of favorite Boards
 */
public class SidebarBoardsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Forum> {

    private ArrayList<Board> mBoards;
    private BoardListAdapter mListAdapter;
    private DatabaseWrapper mDatabase;

    // this member indicates, whether the view is "dirty" and should be refreshed.
    private Boolean mDirty = true;

    /**
     * Return new instance of SidebarRightFragment. Although this fragment has no parameters,
     * We provide this method for consistency.
     *
     * @return SidebarLeftFragment
     */
    public static SidebarBoardsFragment newInstance() {
        return new SidebarBoardsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = new DatabaseWrapper(getActivity());
        mBoards = mDatabase.getBoards();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_sidebar_right, container, false);

        mListAdapter = new BoardListAdapter();

        ListView listView = (ListView) v.findViewById(R.id.listview_boards);
        listView.setAdapter(mListAdapter);
        listView.setEmptyView(v.findViewById(R.id.empty_bookmarks_text));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseActivity(), BoardActivity.class);
                intent.putExtra(BoardFragment.ARG_ID, mBoards.get(position).getId());
                startActivity(intent);
            }
        });

        Button refreshButton = (Button) v.findViewById(R.id.refresh_boards);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartLoader(SidebarBoardsFragment.this);
            }
        });

        IconButton refresh = (IconButton) v.findViewById(R.id.button_refresh);
        if(!mSettings.isSwipeToRefresh()) {
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartLoader(SidebarBoardsFragment.this);
                }
            });
            refresh.setVisibility(View.VISIBLE);
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.action_remove_bookmark)
                        .positiveText("Ok")
                        .negativeText("Abbrechen")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                DatabaseWrapper db = new DatabaseWrapper(getActivity());
                                db.removeBoard(mBoards.get(pos));
                                showSuccess(R.string.msg_remove_success);
                                refreshBoards();
                            }
                        })
                        .show();

                return true;
            }
        });

        return v;
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        restartLoader(this);
    }

    @Override
    public int getNotificationParent() {
        return R.id.forums_holder;
    }

    public void refreshBoards() {
        restartLoader(this);
    }

    @Override
    public Loader<Forum> onCreateLoader(int id, Bundle args) {
        mDirty = false;
        showLoadingAnimation();
        return new ForumFragment.AsyncContentLoader(getBaseActivity());
    }

    @Override
    public void onLoadFinished(Loader<Forum> loader, Forum success) {
        hideLoadingAnimation();
        if (success != null) {
            mDatabase.refreshBoards(success.getBoards());
            mBoards = mDatabase.getBoards();
            mListAdapter.notifyDataSetChanged();
        } else {
            showError(R.string.msg_loading_error);
        }
    }

    @Override
    public void onLoaderReset(Loader<Forum> loader) {
        hideLoadingAnimation();
    }

    /**
     * Is the sidebar dirty? *rr*
     *
     * @return true if it was not reloaded since it was attached, false otherwise
     */
    public Boolean isDirty() {
        return mDirty;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.notifyDataSetChanged();
    }

    private class BoardListAdapter extends BaseAdapter {

        public int getCount() {
            if (mBoards == null)
                return 0;
            return mBoards.size();
        }

        public Object getItem(int position) {
            return mBoards.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_sidebar_board, null);
            Board b = (Board) getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.name);
            title.setText(b.getName());

            // the last post's topic title
            TextView descr = (TextView) row.findViewById(R.id.text_description);
            Spanned lastpost_text = Html.fromHtml(String.format(
                    getString(R.string.strong), b.getLastPost().getTopic().getTitle()));
            descr.setText(lastpost_text);

            // the last post author and date
            TextView lastpost = (TextView) row.findViewById(R.id.last_post);
            String time = new SimpleDateFormat(getString(R.string.default_time_format)).format(b
                    .getLastPost().getDate());
            Spanned lastpost_text_line2 = Html.fromHtml(String.format(
                    getString(R.string.last_post_sidebar), b.getLastPost().getAuthor().getNick(), time));
            lastpost.setText(lastpost_text_line2);

            return row;
        }
    }


}
