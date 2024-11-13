package managment.repository.order;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
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
	private static final String H2_DATABASE = "test-db";
	private static final String CONNECTION_URL = String.format("jdbc:h2:mem:%s", H2_DATABASE);
	private static final LocalDateTime FIRST_TEST_DATE = LocalDateTime.now();
	private static final LocalDateTime SECOND_TEST_DATE = LocalDateTime.now();
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
			Optional<Order> found = sessionFactory.fromTransaction(session -> {
				addOrderToDataBase(notToBeFound, session);
				addOrderToDataBase(toBeFound, session);
				return orderRepository.findById(2, session);
			});
			assertThat(found).contains(new Order(2, SECOND_TEST_DATE, 5.0));
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
	
	private void addOrderToDataBase(Order order, Session session) {
		session.persist(order);
	}
}
