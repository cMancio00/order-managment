package managment.repository.purchase;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import managment.model.Purchase;

public interface PurchaseRepository {

	Purchase save(Purchase order, Session session);

	Optional<Purchase> findById(int id, Session session);

	void delete(Purchase toDelete, Session session);

	List<Purchase> findAll(Session session);

}
