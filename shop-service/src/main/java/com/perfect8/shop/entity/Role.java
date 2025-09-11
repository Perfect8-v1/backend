package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(name = "role_description", length = 255)
    private String roleDescription;

    @ManyToMany(mappedBy = "roles")
    private Set<Customer> customers = new HashSet<>();

    // Helper method för att lägga till kund
    public void addCustomer(Customer customer) {
        this.customers.add(customer);
        customer.getRoles().add(this);
    }

    // Helper method för att ta bort kund
    public void removeCustomer(Customer customer) {
        this.customers.remove(customer);
        customer.getRoles().remove(this);
    }
}