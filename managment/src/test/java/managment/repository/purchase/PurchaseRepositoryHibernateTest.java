package managment.repository.purchase;

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

import managment.model.Purchase;

@DisplayName("Purchase Repository")
class PurchaseRepositoryHibernateTest {
	private static final String H2_DATABASE = "purchase-test-db";
	private static final String CONNECTION_URL = String.format("jdbc:h2:mem:%s", H2_DATABASE);
	private static final LocalDateTime FIRST_TEST_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	private static final LocalDateTime SECOND_TEST_DATE = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	private SessionFactory sessionFactory;
	private PurchaseRepository purchaseRepository;

	@BeforeEach
	void setUpDatabase() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.connection.url", CONNECTION_URL);
		sessionFactory = configuration.configure("hibernate-unit.cfg.xml").buildSessionFactory();
		purchaseRepository = new PurchaseRepositoryHibernate();
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
			purchaseRepository.save(new Purchase(FIRST_TEST_DATE, 10.0), session));
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
		}
		@DisplayName("Find by id when Purchase is preset")
		@Test
		void testFindByIdWhenIsPresent(){
			Purchase notToBeFound = new Purchase(FIRST_TEST_DATE, 10.0);
			Purchase toBeFound = new Purchase(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(notToBeFound);
			addOrderToDataBase(toBeFound);
			Optional<Purchase> found = sessionFactory.fromTransaction(session -> {
				return purchaseRepository.findById(2, session);
			});
			assertThat(found).contains(new Purchase(2, SECOND_TEST_DATE, 5.0));
		}
		
		@DisplayName("Delete Purchase when is present")
		@Test
		void testDeleteWhenClientIsPresent(){
			Purchase notToDeleted = new Purchase(FIRST_TEST_DATE, 10.0);
			Purchase toBeDeleted = new Purchase(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(notToDeleted);
			addOrderToDataBase(toBeDeleted);
			sessionFactory.inTransaction(session -> {
				Purchase toDelete = session.find(Purchase.class, 2);
				purchaseRepository.delete(toDelete, session);
			});
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
		}
		
		@DisplayName("Find all when database is empty should return an empty list")
		@Test
		void testFindAllWhenDatabaseIsEmpty(){
			List<Purchase> clients = sessionFactory.fromSession(session ->
				 purchaseRepository.findAll(session));
			assertThat(clients).isEmpty();
		}
		@DisplayName("Find all when purchases are present should return the list of purchases")
		@Test
		void testFindAllWhenClientsArePresent(){
			Purchase firstOrder = new Purchase(FIRST_TEST_DATE, 10.0);
			Purchase secondOrder = new Purchase(SECOND_TEST_DATE, 5.0);
			addOrderToDataBase(firstOrder);
			addOrderToDataBase(secondOrder);
			List<Purchase> orders = sessionFactory.fromSession(session -> purchaseRepository.findAll(session));
			assertThat(orders).containsExactly(
					new Purchase(1, FIRST_TEST_DATE, 10.0),
					new Purchase(2, SECOND_TEST_DATE, 5.0)
				);
		}
	}
	
	@Nested
	@DisplayName("Error Cases")
	class ErrorCases{
		@DisplayName("Find by Id when Purchase is not present should return empty optional")
		@Test
		void testFindByIdWhenIsNotPresent(){
			Optional<Purchase> found = sessionFactory.fromTransaction(session -> {
				return purchaseRepository.findById(1,session);
			});
			assertThat(found).isEmpty();
		}
	}
	
	private List<Purchase> readAllOrdersFromDatabase() {
		return sessionFactory.fromTransaction(
				session -> session.createSelectionQuery("from Purchase", Purchase.class).getResultList());
	}
	
	private void addOrderToDataBase(Purchase order) {
		sessionFactory.inTransaction(session -> session.persist(order));
	}
}
