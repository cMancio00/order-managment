package managment.controller;

import java.util.List;
import java.util.Optional;

import managment.model.Client;
import managment.model.Purchase;
import managment.service.PurchaseManagmentService;
import managment.view.ManagmentView;

public class Managmentcontroller {
	
	private ManagmentView view;
	private PurchaseManagmentService service;

	public Managmentcontroller(ManagmentView view, PurchaseManagmentService service) {
		this.view = view;
		this.service = service;
	}

	public void findAllClients() {
		List<Client> clients = service.findAllClients();
		view.showAllClients(clients);
		
	}

	public void add(Client toAdd) {
		service.addClient(toAdd);
		view.clientAdded(toAdd);
	}

	public void remove(Client toDelete) {
		Optional<Client> foundClient = service.findClientById(toDelete.getId());
		foundClient.ifPresentOrElse(client -> {
			service.deleteClient(client);
			view.clientRemoved(client);
		},
			() -> view.showClientNotFoundError(toDelete.toString() + " not found", toDelete));
		
	}

	public void findAllPurchasesOf(Client selectedClient) {
		Optional<Client> foundClient = service.findClientById(selectedClient.getId());
		foundClient.ifPresentOrElse(client -> {
			List<Purchase> purchases = service.findallPurchases(foundClient.get());
			view.showAllPurchases(purchases);
		},
			() -> view.showClientNotFoundError(selectedClient.toString() + " not found", selectedClient));
		
	}


}
