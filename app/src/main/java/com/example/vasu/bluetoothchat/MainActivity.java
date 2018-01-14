package com.example.vasu.bluetoothchat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothChatFragment";

    private static final int REQUEST_ENABLE_BT = 3;

    private ListView lvChat;
    private EditText etMessage;
    private ImageButton mSendButton;
    private String mConnectedDeviceName = null;
    private CustomAdapterChat mConversationArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    AlertDialog alertDialog ;
    TextView tvScanDevices , tvScanTitle , tvEmpty;
    ListView lvScan ;

    ArrayList<BlueToothDeviceModel> scanDevicesList;
    ArrayList<ChatModel> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setSubtitle("");

        lvChat = (ListView) findViewById(R.id.lvChat);
        etMessage = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (ImageButton) findViewById(R.id.button_send);

        LayoutInflater myLayout = LayoutInflater.from(this);
        final View dialogView = myLayout.inflate(R.layout.custom_dialog_for_scan, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);
        alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        tvScanTitle = (TextView) dialogView.findViewById(R.id.tvScanTitle) ;
        lvScan = (ListView) dialogView.findViewById(R.id.lvScanDevices) ;
        tvScanDevices = (TextView) dialogView.findViewById(R.id.tvScanDevices) ;
        tvEmpty = (TextView) dialogView.findViewById(R.id.tvEmpty) ;

        scanDevicesList = new ArrayList<>();
        chatList = new ArrayList<>();

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }else{

        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                sendMessage(message);

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new CustomAdapterChat(chatList,MainActivity.this);

        lvChat.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }else{
            Toast.makeText(this, "Already Discoverable", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            etMessage.setText("");
        }else{
            Toast.makeText(this, "Empty Message!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            getSupportActionBar().setSubtitle("Connected to "+mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            getSupportActionBar().setSubtitle("Connecting");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            getSupportActionBar().setSubtitle("Not Connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add(new ChatModel(0,writeMessage));
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(new ChatModel(1,readMessage));
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(MainActivity.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void showDevices(){
        if (mBluetoothAdapter.isEnabled()){

            scanDevicesList.clear();

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            ensureDiscoverable();
            mBluetoothAdapter.startDiscovery();

            if (mBluetoothAdapter.isDiscovering())tvScanDevices.setEnabled(false);
            else tvScanDevices.setEnabled(true);

            tvScanTitle.setText("Scanning Devices");
            if (tvEmpty.getVisibility()==View.VISIBLE)tvEmpty.setVisibility(View.GONE);
            alertDialog.show();

            tvScanDevices.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    alertDialog.dismiss();
                    showDevices();
                }
            });

            lvScan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BlueToothDeviceModel temp = (BlueToothDeviceModel) parent.getItemAtPosition(position) ;
                    mChatService.connect(temp.getDevice(),true);
                    if (alertDialog.isShowing())alertDialog.dismiss();
                }
            });

        }else{
            Toast.makeText(this, "Bluetooth is Not Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                scanDevicesList.add(new BlueToothDeviceModel(device));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                tvScanTitle.setText("Scan Finished");
                tvScanDevices.setEnabled(true);
                if (scanDevicesList.size() == 0) {
                    Toast.makeText(context, "ZERO", Toast.LENGTH_SHORT).show();
                    if (tvEmpty.getVisibility()==View.GONE)tvEmpty.setVisibility(View.VISIBLE);
                }else{
                    CustomAdapterScan adapterScan = new CustomAdapterScan(scanDevicesList,MainActivity.this);
                    lvScan.setAdapter(adapterScan);
                }

            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.connect_scan:
                showDevices();
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
