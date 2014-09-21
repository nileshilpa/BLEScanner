package com.droidCon.bleTalk;

import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ServiceAdapter extends BaseAdapter {
    private ArrayList<BluetoothGattService> services;
    LayoutInflater inflater;

    public ServiceAdapter(LayoutInflater layoutInflator) {
        super();
        services = new ArrayList<BluetoothGattService>();
        inflater = layoutInflator;
    }

    public void addService(BluetoothGattService device) {
        if (!services.contains(device)) {
            services.add(device);
        }
    }

    public void clear() {
        services.clear();
    }

    @Override
    public int getCount() {
        return services.size();
    }

    @Override
    public BluetoothGattService getItem(int i) {
        return services.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.i("BLEDemo","Service Changed");
        ServiceViewHolder viewHolder;
        BluetoothGattService bluetoothGattService = services.get(i);

        if (view == null) {
            view = inflater.inflate(R.layout.layout_list_service, null);
            viewHolder = new ServiceViewHolder();
            viewHolder.serviceName = (TextView) view.findViewById(R.id.service_name);
            viewHolder.serviceUUID = (TextView) view.findViewById(R.id.service_uuid);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ServiceViewHolder) view.getTag();
        }

        String lookup = GattAttributes.lookup(bluetoothGattService.getUuid().toString(), "UNKNOWN SERVICE");
        viewHolder.serviceName.setText(lookup);
        viewHolder.serviceUUID.setText(bluetoothGattService.getUuid().toString());

        return view;
    }

    static class ServiceViewHolder {
        TextView serviceName;
        TextView serviceUUID;
    }

}

