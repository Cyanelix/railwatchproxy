package com.cyanelix.railwatch.controller;

import com.cyanelix.railwatch.converter.ScheduleToDTOConverter;
import com.cyanelix.railwatch.converter.UserEntityToDTOConverter;
import com.cyanelix.railwatch.domain.*;
import com.cyanelix.railwatch.entity.Schedule;
import com.cyanelix.railwatch.entity.User;
import com.cyanelix.railwatch.service.ScheduleService;
import com.cyanelix.railwatch.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@RunWith(SpringRunner.class)
public class UsersControllerTest {
    @MockBean
    private UserService userService;

    @MockBean
    private ScheduleService scheduleService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenericConversionService conversionService;

    @Before
    public void setup() {
        conversionService.addConverter(new UserEntityToDTOConverter());
        conversionService.addConverter(new ScheduleToDTOConverter());
    }

    @Test
    public void createWithNoBody_returnsBadRequest() throws Exception {
        mockMvc.perform(
                post("/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithEmptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithEmptyNotificationTarget_returnsBadRequest() throws Exception {
        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notificationTarget\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createUser_returnsCreatedWithLocation() throws Exception {
        NotificationTarget notificationTarget = NotificationTarget.of("notification-target");
        UserId userId = UserId.generate();

        given(userService.createUser(notificationTarget)).willReturn(new User(userId, notificationTarget.getTargetAddress(), UserState.ENABLED));

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notificationTarget\":\"" + notificationTarget.getTargetAddress() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/users/" + userId.get()));
    }

    @Test
    public void getFullDetailsForUserWithNoSchedules_success() throws Exception {
        UserId userId = UserId.generate();

        given(userService.getUser(userId))
                .willReturn(new User(userId, "notification-target", UserState.ENABLED));

        mockMvc.perform(
                get("/users/" + userId.get()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"userId\":\"" + userId.get() + "\", \"notificationTarget\":\"notification-target\", \"schedules\":[]}"));
    }

    @Test
    public void getFullDetailsForUserWithSchedules_success() throws Exception {
        UserId userId = UserId.generate();

        User user = new User(userId, "notification-target", UserState.ENABLED);

        given(userService.getUser(userId)).willReturn(user);
        given(scheduleService.getSchedulesForUser(user))
                .willReturn(Collections.singletonList(new Schedule(LocalTime.MIN, LocalTime.MAX, DayRange.of(DayOfWeek.MONDAY), Station.of("FOO"), Station.of("BAR"), ScheduleState.ENABLED, user)));

        mockMvc.perform(
                get("/users/" + userId.get()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"userId\":\"" + userId.get() + "\", \"notificationTarget\":\"notification-target\", \"schedules\":[{\"startTime\":\"00:00\", \"endTime\":\"23:59\", \"days\":[\"Monday\"], \"fromStation\":\"FOO\", \"toStation\": \"BAR\", \"userId\":" + userId.get() + "}]}"));
    }

    @Test
    public void noMatchingUser_getFullDetails_returns404() throws Exception {
        UserId userId = UserId.generate();

        mockMvc.perform(
                get("/users/" + userId.get()))
                .andExpect(status().isNotFound());
    }
}
