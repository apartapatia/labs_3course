package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class User {

    private String name;
    private List<String> phoneNumbers;

    public User(String name) {
        this.name = name;
        this.phoneNumbers = new ArrayList<>();
    }

    public void addPhoneNumber(String phoneNumber) {
        phoneNumbers.add(phoneNumber);
    }
}
