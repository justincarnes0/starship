package app.justincarnes.shipping;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;

public class ShippingProgramGUI extends JFrame 
{

	private JPanel contentPane;

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
		DatabaseManager dbm = new DatabaseManager(this);				//Creates a database manager to interface with MySQL
		
		setTitle("Starfish Shipping");									//Creates the label at the top of the window
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);					//Behavior when closing program
		setBounds(100, 100, 450, 300);									//Sets default window size
		contentPane = new JPanel();										//Creates a content pane
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));				//Content pane's border
		setContentPane(contentPane);									//Attaches content pane to frame
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));	//Pane in flow layout
		
		JLabel lblSelectACustomer = new JLabel("Select a customer");	//A label for the combo box
		contentPane.add(lblSelectACustomer);							//Adds label to the content pane
		
		JComboBox comboBox = new JComboBox(dbm.getCustList());			//Creates a combo box for the customer list
		contentPane.add(comboBox);										//Adds combo box to content pane
	}

}
