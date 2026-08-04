package com.backend.bilanko.models.person;

import jakarta.persistence.*;

@Entity
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    private String subname;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;

    public Merchant() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubname() {
        return subname;
    }

    public void setSubname(String subname) {
        this.subname = subname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Merchant(long id, String name, String subname, String email, String password) {
        this.id = id;
        this.name = name;
        this.subname = subname;
        this.email = email;
        this.password = password;
    }

}
