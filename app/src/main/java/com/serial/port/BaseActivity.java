package com.serial.port;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public abstract class BaseActivity extends Activity {
    protected Application mApplication;
    protected SerialPortFinder mSerialPortFinder;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    protected InputStream mInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mConfigureParams = false;
        mApplication = (Application) getApplication();
        mSerialPortFinder = mApplication.mSerialPortFinder;
    }

    boolean mConfigureParams = false;

    protected boolean initSP() {
        Log.d("MainActivity", "initSP " + mConfigureParams);
        if (!mConfigureParams && true) {
            try {
                mSerialPort = mApplication.getSerialPort();

                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();
            } catch (SecurityException e) {
                showError(R.string.error_security);
                return false;
            } catch (IOException e) {
                showError(R.string.error_unknown);
                return false;
            } catch (InvalidParameterException e) {
                showError(R.string.error_configuration);
                return false;
            }

            /* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
            mConfigureParams = true;
        }
        return true;
    }

    public void showError(int resourceId) {
    }

    public void toast(String msg) {
    }

    @Override
    protected void onDestroy() {
        mConfigureParams = false;
        if (mReadThread != null)
            mReadThread.interrupt();
        mApplication.closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private ReadThread mReadThread;

    protected abstract void onDataReceived(final byte[] buffer, final int size);


}
