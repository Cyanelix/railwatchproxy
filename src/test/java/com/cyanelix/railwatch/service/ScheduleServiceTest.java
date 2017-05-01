package com.cyanelix.railwatch.service;

import com.cyanelix.railwatch.domain.NotificationTarget;
import com.cyanelix.railwatch.domain.Schedule;
import com.cyanelix.railwatch.domain.Station;
import com.cyanelix.railwatch.entity.ScheduleEntity;
import com.cyanelix.railwatch.repository.ScheduleRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScheduleServiceTest {
    @TestConfiguration
    public static class TestApplicationConfiguration {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2017-01-01T10:30:00Z"), ZoneId.systemDefault());
        }
    }

    @MockBean
    private TrainTimesService mockTrainTimesService;

    @MockBean
    private NotificationService mockNotificationService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Before
    public void setup() {
        scheduleService.getSchedules().clear();
    }

    @Test
    public void createSchedule_savedInRepo() {
        // Given...
        Schedule schedule = Schedule.of(LocalTime.MIN, LocalTime.MAX, Station.of("FOO"), Station.of("BAR"), NotificationTarget.of("notification-target"));

        // When...
        scheduleService.createSchedule(schedule);

        // Then...
        ArgumentCaptor<ScheduleEntity> scheduleEntityCaptor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository).save(scheduleEntityCaptor.capture());

        ScheduleEntity scheduleEntity = scheduleEntityCaptor.getValue();
        assertThat(scheduleEntity.getStartTime(), is(schedule.getStartTime()));
        assertThat(scheduleEntity.getEndTime(), is(schedule.getEndTime()));
        assertThat(scheduleEntity.getFromStation(), is(schedule.getFromStation().getStationCode()));
        assertThat(scheduleEntity.getToStation(), is(schedule.getToStation().getStationCode()));
        assertThat(scheduleEntity.getNotificationTarget(), is(schedule.getNotificationTarget().getTargetAddress()));
    }

    @Test
    public void singleScheduleActiveNow_checkTimes_routeLookedUp() {
        // Given...
        Schedule activeSchedule = Schedule.of(LocalTime.MIN, LocalTime.MAX, Station.of("FOO"), Station.of("BAR"), NotificationTarget.of("target"));
        given(scheduleRepository.findAll()).willReturn(Collections.singletonList(ScheduleEntity.of(activeSchedule)));

        // When...
        scheduleService.checkTimes();

        // Then...
        verify(mockTrainTimesService).lookupTrainTimes(any(), any());
        verify(mockNotificationService).sendNotification(eq(activeSchedule), any());
    }

    @Test
    public void oneActiveOneInactiveSchedule_checkTimes_onlyActiveRouteLookedUp() {
        // Given...
        Schedule activeSchedule = Schedule.of(LocalTime.MIN, LocalTime.MAX, Station.of("FOO"), Station.of("BAR"), NotificationTarget.of("target"));
        Schedule inactiveSchedule = Schedule.of(LocalTime.MAX, LocalTime.MIN, Station.of("XXX"), Station.of("ZZZ"), NotificationTarget.of("target"));
        given(scheduleRepository.findAll()).willReturn(Arrays.asList(ScheduleEntity.of(activeSchedule), ScheduleEntity.of(inactiveSchedule)));

        // When...
        scheduleService.checkTimes();

        // Then...
        verify(mockTrainTimesService).lookupTrainTimes(Station.of("FOO"), Station.of("BAR"));
        verify(mockTrainTimesService, never()).lookupTrainTimes(Station.of("XXX"), Station.of("ZZZ"));

        verify(mockNotificationService).sendNotification(eq(activeSchedule), any());
        verify(mockNotificationService, never()).sendNotification(eq(inactiveSchedule), any());
    }

    @Test
    public void singleSchedule_getSchedules() {
        // Given...
        Schedule schedule = Schedule.of(LocalTime.MIN, LocalTime.MAX, Station.of("FOO"), Station.of("BAR"), NotificationTarget.of("notification-to"));
        given(scheduleRepository.findAll()).willReturn(Collections.singletonList(ScheduleEntity.of(schedule)));

        // When...
        Set<Schedule> schedules = scheduleService.getSchedules();

        // Then...
        assertThat(schedules, hasSize(1));
        assertThat(schedules.contains(schedule), is(true));
    }
}
