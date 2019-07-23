package app.justincarnes.shipping;

import java.sql.*;
import java.util.ArrayList;
import java.io.File;

//A class to manage the MySQL database containing Starfish customer information
//Fetches information from the database
//Adds new info to the database
//Creates backup files to rebuild the database in case of data loss
public class DatabaseManager 
{
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";	//Driver package
	static final String DB_URL = "jdbc:mysql://localhost/starfishcustomers";	//URL for database: for now, locally hosted
	
	//Database credentials
	static final String USERNAME = "shipping";
	static final String PASSWORD = "9146";
	
	ShippingProgramGUI gui = null;	//Holds the current GUI instance
	
	ArrayList<String> custKeys = new ArrayList<String>();	//This will hold all customer primary keys
	ArrayList<String> siteKeys = new ArrayList<String>();	//This will hold siteName for all sites of selected customer
	ArrayList<String> accNumKeys = new ArrayList<String>();	//This will hold serviceName for all accounts of selected customer
	
	String activeCustomer = "";		//Keeps track of what customer is currently selected in the GUI combobox
	String activeSite = "";			//Keeps track of which site is currently selected in the GUI combobox
	
	//Constructor for the database manager object
	//Currently accepts the active GUI instance, ideally console messages will be shown in the window in the future
	public DatabaseManager(ShippingProgramGUI SPGui)
	{
		gui = SPGui;
		try {	//to register the driver and retrieve the list of customers
			Class.forName(JDBC_DRIVER);
			getCustList();
		}
		catch(Exception e) { e.printStackTrace(); }	//If there is a problem registering the driver
	}
	
	//A method to fetch the customer list from the MySQL database
	public Object[] getCustList()
	{
		//If something is already in the list, flush it
		if(!custKeys.isEmpty()) 
			custKeys.clear();		
		
		custKeys.add("--Select one--");	//Gives the combobox a default value, will be an invalid selection
		
		Connection conn = null;			//Will represent a connection with the database
		Statement custStmt = null;		//An object that will execute queries in the database
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);		
			
			custStmt = conn.createStatement();			
			String sql_Customer = "select custName from customer;";
			ResultSet customers = custStmt.executeQuery(sql_Customer);	//Takes the above query and executes it in the DB
			
			//Iterates through result set and adds all "custName" values to the "custKeys" AList
			while(customers.next())
				custKeys.add(customers.getString("custName"));
			
			conn.close(); 		//Closes all database resources: these are not local so they will not be destroyed by garbage collection
			custStmt.close();	//Connections, statements, and result sets are representations of database objects, and are thus held in the DB
			customers.close();	//If we don't close after opening, they will stay open in the DB indefinitely even after the Java variables are gone
			
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
		
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			try { if(custStmt != null) custStmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
		
		return custKeys.toArray();	//ComboBox can only accept arrays, not ALists
	}
	
	//A method to fetch the site list for the customer selected
	//Will never be run before getCustList, only called when a selection is made in the customer ComboBox
	public Object[] getSiteList(String custSelection)	
	{
		activeCustomer = custSelection;	//Saves the customer that the user currently has selected
		
		//Fail-safe if somehow "--Select one--" makes it here or customer has not been selected
		//Neither scenario should ever happen, but you know
		if(activeCustomer.equals("--Select one--") || activeCustomer.equals("")) return new Object[1];
		
		if(!siteKeys.isEmpty())
			siteKeys.clear();
		
		siteKeys.add("--Select one--");
		
		Connection conn = null;
		Statement siteStmt = null;
		
		try {
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			siteStmt = conn.createStatement();
			//This SQL statement must select only sites for the selected customer
			String sql_Site = "select custName, siteName from site where custName='" + activeCustomer + "';";
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
			System.out.println("DB connection terminated.");
		}
		
		return siteKeys.toArray();
	}
	
	//A method to fetch the account information for the selected site
	//Will never be called before customer or site are selected
	public Object[] getAccountList(String siteSelection)
	{
		activeSite = siteSelection;
		
		//Fail-safe if somehow "--Select one--" makes it to cust/site, or cust/site has not been selected
		//Neither scenario should ever happen, but you know
		if(activeCustomer.equals("--Select one--") || activeCustomer.equals("") || activeSite.equals("--Select one--") || activeSite.equals(""))
			return new Object[1];
		
		if(!accNumKeys.isEmpty())
			accNumKeys.clear();
	
		accNumKeys.add("--Select one--");
		
		Connection conn = null;
		Statement acctStmt = null;
		
		try {
			//TODO: Add a tooltip in bottom left of GUI window instead of printing to console
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			acctStmt = conn.createStatement();
			
			//Will initially store primary keys. Further info to be retrieved later using PKs
			//Only select keys for selected customer/site combo
			String sql_AccountNumber = "select custName, siteName, serviceName from accountnumber where custName='" + activeCustomer + "' and siteName='" + activeSite + "';";
			ResultSet accountNums = acctStmt.executeQuery(sql_AccountNumber);
			while(accountNums.next())
				accNumKeys.add(accountNums.getString("serviceName"));
			
			conn.close();
			acctStmt.close();
			accountNums.close();
			
		} 	catch(SQLException se) { se.printStackTrace(); }
		
		finally {
			try { if(acctStmt != null) acctStmt.close(); } catch(SQLException se2) {}
			try { if(conn != null) conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			System.out.println("DB connection terminated.");
		}
		
		return accNumKeys.toArray();
	}
	
}


