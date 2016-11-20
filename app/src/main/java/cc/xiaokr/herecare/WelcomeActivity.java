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
        mWelcomeLayout.setBackground(Utils.readBgDrawable(this, R.drawable.cpr_first_page));
//        mWelcomeLayout.setBackgroundDrawable(readBgDrawable(this, R.drawable.cpr_first_page));

        mBtMap = (ImageButton) findViewById(R.id.bt_love_map);
        mBtMap.setOnClickListener(this);

        mBtTeaching = (ImageButton) findViewById(R.id.bt_teaching_material);
        mBtTeaching.setOnClickListener(this);

        mBtTraining = (ImageButton) findViewById(R.id.bt_training);
        mBtTraining.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.bt_love_map:
                intent = new Intent(this, LoveMapActivity.class);
                startActivity(intent);
                doJoinActAnim();
                break;

            case R.id.bt_training:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                doJoinActAnim();
                break;

            case R.id.bt_teaching_material:
                intent = new Intent(this, KnowledgeActivity.class);
                startActivity(intent);
                doJoinActAnim();
                break;

            default:
                break;
        }
    }
}
