package cc.xiaokr.herecare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtTeaching, mBtMap, mBtTraining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mBtMap = (Button) findViewById(R.id.bt_love_map);
        mBtMap.setOnClickListener(this);

        mBtTeaching = (Button) findViewById(R.id.bt_teaching_material);
        mBtTeaching.setOnClickListener(this);

        mBtTraining = (Button) findViewById(R.id.bt_training);
        mBtTraining.setOnClickListener(this);
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
