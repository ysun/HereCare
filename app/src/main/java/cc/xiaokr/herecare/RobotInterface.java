package cc.xiaokr.herecare;

/**
 * Created by "Bobby Wang <wbo4958@gmail.com>" on 10/23/2016.
 */

public interface RobotInterface {
    boolean isSupportingBluetooth();

    boolean isBluetoothEnabled();

    void startBluetoothScan();

    void cancelScan();

    boolean connect(String mac);

     /**
     * Set Robot mode
     *
     * @param mode
     */
    void setRobotMode(int mode);

    void cleanUp();
}
