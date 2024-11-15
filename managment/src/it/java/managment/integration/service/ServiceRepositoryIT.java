package managment.integration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import managment.model.Client;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;

import org.junit.jupiter.api.Test;

@Testcontainers
class ServiceRepositoryIT {
	
	private static String mysqlVersion = System.getProperty("mysql.version", "9.1.0");
	
	@Container
	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:" + mysqlVersion))
			.withDatabaseName("it-db")
			.withUsername("manager")
			.withPassword("it");
	
	private static SessionFactory sessionFactory;

	private ClientRepository clientRepository;

	private PurchaseRepository purchaseRepository;

	private PurchaseManagmentService service;
	
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Properties mysqlProperties = new Properties();
		mysqlProperties.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
		mysqlProperties.setProperty("hibernate.connection.username", mysql.getUsername());
		mysqlProperties.setProperty("hibernate.connection.password", mysql.getPassword());
		System.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
		System.setProperty("hibernate.connection.username", mysql.getUsername());
		System.setProperty("hibernate.connection.password", mysql.getPassword());
		sessionFactory = new Configuration().setProperties(mysqlProperties).configure("hibernate-integration.cfg.xml").buildSessionFactory();
	}
	
	@BeforeEach
	void setup() {
		sessionFactory.getCache().evictAllRegions();
		clientRepository = new ClientRepositoryHibernate();
		purchaseRepository = new PurchaseRepositoryHibernate();
		service = new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
	}
	
	@AfterEach
	void tearDown() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
	}

	@Test
	@DisplayName("Add Client should add a client to the database")
	void testAddClient() {
		Client client = new Client("existingClient");
		addTestClientToDatabase(client);
		Client toAdd = new Client("toAdd");
		service.addClient(toAdd);
		assertThat(readAllClientFromDatabase()).containsExactly(
				new Client(1,"existingClient"),
				new Client(2,"toAdd")
				);
		
	}
	
	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromTransaction(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}
	
	private void addTestClientToDatabase(Client client) {
		sessionFactory.inTransaction(session ->
			session.persist(client));
	}

}
