package managment.repository.order;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import managment.model.Order;

@DisplayName("Order Repository")
class OrderRepositoryHibernateTest {
	private static final String H2_DATABASE = "order-test-db";
	private static final String CONNECTION_URL = String.format("jdbc:h2:mem:%s", H2_DATABASE);
	private static final LocalDateTime FIRST_TEST_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	private static final LocalDateTime SECOND_TEST_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	private SessionFactory sessionFactory;
	private OrderRepository orderRepository;

	@BeforeEach
	void setUpDatabase() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.connection.url", CONNECTION_URL);
		sessionFactory = configuration.configure("hibernate-unit.cfg.xml").buildSessionFactory();
		orderRepository = new OrderRepositoryHibernate();
	}

	@AfterEach
	void tearDownDatabase() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		sessionFactory.close();
	}

	@Nested
	@DisplayName("Happy Cases")
	class HappyCases{
		
		@DisplayName("Save when database is empty")
		@Test
		void testSave() {
			sessionFactory.inTransaction(session -> 
			orderRepository.save(new Order(FIRST_TEST_DATE, 10.0), session));
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Order(1, FIRST_TEST_DATE, 10.0));
		}
		@DisplayName("Find by id when Order is preset")
		@Test
		void testFindByIdWhenIsPresent(){
			Order notToBeFound = new Order(FIRST_TEST_DATE, 10.0);
			Order toBeFound = new Order(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(notToBeFound);
			addOrderToDataBase(toBeFound);
			Optional<Order> found = sessionFactory.fromTransaction(session -> {
				return orderRepository.findById(2, session);
			});
			assertThat(found).contains(new Order(2, SECOND_TEST_DATE, 5.0));
		}
		
		@DisplayName("Delete Order when is present")
		@Test
		void testDeleteWhenClientIsPresent(){
			Order notToDeleted = new Order(FIRST_TEST_DATE, 10.0);
			Order toBeDeleted = new Order(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(notToDeleted);
			addOrderToDataBase(toBeDeleted);
			sessionFactory.inTransaction(session -> {
				Order toDelete = session.find(Order.class, 2);
				orderRepository.delete(toDelete, session);
			});
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Order(1, FIRST_TEST_DATE, 10.0));
		}
		
		@DisplayName("Find all when database is empty should return an empty list")
		@Test
		void testFindAllWhenDatabaseIsEmpty(){
			List<Order> clients = sessionFactory.fromSession(session ->
				 orderRepository.findAll(session));
			assertThat(clients).isEmpty();
		}
		@DisplayName("Find all when orders are present should return the list of orders")
		@Test
		void testFindAllWhenClientsArePresent(){
			Order firstOrder = new Order(FIRST_TEST_DATE, 10.0);
			Order secondOrder = new Order(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(firstOrder);
			addOrderToDataBase(secondOrder);
			List<Order> orders = sessionFactory.fromSession(session -> orderRepository.findAll(session));
			assertThat(orders).containsExactly(
					new Order(1, FIRST_TEST_DATE, 10.0),
					new Order(2, SECOND_TEST_DATE, 5.0)
				);
		}
	}
	
	@Nested
	@DisplayName("Error Cases")
	class ErrorCases{
		@DisplayName("Find by Id when Order is not present should return empty optional")
		@Test
		void testFindByIdWhenIsNotPresent(){
			Optional<Order> found = sessionFactory.fromTransaction(session -> {
				return orderRepository.findById(1,session);
			});
			assertThat(found).isEmpty();
		}
	}
	
	private List<Order> readAllOrdersFromDatabase() {
		return sessionFactory.fromTransaction(
				session -> session.createSelectionQuery("from Order", Order.class).getResultList());
	}
	
	private void addOrderToDataBase(Order order) {
		sessionFactory.inTransaction(session -> session.persist(order));
	}
}