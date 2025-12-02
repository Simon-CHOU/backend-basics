package com.example.mqlabs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TestDBConnection {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/mqlabs", 
                "postgres", "postgres");
            
            ResultSet rs = conn.createStatement().executeQuery("SELECT 1");
            if (rs.next()) {
                System.out.println("Database connection successful!");
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}