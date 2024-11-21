package managment.view.swing;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import managment.model.Client;
import managment.model.Purchase;
import managment.view.ManagmentView;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ManagmentViewSwing extends JFrame implements ManagmentView{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtClientName;
	private JLabel lblPurchaseAmmount;
	private JTextField txtPurchaseAmmount;
	private JButton btnAddNewClient;
	private JButton btnAddAmmount;
	private JList<Client> listClients;
	private JList<Purchase> listPurchases;
	private JButton btnDeleteSelectedClient;
	private JButton btnDeleteSelectedPurchase;
	private JLabel messageLable;
	
	private DefaultListModel<Client> listClientsModel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
				try {
					ManagmentViewSwing frame = new ManagmentViewSwing();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
	DefaultListModel<Client> getListClientsModel() {
		return listClientsModel;
	}

	/**
	 * Create the frame.
	 */
	public ManagmentViewSwing() {
		
		setTitle("Purchase Managment View");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 480);
		contentPane = new JPanel();
		contentPane.setForeground(new Color(51, 51, 51));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{139, 152, 84, 0, 181, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 311, 0, 40, 30, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblClientName = new JLabel("Client Name");
		lblClientName.setName("clientNameLable");
		GridBagConstraints gbc_lblClientName = new GridBagConstraints();
		gbc_lblClientName.insets = new Insets(0, 0, 5, 5);
		gbc_lblClientName.gridx = 0;
		gbc_lblClientName.gridy = 1;
		contentPane.add(lblClientName, gbc_lblClientName);
		
		txtClientName = new JTextField();
		txtClientName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddNewClient.setEnabled(
						!txtClientName.getText().trim().isEmpty()
						);
			}
		});
		txtClientName.setName("clientNameBox");
		GridBagConstraints gbc_txtClientName = new GridBagConstraints();
		gbc_txtClientName.insets = new Insets(0, 0, 5, 5);
		gbc_txtClientName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtClientName.gridx = 1;
		gbc_txtClientName.gridy = 1;
		contentPane.add(txtClientName, gbc_txtClientName);
		txtClientName.setColumns(10);
		
		lblPurchaseAmmount = new JLabel("Purchase Ammount");
		GridBagConstraints gbc_lblPurchaseAmmount = new GridBagConstraints();
		gbc_lblPurchaseAmmount.insets = new Insets(0, 0, 5, 5);
		gbc_lblPurchaseAmmount.gridx = 3;
		gbc_lblPurchaseAmmount.gridy = 1;
		contentPane.add(lblPurchaseAmmount, gbc_lblPurchaseAmmount);
		
		txtPurchaseAmmount = new JTextField();
		txtPurchaseAmmount.setName("purchaseAmmountBox");
		GridBagConstraints gbc_txtPurchaseAmmount = new GridBagConstraints();
		gbc_txtPurchaseAmmount.insets = new Insets(0, 0, 5, 0);
		gbc_txtPurchaseAmmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPurchaseAmmount.gridx = 4;
		gbc_txtPurchaseAmmount.gridy = 1;
		contentPane.add(txtPurchaseAmmount, gbc_txtPurchaseAmmount);
		txtPurchaseAmmount.setColumns(10);
		
		btnAddNewClient = new JButton("Add New Client");
		btnAddNewClient.setEnabled(false);
		btnAddNewClient.setName("addNewClientButton");
		GridBagConstraints gbc_btnAddNewClient = new GridBagConstraints();
		gbc_btnAddNewClient.gridwidth = 2;
		gbc_btnAddNewClient.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddNewClient.gridx = 0;
		gbc_btnAddNewClient.gridy = 2;
		contentPane.add(btnAddNewClient, gbc_btnAddNewClient);
		
		btnAddAmmount = new JButton("Add Ammount");
		btnAddAmmount.setEnabled(false);
		btnAddAmmount.setName("AddAmmountButton");
		GridBagConstraints gbc_btnAddAmmount = new GridBagConstraints();
		gbc_btnAddAmmount.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddAmmount.gridwidth = 2;
		gbc_btnAddAmmount.gridx = 3;
		gbc_btnAddAmmount.gridy = 2;
		contentPane.add(btnAddAmmount, gbc_btnAddAmmount);
		
		listClientsModel = new DefaultListModel<>();
		listClients = new JList<>(listClientsModel);
		listClients.addListSelectionListener(e -> 
			btnDeleteSelectedClient.setEnabled(listClients.getSelectedIndex() != -1));
		listClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listClients.setName("clientList");
		GridBagConstraints gbc_listClients = new GridBagConstraints();
		gbc_listClients.gridwidth = 2;
		gbc_listClients.insets = new Insets(0, 0, 5, 5);
		gbc_listClients.fill = GridBagConstraints.BOTH;
		gbc_listClients.gridx = 0;
		gbc_listClients.gridy = 3;
		contentPane.add(listClients, gbc_listClients);
		
		listPurchases = new JList<Purchase>();
		listPurchases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listPurchases.setName("purchaseList");
		GridBagConstraints gbc_listPurchases = new GridBagConstraints();
		gbc_listPurchases.insets = new Insets(0, 0, 5, 0);
		gbc_listPurchases.gridwidth = 2;
		gbc_listPurchases.fill = GridBagConstraints.BOTH;
		gbc_listPurchases.gridx = 3;
		gbc_listPurchases.gridy = 3;
		contentPane.add(listPurchases, gbc_listPurchases);
		
		btnDeleteSelectedClient = new JButton("Delete Selected Client");
		btnDeleteSelectedClient.setName("deleteSelectedClient");
		btnDeleteSelectedClient.setEnabled(false);
		GridBagConstraints gbc_btnDeleteSelectedClient = new GridBagConstraints();
		gbc_btnDeleteSelectedClient.gridwidth = 2;
		gbc_btnDeleteSelectedClient.insets = new Insets(0, 0, 5, 5);
		gbc_btnDeleteSelectedClient.gridx = 0;
		gbc_btnDeleteSelectedClient.gridy = 4;
		contentPane.add(btnDeleteSelectedClient, gbc_btnDeleteSelectedClient);
		
		btnDeleteSelectedPurchase = new JButton("Delete Selected Purchase");
		btnDeleteSelectedPurchase.setEnabled(false);
		btnDeleteSelectedPurchase.setName("deleteSelectedPurchase");
		GridBagConstraints gbc_btnDeleteSelectedPurchase = new GridBagConstraints();
		gbc_btnDeleteSelectedPurchase.gridwidth = 2;
		gbc_btnDeleteSelectedPurchase.insets = new Insets(0, 0, 5, 0);
		gbc_btnDeleteSelectedPurchase.gridx = 3;
		gbc_btnDeleteSelectedPurchase.gridy = 4;
		contentPane.add(btnDeleteSelectedPurchase, gbc_btnDeleteSelectedPurchase);
		
		messageLable = new JLabel(" ");
		messageLable.setName("messsageLable");
		GridBagConstraints gbc_messageLable = new GridBagConstraints();
		gbc_messageLable.gridwidth = 5;
		gbc_messageLable.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageLable.insets = new Insets(0, 0, 0, 5);
		gbc_messageLable.gridx = 0;
		gbc_messageLable.gridy = 6;
		contentPane.add(messageLable, gbc_messageLable);
		

	}

	@Override
	public void showAllClients(List<Client> clients) {
		// TODO Auto-generated method stub
	}

	@Override
	public void clientAdded(Client toAdd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientRemoved(Client toDelete) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showAllPurchases(List<Purchase> purchases) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showClientNotFoundError(String string, Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void purchaseAdded(Purchase toAdd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void purchaseRemoved(Purchase toDelete) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showPurchaseNotFoundError(String string, Purchase toDelete) {
		// TODO Auto-generated method stub
		
	}

	
}
