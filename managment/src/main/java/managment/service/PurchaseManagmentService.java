package managment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		if(client.getPurchases() == null)
			client.setPurchases(new ArrayList<>());
		client.getPurchases().add(purchase);
		sessionFactory.inTransaction(session -> {
			purchaseRepository.save(purchase, session);
			clientRepository.save(client, session);
		});
	}

	public void addClient(Client client) {
		sessionFactory.inTransaction(session -> clientRepository.save(client, session));
		
	}

	public void deletePurchase(Purchase toDelete) {
		sessionFactory.inTransaction(session -> {
			Client client = clientRepository.findById(toDelete.getClient().getId(), session);
			client.getPurchases().remove(toDelete);
			purchaseRepository.delete(toDelete, session);
			clientRepository.save(client, session);
		});
		
	}

	public void deleteClient(Client client) {
		sessionFactory.inTransaction(session -> {
			for (Purchase purchase : client.getPurchases()) {
				purchaseRepository.delete(purchase, session);
			}
			clientRepository.delete(client, session);
		});
		
	}

	public List<Client> findAllClients() {
		return sessionFactory.fromTransaction(session -> clientRepository.findAll(session));
	}

	public List<Purchase> findallPurchases(Client client) {
		return sessionFactory.fromTransaction(session -> client.getPurchases());
	}

	public Optional<Client> findClientById(int id) {
		return null;
		
	}

}
