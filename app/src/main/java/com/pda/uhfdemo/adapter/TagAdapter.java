package com.pda.uhfdemo.adapter;


import android.text.TextUtils;
import android.graphics.Color;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.TagInfo;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TagAdapter extends BaseQuickAdapter<TagInfo, BaseViewHolder> {

    private boolean showTid;
    private boolean showUser;
    private boolean showReserved;

    public TagAdapter(@Nullable List<TagInfo> data) {
        super(R.layout.tag_view, data);
    }

    public boolean isShowTid() {
        return showTid;
    }

    public void setShowTid(boolean showTid) {
        this.showTid = showTid;
    }

    public boolean isShowUser() {
        return showUser;
    }

    public void setShowUser(boolean showUser) {
        this.showUser = showUser;
    }

    public boolean isShowReserved() {
        return showReserved;
    }

    public void setShowReserved(boolean showReserved) {
        this.showReserved = showReserved;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TagInfo info) {
        baseViewHolder.setText(R.id.tag_index, info.getIndex() + "");
        baseViewHolder.setText(R.id.tag_epc, info.getEpc());
        String encodedDisplay = info.isEncodedReadSuccess()
                ? info.getEncodedData()
                : "ERROR: " + (TextUtils.isEmpty(info.getEncodedReadError())
                ? "No encoded data" : info.getEncodedReadError());
        baseViewHolder.setText(R.id.tag_encoded_data, encodedDisplay);
        baseViewHolder.getView(R.id.tag_encoded_data).setContentDescription(
                info.isEncodedReadSuccess()
                        ? "Encoded data from " + info.getEncodedDataBank()
                        : "Encoded data read failed: " + info.getEncodedReadError());
        baseViewHolder.setTextColor(R.id.tag_encoded_data,
                info.isEncodedReadSuccess() ? Color.rgb(27, 105, 78) : Color.RED);
        baseViewHolder.setText(R.id.tag_rssi, info.getRssi() == null ? "" : info.getRssi());
        baseViewHolder.setText(R.id.tag_first_scan_time, formatTime(info.getFirstScanTime()));
        baseViewHolder.setText(R.id.tag_last_scan_time, formatTime(info.getLastScanTime()));
        if (showTid) {
            baseViewHolder.getView(R.id.tag_tid).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_tid, info.getTid());
        } else {
            baseViewHolder.getView(R.id.tag_tid).setVisibility(View.GONE);
        }
        if (showUser) {
            baseViewHolder.getView(R.id.tag_user).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_user, info.getUserData());
        } else {
            baseViewHolder.getView(R.id.tag_user).setVisibility(View.GONE);
        }
        if (showReserved) {
            baseViewHolder.getView(R.id.tag_reserved).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_reserved, info.getReserved());
        } else {
            baseViewHolder.getView(R.id.tag_reserved).setVisibility(View.GONE);
        }

        if (info.isErrorTag()) {
            baseViewHolder.setBackgroundColor(R.id.tag_epc, Color.RED);
        } else {
            baseViewHolder.setBackgroundColor(R.id.tag_epc, Color.WHITE);
        }

//        baseViewHolder.setText(R.id.tag_rssi, info.getEpc());
        baseViewHolder.setText(R.id.tag_count, String.valueOf(info.getScanCount()));


    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
