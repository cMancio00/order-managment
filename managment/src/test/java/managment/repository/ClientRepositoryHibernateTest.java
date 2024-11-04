package managment.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import managment.model.Client;

class ClientRepositoryHibernateTest {
	private static final String H2_DATABASE = "test-db";
	private static final String CONNECTION_URL = String.format("jdbc:h2:mem:%s", H2_DATABASE);
	private SessionFactory sessionFactory;
	private ClientRepository clientRepository;

	@BeforeEach
	void setUpDatabase() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.connection.url", CONNECTION_URL);
		sessionFactory = configuration.configure("hibernate-unit.cfg.xml").buildSessionFactory();
		clientRepository = new ClientRepositoryHibernate();
	}

	@AfterEach
	void tearDownDatabase(){
		sessionFactory.getSchemaManager().truncateMappedObjects();
		sessionFactory.close();
	}

	@Test
	void testSave() {
		sessionFactory.inTransaction(session -> 
		clientRepository.save(new Client("toAdd"), session));
		assertThat(readAllClientFromDatabase()).containsExactly(new Client(1, "toAdd"));
	}

	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromSession(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}

}
