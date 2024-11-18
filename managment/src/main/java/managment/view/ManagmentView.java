package managment.view;

import java.util.List;

import managment.model.Client;

public interface ManagmentView {

	void showAllClients(List<Client> clients);

	void clientAdded(Client toAdd);

}
