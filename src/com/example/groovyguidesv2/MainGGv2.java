package com.example.groovyguidesv2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainGGv2 extends Activity {

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private Button startBtn;
	private ProgressDialog mProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_ggv2);
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDownload();
			}
		});
	}

	private void startDownload() {
		String url = "https://dl.dropboxusercontent.com/s/vs1oz5lsmw5dmcr/cannedtts.zip";
		System.out.println("EXECUTED: startDownload");
		new DownloadFileAsync().execute(url);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Downloading file ...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			System.out.println("EXECUTED: onPreExecute");
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		/**
		 * (1) Download the ZIP.
		 * (2) Unzip the file via "unzip()"
		 */
		@Override
		protected String doInBackground(String... aurl) {
			int count;
			System.out.println("EXECUTED: doInBackground");

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lengthOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lengthOfFile);
				System.out.println("EXECUTED: 1 -- " + lengthOfFile);

				/**
				 * Groundwork for "cannedtts" folder. Create the folder if
				 * necessary.
				 **/
				String testdataDir = Environment.getExternalStorageDirectory()
						+ "/Android/data/com.google.android.apps.maps/testdata/";
				String cannedttsDir = Environment.getExternalStorageDirectory()
						+ "/Android/data/com.google.android.apps.maps/testdata/cannedtts/";
				File folder = new File(cannedttsDir);
				folder.mkdirs();

				System.out.println("EXECUTED: 2");

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(testdataDir
						+ "cannedtts.zip");

				System.out.println("EXECUTED: 3");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lengthOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
				System.out.println("EXECUTED: 4 -- ");
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
			
			unzip();
			
			return null;

		}
		
		public void unzip() {
			try {
				String testdataDir = Environment.getExternalStorageDirectory()
						+ "/Android/data/com.google.android.apps.maps/testdata/";
				String cannedttsDir = Environment.getExternalStorageDirectory()
						+ "/Android/data/com.google.android.apps.maps/testdata/cannedtts/";

				FileInputStream fin = new FileInputStream(testdataDir + "cannedtts.zip");
				ZipInputStream zin = new ZipInputStream(fin);
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					Log.v("Decompress", "Unzipping " + ze.getName());

					if (ze.isDirectory()) {
						File f = new File(cannedttsDir + ze.getName()); 
					    if(!f.isDirectory()) 
					      f.mkdirs(); 
					      
					} else {
						FileOutputStream fout = new FileOutputStream(cannedttsDir 
								+ ze.getName());
						for (int c = zin.read(); c != -1; c = zin.read())
							fout.write(c);

						zin.closeEntry();
						fout.close();
					}
				}
				zin.close();
			} catch (Exception e) {
				Log.e("Decompress", "unzip", e);
			}
		}

		protected void onProgressUpdate(String... progress) {
			Log.d("ANDRO_ASYNC", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		@SuppressWarnings("deprecation")
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
		}
	}

	
}