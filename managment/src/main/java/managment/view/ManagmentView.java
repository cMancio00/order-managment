package managment.view;

import java.util.List;

import managment.model.Client;

public interface ManagmentView {

	void showAllClients(List<Client> clients);

	void clientAdded(Client toAdd);

	void clientRemoved(Client toDelete);

	void showClientRemovedError(String string, Client toDelete);

}
