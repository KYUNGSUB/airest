package com.talanton.android.airest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerInterworking {
	private static final String TAG = "Classic";
	private final String USER_AGENT = "Mozilla/5.0";
	private String mParameter;
	private Context mContext;

	public ServerInterworking(Context context) {
		mContext = context;
	}

	public interface ReportPlayStatus {
		void callbackReturnReportPlayStatus(String result);
	}

	ReportPlayStatus mPlayStatusClass;

	public void registerCallbackPlay(ReportPlayStatus callbackClass) {
		mPlayStatusClass = callbackClass;
	}

	public void reportPlayStateToServer(String urlPath, String parameter) {
		// do something here
		ReportPlayStatusTask myTask = new ReportPlayStatusTask();
		myTask.execute(urlPath, parameter);
	}

	public class ReportPlayStatusTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urlString) {
			StringBuilder output = new StringBuilder();
			try {
				URL urlValue = new URL(urlString[0]);
				HttpURLConnection conn = (HttpURLConnection)urlValue.openConnection();
				if (conn != null) {
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-type", "application/json");
					conn.setRequestProperty("User-Agent", USER_AGENT);
					conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					conn.setDoOutput(true);

					OutputStream os = conn.getOutputStream();
					BufferedWriter writer = new BufferedWriter(
							new OutputStreamWriter(os, "UTF-8"));
					writer.write(urlString[1]);
					writer.flush();
					writer.close();
					os.close();

					int resCode = conn.getResponseCode();
					if (resCode == HttpURLConnection.HTTP_OK) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
						String line = null;
						while(true) {
							line = reader.readLine();
							if (line == null) {
								break;
							}
							output.append(line);
						}

						reader.close();
						conn.disconnect();
					}
				}
			} catch(Exception ex) {
				Log.e(TAG, "Exception in processing response.", ex);
			}

			return output.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.i(TAG, "result = " + result);

			// call callback method
			mPlayStatusClass.callbackReturnReportPlayStatus(result);
		}
	}
}