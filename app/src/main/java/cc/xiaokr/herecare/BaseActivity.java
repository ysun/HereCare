package cc.xiaokr.herecare;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends Activity {
    private Toast mToastText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToastText = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void showToast(final String msg) {
        mToastText.setText(msg + "");
        mToastText.show();
    }

    public String getResString(int resId) {
        return this.getResources().getString(resId);
    }

    public void doJoinActAnim(){
        this.overridePendingTransition(R.anim.anim_act_left_join,
                R.anim.anim_act_left_back);
    }

    public void doBackActAnim(){
        this.overridePendingTransition(R.anim.anim_act_right_join,
                R.anim.anim_act_right_back);
    }
}
