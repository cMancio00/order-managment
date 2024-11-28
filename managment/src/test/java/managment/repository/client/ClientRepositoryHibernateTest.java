package managment.repository.client;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

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
	private static final String H2_DATABASE = "client-test-db";
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
	@DisplayName("Save")
	class SaveTests{
		
		@DisplayName("When database is empty")
		@Test
		void testSaveEmpty() {
			Client toAdd = sessionFactory.fromTransaction(session -> 
			clientRepository.save(new Client("toAdd"), session));
			assertThat(readAllClientFromDatabase()).containsExactly(toAdd);
			assertThat(toAdd.getId()).isEqualTo(1);
		}
		
		@DisplayName("When database is not empy")
		@Test
		void testSaveNotEmpty(){
			addClientToDatabase("existingClient");
			Client toAdd = sessionFactory.fromTransaction(session -> 
			clientRepository.save(new Client("toAdd"), session));
			assertThat(readAllClientFromDatabase()).containsExactly(
					new Client(1, "existingClient"),
					toAdd
					);
			assertThat(toAdd.getId()).isEqualTo(2);
		}

	}
	
	@Nested
	@DisplayName("FindById")
	class FindById{
		@DisplayName("When Client is preset should return an Optional with the client")
		@Test
		void testFindByIdWhenIsPresent(){
			addClientToDatabase("notToBeFound");
			addClientToDatabase("toBeFound");
			Optional<Client> found = sessionFactory.fromTransaction(session -> {
				return clientRepository.findById(2, session);
			});
			assertThat(found).contains(new Client(2, "toBeFound"));
		}
		
		@DisplayName("When Client is not present should return empty optional")
		@Test
		void testFindByIdWhenIsNotPresent(){
			Optional<Client> found = sessionFactory.fromTransaction(session -> {
				return clientRepository.findById(1,session);
			});
			assertThat(found).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Delete")
	class DeleteTests{
		
		@DisplayName("When is present should remove it")
		@Test
		void testDeleteWhenClientIsPresent(){
			addClientToDatabase("notToBeDeleted");	
			addClientToDatabase("toBeDeleted");
			sessionFactory.inTransaction(session -> {
				Client toDelete = session.find(Client.class, 2);
				clientRepository.delete(toDelete, session);
			});
			assertThat(readAllClientFromDatabase()).containsExactly(new Client(1,"notToBeDeleted"));
		}
		
		@DisplayName("When is not present should do nothing")
		@Test
		void testDeleteWhenClientIsNotPresent(){
			addClientToDatabase("notToBeDeleted");	
			sessionFactory.inTransaction(session -> {
				Client toDelete = session.find(Client.class, 2);
				clientRepository.delete(toDelete, session);
			});
			assertThat(readAllClientFromDatabase())
				.containsExactly(new Client(1,"notToBeDeleted"));
		}
	}
	
	@Nested
	@DisplayName("FindAll")
	class FindAll{
		@DisplayName("When database is empty should return an empty list")
		@Test
		void testFindAllWhenDatabaseIsEmpty(){
			List<Client> clients = sessionFactory.fromSession(session ->
				 clientRepository.findAll(session));
			assertThat(clients).isEmpty();
		}
		
		@Test
		@DisplayName("When clients are present should return the list of clients")
		void testFindAllWhenClientsArePresent(){
			addClientToDatabase("firstClient");
			addClientToDatabase("secondClient");
			List<Client> clients = sessionFactory.fromSession(session -> clientRepository.findAll(session));
			assertThat(clients).containsExactly(
					new Client(1,"firstClient"),
					new Client(2,"secondClient")
					);
		}
	}

	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromSession(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}
	
	private void addClientToDatabase(String name) {
		sessionFactory.inTransaction(session ->
			session.persist(new Client(name)));
	}

}
