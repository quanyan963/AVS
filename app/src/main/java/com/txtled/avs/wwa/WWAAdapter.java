package com.txtled.avs.wwa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.txtled.avs.R;
import com.txtled.avs.bean.WWADeviceInfo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.Quan on 2019/12/11.
 */
public class WWAAdapter extends RecyclerView.Adapter<WWAAdapter.WWAViewHolder> {
    private ArrayList<WWADeviceInfo> mData;
    private Context mContext;
    private OnWWAItemClickListener listener;
    private String userId;

    public WWAAdapter(ArrayList<WWADeviceInfo> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setListener(OnWWAItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WWAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wwa, parent, false);
        return new WWAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WWAViewHolder holder, final int position) {
        if (mData != null) {
            if (userId == null || userId.isEmpty()){
                holder.tvWwaItem.setText(mData.get(position).getIp());
                holder.btWwaItem.setText(R.string.sign_in);
            }else if (mData.get(position).getFriendlyNames().isEmpty()){
                holder.tvWwaItem.setText(mData.get(position).getIp());
                holder.btWwaItem.setText(R.string.configure);
            }else {
                String names = mData.get(position).getFriendlyNames();
                String[] id_name = names.split(",");
                for (int i = 0; i < id_name.length; i++) {
                    String[] value = id_name[i].split("_");
                    if (value[0].equals(userId)){
                        holder.tvWwaItem.setText(value[1]);
                        holder.btWwaItem.setText(R.string.change_name);
                        break;
                    }
                    if (i == id_name.length - 1){
                        holder.tvWwaItem.setText(mData.get(position).getIp());
                        holder.btWwaItem.setText(R.string.configure);
                    }
                }

            }

            holder.btWwaItem.setOnClickListener(v -> listener.onWWAClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(ArrayList<WWADeviceInfo> strReceive) {
        mData = strReceive;
    }

    public void update(int position, ArrayList<WWADeviceInfo> newNames) {
        mData = newNames;
        notifyItemChanged(position);
    }

    public class WWAViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_wwa_item)
        TextView tvWwaItem;
        @BindView(R.id.bt_wwa_item)
        MaterialButton btWwaItem;

        public WWAViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnWWAItemClickListener {
        void onWWAClick(int position);
    }
}
