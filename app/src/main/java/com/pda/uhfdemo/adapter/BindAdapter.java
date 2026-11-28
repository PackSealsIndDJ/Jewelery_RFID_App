package com.pda.uhfdemo.adapter;




import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.BindInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BindAdapter extends BaseQuickAdapter<BindInfo, BaseViewHolder> {

    public interface OnBindingActionListener {
        void onEdit(BindInfo binding);
        void onDelete(BindInfo binding);
    }

    private OnBindingActionListener actionListener;

    public BindAdapter(@Nullable List<BindInfo> data) {
        super(R.layout.bind_view, data);
    }

    public void setOnBindingActionListener(OnBindingActionListener listener) {
        actionListener = listener;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, BindInfo info) {
        baseViewHolder.setText(R.id.bind_item_epc, info.getEpc());
        baseViewHolder.setText(R.id.bind_item_name, info.getName());
        baseViewHolder.getView(R.id.bind_item_edit).setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onEdit(info);
            }
        });
        baseViewHolder.getView(R.id.bind_item_delete).setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onDelete(info);
            }
        });
    }
}
