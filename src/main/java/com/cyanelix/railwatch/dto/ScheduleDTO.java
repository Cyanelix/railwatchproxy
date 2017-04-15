package com.cyanelix.railwatch.dto;

import com.cyanelix.railwatch.domain.Schedule;
import com.cyanelix.railwatch.domain.Station;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ScheduleDTO {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String startTime;
    private String endTime;
    private String fromStation;
    private String toStation;
    private String notificationTarget;

    public ScheduleDTO(Schedule schedule) {
        startTime = schedule.getStartTime().format(TIME_FORMATTER);
        endTime = schedule.getEndTime().format(TIME_FORMATTER);
        fromStation = schedule.getFromStation().getStationCode();
        toStation = schedule.getToStation().getStationCode();
        notificationTarget = schedule.getNotificationTarget();
    }

    public ScheduleDTO() {
        // Default constructor required for Jackson.
    }

    public Schedule toSchedule() {
        Schedule schedule = new Schedule();
        schedule.setStartTime(LocalTime.parse(startTime));
        schedule.setEndTime(LocalTime.parse(endTime));
        schedule.setFromStation(Station.of(fromStation));
        schedule.setToStation(Station.of(toStation));
        schedule.setNotificationTarget(notificationTarget);
        return schedule;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getNotificationTarget() {
        return notificationTarget;
    }

    public void setNotificationTarget(String notificationTarget) {
        this.notificationTarget = notificationTarget;
    }
}