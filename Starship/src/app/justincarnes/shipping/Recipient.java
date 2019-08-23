package app.justincarnes.shipping;

import java.util.*;

public class Recipient 
{
	private HashMap<String, String> data;
	
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
	
	public Recipient()
	{
		data = new HashMap<String, String>();
	}
	
	public void setField(String fieldName, String val)
	{
		data.put(fieldName, val);
	}
	
	public String getField(String fieldName)
	{
		return data.get(fieldName);
	}
}
