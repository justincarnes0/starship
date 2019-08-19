package app.justincarnes.shipping;

import java.util.*;

public class Recipient 
{
	private HashMap<String, String> data;
	
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
