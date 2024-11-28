package managment.repository.client;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import managment.model.Client;

public class ClientRepositoryHibernate implements ClientRepository {

	@Override
	public Client save(Client client, Session session) {
		return session.merge(client);
	}

	@Override
	public Optional<Client> findById(int id, Session session) {
		return Optional.ofNullable(session.find(Client.class, id));
	}

	@Override
	public void delete(Client toDelete, Session session) {
		if(toDelete != null)
			session.remove(toDelete);
	}

	@Override
	public List<Client> findAll(Session session) {
		return session.createSelectionQuery("from Client", Client.class).getResultList();
	}
}
