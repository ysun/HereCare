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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cc.xiaokr.herecare.chart.data.GridData;
import cc.xiaokr.herecare.chart.view.BarChart;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

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

    private ProgressDialog mBtConnectProgressDialog;

    private Button mBtConnect;
    private View mLayoutDeviceFound, mLayoutController;
    private ListView mListViewDeviceFound;
    private Button mBtDismissLayoutDeviceFound;

    private Animation mSearchAnimation;
    private ImageView mIvSearch;
    private BarChart mBarChart;

    private static final int GRID_DATA_SIZE = 10;
    private List<GridData> mGridData = new ArrayList<>();

    private Button mBtTest, mBtGasTest;
    private ImageView mIvAirFlow;

    private TextView mTvForce, mTvFrenquency, mTvPressLocation;
    private ImageView mIvForce, mIvFrenquency, mIvPressLocation;

    private static final int COLOR_RED = 0XFFFF4A39;
    private static final int COLOR_BLUE = 0xFF21A9D5;
    private static final int COLOR_GRAY = 0xFF898989;


    public static final int EVENT_SENSOR = 100;
    public static final int SENSOR_PRESSURE = 1;
    public static final int SENSOR_AXIS  = 2;
    public static final int SENSOR_AIR_FLOW = 3;
    public static final int SENSOR_PUSH = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mRobotImpl = new RobotImpl(this, mUiHandler);
        initView();

        if (!mRobotImpl.isSupportingBluetooth()) {
            return;
        }
    }

    private void initView() {
        mLayoutController = findViewById(R.id.controller);

        mLayoutDeviceFound = findViewById(R.id.layout_device_found);
        mListViewDeviceFound = (ListView) findViewById(R.id.listview_device_found);
        mDevicesAdapter = new DevicesAdapter(this);
        mListViewDeviceFound.setAdapter(mDevicesAdapter);
        mListViewDeviceFound.setOnItemClickListener(this);

        mBtDismissLayoutDeviceFound = (Button) findViewById(R.id.bt_dismiss_layout_device_found);
        mBtDismissLayoutDeviceFound.setOnClickListener(this);

        mBtConnect = (Button) findViewById(R.id.bt_connect);
        mBtConnect.setOnClickListener(this);

        mSearchAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator localLinearInterpolator = new LinearInterpolator();
        mSearchAnimation.setInterpolator(localLinearInterpolator);
        mIvSearch = (ImageView) findViewById(R.id.iv_search);

        mIvAirFlow = (ImageView) findViewById(R.id.iv_air_flow);
        mIvAirFlow.setImageDrawable(getResources().getDrawable(R.drawable.airflow_0));


        mTvForce = (TextView) findViewById(R.id.tv_force);
        mTvFrenquency = (TextView) findViewById(R.id.tv_frenqency);
        mTvPressLocation = (TextView) findViewById(R.id.tv_press_location);

        mIvForce = (ImageView) findViewById(R.id.iv_force_result);
        mIvFrenquency = (ImageView) findViewById(R.id.iv_frenqency_result);
        mIvPressLocation = (ImageView) findViewById(R.id.iv_press_result);

        mBtTest = (Button) findViewById(R.id.bt_test);
        mBtTest.setOnClickListener(this);

        mBtGasTest = (Button) findViewById(R.id.bt_test_gas);
        mBtGasTest.setOnClickListener(this);

        mBarChart = (BarChart) findViewById(R.id.bar_chart);
        initializeGridData();
    }

    private void initializeGridData() {
        for (int i = 0; i < GRID_DATA_SIZE; i++) {
            mGridData.add(generateData(0, true));
        }
        mBarChart.setDataList(mGridData, false);
    }

    private GridData generateData(int value, boolean ignore) {

        int color;
        if (value > 90) {
            color = COLOR_RED;

            if (!ignore) {
                mIvForce.setImageDrawable(getResources().getDrawable(R.drawable.un_correct));
                mTvForce.setText(getResString(R.string.bigger));
            }
        } else if (value > 70 && value <= 90) {
            color = COLOR_BLUE;
            if (!ignore) {
                mTvForce.setText(getResString(R.string.normal));
                mIvForce.setImageDrawable(getResources().getDrawable(R.drawable.correct));
            }
        } else {
            color = COLOR_GRAY;
            if (!ignore) {
                mTvForce.setText(getResString(R.string.smaller));
                mIvForce.setImageDrawable(getResources().getDrawable(R.drawable.un_correct));
            }
        }
        GridData.Entry[] entries = new GridData.Entry[1];
        entries[0] = new GridData.Entry(color, "", value);
        return new GridData("", entries);
    }

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
                        mLayoutController.setVisibility(View.VISIBLE);
                    } else if (msg.arg1 == CONNECT_FAIL) {
                        showToast(getResString(R.string.connect_fail));
                    }
                    break;

                case EVENT_CLEARCONNECT:
                    LogUtils.d("in EVENT_CLEARCONNECT");
                    dismissProgressDialog();
                    break;


                case EVENT_SENSOR:
                    int type = msg.arg1;
                    int value = msg.arg2;

                    switch (type) {
                        case SENSOR_AIR_FLOW:
                            break;
                        case SENSOR_AXIS:
                            break;
                        case SENSOR_PRESSURE:
                            break;
                        case SENSOR_PUSH:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void addPressueData(int value) {
        mGridData.add(generateData(value, false));
        if (mGridData.size() > GRID_DATA_SIZE) {
            mGridData.remove(0);
        }
        mBarChart.setDataList(mGridData, false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mRobotImpl.cancelScan();
        stopScanAnimation();
        mRobotImpl.connect(mDevicesList.get(position).getMac());
    }

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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_connect:
                mLayoutDeviceFound.setVisibility(View.VISIBLE);

                if (!mRobotImpl.isBluetoothEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
                } else {
                    startScan();
                }
                mBtConnect.setVisibility(View.INVISIBLE);
                break;

            case R.id.bt_dismiss_layout_device_found:
                stopScanAnimation();
                mLayoutDeviceFound.setVisibility(View.INVISIBLE);
                mBtConnect.setVisibility(View.VISIBLE);
                break;

            case R.id.bt_test:
                addPressueData(new Random().nextInt(101));
                break;

            case R.id.bt_test_gas:
                mIvAirFlow.setImageDrawable(getResources().getDrawable(AIR_FLOW_PIC[mIndex++%11]));
                break;
        }
    }

    private int mIndex = 1;
    private static final int[] AIR_FLOW_PIC = new int[] {
            R.drawable.airflow_0, R.drawable.airflow_1, R.drawable.airflow_2, R.drawable.airflow_3,
            R.drawable.airflow_4, R.drawable.airflow_5, R.drawable.airflow_6, R.drawable.airflow_7,
            R.drawable.airflow_8, R.drawable.airflow_9, R.drawable.airflow_11,
    };

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

    private long firstTime = 0;

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
