package com.droidCon.bleTalk;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BLEScanner extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        getBluetoothAdapter();
    }

    private void getBluetoothAdapter() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

//        //Old Way
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

        //New way
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
    }

    private void initialize() {
        setContentView(R.layout.main);
        handler = new Handler();

        deviceListAdapter = new DeviceListAdapter(this.getLayoutInflater());
        deviceList = (ListView) findViewById(R.id.mainListView);
        deviceList.setAdapter(deviceListAdapter);

        serviceListAdapter = new ServiceAdapter(this.getLayoutInflater());
        serviceList = (ListView) findViewById(R.id.serviceListView);
        serviceList.setAdapter(serviceListAdapter);

        characteristicAdapter = new CharacteristicAdapter(this.getLayoutInflater());
        charList = (ListView) findViewById(R.id.charListView);
        charList.setAdapter(characteristicAdapter);


        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                BluetoothDevice device = deviceListAdapter.getItem(position).device;
                connectGatt(device);
            }
        });

        serviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                BluetoothGattService service = serviceListAdapter.getItem(position);
                characteristicAdapter.addCharacteristic(service.getCharacteristics());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        characteristicAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        startScanButton = (Button) findViewById(R.id.start_scan_button);
        stopScanButton = (Button) findViewById(R.id.stop_scan_button);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
                stopScanButton.setEnabled(true);
                startScanButton.setEnabled(false);
            }
        });
        stopScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScan();
                stopScanButton.setEnabled(false);
                startScanButton.setEnabled(true);
            }
        });
    }

    //Helper Methods
    private void startScan() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScanTimer(), 1000, SCAN_PERIOD + 1000);
        clearAdapter();
    }

    private void clearAdapter() {
        deviceListAdapter.clear();
        serviceListAdapter.clear();
        characteristicAdapter.clear();
        deviceListAdapter.notifyDataSetChanged();
        serviceListAdapter.notifyDataSetChanged();
        characteristicAdapter.notifyDataSetChanged();
    }

    private void stopScan() {
        timer.cancel();
        scanLeDevice(false);
    }

    private class ScanTimer extends TimerTask {
        @Override
        public void run() {
            scanLeDevice(true);
        }
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            startScanButton.setEnabled(false);
            makeToast("Can't use App without Bluetooth");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            scanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BLEDevice bleDevice = BLEDevice.parseScanData(device, scanRecord, rssi);
                            if(bleDevice.device.getName()!=null && bleDevice.device.getName().length()>0)
                                deviceListAdapter.addDevice(bleDevice);
                        }
                    });
                }
            };

    public void connectGatt(BluetoothDevice device) {
        Log.i(TAG, "Trying to connect to gatt " + device.getAddress());
        bluetoothGatt = device.connectGatt(this, true, gattCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        deviceListAdapter = new DeviceListAdapter(this.getLayoutInflater());
        deviceList.setAdapter(deviceListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        stopScanButton.setEnabled(false);
        startScanButton.setEnabled(true);
        deviceListAdapter.clear();
        serviceListAdapter.clear();
        characteristicAdapter.clear();
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED from GATT server.");
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service:services){
                        Log.i(TAG," " + service.getUuid().toString() + " " + service.toString() + " ");
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        serviceListAdapter.addService(service);
                        for(BluetoothGattCharacteristic characteristic:characteristics){
                            Log.i(TAG," " + characteristic.getUuid().toString() + " " + characteristic.getProperties() + " ");
                            gatt.readCharacteristic(characteristic);
                        }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serviceListAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringData = new StringBuilder(data.length);
                    for(byte byteChar : data)
                        stringData.append(String.format("%02X ", byteChar));
                    Log.i(TAG, "ACTION_DATA_AVAILABLE from GATT server. " + characteristic.getUuid().toString() + " " + stringData.toString());
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "ACTION_DATA_AVAILABLE - Characteristics from GATT server.");
        }


    };



    String TAG = BLEScanner.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 10;
    Handler handler = null;
    Boolean scanning = false;

    Button startScanButton = null;
    Button stopScanButton = null;

    BluetoothGatt bluetoothGatt = null;

    private static final long SCAN_PERIOD = 4000;
    private Timer timer = null;

    BluetoothAdapter bluetoothAdapter = null;
    ListView deviceList = null;
    DeviceListAdapter deviceListAdapter = null;

    private ListView serviceList;
    private ServiceAdapter serviceListAdapter;

    private ListView charList;
    private CharacteristicAdapter characteristicAdapter;
}
