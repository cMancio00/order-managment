package managment.service;

import org.hibernate.SessionFactory;

import managment.model.Client;
import managment.model.Purchase;
import managment.repository.client.ClientRepository;
import managment.repository.purchase.PurchaseRepository;

public class PurchaseManagmentService {
	
	private ClientRepository clientRepository;
	private PurchaseRepository purchaseRepository;
	private SessionFactory sessionFactory;

	public PurchaseManagmentService(SessionFactory sessionFactory, ClientRepository clientRepository, PurchaseRepository purchaseRepository) {
		this.sessionFactory = sessionFactory;
		this.clientRepository = clientRepository;
		this.purchaseRepository = purchaseRepository;
	}

	public void addPurchaseToClient(Client client, Purchase purchase) {
		purchase.setClient(client);
		client.getPurchases().add(purchase);
		sessionFactory.inTransaction(session -> {
			purchaseRepository.save(purchase, session);
			clientRepository.save(client, session);
		});
	}

}
