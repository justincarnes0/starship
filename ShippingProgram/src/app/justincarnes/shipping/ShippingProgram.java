package app.justincarnes.shipping;

import java.awt.EventQueue;

public class ShippingProgram 
{
	public static final String SELECT_ONE = "--Select one--";
	public static final String ADD_NEW 	  = "--Add new--";
	
	//An enumeration of the available table names
	public static final int CUSTOMERS = 1;
	public static final int SITES	  = 2;
	public static final int ACCOUNTS  = 3;
	
	//Launches program
	public static void main(String[] args) 
	{
		//I believe this creates the program instance in a new thread
		//From what I understand, this is preferable to doing it directly in the main method
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				ShippingProgramGUI GUI = new ShippingProgramGUI();
			}
		});
	}
}
