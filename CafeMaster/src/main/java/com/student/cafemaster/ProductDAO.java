package com.student.cafemaster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

/**
 * Data Access Object for the `product` table in CafeMaster DB.
 *
 * DB Schema:
 *   product (
 *     id            INT PRIMARY KEY,
 *     product_name  VARCHAR(100) NOT NULL,
 *     type          VARCHAR(50),
 *     stock         INT,
 *     price         DOUBLE
 *   )
 */
public class ProductDAO {

    // ─── READ ────────────────────────────────────────────────────────────────
    public static ObservableList<productData> getAllProducts() throws SQLException {
        ObservableList<productData> list = FXCollections.observableArrayList();
        String sql = "SELECT id, product_name, type, stock, price FROM product ORDER BY id";

        try (Connection conn = Database.connectDB();
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new productData(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("product_name"),
                    rs.getString("type"),
                    rs.getInt("stock"),
                    rs.getDouble("price")
                ));
            }
        }
        return list;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    public static void insert(productData p) throws SQLException {
        String sql = "INSERT INTO product (id, product_name, type, stock, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn      = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    Integer.parseInt(p.getProductID()));
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getType());
            ps.setInt(4,    p.getStock());
            ps.setDouble(5, p.getPrice());
            ps.executeUpdate();
        }
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────
    public static void update(productData p, int originalId) throws SQLException {
        String sql = "UPDATE product SET id=?, product_name=?, type=?, stock=?, price=? WHERE id=?";

        try (Connection conn      = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    Integer.parseInt(p.getProductID()));
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getType());
            ps.setInt(4,    p.getStock());
            ps.setDouble(5, p.getPrice());
            ps.setInt(6,    originalId);
            ps.executeUpdate();
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────
    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM product WHERE id = ?";

        try (Connection conn      = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── UTILITY ─────────────────────────────────────────────────────────────
    public static boolean idExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM product WHERE id = ?";

        try (Connection conn      = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // ─── ADDED FOR ORDERS ────────────────────────────────────────────────────
    public static void deductStock(Connection conn, String productName, int qty) throws SQLException {
        String sql = "UPDATE product SET stock = stock - ? WHERE product_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, productName);
            ps.executeUpdate();
        }
    }

    public static int getStockByName(Connection conn, String productName) throws SQLException {
        String sql = "SELECT stock FROM product WHERE product_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
