package app.justincarnes.shipping;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

//A class to manage the MySQL database containing Starfish customer information
//Fetches information from the database
//Adds new info to the database
//Creates backup files to rebuild the database in case of data loss
public class DatabaseManager 
{
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";	//Driver package
	static final String DB_URL 		= "jdbc:mysql://localhost/starfishcustomers";	//URL for database: for now, locally hosted
	
	//Database credentials
	static final String USERNAME = "shipping";
	static final String PASSWORD = "9146";
	
	private ShippingProgramGUI gui = null;	//Holds the current GUI instance
	
	private ArrayList<String> custKeys	 = new ArrayList<String>();	//This will hold all customer primary keys
	private ArrayList<String> siteKeys	 = new ArrayList<String>();	//This will hold siteName for all sites of selected customer
	private ArrayList<String> accNumKeys = new ArrayList<String>();	//This will hold serviceName for all accounts of selected customer
	
	private String activeCustomer 	= "";		//Keeps track of which customer is currently selected in the GUI combobox
	private String activeSite 		= "";		//Keeps track of which site is currently selected in the GUI combobox
	private String activeAcct 		= "";		//Keeps track of which account is currently selected in the GUI combobox
	
	private HashMap<String, ArrayList<String>> primaryKeys	= new HashMap<String, ArrayList<String>>();
	private HashMap<String, String> activeSelections 		= new HashMap<String, String>();
	
	///////////////////////////////////////////////
	//Constructor for the database manager object//
	///////////////////////////////////////////////
	
	//Currently accepts the active GUI instance, ideally console messages will be shown in the window in the future
	public DatabaseManager(ShippingProgramGUI SPGui)
	{
		gui = SPGui;
		
		primaryKeys.put("Customers", new ArrayList<String>());
		primaryKeys.put("Sites", 	 new ArrayList<String>());
		primaryKeys.put("Accounts",  new ArrayList<String>());
		
		activeSelections.put("Customers", "");
		activeSelections.put("Sites", 	  "");
		activeSelections.put("Accounts",  "");
		
		try { //to register the driver
			Class.forName(JDBC_DRIVER);
		}
		catch(Exception e) { e.printStackTrace(); }	//If there is a problem registering the driver
	}
	
	///////////////////////
	//Getters and setters//
	///////////////////////
	
	public Object[] getPrimaryKeyList(String tableName)
	{
		ArrayList<String> tableKeys = primaryKeys.get(tableName);
		String activeCustomer = activeSelections.get("Customers");
		String activeSite = activeSelections.get("Sites");
		//String activeAccount = activeSelections.get("Accounts");
		String fieldName = tableName.equals("Customers") ? "custName" : (tableName.equals("Sites") ? "siteName" : "serviceName");
		
		//If something is already in the list, flush it
		if(!tableKeys.isEmpty()) tableKeys.clear();		
				
		tableKeys.add("--Select one--");	//Gives the combobox a default value, will be an invalid selection
		tableKeys.add("--Add new--");		//If selected, will allow the user to add a new entry
		
		boolean invalidActiveCust = activeCustomer.equals("--Select one--") || activeCustomer.equals("");
		boolean invalidActiveSite = activeSite.equals("--Select one--") || activeSite.equals("");
		
		if(tableName.equals("Site") && invalidActiveCust) 							return new Object[1];
		if(tableName.equals("Account")&& (invalidActiveCust || invalidActiveSite))  return new Object[1];
		
		Connection conn = null;		//Will represent a connection with the database
		Statement stmt  = null;		//An object that will execute queries in the database
				
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);		
					
			stmt = conn.createStatement();
			String sql = "";
			if(tableName.equals("Customers"))
				sql = "select custName from customer;";			//Builds an SQL statement to get all custName values
			else if(tableName.equals("Sites"))
				sql = "select siteName from site where custName='" + activeCustomer + "';";
			else if(tableName.equals("Accounts")) 
				sql = "select serviceName from accountnumber where custName='" + activeCustomer + "' AND siteName='" + activeSite + "';";
			else System.out.println("Somehow the table name is incorrect.");
			
			ResultSet customers = stmt.executeQuery(sql);		//Takes the above query and executes it in the DB
					
			//Iterates through result set and adds all primary key values to the current AList
			while(customers.next()) tableKeys.add(customers.getString(fieldName));
					
			conn.close(); 		//Closes all database resources: these are not local so they will not be destroyed by garbage collection
			stmt.close();		//Connections, statements, and result sets are representations of database objects, and are thus held in the DB
			customers.close();	//If we don't close after opening, they will stay open in the DB indefinitely even after the Java variables are gone
					
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
				
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) 	conn.close();     } catch(SQLException se)  { se.printStackTrace(); }
			try { if(stmt != null)  stmt.close(); 	  } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
				
		return tableKeys.toArray();	//ComboBox can only accept arrays, not ALists
	}
	
	
	
	//A method to fetch the customer list from the MySQL database
	public Object[] getCustList()
	{
		//If something is already in the list, flush it
		if(!custKeys.isEmpty()) custKeys.clear();		
		
		custKeys.add("--Select one--");	//Gives the combobox a default value, will be an invalid selection
		custKeys.add("--Add new--");	//If selected, will allow the user to add a new customer
		
		Connection conn 	= null;			//Will represent a connection with the database
		Statement custStmt  = null;		//An object that will execute queries in the database
		
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);		
			
			custStmt = conn.createStatement();			
			String sql_Customer = "select custName from customer;";		//Builds an SQL statement to get all custName values
			ResultSet customers = custStmt.executeQuery(sql_Customer);	//Takes the above query and executes it in the DB
			
			//Iterates through result set and adds all "custName" values to the "custKeys" AList
			while(customers.next()) custKeys.add(customers.getString("custName"));
			
			conn.close(); 		//Closes all database resources: these are not local so they will not be destroyed by garbage collection
			custStmt.close();	//Connections, statements, and result sets are representations of database objects, and are thus held in the DB
			customers.close();	//If we don't close after opening, they will stay open in the DB indefinitely even after the Java variables are gone
			
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
		
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) 		conn.close();     } catch(SQLException se)  { se.printStackTrace(); }
			try { if(custStmt != null)  custStmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
		
		return custKeys.toArray();	//ComboBox can only accept arrays, not ALists
	}
	
	//A method to fetch the site list for the customer selected
	//Will never be run before getCustList, only called when a selection is made in the customer ComboBox
	//Less commented because a lot of similar stuff is well-documented in the above method
	public Object[] getSiteList()	
	{
		//Fail-safe if somehow "--Select one--" makes it here or customer has not been selected
		//Neither scenario should ever happen, but you know
		if(activeCustomer.equals("--Select one--") || activeCustomer.equals("")) return new Object[1];
		
		if(!siteKeys.isEmpty()) siteKeys.clear();
		
		siteKeys.add("--Select one--");
		siteKeys.add("--Add new--");
		
		Connection conn 	= null;
		Statement siteStmt  = null;
		
		try {
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			siteStmt = conn.createStatement();
			//This SQL statement must select only sites for the selected customer
			String sql_Site = "select custName, siteName from site where custName='" + activeCustomer + "';";
			ResultSet sites = siteStmt.executeQuery(sql_Site);
			
			while(sites.next()) siteKeys.add(sites.getString("siteName"));
			
			conn.close();
			siteStmt.close();
			sites.close();
			
		} catch(SQLException se) { se.printStackTrace(); }
		
		finally {
			try { if(conn != null) 		conn.close(); 	  }	catch(SQLException se)  { se.printStackTrace(); }
			try { if(siteStmt != null)  siteStmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
		
		return siteKeys.toArray();
	}
	
	//A method to fetch the account information for the selected site
	//Will never be called before customer or site are selected
	public Object[] getAccountList()
	{
		//Fail-safe if somehow "--Select one--" makes it to cust/site, or cust/site has not been selected
		//Neither scenario should ever happen, but you know
		if(activeCustomer.equals("--Select one--") || activeCustomer.equals("") || activeSite.equals("--Select one--") || activeSite.equals(""))
			return new Object[1];
		
		if(!accNumKeys.isEmpty()) accNumKeys.clear();
	
		accNumKeys.add("--Select one--");
		accNumKeys.add("--Add new--");
		
		Connection conn = null;
		Statement acctStmt = null;
		
		try {
			//TODO: Add a tooltip in bottom left of GUI window instead of printing to console
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			acctStmt = conn.createStatement();
			
			//Will initially store primary keys. Further info to be retrieved later using PKs
			//Only select keys for selected customer/site combo
			String sql_AccountNumber = "select custName, siteName, serviceName from accountnumber where custName='" 
					+ activeCustomer + "' and siteName='" + activeSite + "';";
			
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
	
	//Method to get active selections
	public String getActiveSelection(String tableName)
	{
		return activeSelections.get(tableName);
	}
	
	public String getActiveCustomer()
	{
		return activeCustomer;
	}
	
	public String getActiveSite()
	{
		return activeSite;
	}
	
	public String getActiveAccount()
	{
		return activeAcct;
	}
	
	//Methods to set active selections
	public void setActiveSelection(String tableName, String selection)
	{
		activeSelections.put(tableName, selection);
	}
	
	public void setActiveCustomer(String selection)
	{
		activeSelections.put("Customers", selection);
	}
	
	public void setActiveSite(String selection)
	{
		activeSelections.put("Sites", selection);
	}
	
	public void setActiveAccount(String selection)
	{
		activeSelections.put("Accounts", selection);
	}
	
	/////////////////////////////
	//Adding new database items//
	/////////////////////////////
	
	//A method to add a new, user-defined customer to the database
	//Accepts the three necessary values (custName, abbreviation, PPA) to create a customer
	public void addNewCustomer(String custName, String abbreviation, String PPA)
	{
		//Build an insert statement based on the given info
		String insertQuery = "insert into customer(custName, abbreviation, PPA)\n\tvalues('" 
				+ custName + "', '" 
				+ abbreviation + "' , '" 
				+ PPA + "');\n";
		
		addToInsertFile(insertQuery, 1);	//Passes the generated query to addToInsertFile
		
		runBasicQuery(insertQuery);
	}
	
	//A method to add a new, user-defined site to the database for the selected customer
	//Accepts all necessary fields to create a site except custName- uses globally stored activeCustomer instead
	public void addNewSite(String siteName, String street, String city, String state, String country, String zip)
	{
		String insertQuery = "insert into site(custName, siteName, streetAddress, cityAddress, stateAddress, countryAddress, zipAddress)"
				+ "\n\tvalues('" 
				+ activeCustomer + "', '"
				+ siteName + "', '"
				+ street + "', '"
				+ city + "', '"
				+ state + "', '"
				+ country + "', '"
				+ zip + "');";
		
		addToInsertFile(insertQuery, 2);
		
		runBasicQuery(insertQuery);
	}
	
	//A method to ensure new additions are added to the original SQL files
	//This provides me with a way to easily rebuild the database if there is a data loss
	//Accepts the SQL query generated in the addNew____ methods
	//fileSelection will be either 1, 2, or 3. Not worried about invalid numbers since this will only be called internally
	private void addToInsertFile(String insertQuery, int fileSelection)
	{
		//The path of the file to be written to
		//Selects 1 of 3 insert files based on the value of the second arg
		String filepath = "sql/ShipmentProgramInsert-" + (fileSelection == 1 ? "Customer" : fileSelection == 2 ? "Site" : "Accounts") + ".sql";
		
		try {	//to point a FileWriter/PrintWriter combo at the desired SQL file
			FileWriter fw = new FileWriter(new File(filepath), true); //Append to file
			PrintWriter pw = new PrintWriter(fw);
			pw.println(insertQuery);	//Add to the file
			pw.close();					//Cleanup
			fw.close();
		} catch (FileNotFoundException e) { e.printStackTrace(); }	//If the file isn't found
		  catch (IOException ie) { ie.printStackTrace(); }			//If the file can't be written to
		
	}
	
	////////////////////////////
	//General helper functions//
	////////////////////////////
	
	//A method to run a query where no data needs to be extracted from the ResultSet
	private void runBasicQuery(String sql)
	{
		Connection conn = null;
		Statement stmt = null;
		
		try { //to connect to database, execute the generated statement, and close the connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
			
			conn.close();
			stmt.close();
		} catch(SQLException se) { se.printStackTrace(); } //If the SQL fails
		
		finally { //Close any instantiated DB objects no matter what
			try { if (conn != null) conn.close(); } catch(SQLException se) {se.printStackTrace();}
			try { if (stmt != null) stmt.close(); } catch(SQLException se2) {}
		}
	}


}