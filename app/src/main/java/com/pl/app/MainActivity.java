package com.pl.app;

import android.app.Activity;
import android.os.Bundle;

import com.pl.app.views.PorterDuffXfermodeView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PorterDuffXfermodeView(this));
    }

}
