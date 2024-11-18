package managment.view;

import java.util.List;

import managment.model.Client;
import managment.model.Purchase;

public interface ManagmentView {

	void showAllClients(List<Client> clients);

	void clientAdded(Client toAdd);

	void clientRemoved(Client toDelete);

	void showAllPurchases(List<Purchase> purchases);

	void showClientNotFoundError(String string, Client client);

	void purchaseAdded(Purchase toAdd);

}
