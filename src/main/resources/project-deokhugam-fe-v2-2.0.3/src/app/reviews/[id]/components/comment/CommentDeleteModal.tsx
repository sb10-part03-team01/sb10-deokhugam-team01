import Modal from "@/components/ui/Modal.tsx";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import {deleteComment, getComments} from "@/api/comments.ts";
import {getReviewDetail} from "@/api/reviews.ts";
import {useState} from "react";
import type {Comment} from "@/types/reviews.ts";

export default function CommentDeleteModal({
                                             isOpen,
                                             close,
                                             comment,
                                             data,
                                             setData,
                                             reviewId,
                                             onCommentCountChange
                                           }: {
  isOpen: boolean;
  close: () => void;
  comment: Comment | null;
  data: Comment[];
  setData: React.Dispatch<React.SetStateAction<Comment[]>>;
  reviewId: string;
  onCommentCountChange?: (count: number) => void;
}) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {showTooltip} = useTooltipStore();

  const handleDeleteComment = async () => {
    if (!comment) return;

    setIsSubmitting(true);
    try {
      await deleteComment(comment.id);

      const refreshed = await getComments({
        reviewId,
        direction: "DESC",
        limit: data.length
      });
      setData(refreshed.content);

      const updatedReview = await getReviewDetail(reviewId);
      onCommentCountChange?.(updatedReview.commentCount);

      close();
      showTooltip("댓글이 삭제되었습니다.");
    } catch (error) {
      console.error("댓글 삭제 실패:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <Modal
          isDelete
          isOpen={isOpen}
          onClose={close}
          disabled={isSubmitting}
          buttonText="삭제"
          action={handleDeleteComment}
      >
        <p className="font-medium">댓글을 삭제하시겠습니까?</p>
      </Modal>
  );
}
