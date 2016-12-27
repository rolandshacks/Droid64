package ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.codewiz.droid64.R;
import util.LogManager;
import util.Logger;

/**
 * Created by roland on 14.08.2016.
 */

public class ObjectArrayAdapter extends ArrayAdapter<Object> {

    private final static Logger logger = LogManager.getLogger(ObjectArrayAdapter.class.getName());

    private int layoutResourceId;

    public ObjectArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        layoutResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            Object item = getItem(position);
            View v = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(layoutResourceId, null);
            } else {
                v = convertView;
            }

            TextView textView = (TextView) v.findViewById(R.id.image_text);
            textView.setText(item.toString());

            return v;
        } catch (Exception ex) {
            logger.info("error: " + ex);
            return null;
        }
    }
}
