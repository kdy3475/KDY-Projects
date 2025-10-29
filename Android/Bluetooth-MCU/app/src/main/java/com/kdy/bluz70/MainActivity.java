package com.kdy.bluz70;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ButtonSettings"; // SharedPreferences 파일 이름
    private static final String KEY_BUTTON_LABEL_PREFIX = "button_label_";
    private static final String KEY_BUTTON_COMMAND_PREFIX = "button_command_";

    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    EditText mTvSendData;
    ToggleButton mBtnToggle;
    Button mBtnConnect;
    Button mBtnSendData;
    Button mBtn1;
    Button mBtn2;
    Button mBtn3;
    Button mBtn4;
    Button mBtn5;
    Button mBtn6;
    Button mBtn7;
    Button mBtn8;
    Button mBtn9;
    Button mBtnUndo;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private void initializeButton(Button button, String defaultTag) {
        String buttonIdName = getResources().getResourceEntryName(button.getId());

        // 저장된 레이블 로드, 없으면 현재 버튼 텍스트 사용 (또는 기본 텍스트)
        String savedLabel = sharedPreferences.getString(KEY_BUTTON_LABEL_PREFIX + buttonIdName, button.getText().toString());
        button.setText(savedLabel);

        // 저장된 명령어 로드, 없으면 기본 태그 사용
        String savedCommand = sharedPreferences.getString(KEY_BUTTON_COMMAND_PREFIX + buttonIdName, defaultTag);
        button.setTag(savedCommand);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();   //타이틀바 숨김 처리
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); // SharedPreferences 초기화


        mTvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        mBtnToggle = findViewById(R.id.btnBluetoothToggle);
        mTvSendData = findViewById(R.id.tvSendData);
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnSendData = findViewById(R.id.btnSendData);
        mBtn1 = findViewById(R.id.button1);
        mBtn2 = findViewById(R.id.button2);
        mBtn3 = findViewById(R.id.button3);
        mBtn4 = findViewById(R.id.button4);
        mBtn5 = findViewById(R.id.button5);
        mBtn6 = findViewById(R.id.button6);
        mBtn7 = findViewById(R.id.button7);
        mBtn8 = findViewById(R.id.button8);
        mBtn9 = findViewById(R.id.button9);
        mBtnUndo = findViewById(R.id.btnUndo);

        initializeButton(mBtn1, "1");
        initializeButton(mBtn2, "2");
        initializeButton(mBtn3, "3");
        initializeButton(mBtn4, "4");
        initializeButton(mBtn5, "5");
        initializeButton(mBtn6, "6");
        initializeButton(mBtn7, "7");
        initializeButton(mBtn8, "8");
        initializeButton(mBtn9, "9");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBtnToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                bluetoothOn();
            } else {
                bluetoothOff();
            }
        });

        mBtnConnect.setOnClickListener(view -> listPairedDevices());

        mBtnSendData.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
            }
        });

        // 버튼 태그 초기화
        mBtn1.setTag("1");
        mBtn2.setTag("2");
        mBtn3.setTag("3");
        mBtn4.setTag("4");
        mBtn5.setTag("5");
        mBtn6.setTag("6");
        mBtn7.setTag("7");
        mBtn8.setTag("8");
        mBtn9.setTag("9");

        mBtn1.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn1);
            return true;
        });
        mBtn2.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn2);
            return true;
        });
        mBtn3.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn3);
            return true;
        });
        mBtn4.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn4);
            return true;
        });
        mBtn5.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn5);
            return true;
        });
        mBtn6.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn6);
            return true;
        });
        mBtn7.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn7);
            return true;
        });
        mBtn8.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn8);
            return true;
        });
        mBtn9.setOnLongClickListener(view -> {
            showEditButtonDialog(mBtn9);
            return true;
        });

        mBtn1.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn2.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn3.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn4.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn5.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn6.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn7.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn8.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });
        mBtn9.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String command = (String) view.getTag();
                if (command != null) {
                    mThreadConnectedBluetooth.write(command);
                } else {
                    mThreadConnectedBluetooth.write("");
                }
            }
        });

        mBtnUndo.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            resetButtonToDefaults(mBtn1, "1", "1", editor);
            resetButtonToDefaults(mBtn2, "2", "2", editor);
            resetButtonToDefaults(mBtn3, "3", "3", editor);
            resetButtonToDefaults(mBtn4, "4", "4", editor);
            resetButtonToDefaults(mBtn5, "5", "5", editor);
            resetButtonToDefaults(mBtn6, "6", "6", editor);
            resetButtonToDefaults(mBtn7, "7", "7", editor);
            resetButtonToDefaults(mBtn8, "8", "8", editor);
            resetButtonToDefaults(mBtn9, "9", "9", editor);

            editor.apply(); // 변경사항 일괄 적용
            Toast.makeText(MainActivity.this, "모든 버튼이 초기화되었습니다.", Toast.LENGTH_SHORT).show();
        });

        mBluetoothHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    byte[] readBuf = (byte[]) msg.obj; 
                    int numBytes = msg.arg1; 
                    String readMessage = new String(readBuf, 0, numBytes, StandardCharsets.UTF_8);
                    if (mTvReceiveData != null) {
                        mTvReceiveData.setText(readMessage);
                    }
                } else if (msg.what == BT_CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        String deviceName = (String) msg.obj;
                        mTvBluetoothStatus.setText("연결됨: " + deviceName);
                        Toast.makeText(getApplicationContext(), deviceName + "에 연결 성공", Toast.LENGTH_LONG).show();
                    } else {
                        mTvBluetoothStatus.setText("연결 실패");
                        Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    // 버튼을 기본값으로 되돌리고 SharedPreferences에서 해당 항목 삭제
    private void resetButtonToDefaults(Button button, String defaultLabel, String defaultCommand, SharedPreferences.Editor editor) {
        button.setText(defaultLabel);
        button.setTag(defaultCommand);

        String buttonIdName = getResources().getResourceEntryName(button.getId());
        editor.remove(KEY_BUTTON_LABEL_PREFIX + buttonIdName);
        editor.remove(KEY_BUTTON_COMMAND_PREFIX + buttonIdName);
    }



    private void showEditButtonDialog(final Button buttonToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_button, null);
        builder.setView(dialogView);

        final EditText editTextButtonLabel = dialogView.findViewById(R.id.editTextButtonLabel);
        final EditText editTextCommand = dialogView.findViewById(R.id.editTextCommand);

        // 현재 버튼 텍스트와 태그(명령어)를 EditText에 설정
        editTextButtonLabel.setText(buttonToEdit.getText());
        String currentCommand = (String) buttonToEdit.getTag();
        editTextCommand.setText(currentCommand != null ? currentCommand : ""); 

        builder.setTitle("버튼 편집");
        builder.setPositiveButton("저장", (dialog, which) -> {
            String newLabel = editTextButtonLabel.getText().toString();
            String newCommand = editTextCommand.getText().toString();

            if (!newLabel.isEmpty()) {
                buttonToEdit.setText(newLabel);
            }
            buttonToEdit.setTag(newCommand); // 명령어는 비어있을 수도 있음


            // SharedPreferences에 저장
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String buttonIdName = getResources().getResourceEntryName(buttonToEdit.getId());
            editor.putString(KEY_BUTTON_LABEL_PREFIX + buttonIdName, buttonToEdit.getText().toString());
            editor.putString(KEY_BUTTON_COMMAND_PREFIX + buttonIdName, newCommand);
            editor.apply(); // 비동기 저장

            Toast.makeText(MainActivity.this, "저장됨", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // The BLUETOOTH_CONNECT specific handling has been removed.
    }

    @SuppressLint("MissingPermission") 
    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth 사용 불가 기기.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth가 이미 켜짐.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("Bluetooth 켜짐");
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth가 꺼져 있음.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    @SuppressLint("MissingPermission") 
    void bluetoothOff() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth 어댑터 사용 불가!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable(); 
            Toast.makeText(getApplicationContext(), "Bluetooth 꺼짐.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("Bluetooth 꺼짐");
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth가 이미 꺼짐.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth 켜짐", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("Bluetooth 켜짐");
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("Bluetooth 꺼짐");
                mBtnToggle.setChecked(false); // 토글 버튼 상태 업데이트
            }
        }
    }

    @SuppressLint("MissingPermission") 
    void listPairedDevices() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth 어댑터 사용 불가!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices(); 

            if (mPairedDevices != null && !mPairedDevices.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<>();
                for (BluetoothDevice device : mPairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); 
                    if (deviceName != null && !deviceName.isEmpty()) {
                        mListPairedDevices.add(deviceName);
                    } else if (deviceAddress != null && !deviceAddress.isEmpty()) {
                         mListPairedDevices.add(deviceAddress); 
                    } else {
                        mListPairedDevices.add("알 수 없는 장치");
                    }
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[0]);

                builder.setItems(items, (dialog, item) -> {
                    if (item < items.length && items[item] != null) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치 없음.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth가 비활성화 되어 있음.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    void connectSelectedDevice(String selectedDeviceName) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth 어댑터 사용 불가!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPairedDevices == null) {
            Toast.makeText(getApplicationContext(), "페어링된 장치 목록 없음.", Toast.LENGTH_LONG).show();
            return;
        }
        if (selectedDeviceName == null || selectedDeviceName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "선택된 장치 이름이 유효하지 않음.", Toast.LENGTH_LONG).show();
            return;
        }

        mBluetoothDevice = null;
        for (BluetoothDevice tempDevice : mPairedDevices) {
            String deviceName = tempDevice.getName();
            String deviceAddress = tempDevice.getAddress();

            if (selectedDeviceName.equals(deviceName) || selectedDeviceName.equals(deviceAddress)) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }

        if (mBluetoothDevice == null) {
            Toast.makeText(getApplicationContext(), "선택된 장치('" + selectedDeviceName + "')를 찾을 수 없음.", Toast.LENGTH_LONG).show();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1, selectedDeviceName).sendToTarget();
            return;
        }

        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            if (mBluetoothSocket == null) {
                 Toast.makeText(getApplicationContext(), "Bluetooth 소켓 생성 실패", Toast.LENGTH_LONG).show();
                 mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1, selectedDeviceName).sendToTarget();
                return;
            }
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(MainActivity.this, mBluetoothHandler, mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            String connectedDeviceName = mBluetoothDevice.getName() != null ? mBluetoothDevice.getName() : mBluetoothDevice.getAddress();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1, connectedDeviceName).sendToTarget();

        } catch (IOException e) {
            Log.e(TAG, "Bluetooth connection failed: " + selectedDeviceName, e);
            Toast.makeText(getApplicationContext(), "Bluetooth 연결 중 오류 ("+selectedDeviceName+"): " + e.getMessage(), Toast.LENGTH_LONG).show();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1, selectedDeviceName).sendToTarget();
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                }
            } catch (IOException ex) {
                Log.e(TAG, "Could not close the client socket", ex);
            }
            mBluetoothSocket = null; 
        } catch (SecurityException se) {
             Log.e(TAG, "Bluetooth SecurityException: " + selectedDeviceName, se);
             Toast.makeText(getApplicationContext(), "Bluetooth 보안 오류 ("+selectedDeviceName+"): " + se.getMessage(), Toast.LENGTH_LONG).show();
             mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1, selectedDeviceName).sendToTarget();
        }
    }

    private static class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler mHandler;
        private final Context mContext;

        public ConnectedBluetoothThread(Context context, Handler handler, BluetoothSocket socket) {
            mContext = context;
            mHandler = handler;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error creating streams from socket", e);
                 mHandler.post(() -> Toast.makeText(mContext, "소켓 스트림 생성 중 오류 발생.", Toast.LENGTH_LONG).show());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  
            int numBytes; 

            while (true) {
                try {
                    if (mmInStream == null) {
                        Log.e(TAG, "Input stream is null, exiting thread.");
                        break;
                    }
                    numBytes = mmInStream.read(buffer); 
                    if (numBytes > 0) {
                        mHandler.obtainMessage(MainActivity.BT_MESSAGE_READ, numBytes, -1, buffer)
                                .sendToTarget();
                    } else {
                        Log.d(TAG, "Input stream read returned " + numBytes + ", possibly disconnected.");
                         mHandler.post(() -> {
                            Toast.makeText(mContext, "원격 장치와 연결이 끊어짐.", Toast.LENGTH_SHORT).show();
                            // Update Bluetooth status on UI thread
                            if (mContext instanceof MainActivity) {
                                ((MainActivity) mContext).mTvBluetoothStatus.setText("연결 끊김");
                            }
                         });
                        break; 
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                     mHandler.post(() -> {
                        Toast.makeText(mContext, "데이터 수신 중 연결 끊어짐.", Toast.LENGTH_LONG).show();
                        // Update Bluetooth status on UI thread
                        if (mContext instanceof MainActivity) {
                            ((MainActivity) mContext).mTvBluetoothStatus.setText("연결 끊김");
                        }
                     });
                    break;
                }
            }
        }

        public void write(String str) {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            try {
                if (mmOutStream == null) {
                    Log.e(TAG, "Output stream is null, cannot write.");
                     mHandler.post(() -> Toast.makeText(mContext, "데이터 전송 채널이 없음.", Toast.LENGTH_LONG).show());
                    return;
                }
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                 mHandler.post(() -> Toast.makeText(mContext, "데이터 전송 중 오류 발생.", Toast.LENGTH_LONG).show());
            }
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
                 mHandler.post(() -> Toast.makeText(mContext, "소켓 해제 중 오류 발생.", Toast.LENGTH_LONG).show());
            }
        }
    }
}
