import {deleteReview, getReviews} from "@/api/reviews.ts";
import Modal from "@/components/ui/Modal.tsx";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import {Review} from "@/types/reviews.ts";
import {Dispatch, SetStateAction, useState} from "react";

export default function ReviewDeleteModal({
                                            isOpen,
                                            close,
                                            reviewId,
                                            setData,
                                            setTotalElements,
                                            bookId
                                          }: {
  isOpen: boolean;
  close: () => void;
  reviewId: string;
  data: Review[];
  setData: Dispatch<SetStateAction<Review[]>>;
  setTotalElements: Dispatch<SetStateAction<number>>;
  bookId: string;
}) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {showTooltip} = useTooltipStore();

  const handleDeleteReview = async () => {
    setIsSubmitting(true);
    try {
      await deleteReview(reviewId);

      const refreshed = await getReviews(bookId, {limit: 20});
      setData(refreshed.content);
      setTotalElements(refreshed.totalElements);

      close();
      showTooltip("리뷰를 정상적으로 삭제하였습니다!");
    } catch (error) {
      console.error("리뷰 삭제 실패:", error);
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
          action={handleDeleteReview}
      >
        <p className="font-medium">리뷰를 삭제하시겠습니까?</p>
      </Modal>
  );
}
