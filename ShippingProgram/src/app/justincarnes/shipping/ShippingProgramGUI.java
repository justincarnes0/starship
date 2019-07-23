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

public class ShippingProgramGUI extends JFrame 
{

	private JPanel contentPane;
	private DatabaseManager dbm;
	private JComboBox comboBox_Customers;
	private JComboBox comboBox_Sites;

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
		dbm = new DatabaseManager(this);								//Creates a database manager to interface with MySQL
		
		setTitle("Starfish Shipping");									//Creates the label at the top of the window
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);					//Behavior when closing program
		setBounds(100, 100, 500, 300);									//Sets default window size
		contentPane = new JPanel();										//Creates a content pane
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));				//Content pane's border
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		createCustomersBox();
	}
	
	private void createCustomersBox()
	{
		JLabel lblSelectACustomer = new JLabel("Select a customer");
		contentPane.add(lblSelectACustomer);							
		
		comboBox_Customers = new JComboBox(dbm.getCustList());
		comboBox_Customers.setModel(new DefaultComboBoxModel(new String[] {"--Select one--", "Aero Controls & DLE Consultants", "Aero Land & Marine", "Albioma Galion", "Alkhorayef Petroleum", "Allied Power Technologies", "APR Energy", "Arizona Public Service", "Arkansas Electric Cooperative Corporation", "Arnet Cleanpower", "Austin Energy", "Aviation Power & Marine", "Baseload Power", "BasicEParts", "Basin Electric Power Cooperative", "Belize Electricity Limited", "Black & Veatch", "Black Hills", "Brownsville Public Utility Board", "Bryan Texas Utilities", "Calpine Operating Services Co", "City of Medicine Hat", "City Water & Light (Jonesboro)", "Consumers Energy Company", "Coolidge Power", "CPS Energy", "CPV Sentinel", "Craig International", "Craig International Supplies", "DCP Midstream", "DEMEC", "Didrikson Associates", "DM Lieferant", "Dominion Energy", "DXP Enterprises", "East Kentucky Power Cooperative", "El Paso Electric Company", "Elliott Ebara Turbomachinery Corporation", "ELPower Trading", "Enesur/Adenty S.A.", "EthosEnergy", "EthosEnergy Australia", "Evolution Well Service", "Gamatech S.A.", "Gas Turbine Services", "Global IGT Solutions", "Hawaii Electric Light Company", "Hoosier Energy Rural Electric Cooperative", "I.S.R. Service Co.", "Instrumentation & Control Spares", "International Alliance Group", "LM Parts", "Los Angeles Department of Water and Power", "Louisiana Energy & Power Authority", "Lubbock Power & Light", "Madison Gas & Electric Company", "Maximum Turbine Support Europe", "Meritek Inc.", "Midwest Paper Group", "MSU Energy", "MTU Maintenance", "Municipal Light & Power", "New York Power Authority", "NRG Energy Center", "Oklahoma Municipal Power Authority", "Overseas Aviation", "Peru LNG S.R.L.", "Pinnacle Parts and Service Corporation", "Pio Pico Energy Center", "ProEnergy Services", "Ratchaburi World Cogeneration", "Siemens Energy", "SJ Turbine", "Sociedad Austral de Generacion y Energia Chile", "South Texas Electric Cooperative", "Starfish PPS", "Streamline Parts & Tooling", "Talen (Topaz Power Group)", "Texas A&M University", "The University of Texas at Austin", "Thermal Energy Corporation", "Toyo Thai Power Myanmar", "TransCanada", "University Park Energy", "V&A Hi-Tech", "Valve & Instrument Express", "VBR Turbine Partners", "WDF Aviation Services", "Western Farmers Electric Cooperative", "Wildflower Energy"}));
		contentPane.add(comboBox_Customers);
		
		comboBox_Customers.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent custSelectedEvent) 
			{
				String selection = (String) custSelectedEvent.getItem();
				if(selection.equals("--Select one--"))
					return;
				else createSitesBox(selection);
			}
		});
	}
	
	private void createSitesBox(String selection)
	{
		JLabel lblSelectASite = new JLabel("Select a site");
		lblSelectASite.setBounds(57, 59, 58, 14);
		contentPane.add(lblSelectASite);
		
		comboBox_Sites = new JComboBox(dbm.getSiteList(selection));
		comboBox_Sites.setBounds(120, 56, 251, 20);
		
		contentPane.add(comboBox_Sites);
		contentPane.validate();
		contentPane.repaint();
		
	}
}
