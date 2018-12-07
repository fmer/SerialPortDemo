package com.serial.port;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Handler mAsynHandler;
    private EditText mRtDevice;
    private Button mBtQueryDevices;
    private EditText mRtRate;
    private Button mBtQueryRate;
    private Button mBtSendBuff;
    private Button mBtGoConsole;
    private Button mBtGoLoop;
    private HandlerThread mHandlerThread;
    private byte[] mBuffer;
    private EditText mRtBuff;
    private TextView mTvDesc;
    private View mBottomView;
    private AlertDialog mParkIdsdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "currentTimeMillis: " + System.currentTimeMillis());

        initView();
        initThread();
        mBuffer = new byte[1024];
        Arrays.fill(mBuffer, (byte) 0x55);
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        Message obtain = Message.obtain();
        obtain.what = 1;
        obtain.arg1 = size;
        obtain.obj = buffer;
        mUiHandler.sendMessage(obtain);
    }

    private void initThread() {
        mHandlerThread = new HandlerThread("SerialPort");
        mHandlerThread.start();
        mAsynHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAsynHandler != null)
            mAsynHandler.removeCallbacksAndMessages(null);
        if (mHandlerThread != null)
            mHandlerThread.interrupt();
        mHandlerThread = null;
    }

    private final MainHandler mUiHandler = new MainHandler(this);

    private static class MainHandler extends UIHandler<MainActivity> {
        MainHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = ref.get();
            activity.mTvDesc.setText("");
            activity.mTvDesc.setText("Size:" + msg.arg1 + " \n buff: " + bytesToHexString((byte[]) msg.obj));
        }
    }


    private void initView() {
        mRtDevice = (EditText) findViewById(R.id.rt_device);
        mRtBuff = (EditText) findViewById(R.id.et_buff);
        mBtQueryDevices = (Button) findViewById(R.id.bt_query_devices);
        mRtRate = (EditText) findViewById(R.id.rt_rate);
        mBtQueryRate = (Button) findViewById(R.id.bt_query_rate);
        mBtSendBuff = (Button) findViewById(R.id.bt_send_buff);
        mBtGoConsole = (Button) findViewById(R.id.bt_go_console);
        mBtGoLoop = (Button) findViewById(R.id.bt_go_loop);
        mTvDesc = (TextView) findViewById(R.id.tv_desc);
        mBtGoLoop.setOnClickListener(this);
        mBtGoConsole.setOnClickListener(this);
        mBtSendBuff.setOnClickListener(this);
        mBtQueryRate.setOnClickListener(this);
        mBtQueryDevices.setOnClickListener(this);
        //填充ListView布局
        mBottomView = View.inflate(this, R.layout.carids_dialog, null);
        ListView lvCarIds = (ListView) mBottomView.findViewById(R.id.lv_carids);//初始化ListView控件
        lvCarIds.setAdapter(new LvCarIdsDailogAdapter(this));//ListView设置适配器
    }

    boolean mSendBuff = false;
    boolean initSp;

    @Override
    protected boolean initSP() {
        if (TextUtils.isEmpty(mRtDevice.getText().toString())) {
            toast("请输入Device地址");
            Log.d(TAG, "请输入Device地址 ");
            return false;
        }
        mApplication.setDevice(mRtDevice.getText().toString().trim());
        if (TextUtils.isEmpty(mRtRate.getText().toString())) {
            toast("请输入比特率");
            Log.d(TAG, "请输入比特率 ");
            return false;
        }
        mApplication.setRate(Integer.parseInt(mRtRate.getText().toString().trim()));
        return super.initSP();
    }

    public void showError(int resourceId) {
        Toast.makeText(this, getString(resourceId), Toast.LENGTH_SHORT).show();
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private Dialog dialog;
    private List<Map<String, String>> devicesList;//获取数据

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_query_devices:
                Log.d(TAG, "bt_query_devices ");
                final String[] devicesList = mApplication.getDevicesList();
                ListDialog.Builder dialogBuilder = new ListDialog.Builder(
                        MainActivity.this).setTitle("Devices").setList(Arrays.asList(devicesList),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Log.d(TAG, "bt_query_devices :" + devicesList[arg1]);
                                dialog.dismiss();
                            }
                        });
                dialog = dialogBuilder.create();
                dialog.show();
                break;
            case R.id.bt_query_rate:
                Log.d(TAG, "bt_query_rate ");
                break;
            case R.id.bt_send_buff:
                initSp = initSP();
                Log.d(TAG, "bt_send_buff " + initSp);
                if (!initSp) return;
                mSendBuff = !mSendBuff;
                if (!mSendBuff) {
                    mBtSendBuff.setText("start send 010101...");
                } else {
                    mBtSendBuff.setText("stop send 010101...");
                    if (mSerialPort != null) {
                        mAsynHandler.post(sendBuff);
                    }
                }
                break;
            case R.id.bt_go_console:
                initSp = initSP();
                Log.d(TAG, "bt_go_console " + initSp);
                if (!initSp) return;
                if (TextUtils.isEmpty(mRtBuff.getText().toString())) {
                    toast("请输入发送值");
                    return;
                }
                final String t = mRtBuff.getText().toString();
                mAsynHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        char[] text = new char[t.length()];
                        for (int i = 0; i < t.length(); i++) {
                            text[i] = t.charAt(i);
                        }
                        try {
                            mOutputStream.write(new String(text).getBytes());
                            mOutputStream.write('\n');
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_go_loop:
                initSp = initSP();
                Log.d(TAG, "bt_go_loop " + initSp);
                if (!initSp) return;
                Intent go = new Intent(MainActivity.this, ConfActivity.class);
                MainActivity.this.startActivity(go);
                break;
        }
    }

    Runnable sendBuff = new Runnable() {

        @Override
        public void run() {
            while (mSendBuff) {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(mBuffer);
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    };

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}

