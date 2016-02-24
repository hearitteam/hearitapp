package everis.com.hearit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class SoundAdapter extends ArrayAdapter<Sound> {

	private Context ctx;
	private ArrayList<Sound> list;
	private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	public SoundAdapter (Context context, List<Sound> objects) {
		super(context, R.layout.list_item, R.id.sound_name, objects);

		this.ctx = context;
		this.list = new ArrayList<Sound>(objects);

		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i).getName(), i);
		}
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_item, parent, false);

		TextView textView = (TextView) rowView.findViewById(R.id.sound_name);
		textView.setText(list.get(position).getName());

		return rowView;
	}

	@Override
	public long getItemId (int position) {
		String item = getItem(position).getName();
		return mIdMap.get(item);
	}

	@Override
	public boolean hasStableIds () {
		return true;
	}

	public void swapItems (ArrayList<Sound> list) {
		this.list = list;
		notifyDataSetChanged();
	}
}
