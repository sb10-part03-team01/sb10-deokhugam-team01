package com.team01.deokhugam.comment.service;

import com.team01.deokhugam.comment.dto.CommentCreateRequest;
import com.team01.deokhugam.comment.dto.CommentDto;
import com.team01.deokhugam.comment.dto.CommentUpdateRequest;
import com.team01.deokhugam.global.enums.SortDirection;
import com.team01.deokhugam.global.pagination.CursorPageRequest;
import com.team01.deokhugam.global.pagination.CursorPageResponse;
import java.util.UUID;

public interface CommentService {
  CommentDto createComment(UUID userId, CommentCreateRequest commentCreateRequest);

  CommentDto getComment(UUID commentId);

  CursorPageResponse<CommentDto> getComments(
      UUID reviewId, CursorPageRequest pageRequest, SortDirection direction);

  CommentDto updateComment(UUID userId, UUID commentId, CommentUpdateRequest commentUpdateRequest);

  // Soft Delete
  void deleteComment(UUID userId, UUID commentId);

  // Hard Delete
  void hardDeleteComment(UUID userId, UUID commentId);
}
