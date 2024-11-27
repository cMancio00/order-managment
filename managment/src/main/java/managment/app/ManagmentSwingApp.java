package managment.app;

import java.awt.EventQueue;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import managment.controller.Managmentcontroller;
import managment.repository.client.ClientRepository;
import managment.repository.client.ClientRepositoryHibernate;
import managment.repository.purchase.PurchaseRepository;
import managment.repository.purchase.PurchaseRepositoryHibernate;
import managment.service.PurchaseManagmentService;
import managment.view.swing.ManagmentViewSwing;
import picocli.CommandLine.Command;

@Command(mixinStandardHelpOptions = true)
public class ManagmentSwingApp implements Callable<Void> {

	private static SessionFactory sessionFactory;
	
@Override
	public Void call() throws Exception {
		return null;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				String mysqlHost = "localhost";
				int mysqlPort = 3306;
				if (args.length > 0)
					mysqlHost = args[0];
				if (args.length > 1)
					mysqlPort = Integer.parseInt(args[1]);
				
				Properties mysqlProperties = new Properties();
				mysqlProperties.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/managment");
				mysqlProperties.setProperty("hibernate.connection.username", "order-manager");
				mysqlProperties.setProperty("hibernate.connection.password", "mysecret");
				sessionFactory = new Configuration().setProperties(mysqlProperties).configure()
						.buildSessionFactory();
				
				PurchaseRepository purchaseRepository = new PurchaseRepositoryHibernate();
				ClientRepository clientRepository = new ClientRepositoryHibernate();
				
				PurchaseManagmentService service = 
						new PurchaseManagmentService(sessionFactory, clientRepository, purchaseRepository);
				ManagmentViewSwing view = new ManagmentViewSwing();
				
				Managmentcontroller controller = new Managmentcontroller(view, service);
				view.setManagmentController(controller);
				
				view.setVisible(true);
				controller.findAllClients();
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
}


