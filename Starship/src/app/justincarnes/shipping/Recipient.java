package app.justincarnes.shipping;

import java.util.*;

public class Recipient 
{
	private String custName;
	private String abbreviation;
	private boolean PPA;
	
	private String siteName;
	private String streetAddress;
	private String streetAddress2;
	private String cityAddress;
	private String stateAddress;
	private String countryAddress;
	private String zipAddress;
	
	private String serviceName;
	private String acctNumber;
	private String billingZip;
	
	public Recipient(ArrayList<String> custVals, ArrayList<String> siteVals, ArrayList<String> acctVals)
	{
		setCustValues(custVals);
		setSiteValues(siteVals);
		setAcctValues(acctVals);
	}
	
	private void setCustValues(ArrayList<String> vals)
	{
		custName = vals.get(0);
		abbreviation = vals.get(1);
		PPA = vals.get(2).equals("Y");
	}
	
	private void setSiteValues(ArrayList<String> vals)
	{
		siteName = vals.get(1);
		if(vals.get(2).contains(":"))
		{
			String[] temp = vals.get(2).split(":");
			streetAddress = temp[0];
			streetAddress2 = temp[1];
		}
		else streetAddress = vals.get(2);
		
		cityAddress = vals.get(4);
		stateAddress = vals.get(3);
		countryAddress = vals.get(5);
		zipAddress = vals.get(6);
	}
	
	private void setAcctValues(ArrayList<String> vals)
	{
		serviceName = vals.get(2);
		acctNumber = vals.get(3);
		billingZip = vals.get(4);
	}
}
