package com.buildcircuit.led;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BuildcircuitActivity extends Activity{
	
	private BluetoothAdapter mAdapter = null;
	
	private static final int REQUEST_ENABLE_BT = 99;
	
	private static final int DEVICE_CONNECTION = 0;
	
	boolean no_device = true;
	
	boolean exit_app = false;
	
	private ListView dev_list = null;
	
	private ArrayAdapter<String> mArrayAdapter = null;
	
	public ProgressDialog pd = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);		
	    
	    final Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initBluetooth();
            }
        });
        if(mAdapter == null){
    		exit_app = true;
    		alertDialog(getString(R.string.no_bluetooth_adapter));
    	}
		if (!mAdapter.isEnabled()) {
            Intent startBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(startBluetooth, REQUEST_ENABLE_BT);
        }
		else{
			initBluetooth();
		}
    }
	
	public void initBluetooth(){
		no_device = true;
		mArrayAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			no_device = false;
			for (BluetoothDevice device : pairedDevices) {
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
			initSearch();
		}
		else{
			initSearch();
		}
		
	}
	
	public void initSearch(){
		// placeholder to search for new bluetooth devices
		if(no_device){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You don't have any paired device\n\nPair the device that you want to use from next screen")
			       .setTitle(R.string.app_name)
			       .setCancelable(false)
			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
				   			startActivityForResult(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS), 0);
			           }
			       })
			       .setNegativeButton("Exit App",new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
					   			finish();
				           }
				       });
			AlertDialog alert = builder.create();
			alert.show();
		}
		else{
			updateView();
		}
	}
	
	public void updateView(){
		if(no_device){
			exit_app = true;
			alertDialog("Device list is empty, try again later");
		}
		else{
			dev_list = (ListView)findViewById(R.id.device_list);
		    dev_list.setAdapter(mArrayAdapter);
		    dev_list.setOnItemClickListener(new ListView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int i, long l) {
					pd = ProgressDialog.show(v.getContext(), "Please wait", "Connecting...");
					String deviceString = ((TextView)v).getText().toString();
					String[] address = deviceString.split("\n");
					Intent intent = new Intent(v.getContext(),DeviceController.class);
					intent.putExtra("device", address[1]);
					startActivityForResult(intent,DEVICE_CONNECTION);
				}
			});

		}
	}
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
			case (REQUEST_ENABLE_BT) :
				if (resultCode != Activity.RESULT_OK) { 
					exit_app = true;
					alertDialog("You have to enable bluetooth to use this application");
				}
				else{
					initBluetooth();
				}
				break;
			case (DEVICE_CONNECTION):
				if (resultCode == 1) {
	                String msg = data.getExtras().getString("msg");
					alertDialog(msg);
					initBluetooth();
				}
				if(pd.isShowing()){
					pd.dismiss();
				}
				break;
			default:
				initBluetooth();
				break;
		} 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.connect:
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Pair the device that you want to use from next screen")
				       .setTitle(R.string.app_name)
				       .setCancelable(false)
				       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
					   			startActivityForResult(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS), 0);
				           }
				       });				       
				AlertDialog alert = builder.create();
				alert.show();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	public void alertDialog(String msg){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setTitle(R.string.app_name)
		       .setCancelable(false)
		       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   if(exit_app){
		        		   finish();
		        	   }
		        	   else{
		        		   dialog.cancel();
		        	   }
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
}
