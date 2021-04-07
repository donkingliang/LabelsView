package com.donkingliang.labelsviewdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.donkingliang.labels.LabelsView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author teach liang
 * @Description
 * @Date 2021/4/7
 */
public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.ViewHolder> {

    private List<ListTestBean> list;
    private Context mContext;

    public LabelAdapter(Context context, List<ListTestBean> data) {
        this.mContext = context;
        this.list = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_label, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ListTestBean bean = list.get(position);

        // 设置数据前先移除OnLabelSelectChangeListener监听
//        holder.labelsView.setOnLabelSelectChangeListener(null);

        // 设置数据
        holder.labelsView.setLabels(bean.getLabels());

        // 设置标签状态监听
//        holder.labelsView.setOnLabelSelectChangeListener(new LabelsView.OnLabelSelectChangeListener() {
//            @Override
//            public void onLabelSelectChange(TextView label, Object data, boolean isSelect, int position) {
//                // 状态发生变化，保存选中状态
//                bean.setSelects(holder.labelsView.getSelectLabels());
//
//                // 使用1 .6 .4 以下版本，要这样写
//                List<Integer> selects = new ArrayList<>();
//                selects.addAll(holder.labelsView.getSelectLabels());
//                bean.setSelects(selects);
//            }
//        });

        // 设置标签点击监听（如果标签的状态只会通过点击标签切换，推荐使用这种方式）
        holder.labelsView.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
            @Override
            public void onLabelClick(TextView label, Object data, int position) {
                bean.setSelects(holder.labelsView.getSelectLabels());

                // 使用1.6.4以下版本，要这样写
//                List<Integer> selects = new ArrayList<>();
//                selects.addAll(holder.labelsView.getSelectLabels());
//                bean.setSelects(selects);
            }
        });

        // 恢复选中状态，放在最后
        holder.labelsView.setSelects(bean.getSelects());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        LabelsView labelsView;

        public ViewHolder(View itemView) {
            super(itemView);
            labelsView = itemView.findViewById(R.id.labels);
        }
    }
}
