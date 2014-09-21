package com.droidCon.bleTalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
    private ArrayList<BLEDevice> devices;
    LayoutInflater inflater;

    public DeviceListAdapter(LayoutInflater layoutInflater) {
        super();
        devices = new ArrayList<BLEDevice>();
        inflater = layoutInflater;
    }

    public void addDevice(BLEDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public BLEDevice getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DeviceViewHolder viewHolder;
        BLEDevice bleDevice = devices.get(i);
        final String deviceName = bleDevice.device.getName();

        if (view == null) {
            view = inflater.inflate(R.layout.layout_list_devices, null);
            viewHolder = new DeviceViewHolder();
            viewHolder.deviceMac = (TextView) view.findViewById(R.id.device_mac);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.beacon = (TextView) view.findViewById(R.id.beacon_or_not);
            view.setTag(viewHolder);
        } else {
            viewHolder = (DeviceViewHolder) view.getTag();
        }

        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);

        viewHolder.deviceMac.setText(bleDevice.device.getAddress());

        if (bleDevice.isBeacon())
            viewHolder.beacon.setText("It's a Beacon");
        else
            viewHolder.beacon.setText("It's a NOT a Beacon");

        return view;
    }

    static class DeviceViewHolder {
        TextView deviceName;
        TextView deviceMac;
        TextView beacon;
    }
}
