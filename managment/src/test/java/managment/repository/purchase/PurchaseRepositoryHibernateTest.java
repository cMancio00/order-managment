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
	@DisplayName("Save")
	class SaveTests{
		@DisplayName("When database is empty")
		@Test
		void testSave() {
			Purchase toAdd = sessionFactory.fromTransaction(session -> 
				purchaseRepository.save(new Purchase(FIRST_TEST_DATE, 10.0), session));
			assertThat(readAllOrdersFromDatabase()).containsExactly(toAdd);
			assertThat(toAdd.getId()).isEqualTo(1);
		}
		
		@DisplayName("When database is not empy")
		@Test
		void testSaveNotEmpty(){
			addOrderToDatabase(new Purchase(FIRST_TEST_DATE, 10.0));
			Purchase toAdd = sessionFactory.fromTransaction(session -> 
				purchaseRepository.save(new Purchase(SECOND_TEST_DATE, 5.0), session));
			assertThat(readAllOrdersFromDatabase())
				.containsExactly(
						new Purchase(1, FIRST_TEST_DATE, 10.0),
						toAdd
						);
			assertThat(toAdd.getId()).isEqualTo(2);
		}
	}
	
	@Nested
	@DisplayName("FindById")
	class FindById{
		@DisplayName("Find by id when Purchase is preset")
		@Test
		void testFindByIdWhenIsPresent(){
			Purchase notToBeFound = new Purchase(FIRST_TEST_DATE, 10.0);
			Purchase toBeFound = new Purchase(SECOND_TEST_DATE, 5.0);
			addOrderToDatabase(notToBeFound);
			addOrderToDatabase(toBeFound);
			Optional<Purchase> found = sessionFactory.fromTransaction(session -> {
				return purchaseRepository.findById(2, session);
			});
			assertThat(found).contains(new Purchase(2, SECOND_TEST_DATE, 5.0));
		}
		
		@DisplayName("Find by Id when Purchase is not present should return empty optional")
		@Test
		void testFindByIdWhenIsNotPresent(){
			Optional<Purchase> found = sessionFactory.fromTransaction(session -> {
				return purchaseRepository.findById(1,session);
			});
			assertThat(found).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("Delete")
	class DeleteTests{
		@DisplayName("Delete Purchase when is present")
		@Test
		void testDeleteWhenClientIsPresent(){
			Purchase notToDeleted = new Purchase(FIRST_TEST_DATE, 10.0);
			Purchase toBeDeleted = new Purchase(SECOND_TEST_DATE, 5.0);
			addOrderToDatabase(notToDeleted);
			addOrderToDatabase(toBeDeleted);
			sessionFactory.inTransaction(session -> {
				Purchase toDelete = session.find(Purchase.class, 2);
				purchaseRepository.delete(toDelete, session);
			});
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
		}
		
		@DisplayName("When is not present should do nothing")
		@Test
		void testDeleteWhenPurchaseIsNotPresent(){
			Purchase notToDeleted = new Purchase(FIRST_TEST_DATE, 10.0);
			addOrderToDatabase(notToDeleted);
			sessionFactory.inTransaction(session -> {
				Purchase toDelete = session.find(Purchase.class, 2);
				purchaseRepository.delete(toDelete, session);
			});
			assertThat(readAllOrdersFromDatabase()).containsExactly(new Purchase(1, FIRST_TEST_DATE, 10.0));
		}
	}
	
	@Nested
	@DisplayName("FindAll")
	class FindAll{
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
			addOrderToDatabase(firstOrder);
			addOrderToDatabase(secondOrder);
			List<Purchase> orders = sessionFactory.fromSession(session -> purchaseRepository.findAll(session));
			assertThat(orders).containsExactly(
					new Purchase(1, FIRST_TEST_DATE, 10.0),
					new Purchase(2, SECOND_TEST_DATE, 5.0)
				);
		}
	}

	private List<Purchase> readAllOrdersFromDatabase() {
		return sessionFactory.fromTransaction(
				session -> session.createSelectionQuery("from Purchase", Purchase.class).getResultList());
	}
	
	private void addOrderToDatabase(Purchase purchase) {
		sessionFactory.inTransaction(session -> session.persist(purchase));
	}
}
