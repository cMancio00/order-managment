package managment.repository;

import org.hibernate.Session;

import managment.model.Client;

public interface ClientRepository {

	void save(Client client, Session session);

}
