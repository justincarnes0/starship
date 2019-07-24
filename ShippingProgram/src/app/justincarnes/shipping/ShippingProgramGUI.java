package app.justincarnes.shipping;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.DefaultComboBoxModel;
import java.awt.CardLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.MatteBorder;
import java.awt.Font;

public class ShippingProgramGUI extends JFrame 
{

	private JPanel contentPane;
	private DatabaseManager dbm;
	private JComboBox comboBox_Customers;
	private JComboBox comboBox_Sites;
	private JComboBox comboBox_Accounts;

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
					ShippingProgramGUI frame = new ShippingProgramGUI();//Creates instance of GUI
					frame.setVisible(true);								//Makes GUI visible to user: window pops up
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	//GUI Constructor
	//Creates the application frame
	public ShippingProgramGUI() 
	{
		dbm = new DatabaseManager(this);	//Creates a database manager to interface with MySQL
		
		setTitle("Starfish Shipping");		//Creates the label at the top of the window
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//Behavior when closing program
		setBounds(100, 100, 500, 300);		//Sets default window size
		contentPane = new JPanel();			//Creates a content pane
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));	//Content pane's border
		setContentPane(contentPane);		//Specifies the created contentPane as the frame's contentPane
		createCustomersBox();				//Calls the method to create the comboBox for customers
	}
	
	//Creates the comboBox containing the list of all Starfish customers
	private void createCustomersBox()
	{
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));	//Flow layout so resizing doesn't create too much empty space
		JLabel lblSelectACustomer = new JLabel("Select a customer");	//A label for the comboBox
		contentPane.add(lblSelectACustomer);							//Add the label to the contentPane
		
		comboBox_Customers = new JComboBox(dbm.getCustList());	//Constructs the comboBox, feeding the constructor the list of customers retrieved by the database manager
		contentPane.add(comboBox_Customers);					//Adds the comboBox to the contentPane
		
		//Creates a listener for the act of selecting a customer
		comboBox_Customers.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent custSelectedEvent) 
			{
				String selection = (String) custSelectedEvent.getItem(); //Holds the newly selected customer
				if(selection.equals("--Select one--"))	//Ensures that the default value is not passed along
					return;
				else {
					dbm.setActiveCustomer(selection);	//Tell the DatabaseManager which customer is actively selected
					if(comboBox_Sites != null)			//If there has already been a selection, repopulate the existing box of sites
						repopSitesBox();				
					else createSitesBox();				//If this is the first selection, create the sites box from scratch
				}
			}
		});
	}
	
	//Creates the comboBox for customer sites
	private void createSitesBox()
	{
		//Creates the label and adds it to the pane
		JLabel lblSelectASite = new JLabel("Select a site");
		lblSelectASite.setBounds(57, 59, 58, 14);
		contentPane.add(lblSelectASite);
		
		//Creates the box
		comboBox_Sites = new JComboBox(dbm.getSiteList());
		comboBox_Sites.setBounds(120, 56, 251, 20);
		
		//Adds the box to the pane and makes sure it displays correctly
		contentPane.add(comboBox_Sites);
		contentPane.validate();	//Must validate the pane after a new component is added
		contentPane.repaint();	//Repaint to visually update after validation
		
		//Adds a listener to the box, checking for a new selection
		comboBox_Sites.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent siteSelectedEvent) 
			{
				String selection = (String) siteSelectedEvent.getItem();
				if(selection.equals("--Select one--"))
					return;
				else {
					dbm.setActiveSite(selection);
					//TODO create a repop method for the accounts box
					createAccountsBox();
				}
			}
		});
		
	}
	
	//A method to refetch sites when the customer selection is changed
	private void repopSitesBox()
	{
		comboBox_Sites.removeAllItems();
		Object[] siteList = dbm.getSiteList();
		for(Object x : siteList)
			comboBox_Sites.addItem(x);
	}
	
	//TODO make this actually create the accounts comboBox
	private void createAccountsBox()
	{
		
	}
	
	//A method to refetch accounts when the customer or site selection changes
	private void repopAccountsBox()
	{
		comboBox_Accounts.removeAllItems();
		Object[] acctList = dbm.getAccountList();
		for(Object x : acctList)
			comboBox_Accounts.addItem(x);
	}
}
