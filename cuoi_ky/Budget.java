package com.example.cuoi_ky;

public class Budget {
    private long id;
    private String name;
    private double totalAmount;
    private double usedAmount;
    private String category;
    private String month; // Định dạng "yyyy-MM"

    public Budget() {}

    public Budget(long id, String name, double totalAmount, double usedAmount, String category) {
        this(id, name, totalAmount, usedAmount, category, "");
    }

    public Budget(long id, String name, double totalAmount, double usedAmount, String category, String month) {
        this.id = id;
        this.name = name;
        this.totalAmount = totalAmount;
        this.usedAmount = usedAmount;
        this.category = category;
        this.month = month;
    }

    public long getId()             { return id; }
    public String getName()         { return name; }
    public double getTotalAmount()  { return totalAmount; }
    public double getUsedAmount()   { return usedAmount; }
    public String getCategory()     { return category; }
    public String getMonth()        { return month; }

    public void setId(long id)                  { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setTotalAmount(double amount)   { this.totalAmount = amount; }
    public void setUsedAmount(double amount)    { this.usedAmount = amount; }
    public void setCategory(String category)    { this.category = category; }
    public void setMonth(String month)          { this.month = month; }

    /** Số tiền còn lại */
    public double getRemainingAmount() {
        return totalAmount - usedAmount;
    }

    /** Phần trăm đã dùng (0-100) */
    public int getUsedPercent() {
        if (totalAmount <= 0) return 0;
        return (int) Math.min(100, (usedAmount / totalAmount) * 100);
    }
}
