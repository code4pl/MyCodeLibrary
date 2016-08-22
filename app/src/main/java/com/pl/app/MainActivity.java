package com.pl.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.app.model.Department;
import com.pl.app.model.Staff;
import com.pl.app.views.PinnedHeaderExpandableListView;
import com.pl.app.views.PorterDuffXfermodeView;
import com.pl.app.views.StickyLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PorterDuffXfermodeView(this));
    }

}
