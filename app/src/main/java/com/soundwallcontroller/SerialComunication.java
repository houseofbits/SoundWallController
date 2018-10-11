package com.soundwallcontroller;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.soundwallcontroller.driver.UsbSerialDriver;
import com.soundwallcontroller.driver.UsbSerialPort;
import com.soundwallcontroller.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

import static android.content.Context.USB_SERVICE;

//https://github.com/mik3y/usb-serial-for-android/tree/master/usbSerialForAndroid/src/main/java/com/hoho/android/usbserial/driver

public class SerialComunication {

    private MainActivity    main;
    private UsbManager      mUsbManager;
    private UsbSerialPort   mSerialPort = null;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            boolean deviceOpen = openSerialPort(mSerialPort.getDriver().getDevice());
                            if(!deviceOpen && mSerialPort != null){
                                main.addDebugString("UART Error re-opening device. Try to restart app");
                            }
                        }
                    }
                    else {
                        main.addDebugString("UART permission denied for device " + device);
                    }
                }
            }
        }
    };

    public SerialComunication(MainActivity m){
        main = m;
    }

    public boolean connectDevice(){
        //Find USB Serial port device
        mUsbManager = (UsbManager) main.getSystemService(USB_SERVICE);
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            for (final UsbSerialPort port : ports) {
                final UsbDevice device = driver.getDevice();
                main.addDebugString("UART Device found: "+device.getDeviceName());
                mSerialPort = port;
            }
        }
        //If Serial port found, try to open it
        if(mSerialPort != null){
            boolean deviceOpen = openSerialPort(mSerialPort.getDriver().getDevice());
            //Request permission for USB Device
            if(!deviceOpen && mSerialPort != null){
                main.addDebugString("UART Opening device failed,trying to grant permissions");
                PendingIntent mPermissionIntent;
                main.addDebugString("UART Setting PermissionIntent");
                mPermissionIntent = PendingIntent.getBroadcast(this.main, 0, new Intent(ACTION_USB_PERMISSION), 0);
                main.addDebugString("UART Setting IntentFilter");
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                main.addDebugString("Setting registerReceiver");
                main.registerReceiver(mUsbReceiver, filter);
                main.addDebugString("UART Setting requestPermission");
                mUsbManager.requestPermission(mSerialPort.getDriver().getDevice(), mPermissionIntent);
            }
        }else{
            main.addDebugString("UART No devices found");
        }
        return false;
    }

    public boolean openSerialPort(UsbDevice device){
        if(mSerialPort != null){
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection == null)return false;
            try {
                mSerialPort.open(connection);
                mSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                main.addDebugString("UART Device is open, 115200,8");
                return true;
            } catch (IOException e) {
                main.addDebugString("UART Error opening device: " + e.getMessage());
                try {
                    mSerialPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mSerialPort = null;
                return false;
            }
        }
        return false;
    }

    public void closeDevice(){
        if(mSerialPort != null){
            try {
                mSerialPort.close();
            } catch (IOException e) {
                // Ignore.
            }
        }
    }
    public void writeData(byte[] data){
        if(mSerialPort != null){
            try {
                mSerialPort.write(data, 0);
            } catch (IOException e) {
             //   Log.i("UART", "UART Write failed " + e.getMessage());
                //main.addDebugString("UART Write failed " + e.getMessage());
            }
        }else{
        //    Log.i("UART", "UART Write failed, no serial port present");
            //main.addDebugString("UART Write failed, no serial port present");
        }
    }
    //In: data[8]
    public void writeDataFrame(ChannelData data) {
    //    main.addDebugString("==================Frame===================");
        int[] serialData = new int[20];

        int counter = 0;
        serialData[counter++] = 'W';
        serialData[counter++] = 'A';
        serialData[counter++] = 'L';
        serialData[counter++] = 'L';
        serialData[counter++] = data.channels[0].value;
        serialData[counter++] = data.channels[1].value;
        serialData[counter++] = data.channels[2].value;
        serialData[counter++] = data.channels[3].value;
        serialData[counter++] = data.channels[4].value;
        serialData[counter++] = data.channels[5].value;
        serialData[counter++] = data.channels[6].value;
        serialData[counter++] = data.channels[7].value;
        serialData[counter++] = main.CRC8JNI(serialData, (counter-1));

//        for (int i=0; i<counter; i++){
//            main.addDebugString("Data:"+Integer.toString(i)+" = "+Integer.toString(serialData[i]));
//        }

        byte[] sendData = new byte[counter];
        for(int i=0; i<counter; i++){
            sendData[i] = (byte)serialData[i];
        }
        writeData(sendData);
    }
}
