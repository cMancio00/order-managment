package managment.repository.client;

import java.util.List;

import org.hibernate.Session;

import managment.model.Client;

public class ClientRepositoryHibernate implements ClientRepository {

	@Override
	public void save(Client client, Session session) {
		session.merge(client);
	}

	@Override
	public Client findById(int id, Session session) {
		return session.find(Client.class, id);
	}

	@Override
	public void delete(Client toDelete, Session session) {
		session.remove(toDelete);
		
	}

	@Override
	public List<Client> findAll(Session session) {
		return session.createSelectionQuery("from Client", Client.class).getResultList();
	}
}
