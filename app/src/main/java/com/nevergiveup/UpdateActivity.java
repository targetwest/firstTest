package com.nevergiveup;

import android.app.Activity;
import android.os.Bundle;

import com.nevergiveup.views.HeaderView;

/**
 * Created by dai on 2015/12/19.
 */
public class UpdateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        HeaderView headerView = (HeaderView) findViewById(R.id.header);
        headerView.hideRightImage();
        headerView.setTitle(R.string.center_soft_update);
        headerView.setTitleLeft();

    }
}
