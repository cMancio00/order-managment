package managment.repository.order;

import org.hibernate.Session;

import managment.model.Order;

public class OrderRepositoryHibernate implements OrderRepository{

	@Override
	public void save(Order order, Session session) {
		session.merge(order);
	}

}
