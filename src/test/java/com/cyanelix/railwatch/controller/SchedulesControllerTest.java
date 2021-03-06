package com.cyanelix.railwatch.controller;

import com.cyanelix.railwatch.converter.ScheduleDTOToScheduleConverter;
import com.cyanelix.railwatch.converter.ScheduleToDTOConverter;
import com.cyanelix.railwatch.domain.*;
import com.cyanelix.railwatch.entity.Schedule;
import com.cyanelix.railwatch.entity.User;
import com.cyanelix.railwatch.service.ScheduleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchedulesController.class)
@RunWith(SpringRunner.class)
public class SchedulesControllerTest {
    private static final UserId USER_ID = UserId.of("123e4567-e89b-12d3-a456-426655440000");

    @MockBean
    private ScheduleService mockScheduleService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenericConversionService conversionService;

    @Before
    public void setup() {
        conversionService.addConverter(new ScheduleDTOToScheduleConverter());
        conversionService.addConverter(new ScheduleToDTOConverter());
    }

    @Test
    public void putNewTimesRequest_success() throws Exception {
        mockMvc.perform(
                put("/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startTime\":\"07:00\", \"endTime\":\"09:00\", \"days\":[\"MONDAY\", \"TUESDAY\"], \"state\":\"ENABLED\", \"userId\":\"" + USER_ID.get() + "\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<Schedule> scheduleArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(mockScheduleService).createSchedule(scheduleArgumentCaptor.capture(), eq(USER_ID));

        Schedule schedule = scheduleArgumentCaptor.getValue();
        assertThat(schedule.getStartTime().toString(), is("07:00"));
        assertThat(schedule.getEndTime().toString(), is("09:00"));
    }

    @Test
    public void putNewTimesRequestWithNoState_scheduleCreatedAsEnabled() throws Exception {
        mockMvc.perform(
                put("/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startTime\":\"07:00\", \"endTime\":\"09:00\", \"days\":[\"MONDAY\", \"TUESDAY\"], \"userId\":\"" + USER_ID.get() + "\"}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<Schedule> scheduleArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(mockScheduleService).createSchedule(scheduleArgumentCaptor.capture(), eq(USER_ID));

        Schedule schedule = scheduleArgumentCaptor.getValue();
        assertThat(schedule.getState(), is(ScheduleState.ENABLED));
    }

    @Test
    public void getAllSchedules_success() throws Exception {
        UserId userId = UserId.generate();

        given(mockScheduleService.getSchedules())
                .willReturn(Collections.singleton(new Schedule(
                        LocalTime.of(7, 0),
                        LocalTime.of(8, 0),
                        DayRange.ALL,
                        Station.of("FOO"), Station.of("BAR"),
                        ScheduleState.ENABLED,
                        new User(userId, "foo", UserState.ENABLED))));

        mockMvc.perform(get("/schedules"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"startTime\":\"07:00\", \"endTime\":\"08:00\", \"fromStation\":\"FOO\", \"toStation\": \"BAR\", \"userId\":" + userId.get() + "}]"));
    }
}
