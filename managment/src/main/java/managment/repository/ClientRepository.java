package managment.repository;

import org.hibernate.Session;

import managment.model.Client;

public interface ClientRepository {

	void save(Client client, Session session);

	Client findById(int id, Session session);

	void delete(Client toDelete, Session session);

}
