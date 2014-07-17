package fubic.com;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.Toast;

public class SubAlbumActivity extends Activity {

	GridView mGrid;

	@SuppressWarnings("unused")
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_sub);

		Intent intent = getIntent();

		Bundle bundle = intent.getExtras();
		if(bundle != null){

		}
		ContentResolver resolver = getContentResolver();

		Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		int fieldIndex;
		Long mId;
		int cnt = 0,VolMax = 0;
		HashMap<Integer, Uri> uriMap = new HashMap<Integer, Uri>();

		do{
			fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			mId = cursor.getLong(fieldIndex);

			Uri bmpUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mId);
			uriMap.put(cnt, bmpUri);
			cnt++;
		}while (cursor.moveToNext());
		VolMax = --cnt;
		cnt = 0;
		cursor.close();
//		String msg;
//		if(getApplicationContext() == null ){
//			msg = "null";
//		}else{
//			msg = "ok";
//		}
//		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
				mGrid = (GridView)findViewById(R.id.myGrid);
				mAdapter mAapter = new mAdapter(resolver, uriMap, VolMax, this);
				mGrid.setAdapter(mAapter);

	}
}

