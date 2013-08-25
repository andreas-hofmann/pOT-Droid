package com.mde.potdroid3;

import android.os.Bundle;
import com.mde.potdroid3.fragments.ForumFragment;

public class ForumActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new ForumFragment())
                .commit();
    }
}