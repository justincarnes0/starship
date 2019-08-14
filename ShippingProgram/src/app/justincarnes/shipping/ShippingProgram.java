package app.justincarnes.shipping;

import java.awt.*;
import javax.swing.*;

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
				try {
					UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
				} 
				catch (UnsupportedLookAndFeelException e) { System.out.println("Unsupported look & feel."); }
				catch (ClassNotFoundException e)		  { System.out.println("Class not found."); }
				catch (InstantiationException e)		  { System.out.println("Error instantiating."); }
				catch (IllegalAccessException e) 		  { System.out.println("Illegal access."); }
				
				new ShippingProgramGUI();
			}
		});
	}
}
