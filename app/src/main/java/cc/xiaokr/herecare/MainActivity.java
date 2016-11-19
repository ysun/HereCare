package cc.xiaokr.herecare;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    public static final int EVENT_DISCOVERY = 1;
    public static final int EVENT_FOUND = 2;
    public static final int EVENT_CONNECT = 3;
    public static final int EVENT_CLEARCONNECT = 10;

    public static final int DISCOVERY_STARTED = 1;
    public static final int DISCOVERY_FINISHED = 2;

    public static final int CONNECT_FAIL = 1;
    public static final int CONNECT_SUCCESS = 2;
    public static final int CONNECT_CONNECTED = 3;
    public static final int CONNECT_CONNECTING = 4;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private ArrayList<DeviceBean> mDevicesList = new ArrayList<>();
    private DevicesAdapter mDevicesAdapter;
    private RobotImpl mRobotImpl;

    private View mLayoutDeviceFound;
    private ListView mListViewDeviceFound;
    private Button mBtDismissLayoutDeviceFound;

    private ProgressDialog mBtConnectProgressDialog;

    private Animation mSearchAnimation;
    private ImageView mIvSearch;

    private RelativeLayout mLayoutDeviceNotFound;
    private ImageView mIvShowBt, mIvLight, mIvHorn;

    private TextView mTvSetting;

    private ImageButton mIbAuto, mIbManual, mIbCruise, mIbGravity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        mRobotImpl = new RobotImpl(this, mUiHandler);
//        initView();

        if (!mRobotImpl.isSupportingBluetooth()) {
            return;
        }

        if (!mRobotImpl.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            startScan();
        }
    }

//    private void initView() {
//        mLayoutDeviceFound = findViewById(R.id.layout_device_found);
//        mListViewDeviceFound = (ListView) findViewById(R.id.listview_device_found);
//        mDevicesAdapter = new DevicesAdapter(this);
//        mListViewDeviceFound.setAdapter(mDevicesAdapter);
//        mListViewDeviceFound.setOnItemClickListener(this);
//
//        mBtDismissLayoutDeviceFound = (Button) findViewById(R.id.bt_dismiss_layout_device_found);
//        mBtDismissLayoutDeviceFound.setOnClickListener(this);
//
//        mIvShowBt = (ImageView) findViewById(R.id.iv_show_devices);
//        mIvShowBt.setOnClickListener(this);
//
//        mIvLight = (ImageView) findViewById(R.id.iv_light);
//        mIvLight.setOnClickListener(this);
//
//        mIvHorn = (ImageView) findViewById(R.id.iv_horn);
//        mIvHorn.setOnClickListener(this);
//
//        mSearchAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
//        LinearInterpolator localLinearInterpolator = new LinearInterpolator();
//        mSearchAnimation.setInterpolator(localLinearInterpolator);
//        mIvSearch = (ImageView) findViewById(R.id.iv_search);
//
//        mLayoutDeviceNotFound = (RelativeLayout) findViewById(R.id.device_found_footer);
//        mLayoutDeviceNotFound.setOnClickListener(this);
//
//        mTvSetting = (TextView) findViewById(R.id.tv_info);
//        mTvSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        mIbAuto = (ImageButton) findViewById(R.id.ib_auto);
//        mIbAuto.setOnClickListener(this);
//        mIbCruise = (ImageButton) findViewById(R.id.ib_cruise);
//        mIbCruise.setOnClickListener(this);
//        mIbGravity = (ImageButton) findViewById(R.id.ib_gravity);
//        mIbGravity.setOnClickListener(this);
//        mIbManual = (ImageButton) findViewById(R.id.ib_manual);
//        mIbManual.setOnClickListener(this);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                startScan();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d("xxxx in onDestroy");
        dismissProgressDialog();
        mRobotImpl.cleanUp();
    }

    private boolean mIsSearching = false;

    private void startScan() {
        if (!mIsSearching) {
            mIsSearching = true;
            mIvSearch.startAnimation(mSearchAnimation);
            mRobotImpl.startBluetoothScan();
        }
    }

    private void stopScanAnimation() {
        if (mIsSearching) {
            mIsSearching = false;
            mIvSearch.clearAnimation();
        }
    }


    private boolean mIsIngoreLastManual = false;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_DISCOVERY:
                    if (msg.arg1 == DISCOVERY_STARTED) {
                        showToast(getResString(R.string.scan_begin));
                    } else if (msg.arg1 == DISCOVERY_FINISHED) {
                        showToast(getResString(R.string.scan_end));
                        stopScanAnimation();
                    }
                    break;

                case EVENT_FOUND:
                    DeviceBean device = (DeviceBean) msg.obj;
                    LogUtils.d(" " + device.getName() + " " + device.getMac());
                    addData(device);
                    break;

                case EVENT_CONNECT:
                    if (msg.arg1 == CONNECT_CONNECTING) {
                        Message tmpMsg = new Message();
                        msg.what = EVENT_CLEARCONNECT;
                        msg.arg1 = CONNECT_FAIL;
                        mUiHandler.sendMessageDelayed(tmpMsg, 15000);
                        showProgressDialog();
                        return;
                    }

                    mUiHandler.removeMessages(EVENT_CLEARCONNECT);
                    dismissProgressDialog();

                    if (msg.arg1 == CONNECT_SUCCESS) {
                        showToast(getResString(R.string.connect_success));
                        mLayoutDeviceFound.setVisibility(View.INVISIBLE);
                    } else if (msg.arg1 == CONNECT_FAIL) {
                        showToast(getResString(R.string.connect_fail));
                    }
                    break;

                case EVENT_CLEARCONNECT:
                    LogUtils.d("in EVENT_CLEARCONNECT");
                    dismissProgressDialog();
                    break;

                default:
                    break;
            }
        }
    };

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        mRobotImpl.cancelScan();
//        stopScanAnimation();
//        mRobotImpl.connect(mDevicesList.get(position).getMac());
//    }

    private void dismissProgressDialog() {
        if (mBtConnectProgressDialog != null && mBtConnectProgressDialog.isShowing()) {
            mBtConnectProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mBtConnectProgressDialog == null) {
            mBtConnectProgressDialog = ProgressDialog.show(this, "", getResString(R.string.is_connecting), false, false);
        }
        mBtConnectProgressDialog.show();
    }


    private class DevicesAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Context context;

        public DevicesAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mDevicesList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevicesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.device_list_view, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.ivState = (ImageView) convertView.findViewById(R.id.iv_state);
                holder.ivEdit = (ImageView) convertView.findViewById(R.id.iv_edit);
                holder.ivEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DeviceBean device = (DeviceBean) getItem(position);
            if (device != null) {
                if (!TextUtils.isEmpty(device.getAlias())) {
                    holder.tvName.setText(device.getAlias());
                } else {
                    String name = TextUtils.isEmpty(device.getName()) ? "unknow" : device.getName();
                    holder.tvName.setText(name);
                }

                if (device.getState() == DeviceBean.DEVICE_STATE_CONNECTED) {
                    holder.ivState.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_connected));
                } else {
                }
            }

            return convertView;
        }

        private class ViewHolder {
            public TextView tvName;
            public ImageView ivState;
            public ImageView ivEdit;
        }
    }

    private void addData(DeviceBean device) {
        if (device == null) {
            return;
        }
        for (DeviceBean tmp : mDevicesList) {
            if (tmp.equals(device)) {
                return;
            }
        }

        mDevicesList.add(device);
        mDevicesAdapter.notifyDataSetChanged();
    }

    private long firstTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mLayoutDeviceFound.getVisibility() == View.VISIBLE) {
                mLayoutDeviceFound.setVisibility(View.INVISIBLE);
                return true;
            }

            if (System.currentTimeMillis() - firstTime > 1500) {
                showToast("再按一次退出程序");
                firstTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
