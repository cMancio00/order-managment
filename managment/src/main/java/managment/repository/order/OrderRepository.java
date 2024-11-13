package managment.repository.order;

import org.hibernate.Session;

import managment.model.Order;

public interface OrderRepository {

	void save(Order order, Session session);

}
