package twilight.of.the.devs.toury;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

import twilight.of.the.devs.mylibrary.Marker;
import twilight.of.the.devs.mylibrary.SimpleGeofence;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectThread extends AsyncTask<List<Marker>, Integer, Void> {

	private static final String TAG = ConnectThread.class.getName();
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothSocket mmSocket;
	private boolean mSingle;
	
	public ConnectThread(BluetoothAdapter adapter, BluetoothDevice device, boolean single) {
		mBluetoothAdapter = adapter;
		mBluetoothDevice = device;
		mSingle = single;
	}
	
	@Override
	protected Void doInBackground(List<Marker>... marker) {

		UUID MY_UUID = UUID.fromString("2166f331-7ff9-4f32-802a-77cf52af027e");
		// Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            mmSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        
        mBluetoothAdapter.cancelDiscovery();

        Log.d("Glass", "Connecting to socket...");
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
        	Log.d(TAG, "Unable to connect: " + connectException.getMessage());
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { 
            	Log.d(TAG, closeException.getMessage());
            }
            return null;
        }
        Log.d("Glass", "Sending: " + marker[0]);
        // Do work to manage the connection (in a separate thread)
        OutputStream os = null;
        PrintWriter pw = null;
        ObjectOutputStream oos = null;
        try {
        	oos = new ObjectOutputStream(mmSocket.getOutputStream());
        	if(!mSingle)
        		oos.writeObject(marker[0]);
        	else
        		oos.writeObject(marker[0].get(0));
        	oos.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

        try {
            mmSocket.close();
        } catch (IOException e) { }
        
        return null;
	}

}
