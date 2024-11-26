package managment.view.swing;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import managment.controller.Managmentcontroller;
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
	
	private Managmentcontroller managmentController;

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
	private DefaultListModel<Purchase> listPurchaseModel;
		
	public void setManagmentController(Managmentcontroller managmentController) {
		this.managmentController = managmentController;
	}

	DefaultListModel<Client> getListClientsModel() {
		return listClientsModel;
	}
	
	DefaultListModel<Purchase> getListPurchaseModel() {
		return listPurchaseModel;
	}
	
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
				btnAddnewClientEnabler();
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
		lblPurchaseAmmount.setName("purchaseAmmountLable");
		GridBagConstraints gbc_lblPurchaseAmmount = new GridBagConstraints();
		gbc_lblPurchaseAmmount.insets = new Insets(0, 0, 5, 5);
		gbc_lblPurchaseAmmount.gridx = 3;
		gbc_lblPurchaseAmmount.gridy = 1;
		contentPane.add(lblPurchaseAmmount, gbc_lblPurchaseAmmount);
		
		txtPurchaseAmmount = new JTextField();
		txtPurchaseAmmount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddAmmountEnabler();
			}
		});
		txtPurchaseAmmount.setName("purchaseAmmountBox");
		GridBagConstraints gbc_txtPurchaseAmmount = new GridBagConstraints();
		gbc_txtPurchaseAmmount.insets = new Insets(0, 0, 5, 0);
		gbc_txtPurchaseAmmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPurchaseAmmount.gridx = 4;
		gbc_txtPurchaseAmmount.gridy = 1;
		contentPane.add(txtPurchaseAmmount, gbc_txtPurchaseAmmount);
		txtPurchaseAmmount.setColumns(10);
		
		btnAddNewClient = new JButton("Add New Client");
		btnAddNewClient.addActionListener(e ->
			managmentController.add(new Client(txtClientName.getText()))
			);
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

		listClients.addListSelectionListener(e -> {
			btnDeleteSelectedClientEnabler();
			btnDeleteSelectedPurchaseEnabler();
			btnAddAmmountEnabler();
		});
		listClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listClients.setName("clientList");
		GridBagConstraints gbc_listClients = new GridBagConstraints();
		gbc_listClients.gridwidth = 2;
		gbc_listClients.insets = new Insets(0, 0, 5, 5);
		gbc_listClients.fill = GridBagConstraints.BOTH;
		gbc_listClients.gridx = 0;
		gbc_listClients.gridy = 3;
		contentPane.add(listClients, gbc_listClients);
		
		listPurchaseModel = new DefaultListModel<>();
		listPurchases = new JList<>(getListPurchaseModel());
		listPurchases.addListSelectionListener(e -> btnDeleteSelectedPurchaseEnabler());
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
		btnDeleteSelectedClient.addActionListener(e -> 
			managmentController.remove(listClients.getSelectedValue())
				);
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
		messageLable.setName("messageLable");
		GridBagConstraints gbc_messageLable = new GridBagConstraints();
		gbc_messageLable.gridwidth = 5;
		gbc_messageLable.insets = new Insets(0, 0, 0, 5);
		gbc_messageLable.gridx = 0;
		gbc_messageLable.gridy = 6;
		contentPane.add(messageLable, gbc_messageLable);
		
	}

	private void btnAddnewClientEnabler() {
		btnAddNewClient.setEnabled(
				!txtClientName.getText().trim().isEmpty()
				);
	}
	
	private void btnDeleteSelectedClientEnabler() {
		btnDeleteSelectedClient.setEnabled(listClients.getSelectedIndex() != -1);
	}
	
	private void btnAddAmmountEnabler() {
		btnAddAmmount.setEnabled(
				!txtPurchaseAmmount.getText().trim().isEmpty() &&
				listClients.getSelectedIndex() != -1
				);
	}
	
	private void btnDeleteSelectedPurchaseEnabler() {
		btnDeleteSelectedPurchase.setEnabled(
		listClients.getSelectedIndex() != -1 &&
		listPurchases.getSelectedIndex() != -1);
	}

	@Override
	public void showAllClients(List<Client> clients) {
		clients.stream().forEach(listClientsModel::addElement);
	}

	@Override
	public void clientAdded(Client toAdd) {
		listClientsModel.addElement(toAdd);
		resetMessageLable();
	}

	@Override
	public void clientRemoved(Client toDelete) {
		listClientsModel.removeElement(toDelete);
		resetMessageLable();
	}

	private void resetMessageLable() {
		messageLable.setText(" ");
	}

	@Override
	public void showAllPurchases(List<Purchase> purchases) {
		purchases.stream().forEach(listPurchaseModel::addElement);
	}

	@Override
	public void showClientNotFoundError(String string, Client client) {
		messageLable.setText(client.toString() + " not found");
	}

	@Override
	public void purchaseAdded(Purchase toAdd) {
		listPurchaseModel.addElement(toAdd);
		resetMessageLable();	
	}

	@Override
	public void purchaseRemoved(Purchase toDelete) {
		listPurchaseModel.removeElement(toDelete);
		resetMessageLable();	
	}

	@Override
	public void showPurchaseNotFoundError(String string, Purchase purchase) {
		messageLable.setText(purchase.toString() + " not found");
		
	}

}
