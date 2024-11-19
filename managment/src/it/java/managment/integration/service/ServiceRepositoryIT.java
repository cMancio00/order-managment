package managment.integration.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.MySQLContainer;
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

	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:" + mysqlVersion))
			.withDatabaseName("Service-Repository-db").withUsername("manager").withPassword("it").withReuse(true);

	private static final LocalDateTime FIRST_TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();
	private static final LocalDateTime SECOND_TEST_DATE = LocalDate.of(2024, Month.FEBRUARY, 1).atStartOfDay();

	
	private static SessionFactory sessionFactory;

	private ClientRepository clientRepository;

	private PurchaseRepository purchaseRepository;

	private PurchaseManagmentService service;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		mysql.start();
		Properties mysqlProperties = new Properties();
		mysqlProperties.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
		mysqlProperties.setProperty("hibernate.connection.username", mysql.getUsername());
		mysqlProperties.setProperty("hibernate.connection.password", mysql.getPassword());
		System.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
		System.setProperty("hibernate.connection.username", mysql.getUsername());
		System.setProperty("hibernate.connection.password", mysql.getPassword());
		sessionFactory = new Configuration().setProperties(mysqlProperties).configure("hibernate-integration.cfg.xml")
				.buildSessionFactory();
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
		assertThat(readAllClientFromDatabase()).containsExactly(new Client(1, "existingClient"),
				new Client(2, "toAdd"));

	}

	@Test
	@DisplayName("Add Purchase to Client should add a purchase to client")
	void addPurchaseToClient() {
		Client clientNotToAddPurchase = new Client("clientNotToAddPurchase");
		addTestClientToDatabase(clientNotToAddPurchase);
		Client clientToAddPurchase = new Client("clientToAddPurchase");
		addTestClientToDatabase(clientToAddPurchase);
		Purchase purchaseToAdd = new Purchase(FIRST_TEST_DATE, 10.0);

		service.addPurchaseToClient(clientToAddPurchase, purchaseToAdd);
		assertThat(findPurchasesOfClient(2)).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
	}

	@Test
	@DisplayName("Delete purchase should remove the purchase from client list")
	void testDeletePurchase() {
		Client clientToAddPurchase = new Client("clientToAddPurchase");
		clientToAddPurchase.setPurchases(new ArrayList<Purchase>());
		addTestClientToDatabase(clientToAddPurchase);
		Purchase purchaseToRemain = new Purchase(FIRST_TEST_DATE, 10.0);
		purchaseToRemain.setClient(clientToAddPurchase);
		Purchase purchaseToRemove = new Purchase(SECOND_TEST_DATE, 5.0);
		purchaseToRemove.setClient(clientToAddPurchase);
		sessionFactory.inTransaction(session -> {
			Client client = session.find(Client.class, clientToAddPurchase.getId());
			session.persist(purchaseToRemain);
			session.persist(purchaseToRemove);
			client.getPurchases().add(purchaseToRemain);
			client.getPurchases().add(purchaseToRemove);
			session.merge(client);
		});
		Purchase toRemove = sessionFactory.fromTransaction(session -> session.find(Purchase.class, 2));
		service.deletePurchase(toRemove);
		Client client = sessionFactory.fromTransaction(session -> session.find(Client.class, 1));
		assertThat(client.getPurchases()).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
	}

	
	@Test
	@DisplayName("Delete client should remove all his purchases")
	void testDeleteClient() {
		Client clientToremove = new Client("clientToAddPurchase");
		clientToremove.setPurchases(new ArrayList<Purchase>());
		addTestClientToDatabase(clientToremove);
		Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
		purchase.setClient(clientToremove);
		sessionFactory.inTransaction(session -> {
			Client client = session.find(Client.class, clientToremove.getId());
			session.persist(purchase);
			client.getPurchases().add(purchase);
			session.merge(client);
		});
		Client toRemove = sessionFactory.fromTransaction(session -> session.find(Client.class, 1));
		service.deleteClient(toRemove);
		Purchase foundPurchase = sessionFactory.fromTransaction(session -> session.find(Purchase.class, 1));
		assertThat(foundPurchase).isNull();
	}
	
	
	@Test
	@DisplayName("Find all purchase should return a list of client's purchase")
	void testFindAllPurchaseOfClient() {
		Client client = new Client("clientToAddPurchase");
		client.setPurchases(new ArrayList<Purchase>());
		addTestClientToDatabase(client);
		Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
		purchase.setClient(client);
		Purchase otherPurchase = new Purchase(SECOND_TEST_DATE, 5.0);
		otherPurchase.setClient(client);
		sessionFactory.inTransaction(session -> {
			Client c = session.find(Client.class, client.getId());
			session.persist(purchase);
			session.persist(otherPurchase);
			c.getPurchases().add(purchase);
			c.getPurchases().add(purchase);
			session.merge(client);
		});
		
		Client toFound = sessionFactory.fromTransaction(session -> session.find(Client.class, 1));
		List<Purchase> purchases = service.findallPurchases(toFound);
		assertThat(purchases).containsExactly(
				new Purchase(1, FIRST_TEST_DATE, 10.0),
				new Purchase(2, SECOND_TEST_DATE, 5.0)
				);
		
	}
	
	@Test
	@DisplayName("Find all clients should return a list of clients")
	void testFindAllClients() {
		Client client = new Client("client");
		Client otherClient = new Client("otherClient");
		addTestClientToDatabase(client);
		addTestClientToDatabase(otherClient);
		
		List<Client> clients = service.findAllClients();
		assertThat(clients).containsExactly(
				new Client(1, "client"),
				new Client(2, "otherClient")
				);
	}
	
	@Test
	@DisplayName("Find client by id")
	void testFindClientById(){
		Client notToBeFound = new Client("notToBeFound");
		Client toBeFound = new Client("toBeFound");
		addTestClientToDatabase(notToBeFound);
		addTestClientToDatabase(toBeFound);
		
		Optional<Client> foundClient = service.findClientById(2);
		assertThat(foundClient).contains(toBeFound);
	}
	
	@Test
	@DisplayName("Find purchase by id")
	void testFindClientByIdWhenExists(){
		Client client = new Client("clientToAddPurchase");
		client.setPurchases(new ArrayList<Purchase>());
		addTestClientToDatabase(client);
		Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
		purchase.setClient(client);
		Purchase purchaseToBeFound = new Purchase(SECOND_TEST_DATE, 5.0);
		purchaseToBeFound.setClient(client);
		sessionFactory.inTransaction(session -> {
			Client c = session.find(Client.class, client.getId());
			session.persist(purchase);
			session.persist(purchaseToBeFound);
			c.getPurchases().add(purchase);
			c.getPurchases().add(purchase);
			session.merge(client);
		});
		
		Optional<Purchase> foundPurchase = service.findPurchaseById(2);
		assertThat(foundPurchase).contains(purchaseToBeFound);
	}
	
	private List<Client> readAllClientFromDatabase() {
		return sessionFactory
				.fromTransaction(session -> session.createSelectionQuery("from Client", Client.class).getResultList());
	}

	private void addTestClientToDatabase(Client client) {
		sessionFactory.inTransaction(session -> session.persist(client));
	}

	private List<Purchase> findPurchasesOfClient(int clientId) {
		return sessionFactory.fromTransaction(session -> session.find(Client.class, clientId).getPurchases());
	}

}
