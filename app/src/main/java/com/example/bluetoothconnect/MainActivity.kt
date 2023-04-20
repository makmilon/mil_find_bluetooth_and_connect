package com.example.bluetoothconnect

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    var textV: TextView?= null
    private lateinit var listView: ListView
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn= findViewById<Button>(R.id.button)
        textV=findViewById(R.id.textView)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            listAdapter.add("Bluetooth not supported")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            listAdapter.add("Bluetooth not enabled")
            return
        }
        listView = findViewById(R.id.listView)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = listAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val deviceName = listAdapter.getItem(position)
            connectToDevice(deviceName!!)
        }

        btn.setOnClickListener {

            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.dialog_layout1)
            dialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_background1))
            dialog.setCancelable(false)
            dialog.show()

            // Start scanning for Bluetooth devices
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    scanForDevices()
                    handler.postDelayed(this, 5000)
                    dialog.dismiss()
                }
            }, 5000)

        }


    }

    @SuppressLint("MissingPermission")
    private fun scanForDevices() {
        listAdapter.clear()
        val pairedDevices = bluetoothAdapter.bondedDevices
        textV?.visibility= View.VISIBLE
        for (device in pairedDevices) {
            listAdapter.add(device.name)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(deviceName: String) {
        // Find the Bluetooth device with the given name
        var device: BluetoothDevice? = null
        val pairedDevices = bluetoothAdapter.bondedDevices
        for (d in pairedDevices) {
            if (d.name == deviceName) {
                device = d
                break
            }
        }

        if (device == null) {
            listAdapter.add("Could not find device with name $deviceName")
            return
        }

        // Connect to the device
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val socket = device.createRfcommSocketToServiceRecord(uuid)
        try {
            socket.connect()
            // Connection successful, send a message to the device
            val outputStream = socket.outputStream
            val message = "Hello, Bluetooth!"
            outputStream.write(message.toByteArray())
            outputStream.flush()
            listAdapter.add("Message sent to $deviceName")
        } catch (e: IOException) {
            listAdapter.add("Error connecting to device: ${e.message}")
            try {
                socket.close()
            } catch (e2: IOException) {
                listAdapter.add("Failed to close socket: ${e2.message}")
            }
        }
    }
}
