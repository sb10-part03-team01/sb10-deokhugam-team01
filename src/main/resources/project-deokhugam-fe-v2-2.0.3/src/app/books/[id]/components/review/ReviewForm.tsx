import clsx from "clsx";
import {
  Dispatch,
  FormEvent,
  SetStateAction,
  useEffect,
  useRef,
  useState
} from "react";
import ReviewRating from "./ReviewRating.tsx";
import {textareaStyle} from "@/app/books/styles.ts";
import Button from "@/components/common/Buttons/Button.tsx";
import {getReviews, postReview} from "@/api/reviews.ts";
import {useAuthStore} from "@/store/authStore.ts";
import {Review} from "@/types/reviews.ts";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import axios from "axios";
import getImagePath from "@/constants/images.ts";

export default function ReviewForm({
                                     setData,
                                     totalElements,
                                     setTotalElements,
                                     bookId
                                   }: {
  data: Review[];
  setData: Dispatch<SetStateAction<Review[]>>;
  totalElements: number;
  setTotalElements: Dispatch<SetStateAction<number>>;
  bookId: string;
}) {
  const [isLoading, setIsLoading] = useState(false);
  const [isDirty, setIsDirty] = useState(false);
  const [rating, setRating] = useState(0);
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  const MAX_REVIEW_LENGTH = 500;

  const {user} = useAuthStore();
  const userId = user?.id;
  const {showTooltip} = useTooltipStore();

  const tooltipErrorImg = getImagePath("/icon/ic_exclamation-circle.svg");

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!bookId || !userId) return;

    const content = textareaRef.current?.value ?? "";
    setIsLoading(true);
    try {
      await postReview({bookId, userId, content, rating});

      const refreshed = await getReviews(bookId, {limit: 20});
      setData(refreshed.content);
      setTotalElements(refreshed.totalElements);
      showTooltip("리뷰 등록이 완료되었습니다!");
      setRating(0);

      if (textareaRef.current) {
        textareaRef.current.value = "";
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;

        if (status === 409) {
          showTooltip(
              "이미 작성된 리뷰가 있습니다. 수정을 원하시면 기존 리뷰를 확인해주세요.",
              tooltipErrorImg
          );
          setRating(0);

          if (textareaRef.current) {
            textareaRef.current.value = "";
          }
        } else {
          showTooltip(
              "서버 응답이 없습니다. 네트워크 상태를 확인해주세요.",
              tooltipErrorImg
          );
        }
      }
      console.error("리뷰 등록 실패:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const value = textareaRef.current?.value ?? "";
    setIsDirty(value.trim() !== "" && value.length > 0 && rating > 0);
  }, [rating]);

  return (
      <div className={clsx("mt-[34px]", "max-sm:mt-0")}>
        <div className="flex items-center gap-[4px] mb-[15px]">
          <h2 className="text-body1 font-semibold text-gray-900">리뷰</h2>
          <span className="text-body1 font-semibold text-gray-500">
          {totalElements ? totalElements : ""}
        </span>
        </div>
        <form onSubmit={e => handleSubmit(e)}>
          <div className="pb-[15px]">
            <ReviewRating totalStars={5} rating={rating} setRating={setRating}/>
          </div>
          <textarea
              ref={textareaRef}
              className={clsx(textareaStyle, "w-full")}
              placeholder="리뷰를 작성해주세요..."
              maxLength={MAX_REVIEW_LENGTH}
              onChange={() => {
                const value = textareaRef.current?.value ?? "";
                setIsDirty(value.trim() !== "" && value.length > 0 && rating > 0);
              }}
          />
          <div className="flex justify-end mt-[15px]">
            <Button variant="primary" disabled={!isDirty || isLoading}>
              {isLoading ? (
                  <div
                      className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-50 mx-auto"/>
              ) : (
                  "등록"
              )}
            </Button>
          </div>
        </form>
      </div>
  );
}
