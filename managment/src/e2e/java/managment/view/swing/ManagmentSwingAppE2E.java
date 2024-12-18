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

import managment.controller.ManagmentController;
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
			DockerImageName.parse("mysql:" + mysqlVersion)).withDatabaseName("test-db").withUsername("manager")
			.withPassword("test");

	private static SessionFactory sessionFactory;
	private ClientRepository clientRepository;
	private PurchaseRepository purchaseRepository;
	private PurchaseManagmentService service;
	private ManagmentController controller;
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
		service.addClient(new Client("otherClient"));

		service.addPurchaseToClient(aClient, new Purchase(getCurrentDate(), 10.0));
		service.addPurchaseToClient(aClient, new Purchase(getCurrentDate(), 5.0));
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
			controller = new ManagmentController(view, service);
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
	public void testDeleteClientButtonSuccess() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "aClient" + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.list("clientList").contents()).noneMatch(e -> e.contains("aClient"));
	}

	@Test
	@GUITest
	public void testDeleteClientButtonError() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "otherClient" + ".*"));
		GuiActionRunner.execute(() -> service.deleteClient(new Client(2, "otherClient")));
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		assertThat(window.label("messageLable").text()).contains("otherClient", "not found");
	}

	@Test
	@GUITest
	public void testAddPurchaseSuccess() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "otherClient" + ".*"));
		window.textBox("purchaseAmountBox").enterText("15.0");
		window.button(JButtonMatcher.withText("Add Amount")).click();
		assertThat(window.list("purchaseList").contents()).anySatisfy(e -> assertThat(e).contains("15.0"));
	}
	
	@Test
	@GUITest
	public void testDeletePurchaseButtonSuccess() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "aClient" + ".*"));
		window.list("purchaseList").selectItem(Pattern.compile(".*" + "10.0" + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).click();
		assertThat(window.list("purchaseList").contents()).noneMatch(e -> e.contains("10.0"));
	}
	
	@Test
	@GUITest
	public void testDeletePurchaseButtonError() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "aClient" + ".*"));
		window.list("purchaseList").selectItem(Pattern.compile(".*" + "10.0" + ".*"));
		GuiActionRunner.execute(() -> service.deletePurchase(view.getListPurchaseModel().getElementAt(0)));
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).click();
		assertThat(window.label("messageLable").text()).contains("10.0", "not found");
	}
	
	@Test
	@GUITest
	public void testDeletePurchaseButtonSuccessMultipleTimes() {
		window.list("clientList").selectItem(Pattern.compile(".*" + "aClient" + ".*"));
		window.list("purchaseList").selectItem(Pattern.compile(".*" + "10.0" + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).click();
		assertThat(window.list("purchaseList").contents()).noneMatch(e -> e.contains("10.0"));
		window.list("purchaseList").selectItem(Pattern.compile(".*" + "5.0" + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).click();
		assertThat(window.list("purchaseList").contents()).noneMatch(e -> e.contains("5.0"));
	}
	

	private LocalDateTime getCurrentDate() {
		return LocalDateTime.of(
				now().getYear(),
				now().getMonth(),
				now().getDayOfMonth(),
				now().getHour(),
				now().getMinute());
	}
}
