package com.company;

import java.sql.*;
import java.util.ArrayList;

public class databaseStuff {
    Connection db;
    Object[][] table;
    String[] columnNames = {"Player Name", "Score"};
    int length;

    databaseStuff() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        db = DriverManager.getConnection("jdbc:postgresql://localhost:5432/scorelist", "newuser", "admin");

        updateTable();


    }

    public void updateTable() throws SQLException {

        Statement sqlStatement = db.createStatement();

        String SQL = "SELECT COUNT(*) FROM scorelist";
        ResultSet count = sqlStatement.executeQuery(SQL);
        count.next();
        length = count.getInt(1);

        table = new Object[length][2];

        SQL = "SELECT * FROM scorelist ORDER BY score DESC";
        ResultSet rs = sqlStatement.executeQuery(SQL);

        if(length == 0){
            return;
        }

        int i = 0;
        while(rs.next() && i < length){
            table[i][0] = rs.getString("names");
            table[i][1] = rs.getString("score");
            i++;
        }
    }

    public void addRecord(String name, int score) throws SQLException {
        String SQL = "INSERT INTO scorelist "
                + "VALUES(?,?,?)";

        PreparedStatement insertionStatement = db.prepareStatement(SQL);

        insertionStatement.setString(1, name);
        insertionStatement.setInt(2, score);
        insertionStatement.setInt(3,length + 1);
        insertionStatement.executeUpdate();
    }
}
