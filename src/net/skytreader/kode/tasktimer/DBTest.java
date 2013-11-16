package net.skytreader.kode.tasktimer;

//import java.sql.*;
//import javax.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
Test class for JDBC stuff.

Coolness: I can access the databases I created in phpMyAdmin in JDBC since
I used the MySQL connector.

@author Chad Estioco
@version Summer 2011
*/
public class DBTest{
	public static void main(String[] args) throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		Connection dbCon = DriverManager.getConnection("jdbc:mysql://localhost/mysql", "root", "");
		Statement stmt = dbCon.createStatement();
		/*
		According to javadocs, executeUpdate(String) is used only for INSERT, UPDATE or DELETE statements.
		Seems that it's valid for CREATE statements too!
		*/
		stmt.executeUpdate("CREATE DATABASE coolstuff");
	}
}
