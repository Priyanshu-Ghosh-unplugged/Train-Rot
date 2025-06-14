package com.ghosh.trainrot.data.model;

import java.util.Date;
import java.util.Objects;

public class Journey {
    private String id;
    private String fromStation;
    private String toStation;
    private Date date;
    private String trainNumber;
    private String trainName;
    private String status;

    public Journey(String id, String fromStation, String toStation, Date date, String trainNumber, String trainName, String status) {
        this.id = id;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.date = date;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getFromStation() {
        return fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public Date getDate() {
        return date;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Journey journey = (Journey) o;
        return Objects.equals(id, journey.id) &&
                Objects.equals(fromStation, journey.fromStation) &&
                Objects.equals(toStation, journey.toStation) &&
                Objects.equals(date, journey.date) &&
                Objects.equals(trainNumber, journey.trainNumber) &&
                Objects.equals(trainName, journey.trainName) &&
                Objects.equals(status, journey.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromStation, toStation, date, trainNumber, trainName, status);
    }
} 