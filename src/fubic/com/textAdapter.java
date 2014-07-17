package fubic.com;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class textAdapter extends BaseAdapter {
	private Context context;
    private String[] strings;

    public textAdapter(Context context) {
        this.context = context;
        strings = new String[100];

        for (int i = 0; i < strings.length; i++) {
            strings[i] = i + "番目";
        }
    }


	@Override
	public int getCount() {
		// TODO 自動生成されたメソッド・スタブ
		return strings.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO 自動生成されたメソッド・スタブ
		TextView textView = null;

        if (convertView == null) {
            textView = new TextView(context);
        } else {
            textView = (TextView) convertView;
        }
        textView.setText(strings[position]);

        return textView;
	}

}
