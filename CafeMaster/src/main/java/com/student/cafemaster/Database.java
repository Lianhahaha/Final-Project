package com.student.cafemaster;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    public static Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Using your actual MySQL password from previous steps
           // Change the end of the URL from /cafe_db to /CafeMaster
Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/CafeMaster", "root", "Lianjustinecruz292006");
            return connect;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
