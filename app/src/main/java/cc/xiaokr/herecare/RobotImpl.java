package cc.xiaokr.herecare;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by "Bobby Wang <wbo4958@gmail.com>" on 10/23/2016.
 */

public class RobotImpl implements RobotInterface {
    public static final int ROBOT_STATE_NONE = 0;
    public static final int ROBOT_STATE_CONNECTING = 1;
    public static final int ROBOT_STATE_CONNECTED = 2;

    private static final int EVENT_MODE = 1;

    private int mBtState = ROBOT_STATE_NONE;

    private static final UUID ROBOT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private boolean mIsSupportingBluetooth;

    private boolean mInScan;

    /**
     * This Handler runs in a seperate thread (non-ui) thread
     * just in case blocking UI thread.
     */
    private RobotHandler mHandler;
    private HandlerThread mThread;

    /**
     * Ui handler runs in UI thread to post bluetooth state to Activity to update UI.
     */
    private Handler mUiHandler;

    private String mConnectedMac = "";
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutPutStream;

    private ReceiverThread mReceiverThread;

    public RobotImpl(Context context, Handler handler) {
        this.mContext = context;
        this.mUiHandler = handler;
        LogUtils.d("RobotImpl +");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mIsSupportingBluetooth = isSupportingBluetooth();

        registerReceiver();

        if (!isSupportingBluetooth()) {
            LogUtils.e("This device didn't support Bluetooth");
            return;
        }

        mThread = new HandlerThread("KRobot thread");
        mThread.start();
        mHandler = new RobotHandler(mThread.getLooper());
        LogUtils.d("RobotImpl -");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtils.d("Bluetooth discovery started");
                mInScan = true;
                sendDiscoveryEvent(MainActivity.DISCOVERY_STARTED);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = DeviceBean.DEVICE_STATE_DISCONNECTED;

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    state = DeviceBean.DEVICE_STATE_CONNECTED;
                }
                sendDeviceFoundMsg(new DeviceBean(device.getName(), device.getAddress(), state));
                LogUtils.d("Found device " + device.getName() + " " + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.d("Bluetooth discovery finished");
                if (mInScan) {
                    sendDiscoveryEvent(MainActivity.DISCOVERY_FINISHED);
                }
                mInScan = false;
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
//                    showToast("Paired");
                    String mac = device.getAddress();
                    mHandler.post(new ConnectRunnable(mac));
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
//                    showToast("Unpaired");
                }
//
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                int state = device.getBondState();
//
//                switch (state) {
//                    case BluetoothDevice.BOND_BONDED:
//                        //paired.
//                        break;
//                }
            }
        }
    };

    /**
     * Register broadcastreceiver to receive Bluetooth event
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.mContext.registerReceiver(mReceiver, filter);
    }


    @Override
    public boolean isSupportingBluetooth() {
        return mBluetoothAdapter != null;
    }

    /**
     * Check Bluetooth enabled;
     *
     * @return
     */
    @Override
    public boolean isBluetoothEnabled() {
        if (!mIsSupportingBluetooth) {
            return false;
        }

        return mBluetoothAdapter.isEnabled();
    }

    /**
     * start to discovery
     */
    @Override
    public void startBluetoothScan() {

        cancelScan();
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Cancel bluetooth discovery
     */
    @Override
    public void cancelScan() {
        if (!isBluetoothEnabled()) {
            return;
        }
        mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * Connect to specific bluetooth with MAC address
     *
     * @param mac
     * @return
     */
    @Override
    public boolean connect(String mac) {
        if (TextUtils.isEmpty(mac)) {
            LogUtils.e("wrong mac " + mac);
            return false;
        }

        if (mBtState == ROBOT_STATE_CONNECTING) {
            return false;
        }

        if (mBtState == ROBOT_STATE_CONNECTED && mac.equals(mConnectedMac)) {
            LogUtils.d("Already connected for " + mac);
            return false;
        }

        mHandler.removeCallbacks(null);
        mHandler.post(new ConnectRunnable(mac));
        return true;
    }

    @Override
    public void setRobotMode(int mode) {

    }

    private void sendModeMsg(int mode) {
        mHandler.removeMessages(EVENT_MODE);
        Message msg = new Message();
        msg.what = EVENT_MODE;
        msg.arg1 = mode;
        mHandler.sendMessage(msg);
    }

    @Override
    public void cleanUp() {
        this.mContext.unregisterReceiver(mReceiver);
        closeSocket();
    }

    private boolean isBluetoothReady() {
        return mIsSupportingBluetooth && isBluetoothEnabled();
    }

    private class RobotHandler extends Handler {
        public RobotHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_MODE:
                    int mode = msg.arg1;
                    LogUtils.d("bobby EVENT_MODE: " + mode);
                    break;

                default:
                    break;
            }
        }
    }

    private void sendDiscoveryEvent(int event) {
        Message msg = new Message();
        msg.what = MainActivity.EVENT_DISCOVERY;
        msg.arg1 = event;
        mUiHandler.sendMessage(msg);
    }

    private void sendDeviceFoundMsg(DeviceBean device) {
        Message msg = new Message();
        msg.what = MainActivity.EVENT_FOUND;
        msg.obj = device;
        mUiHandler.sendMessage(msg);
    }

    private void sendDeviceConnectMsg(int event) {
        Message msg = new Message();
        msg.what = MainActivity.EVENT_CONNECT;
        msg.arg1 = event;
        mUiHandler.sendMessage(msg);
    }

    private void sendSensorMsg(int event, int type, int data) {
        Message msg = new Message();
        msg.what = event;
        msg.arg1 = type;
        msg.arg2 = data;
        mUiHandler.sendMessage(msg);
    }

    private long mLastTimeMills = System.currentTimeMillis();
    private static final float MAX_SPEED = (float) 255.0 / 100;

    private class ConnectRunnable implements Runnable {
        private String mac;

        public ConnectRunnable(String mac) {
            this.mac = mac;
        }

        @Override
        public void run() {
            sendDeviceConnectMsg(MainActivity.CONNECT_CONNECTING);
            mBtState = ROBOT_STATE_CONNECTING;
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);
            if (mBluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                try {
                    LogUtils.d("begin to pair");
                    Method creMethod = BluetoothDevice.class.getMethod("createBond");
                    creMethod.invoke(mBluetoothDevice);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("failed to invoke createBond");
                    mBtState = ROBOT_STATE_NONE;
                    sendDeviceConnectMsg(MainActivity.CONNECT_FAIL);
                    return;
                }
                return;
            }
            LogUtils.d("Paired");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(ROBOT_UUID);
                mBluetoothSocket.connect();

                mInputStream = mBluetoothSocket.getInputStream();
                mOutPutStream = mBluetoothSocket.getOutputStream();

                mReceiverThread = new ReceiverThread();
                mReceiverThread.start();

            } catch (IOException e) {
                LogUtils.e("Error for creating bluetooth socket");
                closeSocket();
                e.printStackTrace();
                sendDeviceConnectMsg(MainActivity.CONNECT_FAIL);
                mBtState = ROBOT_STATE_NONE;
                return;
            }

            mBtState = ROBOT_STATE_CONNECTED;
            sendDeviceConnectMsg(MainActivity.CONNECT_SUCCESS);
        }
    }

    private void closeSocket() {
        try {
            if (mReceiverThread != null) {
                mReceiverThread.setExitFlag(true);
                mReceiverThread.interrupt();
                mReceiverThread = null;
            }

            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }

            if (mOutPutStream != null) {
                mOutPutStream.close();
                mOutPutStream = null;
            }

            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }

        } catch (Exception e) {
            LogUtils.e("close socket error");
        }

    }

    private void sendCmdToRobot(byte[] cmd) {

        if (mOutPutStream == null) {
            return;
        }
        try {
            mOutPutStream.write(cmd);
            mOutPutStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ReceiverThread extends Thread {
        private boolean mExiting = false;
        private List<Byte> mRx = new ArrayList<>();

        private ReceiverThread() {
        }

        public void setExitFlag(boolean exit) {
            mExiting = exit;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            try {
                while (!mExiting && mInputStream != null) {
                    mRx.clear();
                    // wait to read the whole bytes.
                    for (;;) {

                        int readed = mInputStream.read(buffer);
                        if (readed < 0) {
                            LogUtils.e("Seems InputStream broke");
                            mExiting = true;
                            break;
                        }

                        boolean flag = false;
                        for (int i = 0; i < readed; i++) {
//                            LogUtils.d("bobby i: " + i + " v:" + buffer[i]);
                            mRx.add(buffer[i]);
                            if (buffer[i] == -18) {
                                LogUtils.d("bobby ------------- bread for ee");
                                flag = true;
                                break;
                            }
                        }

                        if (flag) {
                            break;
                        }
                    }

                    int size = mRx.size();
                    if (size < 4) {
                        LogUtils.d("bobby size < 4 ...");
                        continue;
                    }

                    if (mRx.get(0) != -1 || mRx.get(1) != 85) {
                        LogUtils.d("bobby invalid value ...");
                        continue;
                    }

                    int len = mRx.get(2);
                    if (len < 2) {
                        LogUtils.d("bobby wrong len");
                        continue;
                    }
//                    LogUtils.d("bobby ------------------------- send sensor size:" + size);

                    //TLV END
                    if (size < 2 + len + 1 + 1) {
                        LogUtils.d("bobby TLV wrong format");
                        continue;
                    }

                    sendSensorMsg(MainActivity.EVENT_SENSOR, mRx.get(3), mRx.get(4));
                }
            } catch (IOException e) {
                LogUtils.e("Failed to receive data, exit");
            }
        }
    }

}
