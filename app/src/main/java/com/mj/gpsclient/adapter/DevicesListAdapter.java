package com.mj.gpsclient.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.mj.gpsclient.R;
import com.mj.gpsclient.global.DebugLog;
import com.mj.gpsclient.model.Devices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by majin on 15/5/27.
 */
public class DevicesListAdapter extends BaseAdapter implements Filterable {


    private List<Devices> backList;
    private List<Devices> devicesList;
    private Context mContext;
    private LayoutInflater m_layoutInflater;
    private MyFilter mFilter;
    private Handler mHandle;
    public DevicesListAdapter(List<Devices> list,Context Context,Handler handler) {
        super();
        mContext = Context;
        this.m_layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setDate(list);
        mHandle=handler;
    }

    @Override
    public int getCount() {
        return this.devicesList.size();
    }

    @Override
    public Object getItem(int i) {
        return devicesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }



    public void setDate(List<Devices> data){
        this.backList = data;
        if(devicesList==null){
            this.devicesList =new ArrayList<Devices>();
        }
        this.devicesList.clear();
        this.devicesList.addAll(data);
    }
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView= m_layoutInflater.inflate(R.layout.item_list_devices,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Devices devices = devicesList.get(i);
        if (devices!=null){
            viewHolder.mTextName.setText(devices.getName());
            if(devices.getLineStatus().equals("离线")){
                viewHolder.mOnoffline.setText("离线");
                viewHolder.mHeard.setImageDrawable(mContext.getResources().getDrawable(R.drawable.carofflineimage));
            }else{
                viewHolder.mOnoffline.setText("在线");
                viewHolder.mHeard.setImageDrawable(mContext.getResources().getDrawable(R.drawable.carstaticimage));
            }
        }
        return convertView;
    }

    static class ViewHolder {
        TextView mTextName;
        ImageView mHeard;
        TextView mOnoffline;
        ViewHolder(View view) {
            mTextName =(TextView) view.findViewById(R.id.device_name);
            mHeard = (ImageView) view.findViewById(R.id.heard_icon);
            mOnoffline=(TextView) view.findViewById(R.id.devices_onoffline);
        }
    }


    @Override
    public Filter getFilter() {
        if (null == mFilter) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }


    // 自定义Filter类
    class MyFilter extends Filter {

        @Override
        // 该方法在子线程中执行
        // 自定义过滤规则
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            DebugLog.e("performFiltering="+constraint);
            List<Devices> newValues = new ArrayList<Devices>();
            String filterString = constraint.toString().trim()
                    .toLowerCase();

            // 如果搜索框内容为空，就恢复原始数据
            if (TextUtils.isEmpty(filterString)) {
                newValues.clear();
                newValues.addAll(backList);
            } else {
                // 过滤出新数据
                for (Devices devices : backList) {
                    DebugLog.e("devices.getName()="+devices.getName());
                    if (-1 != devices.getName().toLowerCase().indexOf(filterString)) {
                        newValues.add(devices);
                    }
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
           // devicesList = (List<Devices>) results.values;
            devicesList.clear();
            devicesList.addAll((List<Devices>) results.values);
            DebugLog.e("publishResults="+results.count);
            if (results.count > 0) {
//                mHandle.sendEmptyMessage(10);
                DevicesListAdapter.this.notifyDataSetChanged();  // 通知数据发生了改变
            } else {
//                mHandle.sendEmptyMessage(11);
                DevicesListAdapter.this.notifyDataSetChanged();  // 通知数据发生了改变
//                DevicesListAdapter.this.notifyDataSetInvalidated(); // 通知数据失效
            }
        }
    }

}
