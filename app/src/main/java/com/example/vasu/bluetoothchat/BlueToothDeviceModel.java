package com.example.vasu.bluetoothchat;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Vasu on 14-01-2018.
 */

public class BlueToothDeviceModel {

    BluetoothDevice device ;

    public BlueToothDeviceModel(){

    }

    public BlueToothDeviceModel(BluetoothDevice device){
        this.device = device ;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

}
