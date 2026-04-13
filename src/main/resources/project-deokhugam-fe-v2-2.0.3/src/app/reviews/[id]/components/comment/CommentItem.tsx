import {useState, useRef, useCallback} from "react";
import Label from "@/components/common/Buttons/Label.tsx";
import Button from "@/components/common/Buttons/Button.tsx";
import Textarea from "@/components/ui/Textarea.tsx";
import ActionMenu from "@/components/common/ActionMenu.tsx";
import {useAuthStore} from "@/store/authStore.ts";
import {useClickOutside} from "@/hooks/common/useClickOutside.ts";
import {updateComment} from "@/api/comments.ts";
import type {Comment} from "@/types/reviews.ts";
import CommentDeleteModal from "./CommentDeleteModal.tsx";
import getImagePath from "@/constants/images.ts";

interface CommentItemProps {
  comment: Comment;
  data: Comment[];
  setData: React.Dispatch<React.SetStateAction<Comment[]>>;
  reviewId: string;
  onCommentUpdate?: (updatedComment: Comment) => void;
  onCommentCountChange?: (count: number) => void;
}

export default function CommentItem({
                                      comment,
                                      data,
                                      setData,
                                      reviewId,
                                      onCommentUpdate,
                                      onCommentCountChange
                                    }: CommentItemProps) {
  const {user} = useAuthStore();
  const isMyComment = user?.id === comment.userId;
  const [isEditMode, setIsEditMode] = useState(false);
  const [isSubmittingEdit, setIsSubmittingEdit] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const editTextareaRef = useRef<HTMLTextAreaElement>(null);

  const MAX_COMMENT_LENGTH = 500;

  const {
    open: isActionMenuOpen,
    setOpen: setIsActionMenuOpen,
    dropdownRef: actionMenuRef
  } = useClickOutside();

  const handleMoreClick = useCallback(() => {
    if (isMyComment) {
      setIsActionMenuOpen(!isActionMenuOpen);
    }
  }, [isMyComment, isActionMenuOpen, setIsActionMenuOpen]);

  const handleEdit = useCallback(() => {
    setIsEditMode(true);
    setIsActionMenuOpen(false);
    setTimeout(() => {
      if (editTextareaRef.current) {
        editTextareaRef.current.value = comment.content;
        editTextareaRef.current.focus();
      }
    }, 0);
  }, [comment.content, setIsActionMenuOpen]);

  const handleCancel = useCallback(() => {
    setIsEditMode(false);
  }, []);

  const handleSave = useCallback(async () => {
    const content = editTextareaRef.current?.value?.trim();
    if (!content || content.length > MAX_COMMENT_LENGTH) return;

    setIsSubmittingEdit(true);
    try {
      const updatedComment = await updateComment({
        commentId: comment.id,
        content
      });
      setIsEditMode(false);
      onCommentUpdate?.(updatedComment);
    } catch (error) {
      console.error("댓글 수정 실패:", error);
    } finally {
      setIsSubmittingEdit(false);
    }
  }, [comment.id, onCommentUpdate]);

  const handleDelete = useCallback(() => {
    setIsActionMenuOpen(false);
    setIsDeleteModalOpen(true);
  }, [setIsActionMenuOpen]);

  return (
      <div
          className={`${isEditMode ? "pt-[24px] pb-0 border-b-0" : "py-[24px] border-b border-gray-100"}`}
      >
        <div className="flex flex-col gap-[10px]">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-[6px]">
              <div className="flex items-center gap-[4px]">
              <span className="my-[3.5px] text-body3 font-semibold text-gray-600">
                {comment.userNickname}
              </span>
                {isMyComment && <Label>내 댓글</Label>}
              </div>
              <span className="my-[3.5px] text-body3 font-medium text-gray-400">
              {new Date(comment.createdAt).toLocaleDateString()}
            </span>
            </div>
            {isMyComment && !isEditMode && (
                <div className="relative" ref={actionMenuRef}>
                  <img
                      src={getImagePath("/icon/ic_more.svg")}
                      alt="더보기"
                      width={24}
                      height={24}
                      className="cursor-pointer relative z-0"
                      onClick={handleMoreClick}
                  />
                  {isActionMenuOpen && (
                      <ActionMenu onEdit={handleEdit} onDelete={handleDelete}/>
                  )}
                </div>
            )}
          </div>

          {isEditMode ? (
              <div className="flex flex-col gap-[10px]">
                <Textarea
                    ref={editTextareaRef}
                    placeholder="댓글을 수정해주세요"
                    className="h-[120px]"
                    maxLength={MAX_COMMENT_LENGTH}
                />
                <div className="flex justify-end gap-[12px]">
                  <Button variant="secondary" onClick={handleCancel}>
                    취소
                  </Button>
                  <Button
                      variant="primary"
                      onClick={handleSave}
                      disabled={isSubmittingEdit}
                  >
                    {isSubmittingEdit ? (
                        <div
                            className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-50 mx-auto"/>
                    ) : (
                        "등록"
                    )}
                  </Button>
                </div>
              </div>
          ) : (
              <div
                  className="text-body2 font-medium text-gray-800 break-words overflow-wrap-anywhere">
                {comment.content}
                {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
                    <span className="ml-2 my-[3.5px] text-body3 font-medium text-gray-400">
                (수정됨)
              </span>
                )}
              </div>
          )}
        </div>

        <CommentDeleteModal
            isOpen={isDeleteModalOpen}
            close={() => setIsDeleteModalOpen(false)}
            comment={comment}
            data={data}
            setData={setData}
            reviewId={reviewId}
            onCommentCountChange={onCommentCountChange}
        />
      </div>
  );
}
