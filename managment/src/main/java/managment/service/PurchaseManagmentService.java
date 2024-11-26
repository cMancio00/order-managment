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

	public Purchase addPurchaseToClient(Client client, Purchase purchase) {
		purchase.setClient(client);
		if(client.getPurchases() == null)
			client.setPurchases(new ArrayList<>());
		Purchase addedPurchase = sessionFactory.fromTransaction(session -> 
				purchaseRepository.save(purchase, session));
		client.getPurchases().add(addedPurchase);
		return purchase;
		
	}

	public Client addClient(Client client) {
		return sessionFactory.fromTransaction(session -> clientRepository.save(client, session));

	}

	public void deletePurchase(Purchase toDelete) {
		sessionFactory.inTransaction(session -> {
			Optional<Client> foundClient = clientRepository.findById(toDelete.getClient().getId(), session);
			foundClient.ifPresent(client ->{
				client.getPurchases().remove(toDelete);
				purchaseRepository.delete(toDelete, session);
				clientRepository.save(client, session);
			});
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
		return sessionFactory.fromTransaction(session -> clientRepository.findById(id, session));
	}
	
	public Optional<Purchase> findPurchaseById(int id) {
		return sessionFactory.fromTransaction(session -> purchaseRepository.findById(id, session));
	}

}
