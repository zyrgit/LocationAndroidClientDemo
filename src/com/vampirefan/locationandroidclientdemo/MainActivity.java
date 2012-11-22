package com.vampirefan.locationandroidclientdemo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vampirefan.locationandroidclientdemo.R;
import com.vampirefan.locationandroidclientdemo.StringUtils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private Button buttonConnect;
	private Button buttonLogin;
	private Button buttonFinger;
	private Button buttonLocate;

	private EditText editTextUrl;
	private TextView textViewStatus;

	// The Message Window:
	private ScrollView svResult;
	private TextView textViewMessage;

	// use a handler to update the UI (send the handler messages from other
	// threads)
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			String bundleResult = msg.getData().getString("RESPONSE");
			textViewMessage.append("\n" + bundleResult);
			svResult.post(new Runnable() {
				public void run() {
					svResult.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.editTextUrl = (EditText) findViewById(R.id.editTextUrl);
		this.textViewStatus = (TextView) findViewById(R.id.textViewStatus);

		this.svResult = (ScrollView) findViewById(R.id.svResult);
		this.textViewMessage = (TextView) findViewById(R.id.textViewMessage);

		this.buttonConnect = (Button) findViewById(R.id.buttonConnect);
		this.buttonConnect.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				String url = "http://" + editTextUrl.getText().toString() + "/start";
				String body = "";
				performPost(url, body);
			}
		});

		this.buttonLogin = (Button) findViewById(R.id.ButtonLogin);
		this.buttonLogin.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				String url = "http://" + editTextUrl.getText().toString() + "/login";
				String body = "";
				performPost(url, body);
			}
		});

		this.buttonFinger = (Button) findViewById(R.id.ButtonFinger);
		this.buttonFinger.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				String url = "http://" + editTextUrl.getText().toString() + "/finger";
				String body = "";
				try {
					JSONObject fingerFrame = new JSONObject();
					JSONArray wapInfo = new JSONArray();
					fingerFrame.put("bearing", "0");
					fingerFrame.put("locationId", "X62010010117");
					fingerFrame.put("wapInfo", wapInfo);
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BC").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BD").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BE").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BF").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BG").put("rssid", "56"));
					body = fingerFrame.toString();
				} catch (Exception excep) {
					returnException(excep);
				}
				performPost(url, body);
			}
		});

		this.buttonLocate = (Button) findViewById(R.id.ButtonLocate);
		this.buttonLocate.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				String url = "http://" + editTextUrl.getText().toString() + "/locate";
				String body = "";
				try {
					JSONObject locateFrame = new JSONObject();
					JSONArray wapInfo = new JSONArray();
					locateFrame.put("bearing", "0");
					locateFrame.put("wapInfo", wapInfo);
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BC").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BD").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BE").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BF").put("rssid", "56"));
					wapInfo.put(new JSONObject().put("bssid", "00602F3A07BG").put("rssid", "56"));
					body = locateFrame.toString();
				} catch (Exception excep) {	
					returnException(excep);
				}
				performPost(url, body);
			}
		});
	}
	
	private void returnException(final Exception excep){
		excep.getStackTrace().toString();		
		String result = excep.getMessage();
		Message message = handler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString("RESPONSE", result);
		message.setData(bundle);
		handler.sendMessage(message);
	}

	private void performPost(final String url, final String body) {

		// use a response handler so we aren't blocking on the HTTP request
		final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response) {
				// when the response happens close the notification and update
				// UI
				HttpEntity entity = response.getEntity();
				String result = null;
				try {
					result = StringUtils.inputStreamToString(entity.getContent());
					Message message = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("RESPONSE", result);
					message.setData(bundle);
					handler.sendMessage(message);
				} catch (Exception excep) {
					returnException(excep);
				}
				return result;
			}
		};

		// do the HTTP dance in a separate thread (the responseHandler will fire
		// when complete)
		new Thread() {

			@Override
			public void run() {

				try {
					DefaultHttpClient client = new DefaultHttpClient();
					HttpPost httpMethod = new HttpPost(url);

					httpMethod.setEntity(new ByteArrayEntity(body.getBytes()));
					client.execute(httpMethod, responseHandler);
				} catch (Exception excep) {
					returnException(excep);
				}
			}
		}.start();
	}

	@Override
	public void onStart() {
		super.onStart();

		ConnectivityManager cMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		textViewStatus.setText("\n" + netInfo.toString() + "\n");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
