package com.project.third.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="email", unique = true)
    private String email;

    @Size(min=6)
    @Column(name="password")
    private String password;

    @Column(name="name")
    private String name;

    @Column(name="surname")
    private String surname;

    @Column(name = "isactive")
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Roles> roles;

    public String getFullName() {
        return this.surname + " " + this.name;
    }
}
