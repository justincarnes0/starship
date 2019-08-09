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
public class DatabaseManager 
{	
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";	//Driver package
	private static final String DB_URL 		= "jdbc:mysql://localhost/starfishcustomers";	//URL for database: for now, locally hosted
	
	//Database credentials
	private static final String USERNAME = "shipping";
	private static final String PASSWORD = "9146";
	
	private ShippingProgramGUI gui = null;	//Holds the current GUI instance
	
	private HashMap<Integer, ArrayList<String>> primaryKeys	= new HashMap<Integer, ArrayList<String>>();
	private HashMap<Integer, String> activeSelections 		= new HashMap<Integer, String>();
	
	///////////////////////////////////////////////
	//Constructor for the database manager object//
	///////////////////////////////////////////////
	
	//Currently accepts the active GUI instance, ideally console messages will be shown in the window in the future
	public DatabaseManager(ShippingProgramGUI SPGui)
	{
		gui = SPGui;
		
		primaryKeys.put(ShippingProgram.CUSTOMERS, new ArrayList<String>());
		primaryKeys.put(ShippingProgram.SITES, 	 new ArrayList<String>());
		primaryKeys.put(ShippingProgram.ACCOUNTS,  new ArrayList<String>());
		
		activeSelections.put(ShippingProgram.CUSTOMERS, "");
		activeSelections.put(ShippingProgram.SITES, 	  "");
		activeSelections.put(ShippingProgram.ACCOUNTS,  "");
		
		try { //to register the driver
			Class.forName(JDBC_DRIVER);
		}
		catch(Exception e) { e.printStackTrace(); }	//If there is a problem registering the driver
	}
	
	///////////////////////
	//Getters and setters//
	///////////////////////
	
	public Object[] getPrimaryKeyList(int tableName)
	{
		ArrayList<String> tableKeys = primaryKeys.get(tableName);
		String activeCustomer 		= activeSelections.get(ShippingProgram.CUSTOMERS);
		String activeSite 			= activeSelections.get(ShippingProgram.SITES);
		//Holds the name of the field to be retrieved for the active table
		String fieldName = ((tableName == ShippingProgram.CUSTOMERS) 
				? "custName" : (tableName == ShippingProgram.SITES ? "siteName" : "serviceName"));
		
		//If something is already in the list, flush it
		if(!tableKeys.isEmpty()) tableKeys.clear();		
				
		tableKeys.add(ShippingProgram.SELECT_ONE);	//Gives the combobox a default value, will be an invalid selection
		tableKeys.add(ShippingProgram.ADD_NEW);		//If selected, will allow the user to add a new entry
		
		boolean invalidActiveCust = activeCustomer.equals(ShippingProgram.SELECT_ONE) || activeCustomer.equals("");
		boolean invalidActiveSite = activeSite.equals(ShippingProgram.SELECT_ONE) || activeSite.equals("");
		
		//Return empty lists if somehow an invalid selection makes it here
		if(tableName == ShippingProgram.SITES 	 &&  invalidActiveCust) 					   return new Object[1];
		if(tableName == ShippingProgram.ACCOUNTS && (invalidActiveCust || invalidActiveSite))  return new Object[1];
		
		//Build an SQL statement to retrieve the desired primary key list
		String sql = "";
		if(tableName == ShippingProgram.CUSTOMERS)
			sql = "select custName from customer;";
		else if(tableName == ShippingProgram.SITES)
			sql = "select siteName from site where custName='" + activeCustomer + "';";
		else if(tableName == ShippingProgram.ACCOUNTS) 
			sql = "select serviceName from accountnumber where custName='" + activeCustomer + "' AND siteName='" + activeSite + "';";
		else System.out.println("Somehow the table name is incorrect.");
		
		runSelectQuery(tableKeys, fieldName, sql);
				
		return tableKeys.toArray();	//ComboBox can only accept arrays, not ALists
	}
	
	public String getActiveSelection(int tableName)
	{
		return activeSelections.get(tableName);
	}
	
	public void setActiveSelection(int tableName, String selection)
	{
		activeSelections.put(tableName, selection);
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
		
		addToInsertFile(insertQuery, ShippingProgram.CUSTOMERS);	//Passes the generated query to addToInsertFile
		
		runUpdateQuery(insertQuery);
	}
	
	//A method to add a new, user-defined site to the database for the selected customer
	//Accepts all necessary fields to create a site except custName- uses globally stored activeCustomer instead
	public void addNewSite(String siteName, String street, String city, String state, String country, String zip)
	{
		String insertQuery = "insert into site(custName, siteName, streetAddress, cityAddress, stateAddress, countryAddress, zipAddress)"
				+ "\n\tvalues('" 
				+ activeSelections.get(ShippingProgram.CUSTOMERS) + "', '"
				+ siteName + "', '"
				+ street + "', '"
				+ city + "', '"
				+ state + "', '"
				+ country + "', '"
				+ zip + "');";
		
		addToInsertFile(insertQuery, ShippingProgram.SITES);
		
		runUpdateQuery(insertQuery);
	}
	
	//A method to ensure new additions are added to the original SQL files
	//This provides me with a way to easily rebuild the database if there is a data loss
	//Accepts the SQL query generated in the addNew____ methods
	//fileSelection will be either 1, 2, or 3. Not worried about invalid numbers since this will only be called internally
	private void addToInsertFile(String insertQuery, int fileSelection)
	{
		//The path of the file to be written to
		//Selects 1 of 3 insert files based on the value of the second arg
		String filepath = "sql/ShipmentProgramInsert-" + (fileSelection == ShippingProgram.CUSTOMERS 
				? "Customer" : fileSelection == ShippingProgram.SITES ? "Site" : "Account") + ".sql";
		
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
	private void runUpdateQuery(String sql)
	{
		Connection conn = null;
		Statement stmt = null;
		
		try { //to connect to database, execute the generated statement, and close the connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			stmt = conn.createStatement();

			stmt.executeUpdate(sql);
			
			conn.close();
			stmt.close();
		} catch(SQLException se) { se.printStackTrace(); } //If the SQL fails
		
		finally { //Close any instantiated DB objects no matter what
			try { if (conn != null) conn.close(); } catch(SQLException se) {se.printStackTrace();}
			try { if (stmt != null) stmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
	}
	
	//A method to run a selection query and populate an ArrayList with the results
	//Accepts the AList to be populated, the name of the field for that specific table, and the sql query to be run
	private void runSelectQuery(ArrayList<String> tableKeys, String fieldName, String sql)
	{
		Connection conn = null;		//Will represent a connection with the database
		Statement stmt  = null;		//An object that will execute queries in the database
				
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);		
					
			stmt = conn.createStatement();
			
			ResultSet customers = stmt.executeQuery(sql);		//Takes the above query and executes it in the DB
					
			//Iterates through result set and adds all primary key values to the current AList
			while(customers.next()) tableKeys.add(customers.getString(fieldName));
					
			conn.close(); 		//Closes all database resources: these are not local so they will not be destroyed by garbage collection
			stmt.close();		//Connections, statements, and result sets are representations of database objects, and are thus held in the DB
			customers.close();	//If we don't close after opening, they will stay open in the DB indefinitely even after the Java variables are gone
					
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
				
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) 	conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			try { if(stmt != null)  stmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
	}


}