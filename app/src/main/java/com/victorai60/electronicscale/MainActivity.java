package com.victorai60.electronicscale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onNewData(byte[] data) {
            if (data.length == 22) {
                System.out.println(bytesToHexStr(data));
            }
        }

        @Override
        public void onRunError(Exception e) {

        }
    };

    public void onClick(View v) {
        action();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void action() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            getPermission(manager, driver.getDevice());
            return;
        }
        // Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setRTS(false);
            port.setDTR(false);
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
//            byte buffer[] = new byte[22];
//            int numBytesRead = port.read(buffer, 1000);
//            Log.d(TAG, "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {
            // Deal with error.
            try {
                port.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        SerialInputOutputManager serialInputOutputManager = new SerialInputOutputManager(port, mListener);
        mExecutor.submit(serialInputOutputManager);
    }

    private void getPermission(UsbManager usbManager, UsbDevice usbDevice) {
        if (usbManager.hasPermission(usbDevice)) {
            Log.d(TAG, "USB已经授权");
        } else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.application, 0, new Intent(), 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    public static String bytesToHexStr(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        String temp;
        for (byte b : data) {
            temp = Integer.toHexString(0xFF & b);
            if (temp.length() < 2) {
                sb.append(0);
            }
            sb.append(temp.toUpperCase());
        }
        return sb.toString();
    }
}
