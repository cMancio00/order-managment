package managment.integration;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import managment.controller.Managmentcontroller;
import managment.model.Client;
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
	@Container
	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:" + mysqlVersion))
			.withDatabaseName("it-db").withUsername("manager").withPassword("it");

	private static SessionFactory sessionFactory;

	private ClientRepository clientRepository;

	private PurchaseRepository purchaseRepository;

	private PurchaseManagmentService service;
	
	private Managmentcontroller controller;
	
	@Mock
	private ManagmentView view;
	

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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
		controller = new Managmentcontroller(view, service);
	}

	@AfterEach
	void tearDown() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
	}

	@Test
	@DisplayName("Find all Clients when a client is present should return it as list")
	void testFindAllClients() {
		addClientToDatabase("firstClient");
		addClientToDatabase("secondClient");
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
		verify(view).clientAdded(toAdd);
	}
	
	private void addClientToDatabase(String name) {
		sessionFactory.inTransaction(session ->
			session.merge(new Client(name)));
	}

}
