package com.ghosh.trainrot.data.model;

import java.util.Date;
import java.util.Objects;

public class Booking {
    private String id;
    private String pnr;
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private String route;
    private int passengerCount;
    private Date date;
    private BookingStatus status;
    private String classType;

    public Booking() {
        // Required empty constructor for Firebase
    }

    public Booking(String id, String pnr, String trainNumber, String trainName, String fromStation, 
                  String toStation, String route, int passengerCount, Date date, BookingStatus status, String classType) {
        this.id = id;
        this.pnr = pnr;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.route = route;
        this.passengerCount = passengerCount;
        this.date = date;
        this.status = status;
        this.classType = classType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum BookingStatus {
        CONFIRMED,
        WAITLIST,
        CANCELLED,
        UNKNOWN
    }
} 