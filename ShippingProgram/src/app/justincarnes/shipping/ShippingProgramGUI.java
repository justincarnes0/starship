package app.justincarnes.shipping;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


public class ShippingProgramGUI
{
	private DatabaseManager dbm;
	private JFrame frame;
	private JPanel cardDeck;
	private JPanel startPageCard;
	private HashMap<Integer, JComboBox> comboBoxes = new HashMap<Integer, JComboBox>();

	//Creates the application frame and launches the start page
	public ShippingProgramGUI() 
	{
		dbm = new DatabaseManager(this);
		
		frame = new JFrame("Starfish Shipping");	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setBounds(100, 100, 500, 300);		
		frame.getContentPane().setLayout(new CardLayout());	
		
		//Will be using card layout to facilitate multiple pages
		cardDeck = new JPanel();
		cardDeck.setLayout(new CardLayout(0, 0));
		startPageCard = new JPanel();
		startPageCard.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		cardDeck.add(startPageCard, "name_1294578395573185");
		frame.getContentPane().add(cardDeck);
		
		createComboBox(ShippingProgram.CUSTOMERS);
		frame.setVisible(true);
	}
	
	//A method to create a ComboBox preceded by a label
	//Accepts an int, will be one of the global constants used for table names
	private void createComboBox(int tableName)
	{
		//Generates a label to proceed the ComboBox
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
		
		//Create the box, add it to the master list and the pane, and attach a listener to it
		comboBoxes.put(tableName, new JComboBox(dbm.getPrimaryKeyList(tableName)));
		JComboBox currentBox = comboBoxes.get(tableName);
		startPageCard.add(currentBox);
		currentBox.addItemListener(new ComboBoxListener(tableName));	//See below for the internal class ComboBoxListener
		
		//These two method calls are important for getting the boxes to display properly after updating
		startPageCard.revalidate();	
		startPageCard.repaint();
	}
	
	//A method to repopulate ComboBoxes after the database has been updated
	//Accepts tableName, should be a global const
	private void repopComboBox(int tableName)
	{	
		//Store the current box and its listener for easy access
		JComboBox currentBox = comboBoxes.get(tableName);
		ComboBoxListener cbListener = (ComboBoxListener) currentBox.getItemListeners()[0];
		
		cbListener.toggleActive();	//Turn off the listener temporarily: this prevents choices from being triggered while the list is repopulating
		
		currentBox.removeAllItems();
		Object[] siteList = dbm.getPrimaryKeyList(tableName);
		for(Object x : siteList)
			currentBox.addItem(x);
		
		currentBox.setSelectedIndex(0);	//Set to "--Select one--" when repopulation is finished
		cbListener.toggleActive();		//Turn the listener back on so the box will function properly
	}
	
	//A method to prompt the user for a new entry to a table
	//Accepts the tableName global consts
	private void promptNewEntry(int tableName)
	{
		//Store the current box and its contents
		ComboBoxModel currentBoxModel = comboBoxes.get(tableName).getModel();
		ArrayList<String> CBModelItems = new ArrayList<String>();	//Storing contents so we can check for duplicate entries
		for(int i = 0; i < currentBoxModel.getSize(); i++)
			CBModelItems.add((String)currentBoxModel.getElementAt(i));
		
		//Each box will need a separate implementation of the prompt due to the differing numbers of fields
		switch(tableName)
		{
			case ShippingProgram.CUSTOMERS:
				JTextField custName = new JTextField();
				JTextField abbreviation = new JTextField();
			
				String[] choices = {ShippingProgram.SELECT_ONE, "Y", "N"}; //Making this one a choice to ensure an invalid entry isn't made
				JComboBox PPA = new JComboBox(choices);
			
				Object[] promptC = { "Customer name: ", custName, "Customer abbreviation: ", abbreviation, "Prepay & Add shipping?", PPA };
				
				//This will reflect whether or not the user wants to confirm the new entry or cancel
				int optionC = JOptionPane.showConfirmDialog(startPageCard, promptC, "New customer", JOptionPane.OK_CANCEL_OPTION);
			
				//Extract user input
				String custNameInput = custName.getText();
				String abbrInput = abbreviation.getText();
				String PPAChoice = (String) PPA.getSelectedItem();
			
				//Things to check if the entry is confirmed: if canceled, nothing needs to be done
				//Eventually I'd like to be able to keep the prompt open, just shuts the window for now
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
				
					else dbm.addNewCustomer(custNameInput, abbrInput, PPAChoice);
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
				if(streetAddressLine2.getText() != "") //If there's not a second line to the street address, we don't want to add the separator
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
					
					else dbm.addNewSite(siteNameInput, streetAddInput, cityInput, stateInput, countryInput, zipInput);
				}
				break;	
			
			default:
				System.out.println("Completely illogical result");
		}
		repopComboBox(tableName);
	}
	
	
	//A toggleable ItemListener to be used for ComboBoxes
	class ComboBoxListener implements ItemListener
	{
		private boolean active;
		private int tableName;
		
		public ComboBoxListener(int tn)
		{
			active = true;
			tableName = tn;
		}
		
		public void toggleActive()
		{
			active = !active;
		}
		
		public void itemStateChanged(ItemEvent selectionEvent) 
		{
			if(active)
			{
				String selection = (String) selectionEvent.getItem(); //Holds the newly selected value
				if(selection.equals(ShippingProgram.SELECT_ONE))	//Ensures that the default value is not passed along
					return;
				else if(selection.equals(ShippingProgram.ADD_NEW))	//Prompt a new entry if "--Add new--" is selected
					promptNewEntry(tableName);
				else {
					dbm.setActiveSelection(tableName, selection); 
					
					//Repopulate the next box for customers and sites to match new selections
					if(tableName < ShippingProgram.ACCOUNTS)
						if(comboBoxes.get(tableName + 1) != null)
							repopComboBox(tableName + 1);
						else createComboBox(tableName + 1);		
				}	
			}
			
		}
	}
}


