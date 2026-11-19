package com.pda.uhfdemo.fragment;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.utils.CsvExportManager;

import java.util.List;

public abstract class CsvExportFragment extends Fragment {
    private Runnable pendingPermissionExport;
    private AlertDialog progressDialog;
    private boolean exportInProgress;

    private final ActivityResultLauncher<String> storagePermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                Runnable export = pendingPermissionExport;
                pendingPermissionExport = null;
                if (granted && export != null) {
                    startExport(export);
                } else {
                    exportInProgress = false;
                    if (isAdded()) {
                        Toast.makeText(requireContext(), R.string.export_permission_denied,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

    protected final void exportBindDataToCsv(List<BindInfo> records) {
        if (records == null || records.isEmpty()) {
            showNoData();
            return;
        }
        runWithStoragePermission(() -> CsvExportManager.exportBindDataToCsv(
                requireContext(), records, exportCallback()));
    }

    protected final void exportFindDataToCsv(List<BindInfo> records, String foundStatus,
                                              boolean idIsTid) {
        if (records == null || records.isEmpty()) {
            showNoData();
            return;
        }
        runWithStoragePermission(() -> CsvExportManager.exportFindDataToCsv(
                requireContext(), records, foundStatus, idIsTid, exportCallback()));
    }

    private void runWithStoragePermission(Runnable export) {
        if (exportInProgress) return;
        exportInProgress = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingPermissionExport = export;
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        startExport(export);
    }

    private void startExport(Runnable export) {
        showProgress();
        export.run();
    }

    private CsvExportManager.Callback exportCallback() {
        return new CsvExportManager.Callback() {
            @Override
            public void onSuccess(CsvExportManager.ExportedFile file, int recordCount) {
                dismissProgress();
                exportInProgress = false;
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show();
                showExportActions(file, recordCount);
            }

            @Override
            public void onError(Exception error) {
                dismissProgress();
                exportInProgress = false;
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.export_failed, Toast.LENGTH_LONG).show();
            }
        };
    }

    private void showProgress() {
        ProgressBar progressBar = new ProgressBar(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(32, 24, 32, 24);
        content.addView(progressBar);

        TextView message = new TextView(requireContext());
        message.setText(R.string.export_progress_message);
        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        messageParams.setMargins(24, 0, 0, 0);
        content.addView(message, messageParams);

        progressDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.export_progress_title)
                .setView(content)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    private void showExportActions(CsvExportManager.ExportedFile file, int recordCount) {
        String message = getString(R.string.export_complete_message, file.location, recordCount);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.export_complete_title)
                .setMessage(message)
                .setPositiveButton(R.string.export_share,
                        (dialog, which) -> shareFile(file))
                .setNeutralButton(R.string.export_open,
                        (dialog, which) -> openFile(file))
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    private void shareFile(CsvExportManager.ExportedFile file) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, file.uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.export_share)));
        } catch (ActivityNotFoundException error) {
            Toast.makeText(requireContext(), R.string.export_share_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void openFile(CsvExportManager.ExportedFile file) {
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(file.uri, "text/csv");
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(openIntent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(requireContext(), R.string.export_open_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void showNoData() {
        Toast.makeText(requireContext(), R.string.export_no_data, Toast.LENGTH_SHORT).show();
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onDestroyView() {
        dismissProgress();
        super.onDestroyView();
    }
}
