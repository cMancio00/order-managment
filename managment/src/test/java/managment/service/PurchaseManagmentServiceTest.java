package managment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import managment.model.Client;
import managment.model.Purchase;
import managment.repository.client.ClientRepository;
import managment.repository.purchase.PurchaseRepository;

@DisplayName("Purchase Managment Service")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseManagmentServiceTest {

	private static final LocalDateTime FIRST_TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();
	private static final LocalDateTime SECOND_TEST_DATE = LocalDate.of(2024, Month.FEBRUARY, 1).atStartOfDay();
	@Mock
	private SessionFactory sessionFactory;

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@InjectMocks
	private PurchaseManagmentService service;

	@Mock
	private Session session;
	
	@BeforeEach
	void setup() {
		doAnswer(invocation -> {
			Consumer<Session> code = invocation.getArgument(0);
			code.accept(session);
			return null;
		}).when(sessionFactory).inTransaction(any());
		
		when(sessionFactory.fromTransaction(any())).thenAnswer(invocation -> {
			Function<Session,?> code = invocation.getArgument(0);
			return code.apply(session);
		});
	}

	@Nested
	@DisplayName("CRUD methods")
	class CrudMethods {
		@Test
		@DisplayName("Add Purchase should respect relational constraints")
		void testAddPurchaseShouldRespectRelationalConstraints() {
			Client client = new Client("testClient");
			Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
			client.setPurchases(new ArrayList<Purchase>());
			service.addPurchaseToClient(client, purchase);
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(purchaseRepository).save(eq(purchase), any());
			inOrder.verify(clientRepository).save(eq(client), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			assertThat(client.getPurchases()).containsExactly(purchase);
			assertThat(purchase.getClient()).isEqualTo(client);
		}
		
		@Test
		@DisplayName("Add Purchase on new Client should aslo create the list of purchases")
		void testAddPurchaseOnNewClient() {
			Client client = new Client("testClient");
			Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
			service.addPurchaseToClient(client, purchase);
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(purchaseRepository).save(eq(purchase), any());
			inOrder.verify(clientRepository).save(eq(client), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			assertThat(client.getPurchases()).containsExactly(purchase);
			assertThat(purchase.getClient()).isEqualTo(client);
		}
		
		@Test
		@DisplayName("Add Client")
		void testAddClient(){
			Client client = new Client("testClient");
			service.addClient(client);
			verify(clientRepository).save(client, session);
			verify(sessionFactory, times(1)).inTransaction(any());
			verifyNoMoreInteractions(clientRepository);
			verifyNoInteractions(purchaseRepository);
		}
		
		@Test
		@DisplayName("Delete Purchase")
		void testDeletePurchase(){
			Client client = new Client(1,"testClient");
			Purchase purchase = new Purchase(1,FIRST_TEST_DATE, 10.0);
			purchase.setClient(client);
			Purchase toDelete = new Purchase(2,SECOND_TEST_DATE, 5.0);
			toDelete.setClient(client);
			client.setPurchases(new ArrayList<Purchase>(Arrays.asList(purchase,toDelete)));
			
			when(clientRepository.findById(eq(1), any())).thenReturn(client);
			service.deletePurchase(toDelete);
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(purchaseRepository).delete(eq(toDelete), any());
			inOrder.verify(clientRepository).save(eq(client), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			assertThat(client.getPurchases()).containsExactly(purchase);
		}
		
		@Test
		@DisplayName("Delete Client should also delete all its purchases")
		void testDeleteClient(){
			Client client = new Client(1,"testClient");
			Purchase purchase = new Purchase(1,FIRST_TEST_DATE, 10.0);
			purchase.setClient(client);
			Purchase toDelete = new Purchase(2,SECOND_TEST_DATE, 5.0);
			toDelete.setClient(client);
			client.setPurchases(new ArrayList<Purchase>(Arrays.asList(purchase,toDelete)));
			
			service.deleteClient(client);
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(purchaseRepository, times(2)).delete(any(Purchase.class), any());
			inOrder.verify(clientRepository).delete(eq(client), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(purchaseRepository);
		}
		
		@Test
		@DisplayName("Find all Clients should return a list of all clients")
		void findAllClients() {
			Client firstClient = new Client(1,"firstClient");
			Purchase purchase = new Purchase(1,FIRST_TEST_DATE, 10.0);
			purchase.setClient(firstClient);
			Client secondClient = new Client(2,"secondClient");
			when(clientRepository.findAll(any())).thenReturn(
					asList(
							firstClient,
							secondClient));
			List<Client> clients = service.findAllClients();
			verify(clientRepository).findAll(session);
			verify(sessionFactory, times(1)).fromTransaction(any());
			verifyNoInteractions(purchaseRepository);
			assertThat(clients).containsExactly(firstClient,secondClient);
		}
		
		@Test
		@DisplayName("Find all purchases of a given client should return a list of its purchases")
		void findAllPurchaseOfClient() {
			Client client = new Client(1,"testClient");
			Purchase firstPurchase = new Purchase(1,FIRST_TEST_DATE, 10.0);
			firstPurchase.setClient(client);
			Purchase secondPurchase = new Purchase(2,SECOND_TEST_DATE, 5.0);
			secondPurchase.setClient(client);
			client.setPurchases(new ArrayList<Purchase>(Arrays.asList(firstPurchase,secondPurchase)));
			List<Purchase> purchases = service.findallPurchases(client);
			verify(sessionFactory, times(1)).fromTransaction(any());
			assertThat(purchases).containsExactly(firstPurchase,secondPurchase);
		}
	}

}
