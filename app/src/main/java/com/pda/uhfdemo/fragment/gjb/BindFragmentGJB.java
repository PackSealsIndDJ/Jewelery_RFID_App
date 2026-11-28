package com.pda.uhfdemo.fragment.gjb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.coder.zzq.smartshow.dialog.DialogBtnClickListener;
import com.coder.zzq.smartshow.dialog.EnsureDialog;
import com.coder.zzq.smartshow.dialog.SmartDialog;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.adapter.BindAdapter;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.entity.TagInfo;
import com.pda.uhfdemo.fragment.CsvExportFragment;
import com.pda.uhfdemo.utils.BindingStore;
import com.pda.uhfdemo.utils.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindFragmentGJB extends CsvExportFragment {

    @BindView(R.id.bind_epc)
    Spinner bind_epc;
    @BindView(R.id.bind_name)
    EditText bind_name;
    @BindView(R.id.bind_listView)
    RecyclerView bindListView;
    @BindView(R.id.bind_new_count)
    TextView newCountView;
    @BindView(R.id.bind_duplicate_count)
    TextView duplicateCountView;
    @BindView(R.id.bind_total_count)
    TextView totalCountView;
    @BindView(R.id.bind_export)
    Button bindExportButton;

    BindAdapter bindAdapter;

    List<BindInfo> bindInfoList = new ArrayList<>();
    LinkedHashMap<String, BindInfo> bindMap = new LinkedHashMap<>();
    LinkedHashMap<String, TagInfo> tagInfoById = new LinkedHashMap<>();
    List<String> scannedIds = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bind_gjb, container, false);
        ButterKnife.bind(this, view);
        initAdapter();
        Log.e("BindFragment","BindFragment");
        return view;
    }

    private void initAdapter() {
        for (BindInfo binding : BindingStore.loadBindInfo()) {
            bindMap.put(binding.getEpc(), binding);
        }
        bindInfoList.addAll(bindMap.values());
        bindAdapter = new BindAdapter(bindInfoList);
        bindAdapter.setOnBindingActionListener(new BindAdapter.OnBindingActionListener() {
            @Override
            public void onEdit(BindInfo binding) {
                editBinding(binding);
            }

            @Override
            public void onDelete(BindInfo binding) {
                confirmDeleteBinding(binding);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bindListView.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        bindListView.setLayoutManager(layoutManager);
        bindListView.setAdapter(bindAdapter);
        updateCounts();
        updateExportButton();
    }


    public void setData(List<TagInfo> tagInfoList) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        tagInfoById.clear();
        for (TagInfo tagInfo : tagInfoList) {
            if (tagInfo != null && !StringUtils.isNullOfEmpty(tagInfo.getEpc())) {
                String id = tagInfo.getEpc().trim();
                ids.add(id);
                tagInfoById.put(id, tagInfo);
            }
        }
        scannedIds.clear();
        scannedIds.addAll(ids);
        updateTagSpinner(false);
        updateCounts();
    }

    private void updateTagSpinner(boolean clearSelection) {
        List<String> availableIds = new ArrayList<>();
        for (String id : scannedIds) {
            if (!BindingStore.contains(id)) {
                availableIds.add(id);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, availableIds);
        bind_epc.setAdapter(adapter);
        if (clearSelection) {
            bind_epc.setSelection(-1);
        }
    }

    public void selectData(int position) {
        bind_epc.setSelection(position);
    }

    @OnClick(R.id.bind_event)
    public void bindEvent() {
        if (bind_epc.getSelectedItem() == null) {
            Toast.show(getString(R.string.bind_hint_1));
            return;
        }
        String name = bind_name.getText().toString().trim();
        if (StringUtils.isNullOfEmpty(name)) {
            Toast.show(getString(R.string.bind_hint_2));
            return;
        }

        String epc = bind_epc.getSelectedItem().toString().trim();
        if (BindingStore.contains(epc)) {
            Toast.show(getString(R.string.bind_duplicate_error));
            return;
        }
        TagInfo scannedTag = tagInfoById.get(epc);
        String tid = scannedTag == null ? null : scannedTag.getTid();
        if (!BindingStore.save(epc, name, tid)) {
            Toast.show(getString(R.string.bind_save_error));
            return;
        }
        bindMap.put(epc, new BindInfo(epc, name, tid, System.currentTimeMillis()));
        bindInfoList.clear();
        bindInfoList.addAll(bindMap.values());
        bindAdapter.notifyDataSetChanged();
        updateTagSpinner(true);
        updateCounts();
        updateExportButton();
        Toast.show(getString(R.string.bind_success_details, epc, name));
    }

    private void editBinding(BindInfo binding) {
        EditText nameInput = new EditText(requireContext());
        nameInput.setSingleLine(true);
        nameInput.setHint(R.string.bind_goods_name);
        nameInput.setText(binding.getName());
        nameInput.setSelection(nameInput.length());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.bind_edit)
                .setView(nameInput)
                .setPositiveButton(R.string.bind_update, (dialog, which) -> {
                    String updatedName = nameInput.getText().toString().trim();
                    if (StringUtils.isNullOfEmpty(updatedName)) {
                        Toast.show(getString(R.string.bind_hint_2));
                        return;
                    }
                    if (!BindingStore.update(binding.getEpc(), updatedName)) {
                        Toast.show(getString(R.string.bind_save_error));
                        return;
                    }
                    binding.setName(updatedName);
                    bindAdapter.notifyDataSetChanged();
                    updateCounts();
                    Toast.show(getString(R.string.bind_update_success));
                })
                .setNegativeButton(R.string.bind_cancel, null)
                .show();
    }

    private void confirmDeleteBinding(BindInfo binding) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.bind_delete)
                .setMessage(getString(R.string.bind_delete_confirm, binding.getEpc(), binding.getName()))
                .setPositiveButton(R.string.bind_delete, (dialog, which) -> {
                    if (!BindingStore.delete(binding.getEpc())) {
                        Toast.show(getString(R.string.bind_save_error));
                        return;
                    }
                    bindMap.remove(binding.getEpc());
                    bindInfoList.remove(binding);
                    bindAdapter.notifyDataSetChanged();
                    updateTagSpinner(false);
                    updateCounts();
                    updateExportButton();
                    Toast.show(getString(R.string.bind_delete_success));
                })
                .setNegativeButton(R.string.bind_cancel, null)
                .show();
    }

    private void updateExportButton() {
        if (bindExportButton != null) {
            bindExportButton.setEnabled(!bindInfoList.isEmpty());
        }
    }

    private void updateCounts() {
        int duplicates = 0;
        int newlyScanned = 0;
        Map<String, String> bindings = BindingStore.load();
        for (String id : scannedIds) {
            if (bindings.containsKey(id)) {
                duplicates++;
            } else {
                newlyScanned++;
            }
        }
        newCountView.setText(getString(R.string.bind_new_count, newlyScanned));
        duplicateCountView.setText(getString(R.string.bind_duplicate_count, duplicates));
        totalCountView.setText(getString(R.string.bind_total_count, bindings.size()));
    }

    @OnClick(R.id.bind_export)
    public void exportBindings() {
        exportBindDataToCsv(bindInfoList);
    }

    @OnClick(R.id.bind_clean)
    public void bindClean() {
        EnsureDialog ensureDialog = new EnsureDialog();
        ensureDialog.title(getString(R.string.bind_hint_4));
        ensureDialog.message(getString(R.string.bind_hint_5));
        ensureDialog.cancelBtn(getString(R.string.bind_hint_7), (dialog, which, data) -> dialog.dismiss());
        ensureDialog.confirmBtn(getString(R.string.bind_hint_6), new DialogBtnClickListener() {
            @Override
            public void onBtnClick(SmartDialog dialog, int which, Object data) {
                if (!BindingStore.removeAll()) {
                    Toast.show(getString(R.string.bind_save_error));
                    dialog.dismiss();
                    return;
                }
                bindInfoList.clear();
                bindMap.clear();
                bindAdapter.notifyDataSetChanged();
                updateTagSpinner(false);
                updateCounts();
                updateExportButton();
                dialog.dismiss();
            }
        });
        ensureDialog.showInFragment(this);

    }

}