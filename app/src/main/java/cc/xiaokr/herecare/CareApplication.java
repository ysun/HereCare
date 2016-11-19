package cc.xiaokr.herecare;

import android.app.Application;

public class CareApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new RobotImpl(this);
    }
}
