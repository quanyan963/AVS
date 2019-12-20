package com.txtled.avs.wwa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.txtled.avs.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.Quan on 2019/12/11.
 */
public class WWAAdapter extends RecyclerView.Adapter<WWAAdapter.WWAViewHolder> {
    private ArrayList<String> mData;
    private Context mContext;
    private OnWWAItemClickListener listener;

    public WWAAdapter(ArrayList<String> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    public void setListener(OnWWAItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WWAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_avs, parent, false);
        return new WWAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WWAViewHolder holder, final int position) {
        if (mData != null) {
            holder.tvAvsItem.setText(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(ArrayList<String> strReceive) {
        mData = strReceive;
    }

    public class WWAViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_avs_item)
        TextView tvAvsItem;

        public WWAViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnWWAItemClickListener {
        void onAVSClick(int position);
    }
}
