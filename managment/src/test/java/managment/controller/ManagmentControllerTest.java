package managment.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import managment.model.Client;
import managment.model.Purchase;
import managment.service.PurchaseManagmentService;
import managment.view.ManagmentView;

@DisplayName("Controller Tests")
@ExtendWith(MockitoExtension.class)
class ManagmentControllerTest {
	
	private static final LocalDateTime TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();
	
	@Mock
	private PurchaseManagmentService service;
	
	@Mock
	private ManagmentView view;
	
	@InjectMocks
	private ManagmentController controller;
		
	@Nested
	@DisplayName("Add Client")
	class AddClient{
		@Test
		@DisplayName("When non existing")
		void testAddClientWhenNonExisting(){
			Client toAdd = new Client("toAdd");
			when(service.addClient(toAdd)).thenReturn(new Client(1, "toAdd"));
			controller.add(toAdd);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).addClient(new Client("toAdd"));
			inOrder.verify(view).clientAdded(new Client(1, "toAdd"));
		}
	}
	
	@Nested
	@DisplayName("Remove Client")
	class RemoveClient{
		@Test
		@DisplayName("When existing")
		void testRemoveClientWhenExisting(){
			Client toDelete = new Client(1, "toDelete");
			when(service.findClientById(1)).thenReturn(Optional.of(toDelete));
			controller.remove(toDelete);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(service).deleteClient(toDelete);
			inOrder.verify(view).clientRemoved(toDelete);
		}
		
		@Test
		@DisplayName("When not existing")
		void testRemoveClientWhenNotExisting(){
			Client toDelete = new Client(1, "toDelete");
			when(service.findClientById(1)).thenReturn(Optional.empty());
			controller.remove(toDelete);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(view).showClientNotFoundError("Client [id=1, name=toDelete] not found", toDelete);
			verifyNoMoreInteractions(ignoreStubs(service));
		}
	}
	
	@Nested
	@DisplayName("Find All Clients")
	class FindAllClients{
		@Test
		@DisplayName("When clients are present should be passed as list to the view")
		void testFindAllClients() {
			List<Client> clients = asList(
					new Client(1,"aClient"),
					new Client(2,"anOtherClient"));
			when(service.findAllClients()).thenReturn(clients);
			controller.findAllClients();
			verify(view).showAllClients(clients);
		}
		
	}
	
	@Nested
	@DisplayName("Add Purchase to a Client")
	class AddPurchaseToAClient{
		@Test
		@DisplayName("When client exists")
		void testAddPurchaseToSelectedClientWhenExists(){
			Client selectedClient = new Client(1, "selectedClient");
			Purchase toAdd = new Purchase(TEST_DATE, 5.0);
			when(service.findClientById(1)).thenReturn(Optional.of(selectedClient));
			when(service.addPurchaseToClient(selectedClient, toAdd))
				.thenReturn(new Purchase(1, TEST_DATE, 5.0));
			controller.addPurchaseToSelectedClient(selectedClient, toAdd);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(service).addPurchaseToClient(selectedClient, toAdd);
			inOrder.verify(view).purchaseAdded(new Purchase(1, TEST_DATE, 5.0));
		}
		
		@Test
		@DisplayName("When client do not exists")
		void testAddPurchaseToSelectedClientWhenClientDoNotExists(){
			Client selectedClient = new Client(1, "selectedClient");
			Purchase toAdd = new Purchase(1, TEST_DATE, 5.0);
			when(service.findClientById(1)).thenReturn(Optional.empty());

			controller.addPurchaseToSelectedClient(selectedClient, toAdd);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(view).showClientNotFoundError("Client [id=1, name=selectedClient] not found", selectedClient);
			verifyNoMoreInteractions(service);
		}
	}
	
	@Nested
	@DisplayName("Remove Purchase")
	class RemovePurchase{
		@Test
		@DisplayName("When exists")
		void testRemovePurchaseWhenExists(){
			Purchase toDelete = new Purchase(1, TEST_DATE, 5.0);
			when(service.findPurchaseById(1)).thenReturn(Optional.of(toDelete));
			
			controller.remove(toDelete);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findPurchaseById(1);
			inOrder.verify(service).deletePurchase(toDelete);
			inOrder.verify(view).purchaseRemoved(toDelete);
		}
		
		@Test
		@DisplayName("When purchase do not exists")
		void testRemovePurchaseWhenPurchaseDoNotExists(){
			Purchase toDelete = new Purchase(1, TEST_DATE, 5.0);
			when(service.findPurchaseById(1)).thenReturn(Optional.empty());
			
			controller.remove(toDelete);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findPurchaseById(1);
			inOrder.verify(view).showPurchaseNotFoundError("Purchase [id=1, orderDate=2024-01-01T00:00, amount=5.0] not found", toDelete);
			verifyNoMoreInteractions(service);
		}
	}
	
	@Nested
	@DisplayName("Find all Purchases of a Client")
	class FindAllPurchasesOfAClient{
		@Test
		@DisplayName("When client exists")
		void findAllPurchaseOfSelectedExistingClient(){
			Client selectedClient = new Client(1, "selectedClient");
			List<Purchase> purchases = asList(
					new Purchase(1, TEST_DATE, 10.0),
					new Purchase(2, TEST_DATE, 5.0));
			when(service.findClientById(1)).thenReturn(Optional.of(selectedClient));
			when(service.findAllPurchases(selectedClient)).thenReturn(purchases);
			controller.findAllPurchasesOf(selectedClient);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(service).findAllPurchases(selectedClient);
			inOrder.verify(view).showAllPurchases(purchases);
		}
		
		@Test
		@DisplayName("When client has no purchases")
		void findAllPurchaseOfSelectedClientWhenNoPurchaseShouldReturnEmptyList(){
			Client selectedClient = new Client(1, "selectedClient");

			when(service.findClientById(1)).thenReturn(Optional.of(selectedClient));
			when(service.findAllPurchases(selectedClient)).thenReturn(Collections.emptyList());
			controller.findAllPurchasesOf(selectedClient);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(service).findAllPurchases(selectedClient);
			inOrder.verify(view).showAllPurchases(Collections.emptyList());
		}
		
		@Test
		@DisplayName("When selected client do not exists")
		void findAllPurchaseOfNonExistingClient(){
			Client selectedClient = new Client(1, "selectedClient");
			when(service.findClientById(1)).thenReturn(Optional.empty());
			controller.findAllPurchasesOf(selectedClient);
			InOrder inOrder = inOrder(service,view);
			inOrder.verify(service).findClientById(1);
			inOrder.verify(view).showClientNotFoundError("Client [id=1, name=selectedClient] not found", selectedClient);
			verifyNoMoreInteractions(ignoreStubs(service));
		}
	}
	
}
