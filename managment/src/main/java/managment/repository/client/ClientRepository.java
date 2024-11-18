package managment.repository.client;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import managment.model.Client;

public interface ClientRepository {

	void save(Client client, Session session);

	Optional<Client> findById(int id, Session session);

	void delete(Client toDelete, Session session);

	List<Client> findAll(Session session);

}
