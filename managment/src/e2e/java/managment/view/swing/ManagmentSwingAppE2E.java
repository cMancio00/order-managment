package managment.view.swing;

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

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.regex.Pattern;

@RunWith(GUITestRunner.class)
public class ManagmentSwingAppE2E extends AssertJSwingJUnitTestCase {

	private static String mysqlVersion = System.getProperty("mysql.version", "9.1.0");

	@ClassRule
	@SuppressWarnings({ "rawtypes", "resource" })
	public static final MySQLContainer mysql = (MySQLContainer) new MySQLContainer(
			DockerImageName.parse("mysql:" + mysqlVersion)).withDatabaseName("e2e-db").withUsername("manager")
			.withPassword("e2e").withReuse(true);

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
		sessionFactory = new Configuration().setProperties(mysqlProperties).configure("hibernate-integration.cfg.xml")
				.buildSessionFactory();
	}

	@Before
	public void setup() {
		sessionFactory.getCache().evictAllRegions();
		clientRepository = new ClientRepositoryHibernate();
		purchaseRepository = new PurchaseRepositoryHibernate();
		service = new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
		Client aClient = service.addClient(new Client("aClient"));
		Client otherClient = service.addClient(new Client("otherClient"));

		Purchase aPurchase = service.addPurchaseToClient(aClient, new Purchase(getCurrentDate(), 10.0));
		Purchase otherPurchase = service.addPurchaseToClient(aClient, new Purchase(getCurrentDate(), 5.0));
		GuiActionRunner.execute(() -> controller.findAllClients());
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
			controller = new Managmentcontroller(view, service);
			view.setManagmentController(controller);
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();
		
	}

	@Test
	@GUITest
	public void testAddButtonSuccess() {
		window.textBox("clientNameBox").enterText("newClient");
		window.button(JButtonMatcher.withText("Add New Client")).click();
		assertThat(window.list("clientList").contents()).anySatisfy(e -> assertThat(e).contains("newClient"));
	}
	
	@Test
	@GUITest
	public void testDeleteButtonSuccess() {
		window.list("clientList")
		.selectItem(Pattern.compile(".*" + "aClient" + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.list("clientList").contents())
		.noneMatch(e -> e.contains("aClient"));
	}
	
	@Test
	@GUITest
	public void testDeleteButtonError() {
		window.list("clientList")
		.selectItem(Pattern.compile(".*" + "otherClient" + ".*"));
		GuiActionRunner.execute(() -> service.deleteClient(new Client(2, "otherClient")));
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.label("messageLable").text())
		.contains("otherClient", "not found");
	}
	
	

	private LocalDateTime getCurrentDate() {
		return LocalDateTime.of(now().getYear(), now().getMonth(), now().getDayOfMonth(), now().getHour(),
				now().getHour());
	}
}
