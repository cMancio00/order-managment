package managment.repository.purchase;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import managment.model.Purchase;

public class PurchaseRepositoryHibernate implements PurchaseRepository{

	@Override
	public Purchase save(Purchase order, Session session) {
		return session.merge(order);
	}

	@Override
	public Optional<Purchase> findById(int id, Session session) {
		return Optional.ofNullable(session.find(Purchase.class, id));
	}

	@Override
	public void delete(Purchase toDelete, Session session) {
		session.remove(session.find(Purchase.class, toDelete.getId()));
	}

	@Override
	public List<Purchase> findAll(Session session) {
		return session.createSelectionQuery("from Purchase", Purchase.class).getResultList();
	}

}
