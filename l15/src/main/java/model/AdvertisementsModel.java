package model;


import lombok.Data;

import java.util.Date;

@Data
public class AdvertisementsModel {
    private final String title;
    private final String text;
    private final String username;
    private final Date date;

    public AdvertisementsModel(String title, String text, String username) {
        this.title = title;
        this.text = text;
        this.username = username;
        this.date = new Date();
    }
}
