package com.example.eventapp.admin;

public class OrganizerUser {
    public String uid;
    public String email;
    public int eventCount;

    public OrganizerUser(String uid, String email, int eventCount) {
        this.uid = uid;
        this.email = email;
        this.eventCount = eventCount;
    }
}

