package fubic.com;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends ActionBarActivity
implements
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,
OnMyLocationButtonClickListener,
OnClickListener{

	TextView textview;
	Location location;
	ParseObject GpsScore = new ParseObject("GpsScore");
	SharedPreferences pref;
	Timer timer = new Timer(false);
	TaskForLocation taskLocate;

	boolean initFlag = false;
	public String objId;
	public double latitude = 0;
	public double longitude = 0;
	public List<ParseObject> results;
	public GoogleMap mMap;
	MarkerOptions map;
	private LocationClient mLocationClient;
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview = (TextView)findViewById(R.id.textView1);

		Button btn = (Button)findViewById(R.id.get_my_location_button);
		btn.setOnClickListener(this);

		Button btn2 = (Button)findViewById(R.id.Button02);
		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = getIntent();
				finish();
				startActivity(intent);
			}
		});

		pref = this.getSharedPreferences( "Parse_ID", Context.MODE_PRIVATE );
		GpsScore = new ParseObject("GpsScore");
		initFlag = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
		moveInit();
		locateUpdate();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onStop(){
		super.onStop();
		try{
			taskLocate.cancel();
			taskLocate = null;
			TimeUnit.SECONDS.sleep(10);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
				mMap.setOnMyLocationButtonClickListener(this);
			}
		}
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(
					getApplicationContext(),
					this,  // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	/**
	 * マーカーの生成
	 */
	public void makePoint() {
		// TODO 自動生成されたメソッド・スタブ
		ParseQuery<ParseObject> q = ParseQuery.getQuery("GpsScore");
		q.orderByDescending("_created_at");
		try {
			results = q.find();
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if (results != null) {
			for (ParseObject otherGps : results) {
				double lat = otherGps.getDouble("latitude");
				double lng = otherGps.getDouble("longitude");
				map = new MarkerOptions();
				map.position(new LatLng(lat,lng));
				map.title("吹き出しタイトル");
				map.snippet("スニペット");
				BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.tanukiicon);
				map.icon(icon);
				mMap.addMarker(map);

			}
		}
	}

	/**
	 * 初期画面の設定
	 */
	public void moveInit(){

		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(getLatitude(), getLongitude()),15);
		mMap.moveCamera(cu);
	}

	/**
	 * 更新タスク
	 */
	public void locateUpdate(){
		if(taskLocate == null){
			taskLocate = new TaskForLocation(this,MainActivity.this);
		}
		timer.scheduleAtFixedRate(taskLocate, 1000, 30000);
	}

	/**
	 * menu action bar
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * OptionItem
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when the my location button is clicked.
	 */
	@Override
	public boolean onMyLocationButtonClick() {
		// TODO 自動生成されたメソッド・スタブ
		Toast.makeText(this, "現在の位置に移動しろう！", Toast.LENGTH_SHORT).show();

		return false;
	}

	/**
	 *
	 * Parameters:　location	The new location, as a Location object.
	 *
	 * LocationListenerインターフェースで用意されているメソッド.
	 * 位置情報が変更された場合に呼び出される.
	 *
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO 自動生成されたメソッド・スタブ
		setLatitude(location.getLatitude());
		setLongitude(location.getLongitude());

		if(getLatitude() != 0 && getLongitude() != 0 && initFlag == true){
			moveInit();
			initFlag =false;
		}
	}

	/**
	 * objId ,Latitude and Longitude's getter setter.
	 * @return Latitude Longitude.
	 */
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO 自動生成されたメソッド・スタブ
	}

	/**
	 * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(
				REQUEST,
				this);  // LocationListener
		// TODO 自動生成されたメソッド・スタブ
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void onClick(View v) {
		mMap.clear();
		makePoint();
	}
}