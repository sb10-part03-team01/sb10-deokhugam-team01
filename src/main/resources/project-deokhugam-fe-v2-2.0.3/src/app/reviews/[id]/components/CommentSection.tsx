import {useState, useEffect} from "react";
import InfiniteScrollLoader from "@/components/common/InfiniteScrollLoader.tsx";
import {getComments} from "@/api/comments.ts";
import {getReviewDetail} from "@/api/reviews.ts";
import {useInfiniteScroll} from "@/hooks/common/useInfiniteScroll.ts";
import type {Comment, Review} from "@/types/reviews.ts";
import CommentHeader from "./comment/CommentHeader.tsx";
import CommentForm from "./comment/CommentForm.tsx";
import CommentList from "./comment/CommentList.tsx";

interface CommentSectionProps {
  reviewId: string;
  onCommentCountChange?: (count: number) => void;
}

export default function CommentSection({
                                         reviewId,
                                         onCommentCountChange
                                       }: CommentSectionProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [review, setReview] = useState<Review | null>(null);
  const [isUpdatingComment, setIsUpdatingComment] = useState(false);

  // 댓글 개수 변경 시 review 상태도 업데이트
  const handleCommentCountChange = (count: number) => {
    setReview(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        commentCount: count
      };
    });
    onCommentCountChange?.(count);
  };

  const {isLoading, setCursor, setAfter, setIsLoading, resetInfiniteScroll} =
      useInfiniteScroll<
          Comment,
          { reviewId: string; direction: "DESC" | "ASC"; limit: number }
      >({
        initialParams: {
          reviewId,
          direction: "DESC",
          limit: 20
        },
        fetcher: async params => {
          const response = await getComments(params);
          return {
            content: response.content,
            nextCursor: response.nextCursor || "",
            nextAfter: response.nextAfter || "",
            hasNext: response.hasNext
          };
        },
        setData: setComments
      });

  useEffect(() => {
    const fetchInitialComments = async () => {
      if (!reviewId) return;

      setIsLoading(true);
      try {
        const response = await getComments({
          reviewId,
          direction: "DESC",
          limit: 20
        });
        setComments(response.content);
        setCursor(response.nextCursor ?? undefined);
        setAfter(response.nextAfter ?? undefined);
      } catch (error) {
        console.error("초기 댓글 조회 실패:", error);
      } finally {
        setIsLoading(false);
      }
    };

    resetInfiniteScroll();
    setComments([]);
    fetchInitialComments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reviewId]);

  useEffect(() => {
    const fetchReview = async () => {
      if (!reviewId) return;

      try {
        const reviewData = await getReviewDetail(reviewId);
        setReview(reviewData);
      } catch (error) {
        console.error("리뷰 조회 실패:", error);
      }
    };

    fetchReview();
  }, [reviewId]);

  const handleCommentSubmit = (newComment: Comment) => {
    setComments(prev => {
      const combined = [newComment, ...prev];
      const unique = Array.from(
          new Map(combined.map(item => [item.id, item])).values()
      );
      return unique;
    });
    resetInfiniteScroll();

    if (review) {
      const newCommentCount = review.commentCount + 1;
      setReview(prev => {
        if (!prev) return prev;
        return {
          ...prev,
          commentCount: newCommentCount
        };
      });
    }
  };

  return (
      <div className="border-t border-gray-100">
        <div className="mt-[34px] flex flex-col gap-[15px]">
          <CommentHeader commentCount={review?.commentCount}/>
          <CommentForm
              reviewId={reviewId}
              onCommentSubmit={handleCommentSubmit}
              onCommentCountChange={handleCommentCountChange}
              reviewCommentCount={review?.commentCount}
          />
        </div>

        <CommentList
            comments={comments}
            reviewId={reviewId}
            setData={setComments}
            onCommentUpdate={async updatedComment => {
              try {
                setIsUpdatingComment(true);
                setComments(prev =>
                    prev.map(comment =>
                        comment.id === updatedComment.id ? updatedComment : comment
                    )
                );

                const updatedReview = await getReviewDetail(reviewId);
                setReview(updatedReview);
                onCommentCountChange?.(updatedReview.commentCount);
              } catch (error) {
                console.error("댓글 수정 반영 실패:", error);
              } finally {
                setIsUpdatingComment(false);
              }
            }}
            onCommentCountChange={handleCommentCountChange}
        />
        {(isLoading || isUpdatingComment) && <InfiniteScrollLoader/>}
      </div>
  );
}
