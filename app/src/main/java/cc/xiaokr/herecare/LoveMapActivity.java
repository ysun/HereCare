package cc.xiaokr.herecare;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class LoveMapActivity extends BaseActivity {
    private View mIvBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_love_map);
        mIvBackground =  findViewById(R.id.activity_love_map);
        mIvBackground.setBackground(Utils.readBgDrawable(this, R.drawable.love_map));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            doBackActAnim();
        }
        return super.onKeyDown(keyCode, event);
    }
}
