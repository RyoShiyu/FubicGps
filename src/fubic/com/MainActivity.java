package fubic.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import fubic.com.Person;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends ActionBarActivity
implements
ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener,
OnMyLocationButtonClickListener,
OnClickListener,
DialogInterface.OnClickListener,
ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person>
{
	IconGenerator iconFactory;
	TextView textview;
	Location location;
	ParseObject GpsScore = new ParseObject("GpsScore");
	SharedPreferences pref;
	Timer timer = new Timer(false);
	TaskForLocation taskLocate;

	private ClusterManager<Person> mClusterManager;
	BitmapDescriptor icon = null;
	int iconAccount = 0;
	String tweet = "make a comment";
	String userName = "dummy";
	final CharSequence[] items = { "狸", "犬", "猫", "熊", "鼠", "魚" };
	final CharSequence[] prosAndCons = { "表示する", "表示しない" };
	int iconChar = 0;
	int iconNumber = 0;
	boolean initFlag = false;
	boolean prosFlag = false;
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

	private class PersonRenderer extends DefaultClusterRenderer<Person> {
		private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
		private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
		private final ImageView mImageView;
		private final ImageView mClusterImageView;
		private final int mDimension;

		public PersonRenderer() {
			super(getApplicationContext(), getMap(), mClusterManager);

			View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
			mClusterIconGenerator.setContentView(multiProfile);
			mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

			mImageView = new ImageView(getApplicationContext());
			mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
			mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
			int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
			mImageView.setPadding(padding, padding, padding, padding);
			mIconGenerator.setContentView(mImageView);
		}

		@Override
		protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
			// Draw a single person.
			// Set the info window to show their name.
			mImageView.setImageResource(person.profilePhoto);
			Bitmap icon = mIconGenerator.makeIcon();
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
		}

		@Override
		protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
			// Draw multiple people.
			// Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
			List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
			int width = mDimension;
			int height = mDimension;

			for (Person p : cluster.getItems()) {
				// Draw 4 at most.
				if (profilePhotos.size() == 4) break;
				Drawable drawable = getResources().getDrawable(p.profilePhoto);
				drawable.setBounds(0, 0, width, height);
				profilePhotos.add(drawable);
			}
			MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
			multiDrawable.setBounds(0, 0, width, height);

			mClusterImageView.setImageDrawable(multiDrawable);
			Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
		}

		@Override
		protected boolean shouldRenderAsCluster(Cluster cluster) {
			// Always render clusters.
			return cluster.getSize() > 1;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textview = (TextView)findViewById(R.id.textView1);

		Button btn = (Button)findViewById(R.id.update);
		btn.setOnClickListener(this);

		Button btn2 = (Button)findViewById(R.id.Button02);
		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final EditText txt = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this)
				.setIcon(R.drawable.mouse)
				.setTitle("今の状態を入力してください")
				.setMessage("現在の状態は" + pref.getString("tweet", ""))
				.setView(txt)
				.setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ
					}
				})
				.setNegativeButton("確認", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ
						String tweetLoc = txt.getText().toString();

						Editor editor = pref.edit();
						editor.putString("tweet", tweetLoc);
						editor.commit();
						objId = pref.getString( "id", "" );

						Toast.makeText(MainActivity.this, "今の状態は" + tweetLoc + "に変更しました！", Toast.LENGTH_LONG).show();

						if(objId == "" || objId == null){
							GpsScore.put("tweet", tweetLoc);
							GpsScore.saveInBackground();
							mMap.clear();
							makePoint();
						}else{
							ParseQuery<ParseObject> query = ParseQuery.getQuery("GpsScore");
							query.getInBackground(objId, new GetCallback<ParseObject>() {
								@Override
								public void done(ParseObject GpsScore, ParseException e) {
									// TODO 自動生成されたメソッド・スタブ
									if (e == null) {

										GpsScore.put("tweet", pref.getString("tweet", ""));
										GpsScore.saveInBackground();
									}
								}
							});
							mMap.clear();
							makePoint();
						}
					}
				}).show();
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
		if(mMap.getCameraPosition().zoom > 12.1){

		locateUpdate();
		}
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
	 * アイコンの生成
	 */
	public void makePoint() {
		// TODO 自動生成されたメソッド・スタブ
		ParseQuery<ParseObject> q = ParseQuery.getQuery("GpsScore");
		q.orderByDescending("_created_at");
		q.whereNotEqualTo("objectId", pref.getString("id", ""));
		try {
			results = q.find();
		} catch (ParseException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if (results != null) {

			//			List<MyItem> items = new ArrayList<MyItem>();
			mClusterManager = new ClusterManager<Person>(this, getMap());
			mClusterManager.setRenderer(new PersonRenderer());
			getMap().setOnCameraChangeListener(mClusterManager);
			getMap().setOnMarkerClickListener(mClusterManager);
			getMap().setOnInfoWindowClickListener(mClusterManager);
			mClusterManager.setOnClusterClickListener(this);
			mClusterManager.setOnClusterInfoWindowClickListener(this);
			mClusterManager.setOnClusterItemClickListener(this);
			mClusterManager.setOnClusterItemInfoWindowClickListener(this);
			for (ParseObject otherGps : results) {
				double lat = otherGps.getDouble("latitude");
				double lng = otherGps.getDouble("longitude");
				//				map = new MarkerOptions();
				//				map.position(new LatLng(lat,lng));
				//				map.title(otherGps.getString("tweet") != null ? otherGps.getString("tweet"):tweet);
				//				map.snippet(otherGps.getString("userName")!= null ? otherGps.getString("userName"):userName);
				iconNumber = otherGps.getInt("iconId");
				//				setIconView();
				//				map.icon(icon);
				//				mMap.addMarker(map);
				if(mMap.getCameraPosition().zoom > 12.1){
					IconGenerator iconFactory = new IconGenerator(this);
					iconFactory.setContentRotation(-90);
					iconFactory.setRotation(90);
					iconFactory.setStyle(IconGenerator.STYLE_BLUE);
					addIcon(iconFactory,otherGps.getString("tweet") != null ? otherGps.getString("tweet"):tweet, new LatLng(lat, lng));
				}
				mClusterManager.addItem(new Person(position(lat, lng),otherGps.getString("userName")!= null ? otherGps.getString("userName"):userName , IconAccount() ));
				//				items.add(new MyItem(lat, lng));
			}

			try{
				//				mcl.addItems(items);
				mClusterManager.cluster();
				//				ClusterManager<MyItem> mcl = new ClusterManager<MyItem>(this, getMap());
				//				mMap.setOnCameraChangeListener(mcl);
			}catch(Exception e){
				Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
			}
		}
		objId = pref.getString( "id", "" );
		if(objId == "" || objId == null){

			GpsScore.put("latitude", latitude);
			GpsScore.put("longitude", longitude);
			GpsScore.put("iconId", pref.getInt("iconId", 0));
			GpsScore.saveInBackground();
			objId = GpsScore.getObjectId();

			Editor editor = pref.edit();
			editor.putString("id", objId);
			editor.commit();
		}
		ParseQuery<ParseObject> quser = ParseQuery.getQuery("GpsScore");
		quser.whereEqualTo("objectId", pref.getString("id", ""));
		quser.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject object, ParseException arg1) {
				// TODO 自動生成されたメソッド・スタブ
				double lat = object.getDouble("latitude");
				double lng = object.getDouble("longitude");
				map = new MarkerOptions();
				map.position(new LatLng(lat,lng));
				map.title(pref.getString("tweet", "") != ""  ? pref.getString("tweet", "") : tweet);
				map.snippet(pref.getString("userName", "") != "" ? pref.getString("userName", "") : userName);
				setIconView();
				iconNumber = pref.getInt("iconId", 0);
				setIconView();
				map.icon(icon);
				mMap.addMarker(map);
			}
		});
	}

	private void addIcon(IconGenerator iconFactory, String text, LatLng position) {
		// TODO 自動生成されたメソッド・スタブ
		MarkerOptions markerOptions = new MarkerOptions().
				icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
				position(position).
				anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

		mMap.addMarker(markerOptions);
	}

	public void setIconView(){
		switch (iconNumber) {
		case 0:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.raccoon);
			break;
		case 1:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.dog);
			break;
		case 2:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.cat);
			break;
		case 3:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.bear);
			break;
		case 4:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.mouse);
			break;
		case 5:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.fish);
			break;
		default:
			break;
		}
	}

	public int IconAccount(){
		switch (iconNumber) {
		case 0:
			iconAccount = R.drawable.raccoon;
			break;
		case 1:
			iconAccount = R.drawable.dog;
			break;
		case 2:
			iconAccount = R.drawable.cat;
			break;
		case 3:
			iconAccount = R.drawable.bear;
			break;
		case 4:
			iconAccount = R.drawable.mouse;
			break;
		case 5:
			iconAccount = R.drawable.fish;
			break;
		default:
			break;
		}
		return iconAccount;
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

			new AlertDialog.Builder(MainActivity.this)
			.setTitle("アイコン設定")
			.setIcon(R.drawable.raccoon)
			.setSingleChoiceItems(
					items,
					pref.getInt("iconId", 0),
					this).setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 自動生成されたメソッド・スタブ
						}
					})
					.setNegativeButton("確認", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 自動生成されたメソッド・スタブ
							mMap.clear();
							makePoint();
						}
					}).show();
			return true;
		}
		if(id == R.id.profile){

			final EditText txt = new EditText(MainActivity.this);

			new AlertDialog.Builder(MainActivity.this)
			.setIcon(R.drawable.mouse)
			.setTitle("名前を入力してください")
			.setMessage("現在の名前は" + pref.getString("userName", ""))
			.setView(txt)
			.setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
				}
			})
			.setNegativeButton("確認", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					String name = txt.getText().toString();

					Editor editor = pref.edit();
					editor.putString("userName", name);
					editor.commit();
					objId = pref.getString( "id", "" );

					Toast.makeText(MainActivity.this, "お名前は" + name + "に変更しました！", Toast.LENGTH_LONG).show();

					if(objId == "" || objId == null){
						GpsScore.put("userName", name);
						GpsScore.saveInBackground();
						mMap.clear();
						makePoint();
					}else{
						ParseQuery<ParseObject> query = ParseQuery.getQuery("GpsScore");
						query.getInBackground(objId, new GetCallback<ParseObject>() {
							@Override
							public void done(ParseObject GpsScore, ParseException e) {
								// TODO 自動生成されたメソッド・スタブ
								if (e == null) {

									GpsScore.put("userName", pref.getString("userName", ""));
									GpsScore.saveInBackground();
								}
							}
						});
						mMap.clear();
						makePoint();
					}
				}
			}).show();
			return true;
		}
		if(id == R.id.nowInfo){

			new AlertDialog.Builder(MainActivity.this)
			.setIcon(R.drawable.fish)
			.setTitle("更新状態表示しますか")
			.setSingleChoiceItems(
					prosAndCons,
					pref.getInt("prosAndCons", 0),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 自動生成されたメソッド・スタブ
							Editor editor = pref.edit();
							editor.putInt("prosAndCons", which);
							editor.commit();
						}
					}).setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 自動生成されたメソッド・スタブ
						}
					})
					.setNegativeButton("確認", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO 自動生成されたメソッド・スタブ
						}
					}).show();
			return true;
		}
		if(id == R.id.myVersion){
			String str = getString(R.string.my_version);
			Toast.makeText(this, str, Toast.LENGTH_LONG).show();
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when the my location button is clicked.
	 */
	@Override
	public boolean onMyLocationButtonClick() {
		// TODO 自動生成されたメソッド・スタブ
		Toast.makeText(this, "現在の位置に移動する！", Toast.LENGTH_SHORT).show();

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
		if(mMap.getCameraPosition().zoom > 12.1){
		mMap.clear();
		makePoint();
		Log.d("test", "pref  "+ pref.getInt("iconId", 0));
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO 自動生成されたメソッド・スタブ
		switch(which){
		case 0:
			iconChar = 0;
			break;
		case 1:
			iconChar = 1;
			break;
		case 2:
			iconChar = 2;
			break;
		case 3:
			iconChar = 3;
			break;
		case 4:
			iconChar = 4;
			break;
		case 5:
			iconChar = 5;
		default:
			break;
		}
		Editor editor = pref.edit();
		editor.putInt("iconId", which);
		editor.commit();
	}
	public float mapZoom(){
		return mMap.getCameraPosition().zoom;
	}

	protected GoogleMap getMap() {
		setUpMapIfNeeded();
		return mMap;
	}
	private LatLng position(double lat, double lng) {
		return new LatLng(lat, lng);
	}
	@Override
	public void onClusterItemInfoWindowClick(fubic.com.Person item) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public boolean onClusterItemClick(fubic.com.Person item) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void onClusterInfoWindowClick(Cluster<fubic.com.Person> cluster) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public boolean onClusterClick(Cluster<fubic.com.Person> cluster) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public void mapLevel(){
		Log.v("Map","Zoom Level = " + mMap.getCameraPosition().zoom);
	}

}