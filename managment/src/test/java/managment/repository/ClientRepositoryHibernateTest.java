package managment.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import managment.model.Client;

@DisplayName("Client Repository")
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
	@Nested
	@DisplayName("Happy Cases")
	class HappyCases{
		
		@DisplayName("Save when database is empty")
		@Test
		void testSave() {
			sessionFactory.inTransaction(session -> 
			clientRepository.save(new Client("toAdd"), session));
			assertThat(readAllClientFromDatabase()).containsExactly(new Client(1, "toAdd"));
		}
		@DisplayName("Find by id when Client is preset")
		@Test
		void testFindByIdWhenIsPresent(){
			Client found = sessionFactory.fromTransaction(session -> {
				session.persist(new Client("toBeFound"));
				return clientRepository.findById(1,session);
			});
			assertThat(found).isEqualTo(new Client(1,"toBeFound"));
		}
		
		@DisplayName("Update Client when is present")
		@Test
		void testUpdate(){
			addTestClientToDatabase("toBeUpdated");
			Client toUpdate = sessionFactory.fromSession(session -> 
				session.find(Client.class, 1));
			toUpdate.setName("updatedName");
			sessionFactory.inTransaction(session -> clientRepository.save(toUpdate, session));
			assertThat(readAllClientFromDatabase()).containsExactly(new Client(1,"updatedName"));
		}
		
		@DisplayName("Delete Client when is present")
		@Test
		void testDeleteWhenClientIsPresent(){
			addTestClientToDatabase("notToBeDeleted");	
			addTestClientToDatabase("toBeDeleted");
			sessionFactory.inTransaction(session -> {
				Client toDelete = session.find(Client.class, 2);
				clientRepository.delete(toDelete, session);
			});
			assertThat(readAllClientFromDatabase()).containsExactly(new Client(1,"notToBeDeleted"));
		}
		
		@DisplayName("Find all when database is empty should return an empty list")
		@Test
		void testFindAllWhenDatabaseIsEmpty(){
			List<Client> clients = sessionFactory.fromSession(session ->
				 clientRepository.findAll(session));
			assertThat(clients).isEmpty();
		}
		
		@Test
		void testFindAllWhenClientsArePresent(){
			addTestClientToDatabase("firstClient");
			addTestClientToDatabase("secondClient");
			List<Client> clients = sessionFactory.fromSession(session -> clientRepository.findAll(session));
			assertThat(clients).containsExactly(
					new Client(1,"firstClient"),
					new Client(2,"secondClient")
					);
		}
	
	}
	@Nested
	@DisplayName("Error Cases")
	class ErrorCases{
		@DisplayName("Find by Id when Client is not present should return null")
		@Test
		void testFindByIdWhenIsNotPresent(){
			Client found = sessionFactory.fromTransaction(session -> {
				return clientRepository.findById(1,session);
			});
			assertThat(found).isNull();
		}
	
	}
	
	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromSession(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}
	
	private void addTestClientToDatabase(String name) {
		sessionFactory.inTransaction(session ->
			session.persist(new Client(name)));
	}

}
