package app.justincarnes.shipping;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager 
{
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/starfishcustomers";
	
	static final String USERNAME = "root";
	static final String PASSWORD = "9146";
	
	ShippingProgramGUI gui = null;
	
	ArrayList<String> custKeys = new ArrayList<String>();
	ArrayList<String> siteKeys = new ArrayList<String>();
	ArrayList<String> accNumKeys = new ArrayList<String>();
	
	public DatabaseManager(ShippingProgramGUI SPGui)
	{
		gui = SPGui;
		
		Connection conn = null;
		Statement stmt = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			//TODO: Add a tooltip in bottom left of GUI window instead of printing to console
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			//Will initially store primary keys. Further info to be retrieved later using PKs
			String sql_Customer = "select custName from customer;";
			String sql_Site = "select custName, siteName from site;";
			String sql_AccountNumber = "select custName, siteName, serviceName from accountnumber;";
			
			conn.close();
			stmt.close();
			
			ResultSet customers = stmt.executeQuery(sql_Customer);
			ResultSet sites = stmt.executeQuery(sql_Site);
			ResultSet accountNums = stmt.executeQuery(sql_AccountNumber);
			
			while(customers.next())
				custKeys.add(customers.getString("custName"));
			while(sites.next())
				siteKeys.add(sites.getString("custName") + " :: " + sites.getString("siteName"));
			while(accountNums.next())
				accNumKeys.add(accountNums.getString("custName") + " :: " + accountNums.getString("siteName") + " :: " + accountNums.getString("serviceName"));
		
			customers.close();
			sites.close();
			accountNums.close();
			stmt.close();
			conn.close();
		} 	
		catch(SQLException se) {
				se.printStackTrace();}
		catch(Exception e) {
				e.printStackTrace();}	
		
		finally {
			try { if(stmt != null) stmt.close(); } catch(SQLException se2) {}
			try { if(conn != null) conn.close(); } catch(SQLException se) { se.printStackTrace(); }
		}
	}
}
