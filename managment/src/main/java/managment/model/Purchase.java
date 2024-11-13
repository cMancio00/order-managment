package managment.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Purchase")
public class Purchase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "orderDate", nullable = false)
	private LocalDateTime orderDate;
	@Column(name = "amount", nullable = false)
	private double amount;
	@ManyToOne(fetch = FetchType.LAZY)
	Client client;

	public Purchase() {}
	
	public Purchase(LocalDateTime orderDate, double amount) {
		this.orderDate = orderDate;
		this.amount = amount;
	}
	
	public Purchase(int id, LocalDateTime orderDate, double amount) {
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

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, id, orderDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Purchase))
			return false;
		Purchase other = (Purchase) obj;
		return Double.doubleToLongBits(amount) == Double.doubleToLongBits(other.amount) && id == other.id
				&& Objects.equals(orderDate, other.orderDate);
	}

	@Override
	public String toString() {
		return "Purchase [id=" + id + ", orderDate=" + orderDate + ", amount=" + amount + "]";
	}
	
	

}
