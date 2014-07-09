package com.example.mingle;

import android.app.Application;
import com.example.mingle.HttpHelper;

/**
 * Created by Tempnote on 2014-06-12.
 */

public class MingleApplication extends Application {
    public HttpHelper connectHelper;
    public MingleUser currUser;
    public DatabaseHelper dbHelper;
}
