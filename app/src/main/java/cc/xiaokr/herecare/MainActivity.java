package cc.xiaokr.herecare;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    public static final int DISCOVERY_STARTED = 1;
    public static final int DISCOVERY_FINISHED = 2;

    public static final int EVENT_CONNECT = 1;
    public static final int CONNECT_FAIL = 1;
    public static final int CONNECT_CONNECTING = 2;
    public static final int CONNECT_SUCCESS = 3;

    public static final int EVENT_FOUND = 2;
    public static final int EVENT_DISCOVERY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
