package cc.xiaokr.herecare;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by "Bobby Wang <wbo4958@gmail.com>" on 10/24/2016.
 */

public class DeviceBean implements Parcelable {
    public static final int DEVICE_STATE_DISCONNECTED = 0;
    public static final int DEVICE_STATE_CONNECTED = 1;

    private String name;
    private String mac;
    private String alias;
    private int state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public DeviceBean(String name, String mac, int state) {
        if (TextUtils.isEmpty(name)) {
            this.name = "unknow";
        } else {
            this.name = name;
        }
        if (TextUtils.isEmpty(mac)) {
            this.mac = "";
        } else {
            this.mac = mac;
        }

        this.state = state;
    }

    protected DeviceBean(Parcel in) {
        name = in.readString();
        mac = in.readString();
        state = in.readInt();
    }

    public static final Creator<DeviceBean> CREATOR = new Creator<DeviceBean>() {
        @Override
        public DeviceBean createFromParcel(Parcel in) {
            return new DeviceBean(in);
        }

        @Override
        public DeviceBean[] newArray(int size) {
            return new DeviceBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mac);
        dest.writeInt(state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        DeviceBean device = (DeviceBean) obj;
        return mac.equals(device.getMac()) ? true : false;
    }
}
