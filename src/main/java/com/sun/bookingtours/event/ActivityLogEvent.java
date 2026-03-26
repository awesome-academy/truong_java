package com.sun.bookingtours.event;

import com.sun.bookingtours.entity.Booking;
import com.sun.bookingtours.entity.User;
import com.sun.bookingtours.entity.enums.ActivityType;

public record ActivityLogEvent(User user, Booking booking, ActivityType type) {}
