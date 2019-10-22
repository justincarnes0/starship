package app.justincarnes.shipping;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.sqlite.*;


//A class to manage the MySQL database containing Starfish customer information
public class DatabaseManager 
{	
	private static final String DB_URL = "jdbc:sqlite:starship.db";	//database filepath
	private static final String SQL_DIR = "C:/Users/Marc/Google Drive/JPP/misc/Shipping Program/backup/";
	
	private StarshipGUI gui;	//Holds the current GUI instance: ideally this will allow me to display console messages in the GUI later on
	
	private HashMap<Integer, ArrayList<String>> primaryKeys	= new HashMap<Integer, ArrayList<String>>();
	private HashMap<Integer, String> activeSelections 		= new HashMap<Integer, String>();
	public Recipient currentRecipient;
	
	///////////////////////////////////////////////
	//Constructor for the database manager object//
	///////////////////////////////////////////////
	
	//Currently accepts the active GUI instance, ideally console messages will be shown in the window in the future
	public DatabaseManager(StarshipGUI SPGui)
	{
		gui = SPGui;
		
		primaryKeys.put(Starship.CUSTOMERS, new ArrayList<String>());
		primaryKeys.put(Starship.SITES, 	new ArrayList<String>());
		primaryKeys.put(Starship.ACCOUNTS,  new ArrayList<String>());
		
		activeSelections.put(Starship.CUSTOMERS, "");
		activeSelections.put(Starship.SITES, 	 "");
		activeSelections.put(Starship.ACCOUNTS,  "");
		
		try { Class.forName("org.sqlite.JDBC"); }
		catch(Exception e) { System.out.println(e.getClass().getName()); }
		
		checkDatabase();
	}
	
	//Checks to see if the database exists and contains data
	private void checkDatabase()
	{
		Connection conn = null;
		Statement stmt = null;
		
		try { //to connect to database, execute the generated statement, and close the connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL);
			stmt = conn.createStatement();
			
			try { 
				//Try to select data from the Customers table in the DB
				//If there is no data, an SQLiteException will be thrown: if the DB does not exist, running a statement on it will create it
				ResultSet rs = stmt.executeQuery("select * from Customers;"); 
				while(rs.next()) System.out.println(rs.getRow());
				rs.close();
			}
			catch(SQLiteException e) { buildDatabase(); } //If there is no data in the DB, the database will be built
			
			conn.close();
			stmt.close();
		} catch(SQLException se) { se.printStackTrace(); } //If the SQL fails
		
		finally { //Close any instantiated DB objects no matter what
			try { if (conn != null) conn.close(); } catch(SQLException se) {se.printStackTrace();}
			try { if (stmt != null) stmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
	}
	
	//Creates and populates the database by parsing the SQL backup files and running the statements contained within
	private void buildDatabase()
	{
		try { 
			Scanner filereader = new Scanner(new File("sql/StarshipTables.sql")); 
			while(filereader.hasNextLine())
			{
				String sql = filereader.nextLine();
				Connection conn = null;
				Statement  stmt = null;
					
				try { //to connect to database, execute the generated statement, and close the connection
					System.out.println("Connecting to database...");
					conn = DriverManager.getConnection(DB_URL);
					stmt = conn.createStatement();

					stmt.execute(sql);
						
					conn.close();
					stmt.close();
				} catch(SQLException se) { se.printStackTrace(); } //If the SQL fails
					
				finally { //Close any instantiated DB objects no matter what
					try { if (conn != null) conn.close(); } catch(SQLException se) {se.printStackTrace();}
					try { if (stmt != null) stmt.close(); } catch(SQLException se2) {}
					System.out.println("DB connection terminated.");
				}
			}
			filereader.close();
		} catch(FileNotFoundException e) { System.out.println("SQL File StarshipTables.sql could not be opened."); }
		
		//Parse each of the three backup insert files and run their statements
		for(int i = 1; i < 4; i++)
		{
			String filepath = SQL_DIR + "ShipmentProgramInsert-" + (i == Starship.CUSTOMERS 
				? "Customer" : i == Starship.SITES ? "Site" : "Account") + ".sql";
			
			try {
				Scanner filereader = new Scanner(new File(filepath));
				while(filereader.hasNextLine())
				{
					String sql = filereader.nextLine();
					runUpdateQuery(sql);
				}
				filereader.close();
			} catch(FileNotFoundException e) { System.out.println("SQL File " + filepath.substring(4) + " could not be opened."); }
		}
		
	}
	
	///////////////////////
	//Getters and setters//
	///////////////////////
	
	public Object[] getPrimaryKeyList(int tableName)
	{
		ArrayList<String> tableKeys = primaryKeys.get(tableName);
		String activeCustomer 		= activeSelections.get(Starship.CUSTOMERS);
		String activeSite 			= activeSelections.get(Starship.SITES);
		boolean invalidActiveCust   = activeCustomer.equals(Starship.SELECT_ONE) || activeCustomer.equals("");
		boolean invalidActiveSite   = activeSite.equals(Starship.SELECT_ONE) 	 || activeSite.equals("");
		
		//Holds the name of the field to be retrieved for the active table
		String fieldName = ((tableName == Starship.CUSTOMERS) 
				? "custName" : (tableName == Starship.SITES ? "siteName" : "serviceName"));
		
		//If something is already in the list, flush it
		if(!tableKeys.isEmpty()) tableKeys.clear();		
				
		tableKeys.add(Starship.SELECT_ONE);	//Gives the combobox a default value, will be an invalid selection
		tableKeys.add(Starship.ADD_NEW);		//If selected, will allow the user to add a new entry
		
		//Skip selection & return empty lists if an invalid selection makes it here
		if(!(tableName == Starship.SITES 	&&  invalidActiveCust)
		&& !(tableName == Starship.ACCOUNTS && (invalidActiveCust || invalidActiveSite))) 
		{
			//Build an SQL statement to retrieve the desired primary key list
			String sql = "";
			if(tableName == Starship.CUSTOMERS)
				sql = "select custName from customer;";
			else if(tableName == Starship.SITES)
				sql = "select siteName from site where custName='" + activeCustomer + "';";
			else if(tableName == Starship.ACCOUNTS) 
				sql = "select serviceName from accountnumber where custName='" + activeCustomer + "' AND siteName='" + activeSite + "';";
			else System.out.println("Somehow the table name is incorrect.");
		
			runSelectQuery(tableKeys, sql, fieldName);
		}		
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
				+ custName 	   + "', '" 
				+ abbreviation + "', '" 
				+ PPA 		   + "');";
		
		addToInsertFile(insertQuery, Starship.CUSTOMERS);	//Passes the generated query to addToInsertFile
		runUpdateQuery(insertQuery);
	}
	
	//A method to add a new, user-defined site to the database for the selected customer
	//Accepts all necessary fields to create a site except custName- uses globally stored active customer instead
	public void addNewSite(String siteName, String street, String city, String state, String country, String zip)
	{
		String insertQuery = "insert into site(custName, siteName, streetAddress, cityAddress, stateAddress, countryAddress, zipAddress)"
				+ "\n\tvalues('" 
				+ activeSelections.get(Starship.CUSTOMERS) + "', '"
				+ siteName + "', '"
				+ street   + "', '"
				+ city 	   + "', '"
				+ state    + "', '"
				+ country  + "', '"
				+ zip 	   + "');";
		
		addToInsertFile(insertQuery, Starship.SITES);
		runUpdateQuery(insertQuery);
	}
	
	//A method to add a new, user-defined account to the database for the selected customer and site
	//Accepts all necessary fields to create an account except custName and siteName- uses globally store active values instead
	public void addNewAccount(String serviceName, String accountNum, String billingZip)
	{
		String insertQuery = "insert into AccountNumber(custName, siteName, serviceName, accountNum, billingZip)"
				+ "\n\tvalues('"
				+ activeSelections.get(Starship.CUSTOMERS) + "', '"
				+ activeSelections.get(Starship.SITES) 	  + "', '"
				+ serviceName + "', '"
				+ accountNum  + "', '"
				+ billingZip  + "');";
		
		addToInsertFile(insertQuery, Starship.ACCOUNTS);
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
		String filepath = SQL_DIR + "ShipmentProgramInsert-" + (fileSelection == Starship.CUSTOMERS 
				? "Customer" : fileSelection == Starship.SITES ? "Site" : "Account") + ".sql";
		
		try {	//to point a FileWriter/PrintWriter combo at the desired SQL file
			FileWriter  fw = new FileWriter(new File(filepath), true); //Append to file
			PrintWriter pw = new PrintWriter(fw);
			pw.println(insertQuery);	//Add to the file
			pw.close();					//Cleanup
			fw.close();
		} catch (FileNotFoundException fe) { fe.printStackTrace(); }	//If the file isn't found
		  catch (IOException ie) 		   { ie.printStackTrace(); }	//If the file can't be written to
	}
	
	///////////////////////////
	//Updating database items//
	///////////////////////////
	
	public void updateSelectedCustomer(String custName, String abbreviation, String PPA)
	{
	}
	
	
	////////////////////////////
	//General helper functions//
	////////////////////////////
	
	//A method to run a query modifying the database contents
	private void runUpdateQuery(String sql)
	{
		Connection conn = null;
		Statement  stmt = null;
		
		try { //to connect to database, execute the generated statement, and close the connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL);
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
	//This version is to be used when you want to fill an ArrayList with all of the values for one specific entry
	private void runSelectQuery(ArrayList<String> aList, String sql)
	{
		Connection conn = null;		//Will represent a connection with the database
		Statement  stmt = null;		//An object that will execute queries in the database
				
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL);		
					
			stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);		//Takes the above query and executes it in the DB
			ResultSetMetaData rsData = rs.getMetaData();//Provides us extra info about the result set
			int numCols = rsData.getColumnCount();
			rs.next();
			
			for(int i = 1; i <= numCols; i++)
				aList.add(rs.getString(i));
			
			conn.close(); 	//Closes all database resources: these are not local so they will not be destroyed by garbage collection
			stmt.close();	//Connections, statements, and result sets are representations of database objects, and are thus held in the DB
			rs.close();		//If we don't close after opening, they will stay open in the DB indefinitely even after the Java variables are gone
					
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
				
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) 	conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			try { if(stmt != null)  stmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
	}
	
	//This version is to be used when you want to fill an ArrayList with the values of one specific field
	private void runSelectQuery(ArrayList<String> aList, String sql, String fieldName)
	{
		Connection conn = null;		//Will represent a connection with the database
		Statement  stmt = null;		//An object that will execute queries in the database
				
		try {
			//Follows URL to database, provides credentials for access
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL);		
					
			stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);		//Takes the above query and executes it in the DB
					
			//Iterates through result set and adds all values in the desired field to the current AList
			while(rs.next()) aList.add(rs.getString(fieldName));
					
			conn.close();
			stmt.close();
			rs.close();	
					
		} catch(SQLException se) { se.printStackTrace(); }		//If there is some error with the SQL query
				
		finally {	//If an exception is thrown and nothing happens, we must still close the DB resources
			try { if(conn != null) 	conn.close(); } catch(SQLException se)  { se.printStackTrace(); }
			try { if(stmt != null)  stmt.close(); } catch(SQLException se2) {}
			System.out.println("DB connection terminated.");
		}
	}

	//Fetches all stored data for the given active selections
	public void fetchCompleteData(int tableName)
	{
		String activeCust = activeSelections.get(Starship.CUSTOMERS);
		String activeSite = activeSelections.get(Starship.SITES);
		String activeAcct = activeSelections.get(Starship.ACCOUNTS);
		
		ArrayList<String> activeData = new ArrayList<String>();
		
		String sql = "select * from ";
			
		switch(tableName)
		{
			case Starship.CUSTOMERS:
				sql += "customer where custName='" + activeCust + "';";
				break;
			case Starship.SITES:
				sql += "site where custName='" + activeCust + "' AND siteName='" + activeSite + "';";
				break;
			case Starship.ACCOUNTS:
				sql += "accountnumber where custName='" + activeCust + "' AND siteName='" + activeSite + "' AND serviceName='" + activeAcct + "';";
				break;
			default:
				sql += "*;";
			}
		runSelectQuery(activeData, sql);
		currentRecipient.setVals(tableName, activeData);
	}
}