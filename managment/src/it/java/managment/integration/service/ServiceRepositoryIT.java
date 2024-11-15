package managment.integration.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
import managment.model.Purchase;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;

import org.junit.jupiter.api.Test;

@Testcontainers
@DisplayName("Service-Repository IntegrationTest")
class ServiceRepositoryIT {
	
	private static String mysqlVersion = System.getProperty("mysql.version", "9.1.0");
	private static final LocalDateTime FIRST_TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();
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
		Client existingClient = new Client("existingClient");
		addTestClientToDatabase(existingClient);
		Client toAdd = new Client("toAdd");
		service.addClient(toAdd);
		assertThat(readAllClientFromDatabase()).containsExactly(
				new Client(1,"existingClient"),
				new Client(2,"toAdd")
				);
		
	}
	
	@Test
	@DisplayName("Add Purchase to Client should add a purchase to client")
	void addPurchaseToClient(){
		Client clientNotToAddPurchase = new Client("clientNotToAddPurchase");
		addTestClientToDatabase(clientNotToAddPurchase);
		Client clientToAddPurchase = new Client("clientToAddPurchase");
		addTestClientToDatabase(clientToAddPurchase);
		Purchase purchaseToAdd = new Purchase(FIRST_TEST_DATE,10.0);
		
		service.addPurchaseToClient(clientToAddPurchase, purchaseToAdd);
		assertThat(findPurchasesOfClient(2)).containsExactly(new Purchase(1, FIRST_TEST_DATE,10.0));
	}
	
	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromTransaction(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}
	
	private void addTestClientToDatabase(Client client) {
		sessionFactory.inTransaction(session ->
			session.persist(client));
	}
	
	private List<Purchase> findPurchasesOfClient(int clientId){
		return sessionFactory.fromTransaction(session ->
			session.find(Client.class, clientId).getPurchases());
	}
	
	

}
