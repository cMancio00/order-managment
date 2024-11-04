package managment.repository;

import org.hibernate.Session;

import managment.model.Client;

public class ClientRepositoryHibernate implements ClientRepository {

	@Override
	public void save(Client client, Session session) {
		session.merge(client);
	}
}
