package model;

import androidx.room.Entity;

import java.util.UUID;

@Entity
public class Reminder {
    private UUID reminderId;
    private String name;

    private Double longitude;

    private Double lattitide;

    private Float radius;

    private String notification_message;

}
