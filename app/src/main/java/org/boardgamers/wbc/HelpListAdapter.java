package org.boardgamers.wbc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Created by Kevin
 */
public class HelpListAdapter extends BaseExpandableListAdapter {
    protected final LayoutInflater inflater;

    protected final String[] headerStrings;
    protected final String[] textStrings;

    public HelpListAdapter(Context c) {
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        headerStrings = c.getResources().getStringArray(R.array.help_headers);
        textStrings = c.getResources().getStringArray(R.array.help_texts);

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
                             ViewGroup parent) {
        if (view == null)
            view = inflater.inflate(R.layout.list_item_text, parent, false);

        TextView title = (TextView) view.findViewById(R.id.li_text);
        //title.setGravity(Gravity.CENTER);
        title.setText(textStrings[groupPosition]);

        return view;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View view,
                             ViewGroup parent) {
        if (view == null)
            view = inflater.inflate(R.layout.list_group_large, parent, false);

        TextView name = (TextView) view.findViewById(R.id.sg_name);
        name.setText(headerStrings[groupPosition]);

        if (isExpanded) {
            view.setBackgroundResource(R.drawable.group_expanded);
        } else {
            view.setBackgroundResource(R.drawable.group_collapsed);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return textStrings[childPosition];
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headerStrings[groupPosition];
    }

    @Override
    public int getGroupCount() {
        return headerStrings.length;
    }
}