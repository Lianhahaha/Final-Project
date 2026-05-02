package com.student.cafemaster;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    
    // Save an order and its items, also logs stock
    public static void saveOrder(double total, double cash, List<DashboardController.OrderItem> cart, List<productData> currentProducts) throws SQLException {
        String sqlOrder = "INSERT INTO orders (total_amount, cash_tendered, order_date) VALUES (?, ?, ?)";
        String sqlItem = "INSERT INTO order_items (order_id, product_name, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String sqlStock = "INSERT INTO daily_stock (product_name, stock_remaining, record_date) VALUES (?, ?, ?)";
        
        Connection conn = Database.connectDB();
        conn.setAutoCommit(false);
        try {
            LocalDate today = LocalDate.now();
            
            // 1. Insert order
            int orderId = -1;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, total);
                ps.setDouble(2, cash);
                ps.setDate(3, Date.valueOf(today));
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) orderId = rs.getInt(1);
            }
            
            // 2. Insert order items & Update Inventory
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                for (DashboardController.OrderItem item : cart) {
                    psItem.setInt(1, orderId);
                    psItem.setString(2, item.getItem());
                    psItem.setInt(3, item.getQty());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.addBatch();
                    
                    // Deduct from inventory safely
                    ProductDAO.deductStock(conn, item.getItem(), item.getQty());
                }
                psItem.executeBatch();
            }
            
            // 3. Log daily stock (snapshot of END of day / current state)
            // Ideally we delete today's stock and re-insert to avoid duplicates per day
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM daily_stock WHERE record_date = ?")) {
                psDel.setDate(1, Date.valueOf(today));
                psDel.executeUpdate();
            }
            
            try (PreparedStatement psStock = conn.prepareStatement(sqlStock)) {
                for (productData p : currentProducts) {
                    psStock.setString(1, p.getProductName());
                    int liveStock = ProductDAO.getStockByName(conn, p.getProductName());
                    psStock.setInt(2, liveStock);
                    psStock.setDate(3, Date.valueOf(today));
                    psStock.addBatch();
                }
                psStock.executeBatch();
            }
            
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    // Reports queries
    public static Map<String, Double> getSalesByDate(LocalDate date) throws SQLException {
        Map<String, Double> sales = new LinkedHashMap<>();
        String sql = "SELECT oi.product_name, SUM(oi.quantity * oi.unit_price) as revenue " +
                     "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                     "WHERE o.order_date = ? GROUP BY oi.product_name";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sales.put(rs.getString("product_name"), rs.getDouble("revenue"));
            }
        }
        return sales;
    }
    
    public static int getOrderCountByDate(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_date = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
    
    public static Map<String, Integer> getStockHistoryByDate(LocalDate date) throws SQLException {
        Map<String, Integer> stock = new LinkedHashMap<>();
        String sql = "SELECT product_name, stock_remaining FROM daily_stock WHERE record_date = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stock.put(rs.getString("product_name"), rs.getInt("stock_remaining"));
            }
        }
        return stock;
    }
}
