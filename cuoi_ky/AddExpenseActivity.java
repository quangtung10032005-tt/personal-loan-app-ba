package com.example.cuoi_ky;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDate, etNote;
    private TextView tvBudgetWarning;
    private ImageButton btnBack;
    private Button btnSave;

    private LinearLayout catFood, catCafe, catTravel, catGas, catShopping, catOther;
    private String selectedCategory = "an-uong";

    private final Calendar selectedDate = Calendar.getInstance();
    private DatabaseHelper dbHelper;
    private String currentUserEmail;
    private ExpenseManager expenseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        SharedPreferences userPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserEmail = userPref.getString("current_user_email", "");

        dbHelper = new DatabaseHelper(this);
        expenseManager = ExpenseManager.getInstance(this, currentUserEmail);

        etAmount = findViewById(R.id.etAmount);
        etDate   = findViewById(R.id.etDate);
        etNote   = findViewById(R.id.etNote);
        tvBudgetWarning = findViewById(R.id.tvBudgetWarning);
        btnBack  = findViewById(R.id.btnBack);
        btnSave  = findViewById(R.id.btnSaveExpense);

        // Theo dõi thay đổi số tiền để cập nhật cảnh báo ngân sách ngay lập tức
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkBudgetLimit(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        catFood     = findViewById(R.id.catFood);
        catCafe     = findViewById(R.id.catCafe);
        catTravel   = findViewById(R.id.catTravel);
        catGas      = findViewById(R.id.catGas);
        catShopping = findViewById(R.id.catShopping);
        catOther    = findViewById(R.id.catOther);

        updateDateDisplay();

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (dp, y, m, d) -> {
                selectedDate.set(y, m, d);
                updateDateDisplay();
                checkBudgetLimit();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        catFood.setOnClickListener(v     -> selectCategory("an-uong",  catFood));
        catCafe.setOnClickListener(v     -> selectCategory("cafe",     catCafe));
        catTravel.setOnClickListener(v   -> selectCategory("du-lich",  catTravel));
        catGas.setOnClickListener(v      -> selectCategory("xang-xe",  catGas));
        catShopping.setOnClickListener(v -> selectCategory("mua-sam",  catShopping));
        catOther.setOnClickListener(v    -> selectCategory("other",    catOther));

        selectCategory("an-uong", catFood);
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveExpense());
    }

    /**
     * BẪY LỖI QUAN TRỌNG:
     * Hàm này chạy mỗi khi người dùng quay lại màn hình này (ví dụ sau khi xóa ngân sách ở màn hình khác).
     * Nó giúp dữ liệu cảnh báo luôn đồng bộ với trạng thái mới nhất.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkBudgetLimit();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void selectCategory(String category, LinearLayout selected) {
        selectedCategory = category;
        for (LinearLayout cat : new LinearLayout[]{catFood, catCafe, catTravel, catGas, catShopping, catOther}) {
            cat.setBackgroundResource(R.drawable.bg_filter_inactive);
            for (int i = 0; i < cat.getChildCount(); i++) {
                View child = cat.getChildAt(i);
                if (child instanceof TextView) ((TextView) child).setTextColor(ContextCompat.getColor(this, R.color.gray_700));
            }
        }
        selected.setBackgroundResource(R.drawable.bg_filter_active);
        for (int i = 0; i < selected.getChildCount(); i++) {
            View child = selected.getChildAt(i);
            if (child instanceof TextView) ((TextView) child).setTextColor(0xFFFFFFFF);
        }
        checkBudgetLimit();
    }

    /**
     * Logic kiểm tra ngân sách thông minh:
     * 1. Tìm ngân sách riêng cho danh mục đã chọn.
     * 2. Nếu không có ngân sách riêng, kiểm tra ngân sách tổng (category="all").
     * 3. Nếu cả hai đều không có (đã bị xóa), ẩn ngay cảnh báo.
     */
    private void checkBudgetLimit() {
        if (tvBudgetWarning == null) return;

        String amountStr = etAmount.getText().toString().trim();
        double inputAmount = 0;
        try { if (!amountStr.isEmpty()) inputAmount = Double.parseDouble(amountStr); } catch (Exception ignored) {}

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String selectedMonth = sdf.format(selectedDate.getTime());

        // Đọc lại danh sách ngân sách mới nhất từ Manager (tránh dùng dữ liệu cũ)
        List<Budget> budgets = expenseManager.getAllBudgets();
        Budget specificBudget = null;
        Budget allBudget = null;

        for (Budget b : budgets) {
            if (selectedMonth.equals(b.getMonth())) {
                if (b.getCategory().equalsIgnoreCase(selectedCategory)) {
                    specificBudget = b;
                } else if (b.getCategory().equalsIgnoreCase("all")) {
                    allBudget = b;
                }
            }
        }

        // Ưu tiên hiển thị ngân sách danh mục cụ thể
        Budget target = (specificBudget != null) ? specificBudget : allBudget;

        if (target != null && target.getTotalAmount() > 0) {
            double remaining = target.getTotalAmount() - target.getUsedAmount();
            tvBudgetWarning.setVisibility(View.VISIBLE);

            String label = (specificBudget != null) ? FormatUtils.getCategoryName(selectedCategory) : "Tổng tháng";

            if (inputAmount > remaining) {
                tvBudgetWarning.setText("Cảnh báo: Vượt ngân sách " + label + "! (Còn lại: " + FormatUtils.formatCurrency(remaining) + ")");
                tvBudgetWarning.setTextColor(0xFFEF4444); // Màu đỏ cảnh báo
            } else {
                tvBudgetWarning.setText("Ngân sách " + label + " còn lại: " + FormatUtils.formatCurrency(remaining));
                tvBudgetWarning.setTextColor(0xFF10B981); // Màu xanh an toàn
            }
        } else {
            // NẾU NGÂN SÁCH ĐÃ BỊ XÓA: Ẩn ngay lập tức để không gây nhầm lẫn (Fix lỗi "cảnh báo ma")
            tvBudgetWarning.setVisibility(View.GONE);
        }
    }

    private void saveExpense() {
        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // --- BẪY LỖI: KIỂM TRA SỐ DƯ RÒNG ---
        String currentMonthYear = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(selectedDate.getTime());
        double monthIncome = dbHelper.getMonthIncome(currentUserEmail, currentMonthYear);
        double monthExpense = expenseManager.getMonthTotal();
        double netBalance = monthIncome - monthExpense;

        if (amount > netBalance) {
            Toast.makeText(this, "Lỗi: Số dư ròng không đủ! (Hiện có: " +
                    FormatUtils.formatCurrency(netBalance) + ")", Toast.LENGTH_LONG).show();
            return; // Ngăn chặn lưu chi tiêu
        }
        // ------------------------------------

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(selectedDate.getTime());

        Expense expense = new Expense(0, amount, selectedCategory, dateStr, etNote.getText().toString().trim());
        expenseManager.addExpense(expense);

        dbHelper.addNotification(currentUserEmail, "Chi tiêu mới", "Đã thêm chi tiêu " + FormatUtils.formatCurrency(amount) + " cho " + FormatUtils.getCategoryName(selectedCategory), "expense", expense.getId());

        Toast.makeText(this, "Đã thêm chi tiêu!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
