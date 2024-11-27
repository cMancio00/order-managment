package managment.app;

import java.awt.EventQueue;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import managment.controller.Managmentcontroller;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;
import managment.view.swing.ManagmentViewSwing;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/*
 * Start App from Maven with:
 * 
 * mvn exec:java -Dexec.mainClass="managment.app.ManagmentSwingApp"
*/
@Command(mixinStandardHelpOptions = true)
public class ManagmentSwingApp implements Callable<Void> {
	private static final Logger LOGGER = LogManager.getLogger(ManagmentSwingApp.class);

	private SessionFactory sessionFactory;

	@Option(names = { "--mysql-host" }, description = "mySQL host address")
	private String mysqlHost = "localhost";

	@Option(names = { "--mysql-port" }, description = "mySQL host port")
	private int mysqlPort = 3306;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "managment";

	@Option(names = { "--username" }, description = "Username for database access")
	private String username = "order-manager";

	@Option(names = { "--password" }, description = "Password for database access")
	private String password = "mysecret";
	
	private String url = String.format("jdbc:mysql://%s:%s/%s", mysqlHost, mysqlPort, databaseName);
	
	public static void main(String[] args) {
		LOGGER.info("Starting the order managment application...");
		new CommandLine(new ManagmentSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				Properties mysqlProperties = new Properties();
				mysqlProperties.setProperty("hibernate.connection.url", url);
				mysqlProperties.setProperty("hibernate.connection.username", username);
				mysqlProperties.setProperty("hibernate.connection.password", password);
				sessionFactory = new Configuration().setProperties(mysqlProperties).configure().buildSessionFactory();
				
				PurchaseRepository purchaseRepository = new PurchaseRepositoryHibernate();
				ClientRepository clientRepository = new ClientRepositoryHibernate();

				PurchaseManagmentService service = new PurchaseManagmentService(sessionFactory, clientRepository,
						purchaseRepository);
				ManagmentViewSwing view = new ManagmentViewSwing();

				Managmentcontroller controller = new Managmentcontroller(view, service);
				view.setManagmentController(controller);

				view.setVisible(true);
				controller.findAllClients();
				
			}catch (Exception e) {
				LOGGER.error("Exception",e);
			}
		});
		return null;
	}


}
