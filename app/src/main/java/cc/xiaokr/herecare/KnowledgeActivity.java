package cc.xiaokr.herecare;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class KnowledgeActivity extends BaseActivity {
    private View mIvBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        mIvBackground = findViewById(R.id.activity_knowledge);
        mIvBackground.setBackground(Utils.readBgDrawable(this, R.drawable.love_map));
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
