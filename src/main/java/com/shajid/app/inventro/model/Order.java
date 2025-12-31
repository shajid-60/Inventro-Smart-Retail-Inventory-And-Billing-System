// Java
package com.shajid.app.inventro.model;

public class Order {
    private Integer id;
    private String supplier; // or "Customer" / customer name
    private String date;     // ISO string
    private double total;    // sum of sold prices
    private String status;
    private double revenue;  // total - base cost

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
}
