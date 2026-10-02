package com.example.cuoi_ky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FinanceMgr.db";
    private static final int DATABASE_VERSION = 7;

    private static final String TABLE_INCOME = "income";
    private static final String TABLE_INCOME_CATEGORY = "income_category";
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String TABLE_LOAN_HISTORY = "loan_history";

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "full_name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_PIN = "pin_hash";

    // Common column for ownership
    public static final String COL_OWNER_EMAIL = "owner_email";

    // New tables for Loan Module
    public static final String TABLE_LOAN = "tbl_loan";
    public static final String TABLE_LOAN_SCHEDULE = "tbl_loan_schedule";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_INCOME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, category TEXT, date TEXT, description TEXT, " + COL_OWNER_EMAIL + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_INCOME_CATEGORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT, type TEXT, target_id INTEGER, timestamp TEXT, " + COL_OWNER_EMAIL + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_LOAN_HISTORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, purpose TEXT, subject TEXT, amount REAL, years INTEGER, rate REAL, monthly_payment REAL, status TEXT, timestamp TEXT, " + COL_OWNER_EMAIL + " TEXT)");
        
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_PIN + " TEXT)");

        // tbl_loan: id, so_tien_vay, lai_suat, thoi_han_thang, ngay_vay, trang_thai, owner_email
        db.execSQL("CREATE TABLE " + TABLE_LOAN + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "so_tien_vay REAL, " +
                "lai_suat REAL, " +
                "thoi_han_thang INTEGER, " +
                "ngay_vay TEXT, " +
                "trang_thai TEXT, " +
                COL_OWNER_EMAIL + " TEXT)");

        // tbl_loan_schedule: id, loan_id, ky_han_thu_may, goc_phai_tra, lai_phai_tra, tong_phai_tra, trang_thai_da_tra
        db.execSQL("CREATE TABLE " + TABLE_LOAN_SCHEDULE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "loan_id INTEGER, " +
                "ky_han_thu_may INTEGER, " +
                "goc_phai_tra REAL, " +
                "lai_phai_tra REAL, " +
                "tong_phai_tra REAL, " +
                "trang_thai_da_tra INTEGER DEFAULT 0, " +
                "FOREIGN KEY(loan_id) REFERENCES " + TABLE_LOAN + "(id) ON DELETE CASCADE)");

        // Seed default categories
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Lương')");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Thưởng')");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Tiền lãi')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + TABLE_INCOME_CATEGORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Lương')");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Thưởng')");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_INCOME_CATEGORY + " (name) VALUES ('Tiền lãi')");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, message TEXT, type TEXT, target_id INTEGER, timestamp TEXT)");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE " + TABLE_LOAN_HISTORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, purpose TEXT, subject TEXT, amount REAL, years INTEGER, rate REAL, monthly_payment REAL, status TEXT, timestamp TEXT)");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE " + TABLE_LOAN + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "so_tien_vay REAL, " +
                    "lai_suat REAL, " +
                    "thoi_han_thang INTEGER, " +
                    "ngay_vay TEXT, " +
                    "trang_thai TEXT)");
            db.execSQL("CREATE TABLE " + TABLE_LOAN_SCHEDULE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "loan_id INTEGER, " +
                    "ky_han_thu_may INTEGER, " +
                    "goc_phai_tra REAL, " +
                    "lai_phai_tra REAL, " +
                    "tong_phai_tra REAL, " +
                    "trang_thai_da_tra INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(loan_id) REFERENCES " + TABLE_LOAN + "(id) ON DELETE CASCADE)");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_NAME + " TEXT, " +
                    COL_USER_EMAIL + " TEXT UNIQUE, " +
                    COL_USER_PASSWORD + " TEXT, " +
                    COL_USER_PIN + " TEXT)");
        }
        if (oldVersion < 7) {
            // Update tables to include COL_OWNER_EMAIL
            db.execSQL("ALTER TABLE " + TABLE_INCOME + " ADD COLUMN " + COL_OWNER_EMAIL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NOTIFICATIONS + " ADD COLUMN " + COL_OWNER_EMAIL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_LOAN_HISTORY + " ADD COLUMN " + COL_OWNER_EMAIL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_LOAN + " ADD COLUMN " + COL_OWNER_EMAIL + " TEXT");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // --- User Methods ---
    public long registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void updatePin(String email, String pinHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_PIN, pinHash);
        db.update(TABLE_USERS, values, COL_USER_EMAIL + "=?", new String[]{email});
    }

    public String getUserPin(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_PIN + " FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=?", new String[]{email});
        String pin = null;
        if (cursor.moveToFirst()) {
            pin = cursor.getString(0);
        }
        cursor.close();
        return pin;
    }

    // --- Loan Module Methods ---
    public long insertLoan(String ownerEmail, double amount, double rate, int months, String date, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("so_tien_vay", amount);
        values.put("lai_suat", rate);
        values.put("thoi_han_thang", months);
        values.put("ngay_vay", date);
        values.put("trang_thai", status);
        values.put(COL_OWNER_EMAIL, ownerEmail);
        return db.insert(TABLE_LOAN, null, values);
    }

    public void updateLoan(int loanId, double amount, double rate, int months, String date, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("so_tien_vay", amount);
        values.put("lai_suat", rate);
        values.put("thoi_han_thang", months);
        values.put("ngay_vay", date);
        values.put("trang_thai", status);
        db.update(TABLE_LOAN, values, "id = ?", new String[]{String.valueOf(loanId)});
        // Clear old schedule
        db.delete(TABLE_LOAN_SCHEDULE, "loan_id = ?", new String[]{String.valueOf(loanId)});
    }

    public void deleteLoan(int loanId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOAN, "id = ?", new String[]{String.valueOf(loanId)});
    }

    public void insertLoanSchedule(long loanId, int period, double principal, double interest, double total) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("loan_id", loanId);
        values.put("ky_han_thu_may", period);
        values.put("goc_phai_tra", principal);
        values.put("lai_phai_tra", interest);
        values.put("tong_phai_tra", total);
        values.put("trang_thai_da_tra", 0);
        db.insert(TABLE_LOAN_SCHEDULE, null, values);
    }

    public double getActiveMonthlyRepayment(String ownerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(tong_phai_tra) FROM " + TABLE_LOAN_SCHEDULE + 
                       " WHERE trang_thai_da_tra = 0 AND loan_id IN (SELECT id FROM " + TABLE_LOAN + " WHERE trang_thai = 'Đang hoạt động' AND " + COL_OWNER_EMAIL + " = ?)";
        Cursor cursor = db.rawQuery(query, new String[]{ownerEmail});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public double getTotalRemainingLoan(String ownerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(tong_phai_tra) FROM " + TABLE_LOAN_SCHEDULE + " WHERE trang_thai_da_tra = 0 AND loan_id IN (SELECT id FROM " + TABLE_LOAN + " WHERE " + COL_OWNER_EMAIL + " = ?)";
        Cursor cursor = db.rawQuery(query, new String[]{ownerEmail});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public Cursor getAllLoans(String ownerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LOAN + " WHERE " + COL_OWNER_EMAIL + " = ? ORDER BY id DESC", new String[]{ownerEmail});
    }

    public Cursor getLoanDetails(int loanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LOAN + " WHERE id = ?", new String[]{String.valueOf(loanId)});
    }

    public Cursor getLoanSchedule(int loanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LOAN_SCHEDULE + " WHERE loan_id = ? ORDER BY ky_han_thu_may ASC", new String[]{String.valueOf(loanId)});
    }

    public void updateScheduleStatus(int scheduleId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trang_thai_da_tra", status);
        db.update(TABLE_LOAN_SCHEDULE, values, "id = ?", new String[]{String.valueOf(scheduleId)});
    }

    public double getPaidAmount(int loanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(goc_phai_tra + lai_phai_tra) FROM " + TABLE_LOAN_SCHEDULE + " WHERE loan_id = ? AND trang_thai_da_tra = 1", new String[]{String.valueOf(loanId)});
        double paid = 0;
        if (cursor.moveToFirst()) paid = cursor.getDouble(0);
        cursor.close();
        return paid;
    }

    // --- Loan History Methods ---
    public long addLoanHistory(String ownerEmail, String purpose, String subject, double amount, int years, double rate, double monthlyPayment, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("purpose", purpose);
        values.put("subject", subject);
        values.put("amount", amount);
        values.put("years", years);
        values.put("rate", rate);
        values.put("monthly_payment", monthlyPayment);
        values.put("status", status);
        values.put(COL_OWNER_EMAIL, ownerEmail);
        values.put("timestamp", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
        return db.insert(TABLE_LOAN_HISTORY, null, values);
    }

    public List<LoanHistoryItem> getAllLoanHistory(String ownerEmail) {
        List<LoanHistoryItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOAN_HISTORY + " WHERE " + COL_OWNER_EMAIL + " = ? ORDER BY id DESC", new String[]{ownerEmail});
        if (cursor.moveToFirst()) {
            do {
                list.add(new LoanHistoryItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getInt(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getString(8)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- Income Methods ---
    public long addIncome(String ownerEmail, double amount, String cat, String date, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", cat);
        values.put("date", date);
        values.put("description", desc);
        values.put(COL_OWNER_EMAIL, ownerEmail);
        long id = db.insert(TABLE_INCOME, null, values);
        
        addNotification(ownerEmail, "Thu nhập mới", "Đã thêm " + String.format("%,.0fđ", amount) + " từ " + cat, "income", id);
        return id;
    }

    public List<Income> getAllIncomes(String ownerEmail) {
        List<Income> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INCOME + " WHERE " + COL_OWNER_EMAIL + " = ? ORDER BY id DESC", new String[]{ownerEmail});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Income(cursor.getInt(0), cursor.getDouble(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateIncome(String ownerEmail, int id, double amount, String cat, String date, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", cat);
        values.put("date", date);
        values.put("description", desc);
        db.update(TABLE_INCOME, values, "id=? AND " + COL_OWNER_EMAIL + "=?", new String[]{String.valueOf(id), ownerEmail});
        
        addNotification(ownerEmail, "Cập nhật thu nhập", "Thu nhập " + cat + " đã được thay đổi thành " + String.format("%,.0fđ", amount), "income", id);
    }

    public void deleteIncome(String ownerEmail, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INCOME, "id=? AND " + COL_OWNER_EMAIL + "=?", new String[]{String.valueOf(id), ownerEmail});
        addNotification(ownerEmail, "Xóa thu nhập", "Một mục thu nhập đã được gỡ bỏ", "income_history", -1);
    }

    public double getMonthIncome(String ownerEmail, String monthYear) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_INCOME + " WHERE " + COL_OWNER_EMAIL + " = ? AND date LIKE ?", new String[]{ownerEmail, "%/" + monthYear});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // --- Category Methods ---
    public void addIncomeCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        db.insertWithOnConflict(TABLE_INCOME_CATEGORY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<String> getAllIncomeCategories() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM " + TABLE_INCOME_CATEGORY, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- Notification Methods ---
    public void addNotification(String ownerEmail, String title, String message, String type, long targetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("message", message);
        values.put("type", type);
        values.put("target_id", targetId);
        values.put(COL_OWNER_EMAIL, ownerEmail);
        values.put("timestamp", new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date()));
        db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public List<Notification> getAllNotifications(String ownerEmail) {
        List<Notification> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + COL_OWNER_EMAIL + " = ? ORDER BY id DESC", new String[]{ownerEmail});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Notification(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getLong(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getUnreadNotificationsCount(String ownerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS + " WHERE " + COL_OWNER_EMAIL + " = ?", new String[]{ownerEmail});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
