package org.codewiz.droid64.util;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

import org.codewiz.droid64.R;

public class UIHider {

    private Activity activity;
    private View view;

    public UIHider(Activity activity, int view) {
        this.activity = activity;
        this.view = activity.findViewById(view);
    }

    public void hide() {

        view.setVisibility(View.GONE);

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // mActivity.getActionBar().hide();
    }

    public void show() {

        view.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // mActivity.getActionBar().show();
    }

}
