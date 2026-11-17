package com.pda.uhfdemo.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pda.uhfdemo.entity.TagInfo;
import com.pda.uhfdemo.utils.ExcelUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;

public abstract class TagExportFragment extends Fragment {
    private List<TagInfo> pendingExport;

    private final ActivityResultLauncher<String> exportDocument =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/vnd.ms-excel"),
                    this::writeExport);

    protected final void exportTags(List<TagInfo> tags) {
        if (tags == null || tags.isEmpty()) {
            Toast.makeText(requireContext(), "No Data", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingExport = new ArrayList<>(tags);
        exportDocument.launch("Tag_" + System.currentTimeMillis() + ".xls");
    }

    private void writeExport(@Nullable Uri uri) {
        if (uri == null) {
            pendingExport = null;
            return;
        }
        try {
            ExcelUtil.writeTagsToUri(requireContext(), uri, pendingExport);
            Toast.makeText(requireContext(), "Export successful", Toast.LENGTH_SHORT).show();
        } catch (IOException | WriteException error) {
            Toast.makeText(requireContext(), "Export failed. Choose a writable destination and try again.",
                    Toast.LENGTH_LONG).show();
        } finally {
            pendingExport = null;
        }
    }
}
