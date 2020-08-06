package com.books.test;

import com.books.utils.BookConstant;
import com.company.core.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnTest {
    public static void main(String[] args) {

        String test = "CREATE TABLE IF NOT EXISTS `book` (" +
                "`id` int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "`name` varchar(40) NOT NULL default ''" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        try {
            PreparedStatement state = DatabaseUtils.getConn(BookConstant.DATA_BASE).prepareStatement(test);
            state.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
