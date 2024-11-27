package managment.integration;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import managment.controller.Managmentcontroller;
import managment.model.Client;
import managment.model.Purchase;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;
import managment.view.ManagmentView;

@Testcontainers
@ExtendWith(MockitoExtension.class)
@DisplayName("Controller-Service IntegrationTest")
class ControllerServiceIT {

	private static String mysqlVersion = System.getProperty("mysql.version", "9.1.0");
	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:" + mysqlVersion))
			.withDatabaseName("Controller-Service-db").withUsername("manager").withPassword("it").withReuse(true);

	private static final LocalDateTime TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();
	
	private static SessionFactory sessionFactory;

	private ClientRepository clientRepository;

	private PurchaseRepository purchaseRepository;

	private PurchaseManagmentService service;
	
	private Managmentcontroller controller;
	
	@Mock
	private ManagmentView view;
	

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		mysql.start();
		Properties mysqlProperties = new Properties();
		mysqlProperties.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
		mysqlProperties.setProperty("hibernate.connection.username", mysql.getUsername());
		mysqlProperties.setProperty("hibernate.connection.password", mysql.getPassword());
		sessionFactory = new Configuration().setProperties(mysqlProperties).configure("hibernate-integration.cfg.xml")
				.buildSessionFactory();
	}

	@BeforeEach
	void setup() {
		sessionFactory.getCache().evictAllRegions();
		clientRepository = new ClientRepositoryHibernate();
		purchaseRepository = new PurchaseRepositoryHibernate();
		service = new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
		controller = new Managmentcontroller(view, service);
	}

	@AfterEach
	void tearDown() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
	}

	@Test
	@DisplayName("Find all Clients when a client is present should return it as list")
	void testFindAllClients() {
		addClientToDatabase(new Client("firstClient"));
		addClientToDatabase(new Client("secondClient"));
		controller.findAllClients();
		verify(view).showAllClients(asList(
				new Client(1,"firstClient"),
				new Client(2,"secondClient")));
	}
	
	@Test
	@DisplayName("Add client")
	void testAddClient(){
		Client toAdd = new Client("toAdd");
		controller.add(toAdd);
		verify(view).clientAdded(new Client(1, "toAdd"));
	}
	
	@Test
	@DisplayName("Remove client when existing")
	void testRemoveClientWhenExisting(){
		Client toDelete = new Client(1, "toDelete");
		addClientToDatabase(new Client("toDelete"));
		controller.remove(toDelete);
		verify(view).clientRemoved(new Client(1, "toDelete"));
	}
	
	@Test
	@DisplayName("Find all purchase of an existing selected client")
	void findAllPurchaseOfSelectedClient(){
		Client selectedClient = new Client("selectedClient");
		selectedClient.setPurchases(new ArrayList<Purchase>());
		addClientToDatabase(selectedClient);
		Purchase existingPurchase = new Purchase(TEST_DATE, 10.0);
		existingPurchase.setClient(selectedClient);

		sessionFactory.inTransaction(session -> {
			Client client = session.find(Client.class, selectedClient.getId());
			session.persist(existingPurchase);
			client.getPurchases().add(existingPurchase);
			session.merge(client);
		});
		
		controller.findAllPurchasesOf(selectedClient);
		verify(view).showAllPurchases(asList(new Purchase(1, TEST_DATE, 10.0)));
	}
	
	@Test
	@DisplayName("Add purchase to selected client when exists")
	void testAddPurchaseToSelectedClientWhenExists(){
		Client selectedClient = new Client("selectedClient");
		addClientToDatabase(selectedClient);
		Purchase toAdd = new Purchase(TEST_DATE, 5.0);
		
		controller.addPurchaseToSelectedClient(
				new Client(1, "selectedClient"), toAdd);
		verify(view).purchaseAdded(new Purchase(1, TEST_DATE, 5.0));
	}
	
	@Test
	@DisplayName("Remove purchase when exists")
	void testRemovePurchaseWhenExists(){
		Client selectedClient = new Client("selectedClient");
		selectedClient.setPurchases(new ArrayList<Purchase>());
		addClientToDatabase(selectedClient);
		Purchase existingPurchase = new Purchase(TEST_DATE, 10.0);
		existingPurchase.setClient(selectedClient);

		sessionFactory.inTransaction(session -> {
			Client client = session.find(Client.class, selectedClient.getId());
			session.persist(existingPurchase);
			client.getPurchases().add(existingPurchase);
			session.merge(client);
		});
		
		controller.remove(existingPurchase);
		verify(view).purchaseRemoved(existingPurchase);
	}
	
	private void addClientToDatabase(Client client) {
		sessionFactory.inTransaction(session ->
			session.persist(client));
	}

}
