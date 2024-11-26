package managment.integration.view;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Properties;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import managment.controller.Managmentcontroller;
import managment.model.Client;
import managment.model.Purchase;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;
import managment.view.swing.ManagmentViewSwing;

@RunWith(GUITestRunner.class)
public class ManagmentViewSwingIT extends AssertJSwingJUnitTestCase {

	private static String mysqlVersion = System.getProperty("mysql.version", "9.1.0");

	@ClassRule
	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:" + mysqlVersion))
			.withDatabaseName("ViewIT-db").withUsername("manager").withPassword("it").withReuse(true);

	private static SessionFactory sessionFactory;
	private ClientRepository clientRepository;
	private PurchaseRepository purchaseRepository;
	private PurchaseManagmentService service;
	private Managmentcontroller controller;
	private ManagmentViewSwing view;

	private FrameFixture window;
	
	@BeforeClass
	public static void setupServer() {
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
	
	@Before
	public void setup() {
		sessionFactory.getCache().evictAllRegions();
		clientRepository = new ClientRepositoryHibernate();
		purchaseRepository = new PurchaseRepositoryHibernate();
		service = new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
	}
	
	@After
	public void truncateSchema() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
	}
	
	@Override
	protected void onSetUp() throws Exception {
		sessionFactory.getCache().evictAllRegions();
		clientRepository = new ClientRepositoryHibernate();
		purchaseRepository = new PurchaseRepositoryHibernate();
		service = new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
		
		GuiActionRunner.execute(() -> {
		view = new ManagmentViewSwing();
		controller =
		new Managmentcontroller(view, service);
		view.setManagmentController(controller);
		return view;
		});
		window = new FrameFixture(robot(), view);
		window.show(); 
	}
	
	@Test @GUITest
	public void testFindAllClients(){
		Client firstClient = new Client("firstClient");
		Client secondClient = new Client("secondClient");
		service.addClient(firstClient);
		service.addClient(secondClient);
		
		GuiActionRunner.execute(
				() -> controller.findAllClients());
		
		assertThat(window.list("clientList").contents()).containsExactly(
				new Client(1, "firstClient").toString(),
				new Client(2, "secondClient").toString()
				);
	}
	
	@Test @GUITest
	public void testAddClientButtonSuccess(){
		window.textBox("clientNameBox").enterText("test");
		window.button(JButtonMatcher.withText("Add New Client")).click();
		assertThat(window.list("clientList").contents()).containsExactly(
				new Client(1, "test").toString());
	}
	
	@Test @GUITest
	public void testDeleteClientButtonSuccess(){
		GuiActionRunner.execute(() ->
				controller.add(new Client("toDelete")));

		window.list("clientList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.list("clientList").contents()).isEmpty();
	}

	@Test @GUITest
	public void testDeleteClientButtonError(){
		Client notExisting = new Client(1, "notExisting");
		GuiActionRunner.execute(
				() -> view.getListClientsModel().addElement(notExisting));
		window.list("clientList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.list("clientList").contents()).containsExactly(notExisting.toString());
		window.label("messageLable").requireText(notExisting.toString() + " not found");
	}
	
	@Test @GUITest
	public void testShowAllPurchaseOfASelectedClient(){
		Client addedClient = service.addClient(new Client("testClient"));
		Purchase aPurchase = service.addPurchaseToClient(
				addedClient,
				new Purchase(getCurrentDate(), 10.0));
		Purchase anOtherPurchase = service.addPurchaseToClient(
				addedClient,
				new Purchase(getCurrentDate(), 5.0));
		
		GuiActionRunner.execute(
				() -> controller.findAllClients());
		
		window.list("clientList").selectItem(0);
		assertThat(window.list("purchaseList").contents()).containsExactly(
				aPurchase.toString(),
				anOtherPurchase.toString()
				);
		
	}
	
	@Test
	public void testAddPurchaseButtonSuccess(){
		service.addClient(new Client("testClient"));
		GuiActionRunner.execute(
				() -> controller.findAllClients());
		
		window.list("clientList").selectItem(0);
		
		window.textBox("purchaseAmmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Ammount")).click();
		assertThat(window.list("purchaseList").contents()).containsExactly(
				new Purchase(1, getCurrentDate(), 10.0).toString());
		
	}
	
	private LocalDateTime getCurrentDate() {
		return LocalDateTime.of(
						now().getYear(), 
						now().getMonth(), 
						now().getDayOfMonth(), 
						now().getHour(), 
						now().getHour());
	}
}
