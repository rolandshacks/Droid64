package ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import org.codewiz.droid64.R;
import util.LogManager;
import util.Logger;

import java.util.Collection;

/**
 * Created by roland on 22.08.2016.
 */

public class ObjectListView extends ListView {

    private final static Logger logger = LogManager.getLogger(ObjectListView.class.getName());

    public ObjectListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(new ObjectArrayAdapter(context, R.layout.list_small));
    }

    public void clear() {
        ObjectArrayAdapter adapter = (ObjectArrayAdapter) getAdapter();
        if (null == adapter) {
            return;
        }

        adapter.clear();
    }

    public void addAll(Collection<Object> items) {
        ObjectArrayAdapter adapter = (ObjectArrayAdapter) getAdapter();
        if (null == adapter) {
            return;
        }

        adapter.addAll(items);
    }

    public void add(Object item) {
        ObjectArrayAdapter adapter = (ObjectArrayAdapter) getAdapter();
        if (null == adapter) {
            return;
        }

        adapter.add(item);
    }
}
