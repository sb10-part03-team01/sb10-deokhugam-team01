import {formatDate} from "@/app/utils/formatData.ts";
import clsx from "clsx";
import {useState, useEffect} from "react";
import {toggleReviewLike, getReviewDetail} from "@/api/reviews.ts";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import getImagePath from "@/constants/images.ts";

interface ReviewContentProps {
  userNickname?: string;
  bookTitle?: string;
  reviewRating: number;
  reviewContent?: string;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  isEmpty?: boolean;
  maxTitleWidth?: number;
  reviewId?: string;
  likedByMe?: boolean;
  onLikeChange?: (
      reviewId: string,
      newLikeCount: number,
      likedByMe: boolean
  ) => void;
}

export default function ReviewContent({
                                        userNickname,
                                        bookTitle,
                                        reviewRating,
                                        reviewContent,
                                        likeCount,
                                        commentCount,
                                        createdAt,
                                        isEmpty = false,
                                        maxTitleWidth,
                                        reviewId,
                                        likedByMe = false,
                                        onLikeChange
                                      }: ReviewContentProps) {
  const [isLiked, setIsLiked] = useState(likedByMe);
  const [currentLikeCount, setCurrentLikeCount] = useState(likeCount);
  const {showTooltip} = useTooltipStore();

  useEffect(() => {
    setIsLiked(likedByMe);
  }, [likedByMe]);

  const handleLikeClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!reviewId || isEmpty || !onLikeChange) return;

    try {
      await toggleReviewLike(reviewId);

      // 서버에서 최신 데이터 가져오기
      const reviewDetail = await getReviewDetail(reviewId);
      const newLikeCount = reviewDetail.likeCount;
      const newLikedByMe = reviewDetail.likedByMe;

      setCurrentLikeCount(newLikeCount);
      setIsLiked(newLikedByMe);
      onLikeChange(reviewId, newLikeCount, newLikedByMe);
    } catch (error) {
      console.error("좋아요 처리 실패:", error);
      showTooltip("좋아요 처리에 실패했습니다.", "");
    }
  };

  const renderStars = (rating: number) => {
    return [...Array(5)].map((_, index) => {
      const starIndex = index + 1;

      if (starIndex <= Math.floor(rating)) {
        return (
            <img
                key={index}
                src={getImagePath("/icon/ic_star.svg")}
                alt="별점"
                width={18}
                height={18}
            />
        );
      } else if (starIndex === Math.ceil(rating) && rating % 1 >= 0.5) {
        return (
            <img
                key={index}
                src={getImagePath("/icon/ic_star_half.svg")}
                alt="반별점"
                width={18}
                height={18}
            />
        );
      } else {
        return (
            <img
                key={index}
                src={getImagePath("/icon/ic_star_failled.svg")}
                alt="빈별점"
                width={18}
                height={18}
            />
        );
      }
    });
  };

  return (
      <div className="flex-1 flex flex-col">
        <div
            className={clsx(
                "flex items-center justify-between my-2",
                "max-lg1050:flex-col-reverse max-lg1050:items-start max-lg1050:my-0 max-lg1050:mb-2"
            )}
        >
          <div className="flex items-center gap-[6px] flex-1 min-w-0">
          <span className="text-body1 font-semibold text-gray-950 min-w-max">
            {isEmpty ? "" : userNickname || "익명"}
          </span>
            <span
                className={clsx(
                    "text-body2 font-medium text-gray-500 line-clamp-1 min-w-5",
                    maxTitleWidth ? `max-w-[${maxTitleWidth}px]` : "max-w-[200px]"
                )}
            >
            {isEmpty ? "" : bookTitle || "제목 없음"}
          </span>
          </div>
          <div
              className={clsx(
                  "flex ml-2",
                  "max-lg1050:ml-0 max-lg1050:mt-1 max-lg1050:mb-2"
              )}
          >
            {isEmpty
                ? [...Array(5)].map((_, index) => (
                    <img
                        key={index}
                        src={getImagePath("/icon/ic_star_failled.svg")}
                        alt="빈별점"
                        width={18}
                        height={18}
                    />
                ))
                : renderStars(reviewRating || 0)}
          </div>
        </div>

        <div className="flex-1 mb-[12.75px]">
          <p
              className="text-body2 font-medium text-gray-800 overflow-hidden"
              style={{
                display: "-webkit-box",
                WebkitLineClamp: 3,
                WebkitBoxOrient: "vertical",
                lineHeight: "1.4",
                maxHeight: "calc(1.4em * 3)"
              }}
          >
            {isEmpty ? "" : reviewContent || "리뷰 내용이 없습니다."}
          </p>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex gap-[12px]">
            <div
                className={`flex items-center text-body3 font-medium text-gray-500 ${onLikeChange ? "cursor-pointer hover:opacity-70 transition-opacity" : ""}`}
                onClick={onLikeChange ? handleLikeClick : undefined}
            >
              <img
                  src={
                    isLiked
                        ? getImagePath("/icon/ic_heart_black.svg")
                        : getImagePath("/icon/ic_heart.svg")
                  }
                  alt="좋아요"
                  width={16}
                  height={16}
                  className="mr-[2px] w-4 h-4"
              />
              {isEmpty ? "" : currentLikeCount || 0}
            </div>
            <div className="flex items-center text-body3 font-medium text-gray-500">
              <img
                  src={getImagePath("/icon/ic_comment.svg")}
                  alt="댓글"
                  width={16}
                  height={16}
                  className="mr-[2px]"
              />
              {isEmpty ? "" : commentCount || 0}
            </div>
          </div>
          <div className="text-body3 font-medium text-gray-500">
            {isEmpty ? "" : formatDate(createdAt)}
          </div>
        </div>
      </div>
  );
}
