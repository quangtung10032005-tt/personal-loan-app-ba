package com.example.cuoi_ky;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {
    private EditText etAmount, etDate, etDesc, etNewCategory;
    private Spinner spCategory;
    private Button btnSaveBottom, btnAddCat;
    private TextView tvSaveHeader;
    private DatabaseHelper dbHelper;
    private Income editIncome = null;
    private Calendar calendar = Calendar.getInstance();
    private ArrayAdapter<String> categoryAdapter;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        SharedPreferences userPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserEmail = userPref.getString("current_user_email", "");

        dbHelper = new DatabaseHelper(this);
        etAmount = findViewById(R.id.etAmount);
        spCategory = findViewById(R.id.spCategory);
        etNewCategory = findViewById(R.id.etNewCategory);
        etDate = findViewById(R.id.etDate);
        etDesc = findViewById(R.id.etDesc);
        btnSaveBottom = findViewById(R.id.btnSaveIncome);
        btnAddCat = findViewById(R.id.btnAddCat);
        tvSaveHeader = findViewById(R.id.tvSave);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Load Categories
        refreshCategories();

        // Thiết lập ngày mặc định
        updateDateLabel();

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Add Category Logic
        btnAddCat.setOnClickListener(v -> {
            String newCat = etNewCategory.getText().toString().trim();
            if (!newCat.isEmpty()) {
                dbHelper.addIncomeCategory(newCat);
                refreshCategories();
                etNewCategory.setText("");
                // Select the newly added category
                for (int i = 0; i < spCategory.getCount(); i++) {
                    if (spCategory.getItemAtPosition(i).toString().equals(newCat)) {
                        spCategory.setSelection(i);
                        break;
                    }
                }
                Toast.makeText(this, "Đã thêm danh mục: " + newCat, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            }
        });

        // Kiểm tra xem là Sửa hay Thêm mới
        if (getIntent().hasExtra("INCOME_DATA")) {
            editIncome = (Income) getIntent().getSerializableExtra("INCOME_DATA");
            if (editIncome != null) {
                etAmount.setText(String.valueOf(editIncome.getAmount()));
                etDate.setText(editIncome.getDate());
                etDesc.setText(editIncome.getDescription());
                btnSaveBottom.setText("Cập nhật thu nhập");
                
                // Set spinner selection
                for (int i = 0; i < spCategory.getCount(); i++) {
                    if (spCategory.getItemAtPosition(i).toString().equals(editIncome.getCategory())) {
                        spCategory.setSelection(i);
                        break;
                    }
                }
            }
        }

        btnSaveBottom.setOnClickListener(v -> saveIncome());
        tvSaveHeader.setOnClickListener(v -> saveIncome());
    }

    private void refreshCategories() {
        List<String> categories = dbHelper.getAllIncomeCategories();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(categoryAdapter);
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveIncome() {
        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ");
            return;
        }

        String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "Khác";
        String date = etDate.getText().toString();
        String desc = etDesc.getText().toString();

        if (editIncome == null) {
            dbHelper.addIncome(currentUserEmail, amount, category, date, desc);
            Toast.makeText(this, "Đã thêm thu nhập", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateIncome(currentUserEmail, editIncome.getId(), amount, category, date, desc);
            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
