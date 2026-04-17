package com.team01.deokhugam.notification.dto;

import com.team01.deokhugam.review.entity.Review;
import com.team01.deokhugam.user.entity.User;

public record NotificationCreateRequest(
    Review review,
    User user,
    String content
) {

}
