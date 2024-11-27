package managment.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.ignoreStubs;
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
import java.util.Optional;
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
			Client client = new Client(1, "testClient");
			Purchase existingPurchase = new Purchase(1, FIRST_TEST_DATE, 10.0);
			existingPurchase.setClient(client);
			Purchase purchase = new Purchase(SECOND_TEST_DATE, 5.0);
			client.setPurchases(new ArrayList<Purchase>(asList(existingPurchase)));
			
			when(purchaseRepository.save(eq(purchase), any()))
				.thenReturn(new Purchase(2, SECOND_TEST_DATE, 5.0));
			
			Purchase addedPurchase = service.addPurchaseToClient(client, purchase);
			verify(purchaseRepository).save(eq(purchase), any());
			verify(sessionFactory, times(1)).fromTransaction(any());
			
			assertThat(client.getPurchases()).containsExactly(
					existingPurchase,
					new Purchase(2, SECOND_TEST_DATE, 5.0));
			assertThat(addedPurchase.getClient()).isEqualTo(client);
		}
		
		@Test
		@DisplayName("Add Purchase on new Client should aslo create the list of purchases")
		void testAddPurchaseOnNewClient() {
			Client client = new Client(1,"testClient");
			Purchase purchase = new Purchase(FIRST_TEST_DATE, 10.0);
	
			when(purchaseRepository.save(eq(purchase), any()))
				.thenReturn(new Purchase(1, FIRST_TEST_DATE, 10.0));
			
			Purchase addedPurchase = service.addPurchaseToClient(client, purchase);
			
			verify(purchaseRepository)
				.save(eq(purchase), any());

			verify(sessionFactory, times(1)).fromTransaction(any());
			assertThat(client.getPurchases()).containsExactly(
					new Purchase(1, FIRST_TEST_DATE, 10.0)
					);
			assertThat(addedPurchase.getClient().getId()).isEqualTo(1);
		}
		
		
		@Test
		@DisplayName("Add Client")
		void testAddClient(){
			Client client = new Client("testClient");
			when(clientRepository.save(eq(client), any())).thenReturn(new Client(1, "testClient"));
			Client addedClient = service.addClient(client);
			assertNotNull(addedClient);
			verify(clientRepository).save(client, session);
			verify(sessionFactory, times(1)).fromTransaction(any());
			verifyNoMoreInteractions(clientRepository);
			verifyNoInteractions(purchaseRepository);
			assertThat(addedClient.getId()).isEqualTo(1);
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
			
			when(clientRepository.findById(eq(1), any())).thenReturn(Optional.of(client));
			service.deletePurchase(toDelete);
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(purchaseRepository).delete(eq(toDelete), any());
			inOrder.verify(clientRepository).save(eq(client), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			assertThat(client.getPurchases()).containsExactly(purchase);
		}
		
		@Test
		@DisplayName("Delete a non existing Client should do nothing")
		void testDeleteNonExistingClient() {
			when(clientRepository.findById(eq(1), any())).thenReturn(Optional.empty());
			
			service.deleteClient(new Client(1,"notExisting"));
			verify(sessionFactory, times(1)).inTransaction(any());
			verifyNoMoreInteractions(ignoreStubs(clientRepository));
			verifyNoInteractions(purchaseRepository);
		}
		
		@Test
		@DisplayName("Delete Client should also delete all its purchases")
		void testDeleteClient(){
			Optional<Client> client = Optional.of(new Client(1,"testClient"));
			Purchase toDelete = new Purchase(1,SECOND_TEST_DATE, 5.0);
			toDelete.setClient(client.get());
			client.get().setPurchases(new ArrayList<Purchase>(Arrays.asList(toDelete)));
			
			when(clientRepository.findById(eq(1), any())).thenReturn(client);
			
			service.deleteClient(client.get());
			InOrder inOrder = inOrder(clientRepository, purchaseRepository);
			inOrder.verify(clientRepository).findById(eq(1), any());
			inOrder.verify(purchaseRepository, times(1)).delete(any(Purchase.class), any());
			inOrder.verify(clientRepository).delete(eq(client.get()), any());
			verify(sessionFactory, times(1)).inTransaction(any());
			verifyNoMoreInteractions(ignoreStubs(clientRepository));
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
		
		@Test
		@DisplayName("Find purchase by id when purchase is present")
		void testFindPurchaseByIdWhenExists(){
			Purchase purchase = new Purchase(1, FIRST_TEST_DATE,10.0);
			when(purchaseRepository.findById(1, session)).thenReturn(Optional.of(purchase));
			Optional<Purchase> foundPurchase = service.findPurchaseById(1);
			assertThat(foundPurchase).contains(purchase);
		}
		
		@Test
		@DisplayName("Find purchase by id when purchase is not present")
		void testFindPurchaseByIdWhenDoesNotExists(){
			when(purchaseRepository.findById(1, session)).thenReturn(Optional.empty());
			Optional<Purchase> foundPurchase = service.findPurchaseById(1);
			assertThat(foundPurchase).isEmpty();
		}
		
		@Test
		@DisplayName("Find client by id when client is present")
		void testFindClientByIdWhenExists(){
			Client existingClient = new Client(1,"existingClient");
			when(clientRepository.findById(1, session)).thenReturn(Optional.of(existingClient));
			Optional<Client> foundClient = service.findClientById(1);
			assertThat(foundClient).contains(existingClient);
		}
		
		@Test
		@DisplayName("Find client by id when purchase is not present")
		void testFindClientByIdWhenDoesNotExists(){
			when(purchaseRepository.findById(1, session)).thenReturn(Optional.empty());
			Optional<Client> foundClient = service.findClientById(1);
			assertThat(foundClient).isEmpty();
		}
	}
	

}
