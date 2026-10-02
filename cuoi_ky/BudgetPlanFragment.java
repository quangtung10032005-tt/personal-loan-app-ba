package com.example.cuoi_ky;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetPlanFragment extends Fragment {

    private TextView tvCurrentPlanTitle, tvPlanExpected, tvPlanSpent, tvPlanRemaining;
    private ProgressBar pbPlanProgress;
    private LinearLayout layoutAlert;
    private TextView tvAlertMsg;
    private RecyclerView rvPlanList, rvExpensesInPlan, rvCategoryBudgets;
    private TextView btnAddNew;
    private ImageButton btnBack;

    private ExpenseManager expenseManager;
    private PlanAdapter planAdapter;
    private ExpenseAdapter expenseAdapter;
    private CategoryBudgetAdapter categoryBudgetAdapter;
    private Budget selectedPlan;
    private String currentUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences userPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserEmail = userPref.getString("current_user_email", "");

        expenseManager = ExpenseManager.getInstance(requireContext(), currentUserEmail);

        tvCurrentPlanTitle = view.findViewById(R.id.tvCurrentPlanTitle);
        tvPlanExpected     = view.findViewById(R.id.tvPlanExpected);
        tvPlanSpent        = view.findViewById(R.id.tvPlanSpent);
        tvPlanRemaining    = view.findViewById(R.id.tvPlanRemaining);
        pbPlanProgress     = view.findViewById(R.id.pbPlanProgress);
        layoutAlert        = view.findViewById(R.id.layoutAlert);
        tvAlertMsg         = view.findViewById(R.id.tvAlertMsg);
        rvPlanList         = view.findViewById(R.id.rvPlanList);
        rvExpensesInPlan   = view.findViewById(R.id.rvExpensesInPlan);
        rvCategoryBudgets  = view.findViewById(R.id.rvCategoryBudgets);
        btnAddNew          = view.findViewById(R.id.btnAddNew);
        btnBack            = view.findViewById(R.id.btnBack);

        expenseAdapter = new ExpenseAdapter(new ArrayList<>());
        rvExpensesInPlan.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpensesInPlan.setAdapter(expenseAdapter);

        categoryBudgetAdapter = new CategoryBudgetAdapter(new ArrayList<>());
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategoryBudgets.setAdapter(categoryBudgetAdapter);

        updatePlanList();

        btnAddNew.setOnClickListener(v -> showCreateBudgetDialog(null));
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottomNav);
                if (nav != null) nav.setSelectedItemId(R.id.nav_home);
            }
        });
    }

    private void updatePlanList() {
        List<Budget> allBudgets = expenseManager.getAllBudgets();
        List<Budget> mainBudgets = new ArrayList<>();
        for (Budget b : allBudgets) {
            if ("all".equals(b.getCategory())) mainBudgets.add(b);
        }

        if (planAdapter == null) {
            planAdapter = new PlanAdapter(mainBudgets, new OnPlanActionListener() {
                @Override public void onSelected(Budget plan) { onPlanSelected(plan); }
                @Override public void onEdit(Budget plan) { showCreateBudgetDialog(plan); }
                @Override public void onDelete() { updatePlanList(); }
            }, currentUserEmail, expenseManager);
            rvPlanList.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvPlanList.setAdapter(planAdapter);
        } else {
            planAdapter.updateData(mainBudgets);
        }

        if (!mainBudgets.isEmpty()) {
            if (selectedPlan == null) onPlanSelected(mainBudgets.get(0));
            else {
                boolean exists = false;
                for (Budget b : mainBudgets) { if (b.getId() == selectedPlan.getId()) { exists = true; break; } }
                if (!exists) onPlanSelected(mainBudgets.get(0));
                else onPlanSelected(selectedPlan);
            }
        } else {
            selectedPlan = null;
            resetUI();
        }
    }

    private void resetUI() {
        tvCurrentPlanTitle.setText("Chưa có kế hoạch");
        tvPlanExpected.setText("0đ");
        tvPlanSpent.setText("0đ");
        tvPlanRemaining.setText("0đ");
        pbPlanProgress.setProgress(0);
        layoutAlert.setVisibility(View.GONE);
        if (expenseAdapter != null) expenseAdapter.updateData(new ArrayList<>());
        if (categoryBudgetAdapter != null) categoryBudgetAdapter.updateData(new ArrayList<>());
    }

    private void onPlanSelected(Budget plan) {
        if (plan == null) return;
        this.selectedPlan = plan;
        if (planAdapter != null) planAdapter.setSelectedPlanId(plan.getId());
        updateSummaryCard();
        
        List<Expense> allExpenses = expenseManager.getAllExpenses();
        List<Budget> allBudgets = expenseManager.getAllBudgets();
        List<Budget> subBudgets = new ArrayList<>();
        String planMonth = plan.getMonth();

        for (Budget b : allBudgets) {
            if (planMonth.equals(b.getMonth()) && !"all".equals(b.getCategory())) subBudgets.add(b);
        }

        String[] standardCats = {"an-uong", "cafe", "xang-xe", "du-lich", "mua-sam", "tra-no"};
        List<Budget> fullList = new ArrayList<>();
        for (String catId : standardCats) {
            Budget found = null;
            for (Budget b : subBudgets) { if (catId.equals(b.getCategory())) { found = b; break; } }
            if (found != null) {
                fullList.add(found);
            } else {
                double realSpent = 0;
                for (Expense e : allExpenses) {
                    if (e.getDate() != null && e.getDate().startsWith(planMonth) && catId.equals(e.getCategory())) realSpent += e.getAmount();
                }
                fullList.add(new Budget(0, FormatUtils.getCategoryName(catId), 0, realSpent, catId, planMonth));
            }
        }
        categoryBudgetAdapter.updateData(fullList);

        List<Expense> filteredExpenses = new ArrayList<>();
        for(Expense e : allExpenses) {
            if (e.getDate() != null && e.getDate().startsWith(planMonth)) filteredExpenses.add(e);
        }
        if (expenseAdapter != null) expenseAdapter.updateData(filteredExpenses);
    }

    private void updateSummaryCard() {
        if (selectedPlan == null) return;
        tvCurrentPlanTitle.setText(selectedPlan.getName());
        tvPlanExpected.setText(FormatUtils.formatCurrency(selectedPlan.getTotalAmount()));
        tvPlanSpent.setText(FormatUtils.formatCurrency(selectedPlan.getUsedAmount()));
        double remaining = selectedPlan.getTotalAmount() - selectedPlan.getUsedAmount();
        tvPlanRemaining.setText(FormatUtils.formatCurrency(Math.max(0, remaining)));
        int progress = (int) (selectedPlan.getTotalAmount() > 0 ? (selectedPlan.getUsedAmount() / selectedPlan.getTotalAmount() * 100) : 0);
        pbPlanProgress.setProgress(Math.min(100, progress));
        if (selectedPlan.getUsedAmount() > selectedPlan.getTotalAmount()) {
            layoutAlert.setVisibility(View.VISIBLE);
            tvAlertMsg.setText("Bạn đã vượt ngân sách " + FormatUtils.formatCurrency(selectedPlan.getUsedAmount() - selectedPlan.getTotalAmount()) + ".");
        } else {
            layoutAlert.setVisibility(View.GONE);
        }
    }

    private void showCreateBudgetDialog(Budget existingBudget) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_budget, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialog).setView(dialogView).create();

        android.widget.Spinner spinnerQuickCategory = dialogView.findViewById(R.id.spinnerQuickCategory);
        EditText etQuickAmount   = dialogView.findViewById(R.id.etQuickAmount);
        Button btnAddCategory    = dialogView.findViewById(R.id.btnAddCategory);
        EditText etTotalBudget   = dialogView.findViewById(R.id.etTotalBudget);
        TextView tvBudgetError   = dialogView.findViewById(R.id.tvBudgetError);
        RecyclerView rvCategories = dialogView.findViewById(R.id.rvCategories);
        Button btnSavePlan       = dialogView.findViewById(R.id.btnSavePlan);
        Button btnCancel         = dialogView.findViewById(R.id.btnCancel);
        ImageButton btnClose     = dialogView.findViewById(R.id.btnClose);

        String[] categories = {"Ăn uống", "Cà phê", "Xăng xe", "Du lịch", "Mua sắm", "Trả nợ vay", "Khác"};
        spinnerQuickCategory.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories));

        if (existingBudget != null) {
            etTotalBudget.setText(String.valueOf((long)existingBudget.getTotalAmount()));
            btnSavePlan.setText("Cập nhật");
        }

        List<BudgetCategory> categoryList = new ArrayList<>();
        if (existingBudget != null) {
            String bMonth = existingBudget.getMonth();
            for (Budget b : expenseManager.getAllBudgets()) {
                if (bMonth.equals(b.getMonth()) && !"all".equals(b.getCategory())) {
                    String displayName = b.getName();
                    if (displayName.contains(" (")) displayName = displayName.substring(0, displayName.indexOf(" ("));
                    categoryList.add(new BudgetCategory(b.getId(), displayName, b.getTotalAmount()));
                }
            }
        }

        BudgetCategoryAdapter categoryAdapter = new BudgetCategoryAdapter(categoryList);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(categoryAdapter);

        Runnable validateBudget = () -> {
            double totalLimit = 0;
            try { totalLimit = Double.parseDouble(etTotalBudget.getText().toString()); } catch (Exception ignored){}
            double totalInList = 0;
            for (BudgetCategory bc : categoryList) totalInList += bc.amount;
            double inputAmount = 0;
            try { inputAmount = Double.parseDouble(etQuickAmount.getText().toString()); } catch (Exception ignored){}

            String currentName = spinnerQuickCategory.getSelectedItem().toString();
            double oldAmountInList = 0;
            for (BudgetCategory bc : categoryList) { if (bc.name.equals(currentName)) { oldAmountInList = bc.amount; break; } }

            double predictedTotal = totalInList - oldAmountInList + inputAmount;
            if (predictedTotal > totalLimit && totalLimit > 0 && inputAmount > 0) {
                tvBudgetError.setVisibility(View.VISIBLE);
                tvBudgetError.setText("Lỗi: Tổng các mục (" + FormatUtils.formatCurrency(predictedTotal) + ") vượt ngân sách (" + FormatUtils.formatCurrency(totalLimit) + ")");
                btnAddCategory.setEnabled(false); btnAddCategory.setAlpha(0.5f);
            } else {
                if (totalInList > totalLimit && totalLimit > 0) {
                    tvBudgetError.setVisibility(View.VISIBLE);
                    tvBudgetError.setText("Lỗi: Tổng danh mục hiện tại đã vượt ngân sách!");
                    btnSavePlan.setEnabled(false); btnSavePlan.setAlpha(0.5f);
                } else {
                    tvBudgetError.setVisibility(View.GONE);
                    btnSavePlan.setEnabled(totalLimit > 0); btnSavePlan.setAlpha(1.0f);
                }
                boolean canAdd = totalLimit > 0 && inputAmount > 0;
                btnAddCategory.setEnabled(canAdd); btnAddCategory.setAlpha(canAdd ? 1.0f : 0.5f);
            }
        };

        spinnerQuickCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { validateBudget.run(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        TextView tvMonthLabel = dialogView.findViewById(R.id.tvMonthLabel);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("'Tháng' MM yyyy", new Locale("vi")), sdfKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        if (existingBudget != null && !existingBudget.getMonth().isEmpty()) {
            try { calendar.setTime(new SimpleDateFormat("yyyy-MM").parse(existingBudget.getMonth())); } catch (Exception ignored) {}
        }
        tvMonthLabel.setText(sdfMonth.format(calendar.getTime()));

        dialogView.findViewById(R.id.btnSelectMonth).setOnClickListener(v -> {
            new android.app.DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year); calendar.set(Calendar.MONTH, month);
                tvMonthLabel.setText(sdfMonth.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        TextWatcher inputWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateBudget.run(); }
        };
        etTotalBudget.addTextChangedListener(inputWatcher);
        etQuickAmount.addTextChangedListener(inputWatcher);

        btnAddCategory.setOnClickListener(v -> {
            String name = spinnerQuickCategory.getSelectedItem().toString();
            String amtStr = etQuickAmount.getText().toString().trim();
            if (!amtStr.isEmpty()) {
                double amount = Double.parseDouble(amtStr);
                boolean found = false;
                for (BudgetCategory bc : categoryList) { if (bc.name.equals(name)) { bc.amount = amount; found = true; break; } }
                if (!found) categoryList.add(new BudgetCategory(0, name, amount));
                categoryAdapter.notifyDataSetChanged();
                etQuickAmount.setText(""); validateBudget.run();
            }
        });

        btnSavePlan.setOnClickListener(v -> {
            String monthStr = tvMonthLabel.getText().toString(), monthKey = sdfKey.format(calendar.getTime());
            double totalLimit = 0;
            try { totalLimit = Double.parseDouble(etTotalBudget.getText().toString()); } catch (Exception ignored){}
            if (totalLimit <= 0) return;

            if (existingBudget != null && !existingBudget.getMonth().equals(monthKey)) expenseManager.deleteBudgetsByMonth(existingBudget.getMonth());

            Budget mainBudget = existingBudget != null ? existingBudget : new Budget(0, "Tổng ngân sách " + monthStr, totalLimit, 0, "all", monthKey);
            mainBudget.setTotalAmount(totalLimit); mainBudget.setMonth(monthKey); mainBudget.setName("Tổng ngân sách " + monthStr);

            List<Budget> toSave = new ArrayList<>();
            toSave.add(mainBudget);
            for (BudgetCategory bc : categoryList) toSave.add(new Budget(bc.id, bc.name + " (" + monthStr + ")", bc.amount, 0, getCategoryIdFromName(bc.name), monthKey));
            
            expenseManager.addBudgets(toSave);
            Toast.makeText(requireContext(), "Đã lưu!", Toast.LENGTH_SHORT).show();
            dialog.dismiss(); updatePlanList();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        validateBudget.run();
        dialog.show();
    }

    private String getCategoryIdFromName(String name) {
        String n = name.toLowerCase();
        if (n.contains("ăn")) return "an-uong";
        if (n.contains("cafe") || n.contains("phê")) return "cafe";
        if (n.contains("xăng") || n.contains("xe")) return "xang-xe";
        if (n.contains("lịch")) return "du-lich";
        if (n.contains("sắm")) return "mua-sam";
        if (n.contains("nợ")) return "tra-no";
        return "other";
    }

    private static class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder> {
        private List<Budget> list;
        CategoryBudgetAdapter(List<Budget> list) { this.list = list; }
        void updateData(List<Budget> newList) { this.list = newList; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_category_report, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            Budget b = list.get(p);
            h.tvName.setText(b.getName()); h.tvIcon.setText(FormatUtils.getCategoryIcon(b.getCategory()));
            h.tvAmount.setText(FormatUtils.formatCurrency(b.getUsedAmount()) + " / " + FormatUtils.formatCurrency(b.getTotalAmount()));
            int progress = b.getUsedPercent();
            h.itemView.post(() -> {
                h.progressView.getLayoutParams().width = (int) (h.progressContainer.getWidth() * (progress / 100.0));
                h.progressView.requestLayout();
            });
            h.tvPercent.setText(progress + "% danh mục này");
            h.progressView.setBackgroundColor(progress > 100 ? 0xFFEF4444 : 0xFF4F46E5);
        }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount, tvPercent, tvIcon; View progressView, progressContainer;
            ViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tvCatName); tvIcon = v.findViewById(R.id.tvCatIcon); tvAmount = v.findViewById(R.id.tvCatAmount); tvPercent = v.findViewById(R.id.tvCatPercentLabel); progressView = v.findViewById(R.id.progressCat); progressContainer = (View) progressView.getParent(); }
        }
    }

    private interface OnPlanActionListener { void onSelected(Budget plan); void onEdit(Budget plan); void onDelete(); }

    private static class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {
        private List<Budget> list; private final OnPlanActionListener listener; private final String userEmail; private final ExpenseManager expenseManager; private long selectedId = -1;
        PlanAdapter(List<Budget> list, OnPlanActionListener listener, String email, ExpenseManager manager) { this.list = list; this.listener = listener; this.userEmail = email; this.expenseManager = manager; }
        void updateData(List<Budget> newList) { this.list = newList; notifyDataSetChanged(); }
        void setSelectedPlanId(long id) { this.selectedId = id; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_plan, parent, false)); }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Budget item = list.get(position); holder.tvTitle.setText(item.getName()); holder.tvBudget.setText("Ngân sách: " + FormatUtils.formatCurrency(item.getTotalAmount()));
            boolean isSelected = item.getId() == selectedId;
            holder.cardPlan.setBackgroundResource(isSelected ? R.drawable.bg_button_primary : R.drawable.bg_card_white);
            holder.tvTitle.setTextColor(isSelected ? 0xFFFFFFFF : 0xFF1F2937); holder.rvItemCategories.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            if (isSelected) {
                List<Budget> sub = new ArrayList<>();
                for (Budget b : expenseManager.getAllBudgets()) { if (item.getMonth().equals(b.getMonth()) && !"all".equals(b.getCategory())) sub.add(b); }
                holder.rvItemCategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                holder.rvItemCategories.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new RecyclerView.ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_budget_category_dialog, p, false)) {}; }
                    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int p) {
                        Budget b = sub.get(p);
                        ((TextView)h.itemView.findViewById(R.id.tvCategoryName)).setText(FormatUtils.getCategoryIcon(b.getCategory()) + " " + FormatUtils.getCategoryName(b.getCategory()));
                        ((TextView)h.itemView.findViewById(R.id.tvCategoryName)).setTextColor(0xFFFFFFFF);
                        ((TextView)h.itemView.findViewById(R.id.tvCategoryAmount)).setText(FormatUtils.formatCurrency(b.getTotalAmount()));
                        ((TextView)h.itemView.findViewById(R.id.tvCategoryAmount)).setTextColor(0xCCFFFFFF);
                    }
                    @Override public int getItemCount() { return sub.size(); }
                });
            }
            holder.itemView.setOnClickListener(v -> listener.onSelected(item));
            holder.btnDelete.setOnClickListener(v -> { expenseManager.deleteBudgetsByMonth(item.getMonth()); listener.onDelete(); });
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvBudget, tvStatus, btnDelete, btnEdit; LinearLayout cardPlan; RecyclerView rvItemCategories;
            ViewHolder(View v) { super(v); tvTitle = v.findViewById(R.id.tvItemTitle); tvBudget = v.findViewById(R.id.tvItemBudget); tvStatus = v.findViewById(R.id.tvItemStatus); btnDelete = v.findViewById(R.id.btnDeletePlan); btnEdit = v.findViewById(R.id.btnEditPlan); cardPlan = v.findViewById(R.id.cardPlan); rvItemCategories = v.findViewById(R.id.rvItemCategories); }
        }
    }

    private static class BudgetCategory {
        long id; String name; double amount;
        BudgetCategory(long id, String name, double amount) { this.id = id; this.name = name; this.amount = amount; }
    }

    private static class BudgetCategoryAdapter extends RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder> {
        private final List<BudgetCategory> list;
        BudgetCategoryAdapter(List<BudgetCategory> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_budget_category_dialog, p, false)); }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) { h.tvName.setText(list.get(p).name); h.tvAmount.setText(FormatUtils.formatCurrency(list.get(p).amount)); }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount;
            ViewHolder(View v) { super(v); tvName = v.findViewById(R.id.tvCategoryName); tvAmount = v.findViewById(R.id.tvCategoryAmount); }
        }
    }
}
