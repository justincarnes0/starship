package app.justincarnes.shipping;

import java.util.*;

//An object representing the recipient of the current shipment
//Holds all the currently selected data from each of the three tables
public class Recipient 
{
	//Data from the Customer table
	private String custName;
	private String abbreviation;
	private boolean PPA;
	//Data from the Site table
	private String siteName;
	private String streetAddress;
	private String streetAddress2;
	private String cityAddress;
	private String stateAddress;
	private String countryAddress;
	private String zipAddress;
	//Data from the Account table
	private String serviceName;
	private String acctNumber;
	private String billingZip;
	
	//Constructor accepts the values for the three tables as ALists
	public Recipient(ArrayList<String> custVals, ArrayList<String> siteVals, ArrayList<String> acctVals)
	{
		setCustValues(custVals);
		setSiteValues(siteVals);
		setAcctValues(acctVals);
	}
	
	//A generalized setter: populates the fields corresponding to the designated table
	public void setVals(int tableName, ArrayList<String> vals)
	{
		if(tableName == Starship.CUSTOMERS) setCustValues(vals);
		if(tableName == Starship.SITES)		setSiteValues(vals);
		if(tableName == Starship.ACCOUNTS)  setAcctValues(vals); 
	}
	
	//Populates the fields for values from the Customer table
	private void setCustValues(ArrayList<String> vals)
	{
		custName 	 = vals.get(0);
		abbreviation = vals.get(1);
		PPA 		 = vals.get(2).equals("Y");
	}
	
	//Populates the fields for values from the Site table
	private void setSiteValues(ArrayList<String> vals)
	{
		siteName = vals.get(1);
		//Need to split the second value if the delimiter is present
		if(vals.get(2).contains(":"))
		{
			String[] temp  = vals.get(2).split(":");
			streetAddress  = temp[0];
			streetAddress2 = temp[1];
		}
		else streetAddress = vals.get(2);
		
		cityAddress    = vals.get(4);
		stateAddress   = vals.get(3);
		countryAddress = vals.get(5);
		zipAddress 	   = vals.get(6);
	}
	
	//Populates the fields for values from the Account table
	private void setAcctValues(ArrayList<String> vals)
	{
		serviceName = vals.get(2);
		acctNumber  = vals.get(3);
		billingZip  = vals.get(4);
	}
}
