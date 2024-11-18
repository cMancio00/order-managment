package managment.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import managment.model.Client;
import managment.service.PurchaseManagmentService;
import managment.view.ManagmentView;

@DisplayName("Controller Tests")
@ExtendWith(MockitoExtension.class)
class ManagmentControllerTest {
	
	@Mock
	private PurchaseManagmentService service;
	
	@Mock
	private ManagmentView view;
	
	@InjectMocks
	private Managmentcontroller controller;
	
	@Test
	@DisplayName("Find all Clients when a client is present should return it as list")
	void testFindAllClients() {
		List<Client> clients = asList(new Client(1,"aClient"));
		when(service.findAllClients()).thenReturn(clients);
		controller.findAllClients();
		verify(view).showAllClients(clients);
	}
	
	@Test
	@DisplayName("Add client")
	void testAddClient(){
		Client toAdd = new Client(1, "toAdd");
		controller.add(toAdd);
		InOrder inOrder = inOrder(service,view);
		inOrder.verify(service).addClient(toAdd);
		inOrder.verify(view).clientAdded(toAdd);
	}
	
	@Test
	@DisplayName("Remove client when existing")
	void testRemoveClientWhenExisting(){
		Client toDelete = new Client(1, "toDelete");
		when(service.findClientById(1)).thenReturn(Optional.of(toDelete));
		controller.remove(toDelete);
		InOrder inOrder = inOrder(service,view);
		inOrder.verify(service).findClientById(1);
		inOrder.verify(view).clientRemoved(toDelete);
	}
	
	@Test
	@DisplayName("Remove client when not existing")
	void testRemoveClientWhenNotExisting(){
		Client toDelete = new Client(1, "toDelete");
		when(service.findClientById(1)).thenReturn(Optional.empty());
		controller.remove(toDelete);
		InOrder inOrder = inOrder(service,view);
		inOrder.verify(service).findClientById(1);
		inOrder.verify(view).showClientRemovedError("Client [id=1, name=toDelete] not found", toDelete);
		verifyNoMoreInteractions(ignoreStubs(service));
	}

	
	

}
