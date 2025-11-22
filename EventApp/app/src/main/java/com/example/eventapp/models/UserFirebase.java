package com.example.eventapp.models;

import com.google.firebase.Timestamp;

public class UserFirebase {
    private String id;
    private String email;
    private String name;
    private Timestamp createdAt;

    // Required empty constructor for Firestore
    public UserFirebase() {}

    // Optional full constructor for testing or direct creation
    public UserFirebase(String id, String email, String name, Timestamp createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
