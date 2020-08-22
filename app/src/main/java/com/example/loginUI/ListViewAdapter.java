package com.example.loginUI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private TextView lecture;
    private  TextView result;

    private ArrayList<ListViewItem> listViewITemList = new ArrayList<ListViewItem>();
    public ListViewAdapter(){}

    @Override
    public int getCount() {
        return listViewITemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item,parent,false);
        }

        lecture = (TextView) convertView.findViewById(R.id.listLecture);
        result = (TextView) convertView.findViewById(R.id.listResult);

        ListViewItem listViewItem = listViewITemList.get(position);

        lecture.setText(listViewItem.getLecture());
        result.setText(listViewItem.getResult());

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return listViewITemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String lecture,String result){
        ListViewItem item = new ListViewItem();
        item.setLecture(lecture);
        item.setResult(result);
    }


}
