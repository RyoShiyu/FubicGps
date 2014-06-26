package fubic.com;

import java.util.Arrays;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
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

	TextView texview;
	Location location;
	ParseObject GpsScore;
	SharedPreferences pref;

	boolean restartFlag = false;
	public String objId;
	public double latitude = 0;
	public double longitude = 0;
	private List<ParseObject> results;
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		texview = (TextView)findViewById(R.id.textView1);

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

		GpsScore = new ParseObject("GpsScore");

		pref = this.getSharedPreferences( "Parse_ID", Context.MODE_PRIVATE );
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		makePoint();
		mLocationClient.connect();

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
		objId = pref.getString( "id", "" );
		ParseQuery<ParseObject> query = ParseQuery.getQuery("GpsScore");
		query.getInBackground(objId, new GetCallback<ParseObject>() {
			public void done(ParseObject myGps, ParseException e) {
				if (e == null) {
					myGps.deleteInBackground();
				}
			}
		});

		Editor editor = pref.edit();
		editor.clear();
		editor.commit();
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
				double lon = otherGps.getDouble("longitude");
				MarkerOptions map = new MarkerOptions();
				map.position(new LatLng(lat,lon));
				map.title("吹き出しタイトル");
				map.snippet("スニペット");
				mMap.addMarker(map);
			}
		}

	}

	//    /**
	//     * Button to get current Location. This demonstrates how to get the current Location as required
	//     * without needing to register a LocationListener.
	//     */
	//    public void showMyLocation(View view) {
	//        if (mLocationClient != null && mLocationClient.isConnected()) {
	//            String msg = "Location = " + mLocationClient.getLastLocation();
	//            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	//        }
	//    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

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


	@Override
	public boolean onMyLocationButtonClick() {
		// TODO 自動生成されたメソッド・スタブ

		Toast.makeText(this, "現在の位置に移動しろう！", Toast.LENGTH_SHORT).show();

		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO 自動生成されたメソッド・スタブ
		latitude = location.getLatitude();
		longitude = location.getLongitude();

	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(
				REQUEST,
				this);  // LocationListener
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onDisconnected() {
		// TODO 自動生成されたメソッド・スタブ

	}
	@Override
	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		objId = pref.getString( "id", "" );
		if(objId == "" || objId == null){

			GpsScore.put("latitude", latitude);
			GpsScore.put("longitude", longitude);
			GpsScore.saveInBackground();
			objId = GpsScore.getObjectId();

			Editor editor = pref.edit();
			editor.putString("id", objId);
			editor.commit();

		}else{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("GpsScore");
			query.getInBackground(objId, new GetCallback<ParseObject>() {
				@Override
				public void done(ParseObject GpsScore, ParseException e) {
					// TODO 自動生成されたメソッド・スタブ
					if (e == null) {

						GpsScore.put("latitude", latitude);
						GpsScore.put("longitude", longitude);
						GpsScore.saveInBackground();
					}
				}
			});
		}
		String str = "緯度："+latitude+"\n経度："+longitude +"\nobjectID："+objId;
		texview.setText(str);
	}
}
