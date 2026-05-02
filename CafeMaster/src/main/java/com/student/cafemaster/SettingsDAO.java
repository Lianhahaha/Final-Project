package com.student.cafemaster;

import java.sql.*;

public class SettingsDAO {

    public static String[] getSettings() throws SQLException {
        // Returns {shopName, adminName}
        String[] sets = {"CafeMaster", "Admin User"};
        String sql = "SELECT shop_name, admin_name FROM settings WHERE id = 1";
        try (Connection conn = Database.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                sets[0] = rs.getString("shop_name");
                sets[1] = rs.getString("admin_name");
            }
        }
        return sets;
    }

    public static void saveSettings(String shop, String admin) throws SQLException {
        String sql = "UPDATE settings SET shop_name = ?, admin_name = ? WHERE id = 1";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shop);
            ps.setString(2, admin);
            ps.executeUpdate();
        }
    }
}
