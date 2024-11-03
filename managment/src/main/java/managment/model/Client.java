package managment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Client")
public class Client {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	@Column(name = "name", nullable = false)
	private String name;
	
	// Empty constructor for hibernate
	private Client() {};
	
	private Client(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	private Client(String name) {
		this.name = name;
	}
}
