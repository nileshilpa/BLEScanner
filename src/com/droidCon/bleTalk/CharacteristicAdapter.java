package com.droidCon.bleTalk;

import android.bluetooth.BluetoothGattCharacteristic;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class CharacteristicAdapter extends BaseAdapter {
    private List<BluetoothGattCharacteristic> chars;
    LayoutInflater inflater;

    public CharacteristicAdapter(LayoutInflater layoutInflator) {
        super();
        chars = new ArrayList<BluetoothGattCharacteristic>();
        inflater = layoutInflator;
    }

    public void addCharacteristic(List<BluetoothGattCharacteristic> chars) {
            this.chars = chars;
    }

    public void clear() {
        chars.clear();
    }

    @Override
    public int getCount() {
        return chars.size();
    }

    @Override
    public BluetoothGattCharacteristic getItem(int i) {
        return chars.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ServiceViewHolder viewHolder;
        BluetoothGattCharacteristic characteristic = chars.get(i);

        if (view == null) {
            view = inflater.inflate(R.layout.layout_list_char, null);
            viewHolder = new ServiceViewHolder();
            viewHolder.charUUID = (TextView) view.findViewById(R.id.char_uuid);
            viewHolder.properties = (TextView) view.findViewById(R.id.char_properties);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ServiceViewHolder) view.getTag();
        }

        viewHolder.charUUID.setText(GattAttributes.lookup(characteristic.getUuid().toString(), "UNKNOWN CHAR"));

        final int charProp = characteristic.getProperties();

        String property = charProp + " ( ";
        //There must be better way to do this..
        if (charProp == 138) {
             property = property + "READ/WRITE";
        }

        if (charProp == BluetoothGattCharacteristic.PROPERTY_READ) {
            property = property + "READ";
        }

        if (charProp == BluetoothGattCharacteristic.PROPERTY_WRITE) {
            property = property + "READ";
        }

        if (charProp == 154) {
            property = property + "READ/WRITE/NOTIFY";
        }

        property = property + " ) ";

        viewHolder.properties.setText(property);

        return view;
    }

    static class ServiceViewHolder {
        TextView charUUID;
        TextView properties;
    }

}


