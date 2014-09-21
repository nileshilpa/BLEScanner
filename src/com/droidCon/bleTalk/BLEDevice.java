package com.droidCon.bleTalk;

import android.bluetooth.BluetoothDevice;

class BLEDevice {

    public BluetoothDevice device;
    public String proximityUuid;
    public int major;
    public int minor;
    public int txPower;
    public int rssi;

    public String toString(){
        return " { Major : " + major + " Minor: " + minor + " RSSI: " + rssi +
               " Tx: " + txPower + " " + " UUID: " + proximityUuid + "}";
    }

    public boolean isBeacon(){
        return proximityUuid!=null;
    }

    @Override
    public boolean equals(Object o){
        if (o != null && (o instanceof BLEDevice))
            return ((BLEDevice) o).device.equals(device);
        return false;
    }

    public static BLEDevice parseScanData(BluetoothDevice device, byte[] scanData, int rssi) {
        BLEDevice bleDevice = new BLEDevice();
        bleDevice.device = device;

        int startByte = 2;
        boolean isBeacon = false;
        while (startByte <= 5) {
            //Apple Beacon 4C 00 02 15
            if (((int)scanData[startByte] & 0xff) == 0x4c &&
                    ((int)scanData[startByte+1] & 0xff) == 0x00 &&
                    ((int)scanData[startByte+2] & 0xff) == 0x02 &&
                    ((int)scanData[startByte+3] & 0xff) == 0x15) {
                isBeacon = true;
                break;
            }
            startByte++;
        }

        if (!isBeacon) {
            return bleDevice;
        }

        bleDevice.major = (scanData[startByte+20] & 0xff) * 0x100 + (scanData[startByte+21] & 0xff);
        bleDevice.minor = (scanData[startByte+22] & 0xff) * 0x100 + (scanData[startByte+23] & 0xff);
        bleDevice.txPower = (int)scanData[startByte+24];
        bleDevice.rssi = rssi;

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHex(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0,8));
        sb.append("-");
        sb.append(hexString.substring(8,12));
        sb.append("-");
        sb.append(hexString.substring(12,16));
        sb.append("-");
        sb.append(hexString.substring(16,20));
        sb.append("-");
        sb.append(hexString.substring(20,32));
        bleDevice.proximityUuid = sb.toString();

        return bleDevice;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
