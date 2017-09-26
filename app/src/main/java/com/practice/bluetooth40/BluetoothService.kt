package com.practice.bluetooth40

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.util.*

/**
 * Created by eka on 2017. 9. 26..
 */
class BluetoothService() : Service() {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mHandler = Handler()
    private var string = ""
    private var address = ""
    private var isDataFFront = true
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            address = intent.getStringExtra("address")
        }
        if (mBluetoothAdapter == null && address != "") {
            getAdapter()
        }
        if (mBluetoothGatt == null && mBluetoothAdapter != null && address != "")
            getGatt()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getAdapter() {
        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
    }

    private fun getGatt() {
        mBluetoothGatt = mBluetoothAdapter?.getRemoteDevice(address)?.connectGatt(this, true, mBluetoothGattCallback)
        mBluetoothGatt?.discoverServices()
    }

    private fun setNotification() {
        val services = mBluetoothGatt!!.services
        for (service in services!!) {
            Log.e("uuid", "" + service.uuid.toString() + "\n" + service.characteristics.size)
            if (service.uuid.toString() == "0000ffe0-0000-1000-8000-00805f9b34fb") {
                val char = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
                mBluetoothGatt?.setCharacteristicNotification(char, true)
            }
        }
    }

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mHandler.post { mBluetoothGatt!!.discoverServices() }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val data = characteristic!!.value

            Log.d("data", String(data))
            if (isDataFFront) {
                string = String(data)
                isDataFFront = false
            } else {
                string += String(data)
                isDataFFront = true
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            setNotification()
        }
    }
}