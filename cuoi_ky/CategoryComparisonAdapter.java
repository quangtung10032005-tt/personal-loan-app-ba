package com.example.cuoi_ky;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryComparisonAdapter extends RecyclerView.Adapter<CategoryComparisonAdapter.VH> {

    public static class ComparisonStat {
        public String category;
        public double prevAmount;
        public double thisAmount;

        public ComparisonStat(String category, double prevAmount, double thisAmount) {
            this.category = category;
            this.prevAmount = prevAmount;
            this.thisAmount = thisAmount;
        }
    }

    private List<ComparisonStat> items;

    public CategoryComparisonAdapter(List<ComparisonStat> items) {
        this.items = items;
    }

    public void updateData(List<ComparisonStat> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_comparison, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ComparisonStat stat = items.get(pos);
        h.tvName.setText(FormatUtils.getCategoryName(stat.category));
        h.tvDetail.setText("Tháng trước " + FormatUtils.formatCurrency(stat.prevAmount) +
                " -> Tháng nay " + FormatUtils.formatCurrency(stat.thisAmount));

        double diff = stat.thisAmount - stat.prevAmount;
        h.tvDiff.setText(FormatUtils.formatCurrency(Math.abs(diff)));

        if (diff >= 0) {
            h.tvArrow.setText("↑");
            h.tvArrow.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.report_green_text));
            h.tvDiff.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.report_green_text));
        } else {
            h.tvArrow.setText("↓");
            h.tvArrow.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.danger));
            h.tvDiff.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.danger));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDetail, tvArrow, tvDiff;

        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvCompCatName);
            tvDetail = v.findViewById(R.id.tvCompDetail);
            tvArrow  = v.findViewById(R.id.tvCompArrow);
            tvDiff   = v.findViewById(R.id.tvCompDiff);
        }
    }
}
