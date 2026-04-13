import {useState, useRef} from "react";
import Textarea from "@/components/ui/Textarea.tsx";
import Button from "@/components/common/Buttons/Button.tsx";
import {createComment} from "@/api/comments.ts";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import type {Comment} from "@/types/reviews.ts";
import getImagePath from "@/constants/images.ts";

interface CommentFormProps {
  reviewId: string;
  onCommentSubmit: (newComment: Comment) => void;
  onCommentCountChange?: (count: number) => void;
  reviewCommentCount?: number;
}

export default function CommentForm({
                                      reviewId,
                                      onCommentSubmit,
                                      onCommentCountChange,
                                      reviewCommentCount
                                    }: CommentFormProps) {
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [hasContent, setHasContent] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const showTooltip = useTooltipStore(state => state.showTooltip);

  const MAX_COMMENT_LENGTH = 500;

  const handleSubmitComment = async () => {
    const content = textareaRef.current?.value?.trim();
    if (
        !content ||
        !reviewId ||
        isSubmittingComment ||
        content.length > MAX_COMMENT_LENGTH
    )
      return;

    try {
      setIsSubmittingComment(true);

      const newComment = await createComment({
        reviewId,
        content
      });

      onCommentSubmit(newComment);

      if (reviewCommentCount !== undefined) {
        const newCommentCount = reviewCommentCount + 1;
        onCommentCountChange?.(newCommentCount);
      }

      if (textareaRef.current) {
        textareaRef.current.value = "";
      }
      setHasContent(false);

      showTooltip("댓글이 등록되었습니다.");
    } catch (error: unknown) {
      console.error("댓글 작성 실패:", error);

      const isAxiosError =
          error && typeof error === "object" && "response" in error;
      const status = isAxiosError
          ? (error as { response?: { status?: number } }).response?.status
          : null;

      if (status === 400) {
        showTooltip(
            "입력값이 올바르지 않습니다. 다시 확인해주세요.",
            getImagePath("/icon/ic_exclamation-circle.svg")
        );
      } else if (status === 500) {
        showTooltip(
            "서버 응답이 없습니다. 네트워크 상태를 확인해주세요.",
            getImagePath("/icon/ic_exclamation-circle.svg")
        );
      } else {
        showTooltip(
            "댓글 작성에 실패했습니다. 다시 시도해주세요.",
            getImagePath("/icon/ic_exclamation-circle.svg")
        );
      }
    } finally {
      setIsSubmittingComment(false);
    }
  };

  return (
      <div>
        <Textarea
            ref={textareaRef}
            placeholder="댓글을 입력해주세요..."
            className="h-[120px]"
            maxLength={MAX_COMMENT_LENGTH}
            onChange={value => {
              setHasContent(value.trim().length > 0);
            }}
        />
        <div className="flex justify-end mt-[15px]">
          <Button
              variant="primary"
              onClick={handleSubmitComment}
              disabled={!hasContent || isSubmittingComment}
          >
            {isSubmittingComment ? (
                <div
                    className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-50 mx-auto"/>
            ) : (
                "등록"
            )}
          </Button>
        </div>
      </div>
  );
}
