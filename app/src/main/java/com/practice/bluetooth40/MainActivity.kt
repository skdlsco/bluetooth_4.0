package com.practice.bluetooth40

import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.util.*
import android.bluetooth.BluetoothGattCharacteristic


class MainActivity : AppCompatActivity() {
    var mBluetoothAdapter: BluetoothAdapter? = null
    var mBluetoothDevice: BluetoothDevice? = null
    var mBluetoothGatt: BluetoothGatt? = null
    var mHandler: Handler = Handler()
    var mBluetoothGattServices: List<BluetoothGattService>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAdapter()

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        scanLeDevice(true)
    }

    private fun getAdapter() {
        val mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)

            }, SCAN_PERIOD)
            Log.e("scan", "start")

            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            Log.e("scan", "stop")
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        }
    }

    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        Log.i("deviceInfo address", device.address)
        Log.i("deviceInfo name", "" + device.name)
        Log.i("deviceInfo type", "" + device.type)
        if (device.name == "bt_ble" && mBluetoothDevice == null) {
            scanLeDevice(false)
            mBluetoothDevice = device
            runOnUiThread {
                mBluetoothGatt = mBluetoothDevice?.connectGatt(this@MainActivity, false, mBluetoothGattCallback)
            }
        }
    }

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i("onServicesDiscovered", "onServicesDiscovered" + gatt?.services?.size)
            getServices()
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i("onConnectionStateChange", "onConnectionStateChange:$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread { mBluetoothGatt!!.discoverServices() }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.e("asdf", "" + characteristic!!.value.lastIndex)
            for (value in characteristic.value) {
                Log.e("asdf", "" + value)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val data = characteristic!!.value
            val stringBuilder = StringBuilder(data.size)

            for (value in data) {
                stringBuilder.append(String.format("%04X ", value))
            }
            Log.e("asdfadsf", "" + String(data) + "\n" + stringBuilder.toString())
        }
    }

    private fun getServices() {
        Log.i("getServices", "getServices")
        mBluetoothGattServices = mBluetoothGatt!!.services
        Log.i("getServices", "getServices" + (mBluetoothGattServices?.size))
        for (service in mBluetoothGattServices!!) {
            Log.e("uuid", "" + service.uuid.toString() + "\n" + service.characteristics.size)
            if (service.uuid.toString() == "0000ffe0-0000-1000-8000-00805f9b34fb") {
                val char = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"))
                Log.e("value", "" + char.descriptors[0].characteristic.descriptors.size)
                mBluetoothGatt?.readCharacteristic(char)
                mBluetoothGatt?.setCharacteristicNotification(char, true)
            }
        }
    }

    companion object {
        private val REQUEST_ENABLE_BT = 1
        private val SCAN_PERIOD: Long = 5000
    }
}
