package app.justincarnes.shipping;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


public class ShippingProgramGUI
{
	private JFrame frame;
	
	private JPanel cardDeck;
	private JPanel startPageCard;
	private DatabaseManager dbm;
	private HashMap<Integer, JComboBox> comboBoxes = new HashMap<Integer, JComboBox>();
	
	private JComboBox comboBox_Customers;
	private JComboBox comboBox_Sites;
	private JComboBox comboBox_Accounts;

	
	//GUI Constructor
	//Creates the application frame
	public ShippingProgramGUI() 
	{
		frame = new JFrame("Starfish Shipping");	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setBounds(100, 100, 500, 300);		
		frame.setLayout(new CardLayout());	
		
		cardDeck = new JPanel();
		startPageCard = new JPanel();
		startPageCard.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		createComboBox(ShippingProgram.CUSTOMERS);
		frame.setVisible(true);
	}
	
	
	private void createComboBox(int tableName)
	{
		JLabel comboBoxLabel = new JLabel();
		switch(tableName)
		{
			case ShippingProgram.CUSTOMERS:
				comboBoxLabel.setText("Select a customer:");
				break;
			case ShippingProgram.SITES:
				comboBoxLabel.setText("Select a site:");
				break;
			case ShippingProgram.ACCOUNTS:
				comboBoxLabel.setText("Select an account:");
				break;
			default:
				comboBoxLabel.setText("Something's wrong here");
		}
		
		startPageCard.add(comboBoxLabel);
		comboBoxes.put(tableName, new JComboBox(dbm.getPrimaryKeyList(tableName)));
		
		JComboBox currentBox = comboBoxes.get(tableName);
		startPageCard.add(currentBox);
		
		currentBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent selectionEvent) 
			{
				String selection = (String) selectionEvent.getItem(); //Holds the newly selected customer
				if(selection.equals(ShippingProgram.SELECT_ONE))	//Ensures that the default value is not passed along
					return;
				else if(selection.equals(ShippingProgram.ADD_NEW))
					promptNewEntry(tableName);
				else {
					dbm.setActiveSelection(tableName, selection); 
					
					if(tableName < ShippingProgram.ACCOUNTS)
						if(comboBoxes.get(tableName + 1) != null)
							repopComboBox(tableName + 1);
						else createComboBox(tableName + 1);		
				}
			}
		});
		
		//contentPane.revalidate();
		//contentPane.repaint();
		startPageCard.revalidate();
		startPageCard.repaint();
	}
	
	private void repopComboBox(int tableName)
	{
		JComboBox currentBox = comboBoxes.get(tableName);
		currentBox.removeAllItems();
		Object[] siteList = dbm.getPrimaryKeyList(tableName);
		for(Object x : siteList)
			currentBox.addItem(x);
		currentBox.setSelectedIndex(0);
	}
	
	private void promptNewEntry(int tableName)
	{
		ComboBoxModel currentBoxModel = comboBoxes.get(tableName).getModel();
		ArrayList<String> CBModelItems = new ArrayList<String>();
		for(int i = 0; i < currentBoxModel.getSize(); i++)
			CBModelItems.add((String)currentBoxModel.getElementAt(i));
		
		switch(tableName)
		{
			case ShippingProgram.CUSTOMERS:
				JTextField custName = new JTextField();
				JTextField abbreviation = new JTextField();
			
				String[] choices = {ShippingProgram.SELECT_ONE, "Y", "N"};
				JComboBox PPA = new JComboBox(choices);
			
				Object[] promptC = { "Customer name: ", custName, "Customer abbreviation: ", abbreviation, "Prepay & Add shipping?", PPA };
			
				int optionC = JOptionPane.showConfirmDialog(startPageCard, promptC, "New customer", JOptionPane.OK_CANCEL_OPTION);
			
				String custNameInput = custName.getText();
				String abbrInput = abbreviation.getText();
				String PPAChoice = (String) PPA.getSelectedItem();
			
				if(optionC == JOptionPane.OK_OPTION)
				{
					if(custNameInput == "" || abbrInput == "")
					{
						JOptionPane.showMessageDialog(startPageCard, "Fields cannot be left blank, please try again.");
						optionC = JOptionPane.CANCEL_OPTION;
					}
					else if(PPAChoice == ShippingProgram.SELECT_ONE)
					{
						JOptionPane.showMessageDialog(startPageCard, "You must select an option for 'Prepay & add shipping?'.");
						optionC = JOptionPane.CANCEL_OPTION;
					}
					else if(CBModelItems.contains(custNameInput))
					{
						JOptionPane.showMessageDialog(startPageCard, "This customer already exists in the database.");
						optionC = JOptionPane.CANCEL_OPTION;
					}
				
					else { 
						dbm.addNewCustomer(custNameInput, abbrInput, PPAChoice);
						repopComboBox(tableName);
					}
				}
				break;
				
			case ShippingProgram.SITES:
				JTextField siteName 	 	  = new JTextField();
				JTextField streetAddress 	  = new JTextField();
				JTextField streetAddressLine2 = new JTextField();
				JTextField cityAddress 		  = new JTextField();
				JTextField stateAddress 	  = new JTextField();
				JTextField countryAddress 	  = new JTextField();
				JTextField zipAddress 		  = new JTextField();
				
				Object[] promptS = 
					{ "Customer name: ",  		 						 		  dbm.getActiveSelection(ShippingProgram.CUSTOMERS), 
					  "Site name: ", 				 						 	  siteName, 
					  "Street address (Line 1): ", 						 		  streetAddress,
					  "Street address (Line 2, leave blank if not applicable): ", streetAddressLine2,
					  "City: ",					 						 		  cityAddress,
					  "State (Leave blank if not applicable): ",  		 		  stateAddress,
					  "Country: ", 										 		  countryAddress,
					  "ZIP/Postal Code (Leave blank if not applicable): ", 		  zipAddress };
				
				int optionS = JOptionPane.showConfirmDialog(startPageCard, promptS, "New site", JOptionPane.OK_CANCEL_OPTION);
				
				String siteNameInput  = siteName.getText();
				
				String streetAddInput = streetAddress.getText();
				if(streetAddressLine2.getText() != "")
					streetAddInput 	 += ": " + streetAddressLine2.getText();
				
				String cityInput	  = cityAddress.getText();
				String stateInput	  = (stateAddress.getText() == "" ? "NULL" : stateAddress.getText());
				String countryInput   = countryAddress.getText();
				String zipInput		  = (zipAddress.getText() == ""   ? "NULL" : zipAddress.getText());
				
				if(optionS == JOptionPane.OK_OPTION)
				{
					if(siteNameInput == "" || streetAddInput == "" || cityInput == "" || countryInput == "")
					{
						JOptionPane.showMessageDialog(startPageCard, "Fields cannot be left blank unless specifically stated, please try again.");
						optionS = JOptionPane.CANCEL_OPTION;
					}
					else if(CBModelItems.contains(siteNameInput))
					{
						JOptionPane.showMessageDialog(startPageCard, "This site already exists in the database.");
						optionS = JOptionPane.CANCEL_OPTION;
					}
					
					else { 
						dbm.addNewSite(siteNameInput, streetAddInput, cityInput, stateInput, countryInput, zipInput);
						repopComboBox(tableName);
					}
				}
				break;	
			
			default:
				System.out.println("Completely illogical result");
		}
	}
	
/*	//Creates the comboBox containing the list of all Starfish customers
	private void createCustomersBox()
	{
		JLabel lblSelectACustomer = new JLabel("Select a customer");	//A label for the comboBox
		startPageCard.add(lblSelectACustomer);							//Add the label to the startPageCard
		//Constructs the comboBox, feeding the constructor the list of customers retrieved by the database manager
		comboBox_Customers = new JComboBox(dbm.getPrimaryKeyList(int.CUSTOMERS));	
		startPageCard.add(comboBox_Customers);	//Adds the comboBox to the startPageCard
		
		//Creates a listener for the act of selecting a customer
		comboBox_Customers.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent custSelectedEvent) 
			{
				String selection = (String) custSelectedEvent.getItem(); //Holds the newly selected customer
				if(selection.equals(ShippingProgram.SELECT_ONE))	//Ensures that the default value is not passed along
					return;
				if(selection.equals(ShippingProgram.ADD_NEW))
					promptNewCustomer();
				else {
					dbm.setActiveSelection(ShippingProgram.CUSTOMERS, selection); //Tell the DatabaseManager which customer is actively selected
					if(comboBox_Sites != null)		//If there has already been a selection, repopulate the existing box of sites
						repopSitesBox();				
					else createSitesBox();			//If this is the first selection, create the sites box from scratch
				}
			}
		});
	}
	
	//A method to refetch customers when a new addition is made
	private void repopCustomersBox()
	{
		comboBox_Sites.removeAllItems();
		Object[] siteList = dbm.getPrimaryKeyList(ShippingProgram.CUSTOMERS);
		for(Object x : siteList)
			comboBox_Sites.addItem(x);
		comboBox_Sites.setSelectedIndex(0);
	}
	
	//Creates the comboBox for customer sites
	private void createSitesBox()
	{
		//Creates the label and adds it to the pane
		JLabel lblSelectASite = new JLabel("Select a site");
		lblSelectASite.setBounds(57, 59, 58, 14);
		startPageCard.add(lblSelectASite);
		
		//Creates the box
		comboBox_Sites = new JComboBox(dbm.getPrimaryKeyList(int.SITES));
		comboBox_Sites.setBounds(120, 56, 251, 20);
		
		//Adds the box to the pane and makes sure it displays correctly
		startPageCard.add(comboBox_Sites);
		startPageCard.validate();	//Must validate the pane after a new component is added
		startPageCard.repaint();	//Repaint to visually update after validation
		contentPane.validate();
		contentPane.repaint();
		
		//Adds a listener to the box, checking for a new selection
		comboBox_Sites.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent siteSelectedEvent) 
			{
				String selection = (String) siteSelectedEvent.getItem();
				if(selection.equals(ShippingProgram.SELECT_ONE))
					return;
				if(selection.equals(ShippingProgram.ADD_NEW))
					promptNewSite();
				else {
					dbm.setActiveSelection(ShippingProgram.SITES, selection); //Tell the DatabaseManager which site is actively selected
					if(comboBox_Accounts != null)	//If there has already been a selection, repopulate the existing box of accounts
						repopAccountsBox();				
					else createAccountsBox();		//If this is the first selection, create the accounts box from scratch
				}
			}
		});
		
	}
	
	//A method to refetch sites when the customer selection is changed or a new addition is made
	private void repopSitesBox()
	{
		comboBox_Sites.removeAllItems();
		Object[] siteList = dbm.getPrimaryKeyList(ShippingProgram.SITES);
		for(Object x : siteList)
			comboBox_Sites.addItem(x);
		comboBox_Sites.setSelectedIndex(0);
	}
	
	//TODO make this actually create the accounts comboBox
	private void createAccountsBox()
	{
		
	}
	
	//A method to refetch accounts when the customer or site selection changes or a new addition is made
	private void repopAccountsBox()
	{
		comboBox_Accounts.removeAllItems();
		Object[] acctList = dbm.getPrimaryKeyList(ShippingProgram.ACCOUNTS);
		for(Object x : acctList)
			comboBox_Accounts.addItem(x);
		comboBox_Accounts.setSelectedIndex(0);
	}
	
	private void promptNewCustomer()
	{
		JTextField custName = new JTextField();
		JTextField abbreviation = new JTextField();
		
		String[] choices = {ShippingProgram.SELECT_ONE, "Y", "N"};
		JComboBox PPA = new JComboBox(choices);
		
		Object[] prompt = { "Customer name: ", custName, "Customer abbreviation: ", abbreviation, "Prepay & Add shipping?", PPA };
		
		ComboBoxModel customerBoxModel = comboBox_Customers.getModel();
		ArrayList<String> CBModelItems = new ArrayList<String>();
		for(int i = 0; i < customerBoxModel.getSize(); i++)
			CBModelItems.add((String)customerBoxModel.getElementAt(i));
		
		int option = JOptionPane.showConfirmDialog(startPageCard, prompt, "New customer", JOptionPane.OK_CANCEL_OPTION);
		
		String custNameInput = custName.getText();
		String abbrInput = abbreviation.getText();
		String PPAChoice = (String) PPA.getSelectedItem();
		
		if(option == JOptionPane.OK_OPTION)
		{
			if(custNameInput == "" || abbrInput == "")
			{
				JOptionPane.showMessageDialog(startPageCard, "Fields cannot be left blank, please try again.");
				option = JOptionPane.CANCEL_OPTION;
			}
			else if(PPAChoice == ShippingProgram.SELECT_ONE)
			{
				JOptionPane.showMessageDialog(startPageCard, "You must select an option for 'Prepay & add shipping?'.");
				option = JOptionPane.CANCEL_OPTION;
			}
			else if(CBModelItems.contains(custNameInput))
			{
				JOptionPane.showMessageDialog(startPageCard, "This customer already exists in the database.");
				option = JOptionPane.CANCEL_OPTION;
			}
			
			else { 
				dbm.addNewCustomer(custNameInput, abbrInput, PPAChoice);
				repopCustomersBox();
			}
		}
	}
	
	private void promptNewSite()
	{
		JTextField siteName 	 	  = new JTextField();
		JTextField streetAddress 	  = new JTextField();
		JTextField streetAddressLine2 = new JTextField();
		JTextField cityAddress 		  = new JTextField();
		JTextField stateAddress 	  = new JTextField();
		JTextField countryAddress 	  = new JTextField();
		JTextField zipAddress 		  = new JTextField();
		
		Object[] prompt = 
			{ "Customer name: ",  		 						 		  dbm.getActiveSelection(ShippingProgram.CUSTOMERS), 
			  "Site name: ", 				 						 	  siteName, 
			  "Street address (Line 1): ", 						 		  streetAddress,
			  "Street address (Line 2, leave blank if not applicable): ", streetAddressLine2,
			  "City: ",					 						 		  cityAddress,
			  "State (Leave blank if not applicable): ",  		 		  stateAddress,
			  "Country: ", 										 		  countryAddress,
			  "ZIP/Postal Code (Leave blank if not applicable): ", 		  zipAddress };
		
		ComboBoxModel siteBoxModel = comboBox_Sites.getModel();
		ArrayList<String> SBModelItems = new ArrayList<String>();
		
		for(int i = 0; i < siteBoxModel.getSize(); i++)
			SBModelItems.add((String)siteBoxModel.getElementAt(i));
		
		int option = JOptionPane.showConfirmDialog(startPageCard, prompt, "New site", JOptionPane.OK_CANCEL_OPTION);
		
		String siteNameInput  = siteName.getText();
		
		String streetAddInput = streetAddress.getText();
		if(streetAddressLine2.getText() != "")
			streetAddInput 	 += ": " + streetAddressLine2.getText();
		
		String cityInput	  = cityAddress.getText();
		String stateInput	  = (stateAddress.getText() == "" ? "NULL" : stateAddress.getText());
		String countryInput   = countryAddress.getText();
		String zipInput		  = (zipAddress.getText() == ""   ? "NULL" : zipAddress.getText());
		
		if(option == JOptionPane.OK_OPTION)
		{
			if(siteNameInput == "" || streetAddInput == "" || cityInput == "" || countryInput == "")
			{
				JOptionPane.showMessageDialog(startPageCard, "Fields cannot be left blank unless specifically stated, please try again.");
				option = JOptionPane.CANCEL_OPTION;
			}
			else if(SBModelItems.contains(siteNameInput))
			{
				JOptionPane.showMessageDialog(startPageCard, "This site already exists in the database.");
				option = JOptionPane.CANCEL_OPTION;
			}
			
			else { 
				dbm.addNewSite(siteNameInput, streetAddInput, cityInput, stateInput, countryInput, zipInput);
				repopSitesBox();
			}
		}
	}*/
}
