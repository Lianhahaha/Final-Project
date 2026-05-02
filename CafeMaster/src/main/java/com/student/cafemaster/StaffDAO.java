package com.student.cafemaster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class StaffDAO {

    public static ObservableList<DashboardController.StaffMember> getAllStaff() throws SQLException {
        ObservableList<DashboardController.StaffMember> list = FXCollections.observableArrayList();
        String sql = "SELECT full_name, role, status FROM staff";
        try (Connection conn = Database.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DashboardController.StaffMember(
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("status")
                ));
            }
        }
        return list;
    }

    public static void addStaff(DashboardController.StaffMember staff) throws SQLException {
        String sql = "INSERT INTO staff (full_name, role, status) VALUES (?, ?, ?)";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getName());
            ps.setString(2, staff.getRole());
            ps.setString(3, staff.getStatus());
            ps.executeUpdate();
        }
    }

    public static void removeStaff(String name) throws SQLException {
        String sql = "DELETE FROM staff WHERE full_name = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}
