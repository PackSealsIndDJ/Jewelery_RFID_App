package com.pda.uhfdemo.utils;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.pda.uhfdemo.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class InventoryListExpansion {

    private InventoryListExpansion() {
    }

    public static void apply(View root, boolean expanded, boolean animate) {
        if (animate && root instanceof ViewGroup) {
            AutoTransition transition = new AutoTransition();
            transition.setDuration(220);
            TransitionManager.beginDelayedTransition((ViewGroup) root, transition);
        }

        setVisibility(root, R.id.inventory_heading, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_subtitle, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_speed_divider, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_speed_metric, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_time_divider, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_time_metric, expanded ? View.GONE : View.VISIBLE);
        setVisibility(root, R.id.inventory_filter_panel, expanded ? View.GONE : View.VISIBLE);

        View overview = root.findViewById(R.id.inventory_overview_root);
        if (overview != null) {
            int verticalPadding = dp(root, expanded ? 4 : 12);
            int bottomPadding = dp(root, expanded ? 4 : 10);
            overview.setPaddingRelative(
                    overview.getPaddingStart(), verticalPadding,
                    overview.getPaddingEnd(), bottomPadding);
        }

        View metrics = root.findViewById(R.id.inventory_metrics);
        if (metrics != null) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) metrics.getLayoutParams();
            params.height = dp(root, expanded ? 56 : 76);
            params.topMargin = dp(root, expanded ? 0 : 12);
            metrics.setLayoutParams(params);
        }

        ImageButton button = root.findViewById(R.id.inventory_expand_button);
        if (button != null) {
            button.setContentDescription(root.getContext().getString(
                    expanded ? R.string.inventory_collapse_list : R.string.inventory_expand_list));
            if (animate) {
                button.animate().rotation(expanded ? 180f : 0f).setDuration(220).start();
            } else {
                button.setRotation(expanded ? 180f : 0f);
            }
        }
    }

    public static int getVisibleItemCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int first = linearLayoutManager.findFirstVisibleItemPosition();
            int last = linearLayoutManager.findLastVisibleItemPosition();
            return first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION
                    ? 0 : last - first + 1;
        }
        return recyclerView.getChildCount();
    }

    private static void setVisibility(View root, int viewId, int visibility) {
        View view = root.findViewById(viewId);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private static int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }
}
