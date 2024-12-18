package managment.controller;

import java.util.List;
import java.util.Optional;

import managment.model.Client;
import managment.model.Purchase;
import managment.service.PurchaseManagmentService;
import managment.view.ManagmentView;

public class ManagmentController {
	
	private ManagmentView view;
	private PurchaseManagmentService service;

	public ManagmentController(ManagmentView view, PurchaseManagmentService service) {
		this.view = view;
		this.service = service;
	}

	public void findAllClients() {
		List<Client> clients = service.findAllClients();
		view.showAllClients(clients);
		
	}

	public void add(Client toAdd) {
		view.clientAdded(service.addClient(toAdd));
	}

	public void remove(Client toDelete) {
		Optional<Client> foundClient = service.findClientById(toDelete.getId());
		foundClient.ifPresentOrElse(client -> {
			service.deleteClient(client);
			view.clientRemoved(client);
		},
			() -> handleClientNotFound(toDelete));
		
	}

	public void findAllPurchasesOf(Client selectedClient) {
		Optional<Client> foundClient = service.findClientById(selectedClient.getId());
		foundClient.ifPresentOrElse(client -> {
			List<Purchase> purchases = service.findAllPurchases(foundClient.get());
			view.showAllPurchases(purchases);
		},
			() -> handleClientNotFound(selectedClient));
		
	}


	public void addPurchaseToSelectedClient(Client selectedClient, Purchase toAdd) {
		Optional<Client> foundClient = service.findClientById(selectedClient.getId());
		foundClient.ifPresentOrElse(client -> 
			view.purchaseAdded(service.addPurchaseToClient(client, toAdd)),
			() -> handleClientNotFound(selectedClient));
	}

	private void handleClientNotFound(Client client) {
		view.showClientNotFoundError(client.toString() + " not found", client);
	}

	public void remove(Purchase toDelete) {
		Optional<Purchase> foundPurchase = service.findPurchaseById(toDelete.getId());
		foundPurchase.ifPresentOrElse(purchase -> {
			service.deletePurchase(toDelete);
			view.purchaseRemoved(toDelete);
		},
				() -> view.showPurchaseNotFoundError(toDelete.toString() + " not found", toDelete));
	}

}
