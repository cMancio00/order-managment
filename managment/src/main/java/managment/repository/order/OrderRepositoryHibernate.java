package managment.repository.order;

import java.util.Optional;

import org.hibernate.Session;

import managment.model.Order;

public class OrderRepositoryHibernate implements OrderRepository{

	@Override
	public void save(Order order, Session session) {
		session.merge(order);
	}

	@Override
	public Optional<Order> findById(int id, Session session) {
		return Optional.ofNullable(session.find(Order.class, id));
	}

	@Override
	public void delete(Order toDelete, Session session) {
		session.remove(toDelete);
	}

}
