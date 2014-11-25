package com.symbol.emdk.plugin;

import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.DeviceIdentifier;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.LabelType;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;

public class SymbolEMDKPlugin extends CordovaPlugin implements EMDKListener,
		StatusListener, DataListener {

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Declare a variable to store Barcode Manager object
	private BarcodeManager barcodeManager = null;

	// Declare a variable to hold scanner device to scan
	private Scanner scanner = null;

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		if ("scanner.softScanOn".equals(action)) {
			// The EMDKManager object will be created and returned in the
			// callback.
			EMDKResults results = EMDKManager.getEMDKManager(cordova
					.getActivity().getBaseContext(), this);
			// Check the return status of getEMDKManager and update the status
			// Text
			// View accordingly
			if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
				System.out
						.println("*************EMDKManager Request Failed****************");
			}

			System.out
					.println("*************EMDKManager Request Success****************");
		}
		return true;
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub
		// The EMDK closed abruptly. // Clean up the objects created by EMDK
		// manager
		if (this.emdkManager != null) {

			this.emdkManager.release();
			this.emdkManager = null;
		}
	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		this.emdkManager = emdkManager;

		try {
			// Call this method to enable Scanner and its listeners
			initializeScanner();
		} catch (ScannerException e) {
			e.printStackTrace();
		}

		// Toast to indicate that the user can now start scanning
		Toast.makeText(cordova.getActivity().getBaseContext(),
				"Press Hard Scan Button to start scanning...",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onData(ScanDataCollection scanDataCollection) {
		// TODO Auto-generated method stub
		// Use the scanned data, process it on background thread using AsyncTask
		// and update the UI thread with the scanned results
		new AsyncDataUpdate().execute(scanDataCollection);

	}

	// Update the scan data on UI
	int dataLength = 0;

	// AsyncTask that configures the scanned data on background
	// thread and updated the result on UI thread with scanned data and type of
	// label
	private class AsyncDataUpdate extends
			AsyncTask<ScanDataCollection, Void, String> {

		@Override
		protected String doInBackground(ScanDataCollection... params) {

			// Status string that contains both barcode data and type of barcode
			// that is being scanned
			String statusStr = "";

			try {

				// Starts an asynchronous Scan. The method will not turn ON the
				// scanner. It will, however, put the scanner in a state in
				// which
				// the scanner can be turned ON either by pressing a hardware
				// trigger or can be turned ON automatically.
				scanner.read();

				ScanDataCollection scanDataCollection = params[0];

				// The ScanDataCollection object gives scanning result and the
				// collection of ScanData. So check the data and its status
				if (scanDataCollection != null
						&& scanDataCollection.getResult() == ScannerResults.SUCCESS) {

					ArrayList<ScanData> scanData = scanDataCollection
							.getScanData();

					// Iterate through scanned data and prepare the statusStr
					for (ScanData data : scanData) {
						// Get the scanned data
						String barcodeDate = data.getData();
						// Get the type of label being scanned
						LabelType labelType = data.getLabelType();
						// Concatenate barcode data and label type
						statusStr = barcodeDate + " " + labelType;
					}
				}

			} catch (ScannerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Return result to populate on UI thread
			return statusStr;
		}

		@Override
		protected void onPostExecute(String result) {
			// Update the dataView EditText on UI thread with barcode data and
			// its label type

			System.out.println("*********Barcode Data***************" + result);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	@Override
	public void onStatus(StatusData statusData) {
		// TODO Auto-generated method stub
		// process the scan status event on the background thread using
		// AsyncTask and update the UI thread with current scanner state
		new AsyncStatusUpdate().execute(statusData);

	}

	// AsyncTask that configures the current state of scanner on background
	// thread and updates the result on UI thread
	private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {

		@Override
		protected String doInBackground(StatusData... params) {
			String statusStr = "";
			// Get the current state of scanner in background
			StatusData statusData = params[0];
			ScannerStates state = statusData.getState();
			// Different states of Scanner
			switch (state) {
			// Scanner is IDLE
			case IDLE:
				statusStr = "The scanner enabled and its idle";
				break;
			// Scanner is SCANNING
			case SCANNING:
				statusStr = "Scanning..";
				break;
			// Scanner is waiting for trigger press
			case WAITING:
				statusStr = "Waiting for trigger press..";
				break;
			// Scanner is not enabled
			case DISABLED:
				statusStr = "Scanner is not enabled";
				break;
			default:
				break;
			}

			// Return result to populate on UI thread
			return statusStr;
		}

		@Override
		protected void onPostExecute(String result) {
			// Update the status text view on UI thread with current scanner
			// state
			System.out.println("****************Barcode Status***************"
					+ result);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if (scanner != null) {
				// releases the scanner hardware resources for other application
				// to use. You must call this as soon as you're done with the
				// scanning.
				scanner.disable();
				scanner = null;
			}

			if (emdkManager != null) {

				// Clean up the objects created by EMDK manager
				emdkManager.release();
				emdkManager = null;
			}

		} catch (ScannerException e) {
			e.printStackTrace();
		}
	}

	// Method to initialize and enable Scanner and its listeners
	private void initializeScanner() throws ScannerException {

		if (scanner == null) {

			// Get the Barcode Manager object
			barcodeManager = (BarcodeManager) this.emdkManager
					.getInstance(FEATURE_TYPE.BARCODE);

			// Get default scanner defined on the device
			scanner = barcodeManager.getDevice(DeviceIdentifier.DEFAULT);

			// Add data and status listeners
			scanner.addDataListener(this);
			scanner.addStatusListener(this);

			// Hard trigger. When this mode is set, the user has to manually
			// press the trigger on the device after issuing the read call.
			scanner.triggerType = TriggerType.HARD;

			// Enable the scanner
			scanner.enable();

			// Starts an asynchronous Scan. The method will not turn ON the
			// scanner. It will, however, put the scanner in a state in which
			// the scanner can be turned ON either by pressing a hardware
			// trigger or can be turned ON automatically.
			scanner.read();
		}
	}

}