package com.buildcircuit.led;

import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DeviceController extends Activity{
	private BluetoothAdapter mAdapter = null;
	private String CURRENT_DEVICE = null;
	private BluetoothDevice device = null;
	private static UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static BluetoothSocket mSocket = null;
	//private static InputStream socketIn = null;
	private static OutputStream socketOut = null;
    public Intent intent = new Intent();
    
	public void onCreate(Bundle savedInstanceState) {
		setResult(2,intent);
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		Bundle extras = getIntent().getExtras();
		CURRENT_DEVICE = extras.getString("device");
		device = mAdapter.getRemoteDevice(CURRENT_DEVICE);
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.control);
	    TextView mainTitle = (TextView)findViewById(R.id.control_title);
	    mainTitle.setText("Device connected: "+device.getName()+"("+device.getAddress()+")");
	    final SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
	    seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String seekValue = ""+(int)(seekbar.getProgress()*0.09);
        		String data= seekValue;
            	byte[] buffer = null;
            	try {
            		buffer = data.getBytes("ISO-8859-1");
    			} catch (UnsupportedEncodingException e) {
    				buffer = data.getBytes();
    			}
        	    try {
                	socketOut.write(buffer);
                	TextView brightness = (TextView)findViewById(R.id.brightness);
                	brightness.setText(getString(R.string.brightness)+seekbar.getProgress());
        		} catch (IOException e) {
        			intent.putExtra("msg", "Connection lost!!");
        			setResult(1,intent);
        			finish();
        		}
			}
			public void onStartTrackingTouch(SeekBar seekBar){}
			public void onStopTrackingTouch(SeekBar seekBar){}
	    	
	    });

		try {
			mSocket = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
		} catch (IOException e) {
			intent.putExtra("msg", "Cannot create the communication socket with the device");
			setResult(1,intent);
			finish();
		}
		try {
			//socketIn = mSocket.getInputStream();
			socketOut = mSocket.getOutputStream();
		} catch (IOException e) {
			intent.putExtra("msg", "Cannot create the input and output communication socket with the device");
			setResult(1,intent);
			finish();
		}
		try {
			mSocket.connect();
		} catch (IOException e) {
			intent.putExtra("msg", "Cannot connect with the device");
			try {
	            mSocket.close();
	        } catch (IOException e2) {
	        	intent.putExtra("msg", "Cannot close the socket during connection failure");
	        }
			setResult(1,intent);
			finish();
		}
/*		while(true){
			int deviceReply = 0;
			try {
				deviceReply = socketIn.read();
			} catch (IOException e) {
				intent.putExtra("msg", "Connection lost!!");
    			setResult(1,intent);
    			finish();
    			break;
			}
			Toast.makeText(this, "Device Replied: "+deviceReply, Toast.LENGTH_SHORT).show();
		}
*/
	}

}
