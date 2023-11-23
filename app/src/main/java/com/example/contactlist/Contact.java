package com.example.contactlist;

public class Contact {
    String key = "";
    String name = "";
    String email = "";
    String phoneHome = "";
    String phoneOffice = "";
    String imageString = "";

    public Contact(String key, String name, String email, String phoneHome, String phoneOffice, String imageString){
        this.key = key;
        this.name = name;
        this.email = email;
        this.phoneHome = phoneHome;
        this.phoneOffice = phoneOffice;
        this.imageString = imageString;

    }
}