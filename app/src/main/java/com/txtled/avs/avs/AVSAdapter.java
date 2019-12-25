package com.txtled.avs.avs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.txtled.avs.R;
import com.txtled.avs.bean.DeviceHostInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.Quan on 2019/12/11.
 */
public class AVSAdapter extends RecyclerView.Adapter<AVSAdapter.AVSViewHolder> {
    private ArrayList<DeviceHostInfo> mAddress;
    private Context mContext;
    private OnAVSItemClickListener listener;

    public AVSAdapter(ArrayList<DeviceHostInfo> mAddress, Context mContext) {
        this.mAddress = mAddress;
        this.mContext = mContext;
    }

    public void setListener(OnAVSItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AVSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_avs, parent, false);
        return new AVSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AVSViewHolder holder, final int position) {
        if (mAddress != null) {
            holder.tvAvsItem.setText(mAddress.get(position).getHostname() + ";\n"
                    + mAddress.get(position).getHostip() + ";\n" + mAddress.get(position).getHostmac());
            holder.tvAvsItem.setOnClickListener(v -> listener.onAVSClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return mAddress == null ? 0 : mAddress.size();
    }

    public class AVSViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_avs_item)
        TextView tvAvsItem;

        public AVSViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnAVSItemClickListener {
        void onAVSClick(int position);
    }
}
