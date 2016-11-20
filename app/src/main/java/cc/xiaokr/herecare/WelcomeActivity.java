package cc.xiaokr.herecare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton mBtTeaching, mBtMap, mBtTraining;
    private RelativeLayout mWelcomeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        mWelcomeLayout = (RelativeLayout) findViewById(R.id.layout_welcome);
        mWelcomeLayout.setBackground(readBgDrawable(this, R.drawable.cpr_first_page));
//        mWelcomeLayout.setBackgroundDrawable(readBgDrawable(this, R.drawable.cpr_first_page));

        mBtMap = (ImageButton) findViewById(R.id.bt_love_map);
        mBtMap.setOnClickListener(this);

        mBtTeaching = (ImageButton) findViewById(R.id.bt_teaching_material);
        mBtTeaching.setOnClickListener(this);

        mBtTraining = (ImageButton) findViewById(R.id.bt_training);
        mBtTraining.setOnClickListener(this);
    }

    private BitmapDrawable readBgDrawable(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_love_map:
                showToast("Not ready");
                break;

            case R.id.bt_training:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                doJoinActAnim();
                break;

            case R.id.bt_teaching_material:
                showToast("Not Ready");
                break;

            default:
                break;
        }
    }
}
