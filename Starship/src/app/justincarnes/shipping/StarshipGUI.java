package app.justincarnes.shipping;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


public class StarshipGUI
{
	private DatabaseManager dbm;
	
	private JFrame frame;
	private JPanel contentPane;
	private JPanel controlPanel;
	private JPanel cardDeck;
	private JPanel currentCard;
	private CardLayout cl;
	private ArrayList<JPanel> cards 	= new ArrayList<JPanel>();
	private ArrayList<String> cardNames = new ArrayList<String>();
	private HashMap<Integer, JComboBox> comboBoxes = new HashMap<Integer, JComboBox>();

	//Creates the application frame and launches the start page
	public StarshipGUI() 
	{
		dbm = new DatabaseManager(this);
		
		frame = new JFrame("Starship");	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setBounds(175, 175, 500, 280);		
		
		contentPane = (JPanel) frame.getContentPane();
		contentPane.setLayout(new BorderLayout());	
		
		createCardDeck();
		cl = (CardLayout) cardDeck.getLayout();
		
		createControlPanel();
		createFirstPage();
		createSecondPage();
		
		cl.show(cardDeck, cardNames.get(0));
		
		frame.setVisible(true);
	}
	
	/////////////////
	//Pre-page code//
	/////////////////
	private void createCardDeck()
	{
		cardDeck = new JPanel();
		cardDeck.setLayout(new CardLayout(50, 30));
		cardDeck.setBorder(new LineBorder(Color.GRAY, 1, true));
		contentPane.add(cardDeck, BorderLayout.NORTH);
	}
	
	private void createControlPanel()
	{
		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		
		JButton previous = new JButton("< Previous");
		JButton next 	 = new JButton("Next >");
		
		previous.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int cardIndex = cards.indexOf(currentCard);
				if(cardIndex > 0)
				{
					currentCard = cards.get(cardIndex - 1);
					cl.show(cardDeck, cardNames.get(cardIndex - 1));
					if(cardIndex - 1 == 0)
						previous.setEnabled(false);
					if(cardIndex - 1 == cards.size() - 2)
						next.setEnabled(true);
				}
			}
		});
		
		next.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int cardIndex = cards.indexOf(currentCard);
				if(cardIndex < cards.size() - 1)
				{
					currentCard = cards.get(cardIndex + 1);
					cl.show(cardDeck, cardNames.get(cardIndex + 1));
					if(cardIndex + 1 == cards.size() - 1)
						next.setEnabled(false);
					if(cardIndex + 1 == 1)
						previous.setEnabled(true);
				}
				dbm.fetchCompleteData();
			}
		});
		
		controlPanel.add(previous);
		controlPanel.add(next);
		
		previous.setEnabled(false);
		
		contentPane.add(controlPanel, BorderLayout.SOUTH);
	}
	
	///////////////////
	//First page code//
	///////////////////
	private void createFirstPage()
	{
		cards.add(new JPanel());
		cardNames.add("Page 1");
		currentCard = cards.get(0);
		JPanel startPageCard = cards.get(0);
		startPageCard.setLayout(new GridLayout(3, 1, 0, 40));
		cardDeck.add(startPageCard, cardNames.get(0));
		
		createComboBox(Starship.CUSTOMERS);
		createComboBox(Starship.SITES);
		createComboBox(Starship.ACCOUNTS);
		
		comboBoxes.get(Starship.SITES).setEnabled(false);
		comboBoxes.get(Starship.ACCOUNTS).setEnabled(false);
	}
	
	//A method to create a ComboBox preceded by a label
	//Accepts an int, will be one of the global constants used for table names
	private void createComboBox(int tableName)
	{
		//Generates a label to proceed the ComboBox
		JLabel comboBoxLabel = new JLabel();
		switch(tableName)
		{
			case Starship.CUSTOMERS:
				comboBoxLabel.setText("Select a customer:");
				break;
			case Starship.SITES:
				comboBoxLabel.setText("Select a site:");
				break;
			case Starship.ACCOUNTS:
				comboBoxLabel.setText("Select an account:");
				break;
			default:
				comboBoxLabel.setText("Something's wrong here");
		}
		
		//Create the box, add it to the master list and the pane, and attach a listener to it
		comboBoxes.put(tableName, new JComboBox(dbm.getPrimaryKeyList(tableName)));
		JComboBox currentBox = comboBoxes.get(tableName);
		currentBox.addItemListener(new ComboBoxListener(tableName));	//See below for the internal class ComboBoxListener
		
		currentCard.add(comboBoxLabel);
		currentCard.add(currentBox);
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
		
		currentBox.setEnabled(true);
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
			case Starship.CUSTOMERS:
				JTextField custName 	= new JTextField();
				JTextField abbreviation = new JTextField();
			
				String[] choices = {Starship.SELECT_ONE, "Y", "N"}; //Making this one a choice to ensure an invalid entry isn't made
				JComboBox PPA 	 = new JComboBox(choices);
			
				Object[] promptC = {"Customer name: ", custName, "Customer abbreviation: ", abbreviation, "Prepay & Add shipping?", PPA};
				
				//This will reflect whether or not the user wants to confirm the new entry or cancel
				int optionC = JOptionPane.showConfirmDialog(currentCard, promptC, "New customer", JOptionPane.OK_CANCEL_OPTION);
			
				//Extract user input
				String custNameInput = custName.getText();
				String abbrInput 	 = abbreviation.getText();
				String PPAChoice 	 = (String) PPA.getSelectedItem();
			
				//Things to check if the entry is confirmed: if canceled, nothing needs to be done
				//Eventually I'd like to be able to keep the prompt open, just shuts the window for now
				if(optionC == JOptionPane.OK_OPTION)
				{
					if(custNameInput.equals("") || abbrInput.equals(""))
					{
						JOptionPane.showMessageDialog(currentCard, "Fields cannot be left blank, please try again.");
						optionC = JOptionPane.CANCEL_OPTION; 
					}
					else if(PPAChoice == Starship.SELECT_ONE)
					{
						JOptionPane.showMessageDialog(currentCard, "You must select an option for 'Prepay & add shipping?'.");
						optionC = JOptionPane.CANCEL_OPTION;
					}
					else if(CBModelItems.contains(custNameInput))
					{
						JOptionPane.showMessageDialog(currentCard, "This customer already exists in the database.");
						optionC = JOptionPane.CANCEL_OPTION;
					}
				
					else dbm.addNewCustomer(custNameInput, abbrInput, PPAChoice);
				}
				break;
				
			case Starship.SITES:
				JTextField siteName 	 	  = new JTextField();
				JTextField streetAddress 	  = new JTextField();
				JTextField streetAddressLine2 = new JTextField();
				JTextField cityAddress 		  = new JTextField();
				JTextField stateAddress 	  = new JTextField();
				JTextField countryAddress 	  = new JTextField();
				JTextField zipAddress 		  = new JTextField();
				
				Object[] promptS = 
					{ "Customer name: ",  		 						 		  dbm.getActiveSelection(Starship.CUSTOMERS), 
					  "Site name: ", 				 						 	  siteName, 
					  "Street address (Line 1): ", 						 		  streetAddress,
					  "Street address (Line 2, leave blank if not applicable): ", streetAddressLine2,
					  "City: ",					 						 		  cityAddress,
					  "State (Leave blank if not applicable): ",  		 		  stateAddress,
					  "Country: ", 										 		  countryAddress,
					  "ZIP/Postal Code (Leave blank if not applicable): ", 		  zipAddress };
				
				int optionS = JOptionPane.showConfirmDialog(currentCard, promptS, "New site", JOptionPane.OK_CANCEL_OPTION);
				
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
					if(siteNameInput.equals("") || streetAddInput.equals("") || cityInput.equals("") || countryInput.equals(""))
					{
						JOptionPane.showMessageDialog(currentCard, "Fields cannot be left blank unless specifically stated, please try again.");
						optionS = JOptionPane.CANCEL_OPTION;
					}
					else if(CBModelItems.contains(siteNameInput))
					{
						JOptionPane.showMessageDialog(currentCard, "This site already exists in the database.");
						optionS = JOptionPane.CANCEL_OPTION;
					}
					else dbm.addNewSite(siteNameInput, streetAddInput, cityInput, stateInput, countryInput, zipInput);
				}
				break;	
				
			case Starship.ACCOUNTS:
				JTextField serviceName = new JTextField();
				JTextField accountNum  = new JTextField();
				JTextField billingZip  = new JTextField();
				
				Object[] promptA =  {"Service name: ", serviceName, "Account number: ", accountNum, "Billing ZIP: ", billingZip};
				int optionA = JOptionPane.showConfirmDialog(currentCard, promptA, "New account", JOptionPane.OK_CANCEL_OPTION);
				
				String serviceNameInput = serviceName.getText();
				String accountNumInput  = accountNum.getText();
				String billingZipInput  = billingZip.getText();
				
				if(optionA == JOptionPane.OK_OPTION)
				{
					if(serviceNameInput.equals("") || accountNumInput.equals("") || billingZipInput.equals(""))
					{
						JOptionPane.showMessageDialog(currentCard, "Fields cannot be left blank, please try again.");
						optionA = JOptionPane.CANCEL_OPTION;
					}
					else if(!(serviceNameInput.equals("Fedex") || serviceNameInput.equals("UPS") || serviceNameInput.equals("DHL")))
					{
						JOptionPane.showMessageDialog(currentCard, "Service name can only be \"Fedex\", \"UPS\", or \"DHL\". Please try again.");
						optionA = JOptionPane.CANCEL_OPTION;
					}
					else if(CBModelItems.contains(serviceNameInput))
					{
						JOptionPane.showMessageDialog(currentCard, "An account number for this service is already stored.");
						optionA = JOptionPane.CANCEL_OPTION;
					}
					else dbm.addNewAccount(serviceNameInput, accountNumInput, billingZipInput);
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
		private int 	tableName;
		
		public ComboBoxListener(int tn)
		{
			tableName = tn;
			active 	  = true;
		}
		
		public void toggleActive()
		{
			active = !active;
		}
		
		//Code that triggers whenever the state of a comboBox is changed
		public void itemStateChanged(ItemEvent selectionEvent) 
		{
			if(active)
			{
				String selection = (String) selectionEvent.getItem(); //Holds the newly selected value
				if(selection.equals(Starship.SELECT_ONE))	//Ensures that the default value is not passed along
					return;
				else if(selection.equals(Starship.ADD_NEW))	//Prompt a new entry if "--Add new--" is selected
					promptNewEntry(tableName);
				else {
					dbm.setActiveSelection(tableName, selection); 
					
					//Repopulate the next box for customers and sites to match new selections
					if(tableName < Starship.ACCOUNTS)
					{
						if(tableName < Starship.SITES)
							comboBoxes.get(tableName + 2).setEnabled(false);
						repopComboBox(tableName + 1);
					}
				}	
			}	
		}
	}
	
	////////////////////
	//Second page code//
	////////////////////
	private void createSecondPage()
	{
		cards.add(new JPanel());
		cardNames.add("Page 2");
		JPanel secondPageCard = cards.get(1);
		secondPageCard.setLayout(new GridLayout(3, 1, 0, 40));
		
		cardDeck.add(secondPageCard, cardNames.get(1));
		
		JLabel selectedCust = new JLabel("This is a test");
		JLabel selectedSite = new JLabel("Originally these names were accurate");
		JLabel selectedAcct = new JLabel("Then I realized that there was a logical error so here we are");
		
		secondPageCard.add(selectedCust);
		secondPageCard.add(selectedSite);
		secondPageCard.add(selectedAcct);
	}
}


