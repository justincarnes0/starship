package app.justincarnes.shipping;

import java.sql.*;
import java.util.ArrayList;
import java.io.File;

public class DatabaseManager 
{
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/starfishcustomers";
	
	static final String USERNAME = "shipping";
	static final String PASSWORD = "9146";
	
	ShippingProgramGUI gui = null;
	
	ArrayList<String> custKeys = new ArrayList<String>();		//This will hold all customer primary keys
	ArrayList<String> siteKeys = new ArrayList<String>();		//This will hold siteName for all sites of selected customer
	ArrayList<String> accNumKeys = new ArrayList<String>();		//This will hold serviceName for all accounts of selected customer
	
	String activeCustomer = "";
	String activeSite = "";
	
	public DatabaseManager(ShippingProgramGUI SPGui)
	{
		custKeys.add("--Select one--");
		siteKeys.add("--Select one--");
		accNumKeys.add("--Select one--");
		
		gui = SPGui;
		
		Connection conn = null;
		Statement custStmt = null;
		
		Statement acctStmt = null;
		
		try {
			Class.forName(JDBC_DRIVER);
			
			//TODO: Add a tooltip in bottom left of GUI window instead of printing to console
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			System.out.println("Creating statements...");
			custStmt = conn.createStatement();
			
			acctStmt = conn.createStatement();
			
			//Will initially store primary keys. Further info to be retrieved later using PKs
			String sql_Customer = "select custName from customer;";
			
			String sql_AccountNumber = "select custName, siteName, serviceName from accountnumber;";
			
			ResultSet customers = custStmt.executeQuery(sql_Customer);
			
			ResultSet accountNums = acctStmt.executeQuery(sql_AccountNumber);
			
			while(customers.next())
				custKeys.add(customers.getString("custName"));
			while(accountNums.next())
				accNumKeys.add(accountNums.getString("custName") + " :: " + accountNums.getString("siteName") + " :: " + accountNums.getString("serviceName"));
			
			customers.close();
			accountNums.close();
			custStmt.close();
			acctStmt.close();
			conn.close();
		} 	
		catch(SQLException se) {
				se.printStackTrace(); }
		catch(Exception e) {
				e.printStackTrace(); }	
		
		finally {
			try { if(custStmt != null) custStmt.close(); } catch(SQLException se2) {}
			
			try { if(acctStmt != null) acctStmt.close(); } catch(SQLException se2) {}
			try { if(conn != null) conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
		}
	}
	
	public Object[] getCustList()
	{
		return custKeys.toArray();
	}
	
	public Object[] getSiteList(String custSelection)
	{
		activeCustomer = custSelection;
		
		Connection conn = null;
		Statement siteStmt = null;
		
		try {
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			siteStmt = conn.createStatement();
			String sql_Site = "select custName, siteName from site where custName=" + activeCustomer + ";";
			ResultSet sites = siteStmt.executeQuery(sql_Site);
			
			while(sites.next())
				siteKeys.add(sites.getString("siteName"));
			
			conn.close();
			siteStmt.close();
			sites.close();
			
		} catch(SQLException se) { se.printStackTrace(); }
		
		finally {
			try { if(conn != null) conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			try { if(siteStmt != null) siteStmt.close(); } catch(SQLException se2) {}
		}
		
		return siteKeys.toArray();
	}
	
	
}


