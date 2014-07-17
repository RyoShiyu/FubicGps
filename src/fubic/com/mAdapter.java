package fubic.com;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class mAdapter extends BaseAdapter {
	private ContentResolver cr;
	private HashMap<Integer, Uri> hm;
	private int Max;
	private Bitmap tmpBmp;
	ImageView imageView;
	private Context context;

	public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

	public mAdapter(ContentResolver _cr, HashMap<Integer, Uri> _hm,
			int max, Context context) {
		// TODO 自動生成されたコンストラクター・スタブ
		cr = _cr;
		hm = _hm;
		Max = max;
		this.setContext(context);
	}

	@Override
	public int getCount() {
		// TODO 自動生成されたメソッド・スタブ
		return Max;
	}

	@Override
	public Object getItem(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO 自動生成されたメソッド・スタブ
		if(convertView == null){
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
		}else{
			imageView = (ImageView)convertView;
		}
		try{

			tmpBmp = MediaStore.Images.Media.getBitmap(cr, hm.get(position));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		imageView.setImageBitmap(tmpBmp);
		return imageView;

	}
}
