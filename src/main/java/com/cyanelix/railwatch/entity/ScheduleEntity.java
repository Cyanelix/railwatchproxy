package com.cyanelix.railwatch.entity;

import com.cyanelix.railwatch.domain.DayRange;
import com.cyanelix.railwatch.domain.Journey;
import com.cyanelix.railwatch.domain.ScheduleState;
import com.cyanelix.railwatch.domain.Station;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Document
public class ScheduleEntity {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Id
    private String id;

    private LocalTime startTime;
    private LocalTime endTime;
    private DayRange dayRange;
    private Station fromStation;
    private Station toStation;
    private ScheduleState state;

    @DBRef
    private UserEntity user;

    public ScheduleEntity(LocalTime startTime, LocalTime endTime, DayRange dayRange, Station fromStation, Station toStation, ScheduleState state, UserEntity user) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayRange = dayRange;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.state = state;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public DayRange getDayRange() {
        return dayRange;
    }

    public ScheduleState getState() {
        return state;
    }

    public void setState(ScheduleState state) {
        this.state = state;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getNotificationTarget() {
        return user.getNotificationTarget();
    }

    public boolean isActive(LocalDateTime testDateTime) {
        DayOfWeek dayOfWeek = testDateTime.getDayOfWeek();
        LocalTime testTime = testDateTime.toLocalTime();

        return dayRange.contains(dayOfWeek) && timeWithinRange(testTime);
    }

    private boolean timeWithinRange(LocalTime testTime) {
        return startTime.isBefore(testTime) && endTime.isAfter(testTime);
    }

    public String toString() {
        return Journey.of(fromStation, toStation).toString() + "; "
                + dayRange.toString() + " @ "
                + startTime.format(TIME_FORMATTER) + " -> " + endTime.format(TIME_FORMATTER);
    }
}
