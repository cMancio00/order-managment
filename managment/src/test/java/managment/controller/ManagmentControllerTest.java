package managment.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	

	
	

}
