package fubic.com;

import java.util.List;
import java.util.TimerTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.widget.Toast;

public class TaskForLocation extends TimerTask {

	private Context contxt;
	private MainActivity activity;
	private String objId;
	private double latitude;
	private double longitude;
	private ParseObject GpsScore;
	private List<ParseObject> results;
	private GoogleMap mMap;
	MarkerOptions map;

	public Context getContxt() {
		return contxt;
	}

	public void setContxt(Context contxt) {
		this.contxt = (MainActivity) contxt;
	}

	public Activity getActivity() {
		return (MainActivity) activity;
	}

	public void setActivity(Activity activity) {
		this.activity = (MainActivity) activity;
	}

	Handler mHandler = new Handler();
	public TaskForLocation(Context context, MainActivity activity) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.setContxt(contxt);
		this.setActivity(activity);
	}

	@Override
	public void run() {
		// TODO 自動生成されたメソッド・スタブ
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				mMap = activity.mMap;
				mMap.clear();
				objId = activity.getObjId();
				latitude = activity.getLatitude();
				longitude = activity.getLongitude();
				GpsScore = activity.GpsScore;


				objId = activity.pref.getString( "id", "" );
				if(objId == "" || objId == null){

					GpsScore.put("latitude", latitude);
					GpsScore.put("longitude", longitude);
					GpsScore.saveInBackground();
					objId = GpsScore.getObjectId();

					Editor editor = activity.pref.edit();
					editor.putString("id", objId);
					editor.commit();
				}else{
					ParseQuery<ParseObject> query = ParseQuery.getQuery("GpsScore");
					query.getInBackground(objId, new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject arg0, ParseException e) {
							// TODO 自動生成されたメソッド・スタブ
							if (e == null) {

								GpsScore.put("latitude", latitude);
								GpsScore.put("longitude", longitude);
								GpsScore.saveInBackground();
							}
						}
					});
				}
				ParseQuery<ParseObject> q = ParseQuery.getQuery("GpsScore");
				q.orderByDescending("_created_at");
				try{
					results = q.find();
				}catch(ParseException e){
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
						BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.tanukicon);
						map.icon(icon);
						mMap.addMarker(map);
					}
				}
				String msg = "更新します！" + "\n緯度：" + latitude + "\n経度：" + longitude + "\nobjId：" + objId;
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}
}
