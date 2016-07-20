package com.shutup.kcptun_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements Constants{

    private static final String TAG = "MainActivity";
    private String kcptun = "kcp_tun";
    private String binary_path = null;
    private MyHandler mMyHandler = null;

    @InjectView(R.id.info)
    TextView mInfo;
    @InjectView(R.id.settingBtn)
    Button mSettingBtn;
    @InjectView(R.id.startBtn)
    Button mStartBtn;
    private CmdParam cmdParam = null;
    private Process process = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mMyHandler = new MyHandler(Looper.getMainLooper(), new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                mInfo.append((CharSequence) msg.obj);
                mInfo.append("\n");
                return true;
            }
        });

        tryToStart();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
    }

    private void tryToStart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cmdParam = new CmdParam();
        cmdParam.localaddr = ":" + sharedPreferences.getString(LocalServerPort, "");
        cmdParam.remoteaddr = sharedPreferences.getString(RemoteServerIp, "").equalsIgnoreCase("") ? "": sharedPreferences.getString(RemoteServerIp, "") + ":" + sharedPreferences.getString(RemoteServerPort,"");
        cmdParam.key = sharedPreferences.getString(ServerKey, "");
        cmdParam.mode = sharedPreferences.getString(ServerMode, "");
        if (cmdParam.isBasicOk()){
            handleStartBtnClick();
        }else {
            cmdParam = null;
            if (BuildConfig.DEBUG) Log.d(TAG, "need more info ");
        }
    }

    @OnClick({R.id.settingBtn, R.id.startBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settingBtn:
                handleSettingBtnClick();
                break;
            case R.id.startBtn:
                handleStartBtnClick();
                break;
        }
    }

    private void handleSettingBtnClick() {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
    }

    private void handleStartBtnClick() {
        if (mStartBtn.getText().toString().equalsIgnoreCase(getString(R.string.start))){
            mInfo.setText("");
            mStartBtn.setText(R.string.stop);
            mSettingBtn.setEnabled(false);
            String arch = System.getProperty("os.arch");
            mInfo.append(arch);
            mInfo.append("\n");
            int identifier = detectCpuArchInfo();
            binary_path = installBinary(this, identifier, kcptun);
            mInfo.append(binary_path);
            mInfo.append("\n");
            if (BuildConfig.DEBUG) Log.d(TAG, binary_path);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    runCmdLine(new ShellCallback() {
                        @Override
                        public void shellOut(String shellLine) {
                            Message message = mMyHandler.obtainMessage();
                            message.obj = shellLine;
                            message.what = 1;
                            mMyHandler.sendMessage(message);
                        }

                        @Override
                        public void processComplete(int exitValue) {

                        }
                    });
                }
            }).start();
        }else if (mStartBtn.getText().toString().equalsIgnoreCase(getString(R.string.stop))) {
            killTheKcptun();
            mStartBtn.setText(R.string.start);
            mSettingBtn.setEnabled(true);
        }



    }

    /**
     * 根据CPU指令集的不同，返回不同的可执行程序ID
     *
     * @return
     */
    private int detectCpuArchInfo() {
        String arch = System.getProperty("os.arch");
        int identifierId = 0;
        if (arch.contains("arm")) {
            if (arch.contains("v7")) {
                identifierId = getResources().getIdentifier("client_linux_arm7", "raw", getPackageName());
            } else if (arch.contains("v6")) {
                identifierId = getResources().getIdentifier("client_linux_arm6", "raw", getPackageName());
            } else if (arch.contains("v5")) {
                identifierId = getResources().getIdentifier("client_linux_arm5", "raw", getPackageName());
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "identifierId:" + identifierId);
        return identifierId;
    }

    /**
     * 安装可执行程序
     *
     * @param ctx
     * @param resId
     * @param filename
     * @return
     */
    private static String installBinary(Context ctx, int resId, String filename) {
        try {
            File f = new File(ctx.getDir("bin", 0), filename);
            if (f.exists()) {

            } else {
                copyRawFile(ctx, resId, f, "0755");
            }
            return f.getCanonicalPath();
        } catch (Exception e) {
            Log.e(TAG, "installBinary failed: " + e.getLocalizedMessage());
            return null;
        }
    }

    private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException {
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = ctx.getResources().openRawResource(resid);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }

    /**
     * 执行指令
     *
     * @param sc
     * @return
     */
    private int runCmdLine(ShellCallback sc) {
        // Executes the command.
        if (cmdParam == null){
//            Toast.makeText(this, "Please Fill The Setting First", Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG) Log.d(TAG, "Please Fill The Setting First");
            return -1;
        }
        final String cmd = setup_cmd(binary_path, cmdParam);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInfo.append(cmd);
                mInfo.append("\n");
            }
        });

        process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(
                process.getErrorStream(), "ERROR", sc);

        // any output?
        StreamGobbler outputGobbler = new
                StreamGobbler(process.getInputStream(), "OUTPUT", sc);

        errorGobbler.start();
        outputGobbler.start();

        int exitVal = 0;
        try {
            exitVal = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sc.processComplete(exitVal);

        return exitVal;
    }

    private String setup_cmd(String binary_path, CmdParam cmdParam) {
        ArrayList<String> params = new ArrayList<>();
        if (cmdParam.localaddr != null) {
            params.add(CmdParam.LOCALADDR);
            params.add(cmdParam.localaddr);
        }

        if (cmdParam.remoteaddr != null) {
            params.add(CmdParam.REMOTEADDR);
            params.add(cmdParam.remoteaddr);
        }

        if (cmdParam.key != null) {
            params.add(CmdParam.KEY);
            params.add(cmdParam.key);
        }

        if (cmdParam.mode != null) {
            params.add(CmdParam.MODE);
            params.add(cmdParam.mode);
        }
        StringBuilder stringBuilder = new StringBuilder(binary_path);
        for (String param : params) {
            stringBuilder.append(' ');
            stringBuilder.append(param);
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }


    private void killTheKcptun(){
        if (process != null){
            process.destroy();
            try {
                int retVal = process.waitFor();
                if (BuildConfig.DEBUG) Log.d(TAG, "retVal:" + retVal);
                if (retVal == 9) {
                    mInfo.setText(R.string.stop_kcp_tun_normal);
                }else{
                    mInfo.setText(R.string.stop_kcp_tun_exception);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }
    }
}
