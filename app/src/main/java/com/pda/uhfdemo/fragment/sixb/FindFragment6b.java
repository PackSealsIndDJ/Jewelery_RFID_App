package com.pda.uhfdemo.fragment.sixb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTag6bLog;
import com.gg.reader.api.dal.HandlerTag6bOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBase6bInfo;
import com.gg.reader.api.protocol.gx.LogBase6bOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventory6b;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.adapter.FindAdapter;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.fragment.CsvExportFragment;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.BindingStore;
import com.pda.uhfdemo.utils.Toast;
import com.pda.uhfdemo.utils.VoiceUtils;
import com.pda.uhfdemo.view.RadarView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FindFragment6b extends CsvExportFragment {


    @BindView(R.id.find_listview)
    RecyclerView findListView;
    @BindView(R.id.find_btn)
    Button findBtn;
    @BindView(R.id.find_export)
    Button findExportButton;
    @BindView(R.id.find_radar)
    RadarView radarView;
    @BindView(R.id.find_radar_target)
    TextView radarTarget;
    @BindView(R.id.find_radar_distance)
    TextView radarDistance;
    @BindView(R.id.find_radar_trend)
    TextView radarTrend;
    private FindAdapter findAdapter;

    List<BindInfo> findInfoList = new ArrayList<>();
    LinkedHashMap<String, BindInfo> findMap = new LinkedHashMap<>();

    GClient client = GlobalClient.getClient();

    private final Map<String, Integer> signalById = new LinkedHashMap<>();
    private volatile boolean scanning;
    private volatile String targetId;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_6b, container, false);
        ButterKnife.bind(this, view);
        initAdapter();
        return view;
    }

    private void initAdapter() {
        findAdapter = new FindAdapter(findInfoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        findListView.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        findListView.setLayoutManager(layoutManager);
        findListView.setAdapter(findAdapter);
        findAdapter.setOnItemClickListener((adapter, view, position) -> {
            targetId = findInfoList.get(position).getEpc();
            findAdapter.setTargetId(targetId);
            radarView.reset();
            renderTarget();
        });
    }

    public void tagCallback() {
        client.onTag6bLog = new HandlerTag6bLog() {
            @Override
            public void log(String s, LogBase6bInfo logBase6bInfo) {
                if (logBase6bInfo.getResult() == 0) {
                    onTagDetected(logBase6bInfo.getTid(), logBase6bInfo.getRssi());
                }
            }
        };
        client.onTag6bOver = new HandlerTag6bOver() {
            @Override
            public void log(String s, LogBase6bOver logBase6bOver) {

            }
        };
    }

    private void onTagDetected(String id, int rssi) {
        if (!scanning || id == null) return;
        boolean newlyFound;
        synchronized (findMap) {
            if (!scanning) return;
            BindInfo info = findMap.get(id);
            if (info == null) return;
            newlyFound = !getString(R.string.find_status_found).equals(info.getStatus());
            info.setStatus(getString(R.string.find_status_found));
            info.setRssi(rssi);
            info.incrementScanCount();
            info.setLastSeenMillis(System.currentTimeMillis());
            synchronized (signalById) {
                signalById.put(id, rssi);
            }
        }
        if (newlyFound) VoiceUtils.start();
        android.app.Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (getView() == null || findAdapter == null) return;
            findAdapter.notifyDataSetChanged();
            findExportButton.setEnabled(true);
            if (id.equals(targetId)) renderTarget();
        });
    }

    private void prepareSearch() {
        scanning = false;
        synchronized (signalById) {
            signalById.clear();
        }
        synchronized (findMap) {
            findMap.clear();
            findInfoList.clear();
            for (BindInfo info : BindingStore.loadBindInfo()) {
                info.setStatus(getString(R.string.find_status_searching));
                findMap.put(info.getEpc(), info);
                findInfoList.add(info);
            }
        }
        findExportButton.setEnabled(false);
        targetId = findInfoList.isEmpty() ? null : findInfoList.get(0).getEpc();
        findAdapter.setTargetId(targetId);
        findAdapter.notifyDataSetChanged();
        radarView.reset();
        renderTarget();
        scanning = true;
    }

    private void finishSearch() {
        scanning = false;
        synchronized (findMap) {
            for (BindInfo info : findInfoList) {
                if (!getString(R.string.find_status_found).equals(info.getStatus())) {
                    info.setStatus(getString(R.string.find_status_missing));
                }
            }
        }
        if (findAdapter != null) findAdapter.notifyDataSetChanged();
        updateExportButton();
        renderTarget();
    }

    private void updateExportButton() {
        if (findExportButton == null) return;
        for (BindInfo info : findInfoList) {
            if (getString(R.string.find_status_found).equals(info.getStatus())) {
                findExportButton.setEnabled(true);
                return;
            }
        }
        findExportButton.setEnabled(false);
    }

    private void renderTarget() {
        if (radarView == null) return;
        if (targetId == null) {
            radarTarget.setText(getString(R.string.radar_select_target));
            radarView.reset();
            radarDistance.setText(getString(R.string.radar_distance_unknown));
            radarTrend.setText(getString(R.string.radar_waiting));
            return;
        }
        radarTarget.setText(getString(R.string.radar_target, targetId));
        Integer rssi;
        synchronized (signalById) {
            rssi = signalById.get(targetId);
        }
        if (rssi == null) {
            radarView.reset();
            radarDistance.setText(getString(R.string.radar_distance_unknown));
            radarTrend.setText(scanning
                    ? getString(R.string.find_status_searching)
                    : getString(R.string.find_status_missing));
            return;
        }
        radarView.setRssi(rssi);
        radarDistance.setText(radarView.getDistanceLabel());
        radarTrend.setText(radarView.getTrendLabel());
    }


    @OnClick(R.id.find_export)
    public void exportFindResults() {
        synchronized (findMap) {
            exportFindDataToCsv(findInfoList, getString(R.string.find_status_found), true);
        }
    }

    @OnClick(R.id.find_btn)
    public void findEvent() {
        if (findBtn.getText().equals(getResources().getString(R.string.scan_btn))) {
            prepareSearch();
            tagCallback();
            client.sendUnsynMsg(new MsgBaseStop());
            MsgBaseInventory6b msg = new MsgBaseInventory6b();
            msg.setAntennaEnable(EnumG.AntennaNo_1);
            msg.setInventoryMode(EnumG.InventoryMode_Inventory);
            msg.setArea(EnumG.ReadMode6b_Tid);
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                findBtn.setText(getResources().getString(R.string.stop_btn));
            } else {
                finishSearch();
                Toast.show(getString(R.string.find_hint_1));
            }
        } else {
            MsgBaseStop msg = new MsgBaseStop();
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                findBtn.setText(getResources().getString(R.string.scan_btn));
                finishSearch();
            } else {
                Toast.show(getString(R.string.find_hint_2));
            }
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (findBtn != null && !findBtn.getText().equals(getResources().getString(R.string.scan_btn))) {
            client.sendUnsynMsg(new MsgBaseStop());
            finishSearch();
            findBtn.setText(getResources().getString(R.string.scan_btn));
        }
    }

    @Override
    public void onDestroyView() {
        if (scanning) {
            client.sendUnsynMsg(new MsgBaseStop());
            finishSearch();
        }
        client.onTag6bLog = null;
        client.onTag6bOver = null;
        super.onDestroyView();
    }
}