package com.example.eventapp;

public class Attendee {
    private String name;
    private String email;
    private String status;

    public Attendee(String name, String email, String status) {
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}
