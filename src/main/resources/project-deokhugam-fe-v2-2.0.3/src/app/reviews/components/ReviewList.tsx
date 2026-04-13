import DelayedLoader from "@/components/common/DelayedLoader.tsx";
import InfiniteScrollLoader from "@/components/common/InfiniteScrollLoader.tsx";
import ReviewCard from "@/app/(home)/components/reviews/ReviewCard.tsx";
import type {Review} from "@/types/reviews.ts";
import {useState, useEffect} from "react";
import clsx from "clsx";

interface ReviewListProps {
  reviews: Review[];
  isLoading: boolean;
}

export default function ReviewList({reviews, isLoading}: ReviewListProps) {
  const [reviewsData, setReviewsData] = useState(reviews);

  useEffect(() => {
    setReviewsData(reviews);
  }, [reviews]);

  const handleLikeChange = (
      reviewId: string,
      newLikeCount: number,
      likedByMe: boolean
  ) => {
    setReviewsData(prevReviews =>
        prevReviews.map(review =>
            review.id === reviewId
                ? {...review, likeCount: newLikeCount, likedByMe}
                : review
        )
    );
  };

  return (
      <>
        <DelayedLoader isLoading={isLoading} delay={1000}>
          <InfiniteScrollLoader/>
        </DelayedLoader>

        <div
            className={clsx("grid grid-cols-2 gap-[30px]", "max-md:grid-cols-1")}
        >
          {reviewsData.map(review => (
              <ReviewCard
                  key={review.id}
                  review={review}
                  maxTitleWidth={270}
                  onLikeChange={handleLikeChange}
              />
          ))}
        </div>
      </>
  );
}
