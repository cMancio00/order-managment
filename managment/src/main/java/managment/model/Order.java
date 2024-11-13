package managment.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
// Order is a keyword in SQL, so we need to use escape characters
@Table(name = "\"Order\"")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "orderDate", nullable = false)
	private LocalDateTime orderDate;
	@Column(name = "amount", nullable = false)
	private double amount;

	public Order() {}
	
	public Order(LocalDateTime orderDate, double amount) {
		this.orderDate = orderDate;
		this.amount = amount;
	}
	
	public Order(int id, LocalDateTime orderDate, double amount) {
		this.id = id;
		this.orderDate = orderDate;
		this.amount = amount;
	}

	public int getId() {
		return id;
	}
	
	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, orderDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Order))
			return false;
		Order other = (Order) obj;
		return id == other.id && Objects.equals(orderDate, other.orderDate);
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", orderDate=" + orderDate + ", amount=" + amount + "]";
	}
	
	

}
