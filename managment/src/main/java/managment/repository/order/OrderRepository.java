package managment.repository.order;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import managment.model.Order;

public interface OrderRepository {

	void save(Order order, Session session);

	Optional<Order> findById(int id, Session session);

	void delete(Order toDelete, Session session);

	List<Order> findAll(Session session);

}