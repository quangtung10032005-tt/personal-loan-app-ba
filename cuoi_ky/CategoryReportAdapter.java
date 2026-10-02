package com.example.cuoi_ky;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.VH> {

    public static class CategoryStat {
        public String category;
        public double amount;
        public int count;
        public int percent;

        public CategoryStat(String category, double amount, int count, int percent) {
            this.category = category;
            this.amount = amount;
            this.count = count;
            this.percent = percent;
        }
    }

    private List<CategoryStat> items;

    public CategoryReportAdapter(List<CategoryStat> items) {
        this.items = items;
    }

    public void updateData(List<CategoryStat> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CategoryStat stat = items.get(pos);
        h.tvName.setText(FormatUtils.getCategoryName(stat.category));
        h.tvIcon.setText(FormatUtils.getCategoryIcon(stat.category));
        h.tvAmount.setText(FormatUtils.formatCurrency(stat.amount));
        h.tvPercentLabel.setText(stat.percent + "% của tổng chi tiêu");

        int color = getCategoryColor(h.itemView.getContext(), stat.category);
        h.progressCat.setBackgroundTintList(ColorStateList.valueOf(color));

        // Update progress bar width
        h.progressCat.post(() -> {
            int parentWidth = ((View) h.progressCat.getParent()).getWidth();
            ViewGroup.LayoutParams params = h.progressCat.getLayoutParams();
            params.width = (int) (parentWidth * stat.percent / 100.0);
            h.progressCat.setLayoutParams(params);
        });
    }

    private int getCategoryColor(android.content.Context context, String category) {
        switch (category) {
            case "an-uong": return ContextCompat.getColor(context, R.color.chart_blue);
            case "xang-xe": return ContextCompat.getColor(context, R.color.chart_orange);
            case "du-lich": return ContextCompat.getColor(context, R.color.chart_green);
            case "cafe":    return ContextCompat.getColor(context, R.color.chart_purple);
            default:        return ContextCompat.getColor(context, R.color.gray_400);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvPercentLabel, tvIcon;
        View progressCat;

        VH(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tvCatName);
            tvIcon      = v.findViewById(R.id.tvCatIcon);
            tvAmount    = v.findViewById(R.id.tvCatAmount);
            tvPercentLabel = v.findViewById(R.id.tvCatPercentLabel);
            progressCat = v.findViewById(R.id.progressCat);
        }
    }
}
