package managment.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.util.Collections;
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
	@DisplayName("Client")
	class ClietCRUD{
		@Nested
		@DisplayName("Add Client")
		class AddClient{
			
			@Test
			@DisplayName("When Client is not null")
			void testAddClientWhenDatabaseIsEmpty(){
				Client aClient = new Client("aClient");
				when(clientRepository.save(eq(aClient), any())).thenReturn(new Client(1, "aClient"));
				Client added = service.addClient(aClient);
				assertThat(added).isNotNull();
				verify(clientRepository).save(aClient, session);
				verify(sessionFactory, times(1)).fromTransaction(any());
				verifyNoMoreInteractions(clientRepository);
				verifyNoInteractions(purchaseRepository);
			}
			
			@Test
			@DisplayName("When Client is null should be no interactions")
			void testAddClientWhenClientIsNull(){
				IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
						() -> service.addClient(null));
				assertThat(e.getMessage()).isEqualTo("Can't add a null Object");
				verifyNoInteractions(clientRepository);
				verifyNoInteractions(purchaseRepository);
			}
		}
		
		@Nested
		@DisplayName("Delete Client")
		class DeleteClient{
			@Test
			@DisplayName("When a client do not exists should do nothing")
			void testDeleteNonExistingClient() {
				when(clientRepository.findById(eq(1), any())).thenReturn(Optional.empty());
				
				service.deleteClient(new Client(1,"notExisting"));
				verify(sessionFactory, times(1)).inTransaction(any());
				verifyNoMoreInteractions(ignoreStubs(clientRepository));
				verifyNoInteractions(purchaseRepository);
			}
			
			@Test
			@DisplayName("Should delete the client with no purchases without interacting with the purchase repository")
			void testDeleteClientShouldDeleteTheClient(){
				Optional<Client> toDelete = Optional.of(new Client(1,"toDelete"));
				when(clientRepository.findById(eq(1), any()))
					.thenReturn(Optional.of(new Client(1,"toDelete")));
				
				service.deleteClient(toDelete.get());
				InOrder inOrder = inOrder(clientRepository);
				inOrder.verify(clientRepository).findById(eq(1), any());
				inOrder.verify(clientRepository).delete(eq(toDelete.get()), any());
				verify(sessionFactory, times(1)).inTransaction(any());
				verifyNoMoreInteractions(ignoreStubs(clientRepository));
				verifyNoInteractions(purchaseRepository);
			}
			
			@Test
			@DisplayName("Should also delete all client's purchases")
			void testDeleteClientShouldDeletePurchases(){
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
		}
		
		@Nested
		@DisplayName("Find Client by id")
		class FindClient{
			@Test
			@DisplayName("When client is present")
			void testFindClientByIdWhenExists(){
				Client existingClient = new Client(1,"existingClient");
				when(clientRepository.findById(1, session)).thenReturn(Optional.of(existingClient));
				Optional<Client> foundClient = service.findClientById(1);
				assertThat(foundClient).contains(existingClient);
			}
			
			@Test
			@DisplayName("When client is not present")
			void testFindClientByIdWhenDoesNotExists(){
				when(clientRepository.findById(1, session)).thenReturn(Optional.empty());
				Optional<Client> foundClient = service.findClientById(1);
				assertThat(foundClient).isEmpty();
			}
			
			@Test
			@DisplayName("When client id is not yet set")
			void testFindClientByIdWhenNotSet(){
				// In this case id will be 0.
				Optional<Client> foundClient = service.findClientById(new Client("aClient").getId());
				assertThat(foundClient).isEmpty();
			}
		}
		
		@Nested
		@DisplayName("FindAll Clients")
		class FindAllClients{
			
			@Test
			@DisplayName("When are presents should return a list of all clients")
			void findAllClientsWhenArePresents() {
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
			@DisplayName("When are not presents should return a list of all clients")
			void findAllClientsWhenAreNotPresents() {
				when(clientRepository.findAll(any())).thenReturn(Collections.emptyList());
				List<Client> clients = service.findAllClients();
				verify(clientRepository).findAll(session);
				verify(sessionFactory, times(1)).fromTransaction(any());
				verifyNoInteractions(purchaseRepository);
				assertThat(clients).isEmpty();
			}
		}
	}
	
	@Nested
	@DisplayName("Purchase")
	class PurchaseCRUD {
		@Nested
		@DisplayName("Add Purchase")
		class AddPurchase{
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
		}
		
		@Nested
		@DisplayName("Delete Purchase")
		class DeletePurcahse{
			
			@Test
			@DisplayName("When is present")
			void testDeletePurchaseWhenIsPresent(){
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
			@DisplayName("When is not present in client purchases")
			void testDeletePurchaseWhenIsNotPresent(){
				Client client = new Client(1,"testClient");
				Purchase purchase = new Purchase(1,FIRST_TEST_DATE, 10.0);
				purchase.setClient(client);
				client.setPurchases(new ArrayList<Purchase>(Arrays.asList(purchase)));
				
				Purchase toDelete = new Purchase(SECOND_TEST_DATE, 5.0);
				toDelete.setClient(client);
				
				when(clientRepository.findById(eq(1), any())).thenReturn(Optional.of(client));
				service.deletePurchase(toDelete);
				verifyNoMoreInteractions(ignoreStubs(clientRepository));
				verifyNoInteractions(purchaseRepository);
				verify(sessionFactory, times(1)).inTransaction(any());
				assertThat(client.getPurchases()).containsExactly(purchase);
			}
			
			@Test
			@DisplayName("When it has no client")
			void testDeletePurchaseWhenItHasNoClient(){
				Purchase toDelete = new Purchase(SECOND_TEST_DATE, 5.0);
				IllegalArgumentException e = 
						assertThrows(IllegalArgumentException.class, 
							() -> service.deletePurchase(toDelete));
				assertThat(e.getMessage()).isEqualTo("Purchase has no Client");
				verifyNoInteractions(clientRepository);
				verifyNoInteractions(purchaseRepository);
				verifyNoInteractions(sessionFactory);
			}
			
		}
	
		@Nested
		@DisplayName("Find Purchase by id")
		class FindPurchaseById{
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
			@DisplayName("When purchase id is not yet set")
			void testFindPurchaseByIdWhenNotSet(){
				// In this case id will be 0.
				Optional<Purchase> foundPurchase = service.findPurchaseById(
						new Purchase(FIRST_TEST_DATE, 10.0).getId());
				assertThat(foundPurchase).isEmpty();
			}
		}
		
		@Nested
		@DisplayName("Find all purchases of a Client")
		class FindAllPurchases{
			@Test
			@DisplayName("Should return a list of its purchases")
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
			@DisplayName("With no purchases should return an empty list")
			void findAllPurchaseOfClientwithNoPurchases() {
				Client client = new Client(1,"testClient");
				
				List<Purchase> purchases = service.findallPurchases(client);
				assertThat(purchases).isEmpty();
			}
		}

	}
	
}
