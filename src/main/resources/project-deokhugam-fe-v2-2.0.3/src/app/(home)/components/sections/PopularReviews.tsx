import {useState, useEffect} from "react";
import {Link} from "react-router-dom";
import Button from "@/components/common/Buttons/Button.tsx";
import SectionHeader from "../ui/SectionHeader.tsx";
import EmptyState from "../ui/EmptyState.tsx";
import ReviewCard from "../reviews/ReviewCard.tsx";
import {getPopularReviews} from "@/api/reviews.ts";
import type {PopularReview, PopularReviewsParams} from "@/types/reviews.ts";
import getImagePath from "@/constants/images.ts";

export default function PopularReviews() {
  const [selectedFilter, setSelectedFilter] = useState("전체");
  const [popularReviews, setPopularReviews] = useState<PopularReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasData, setHasData] = useState(false);

  const getPeriodFromFilter = (
      filter: string
  ): PopularReviewsParams["period"] => {
    switch (filter) {
      case "일간":
        return "DAILY";
      case "주간":
        return "WEEKLY";
      case "월간":
        return "MONTHLY";
      case "전체":
        return "ALL_TIME";
      default:
        return "DAILY";
    }
  };

  const fetchPopularReviews = async (
      period: PopularReviewsParams["period"] = "DAILY"
  ) => {
    try {
      setLoading(true);
      setError(null);

      const response = await getPopularReviews({
        period,
        direction: "DESC",
        limit: 20
      });

      const reviews = response.content;

      if (reviews.length === 0) {
        setHasData(false);
        setPopularReviews([]);
      } else {
        setHasData(true);
        setPopularReviews(reviews);
      }
    } catch (err) {
      console.error("인기리뷰 조회 실패:", err);
      setError("인기리뷰를 불러오는데 실패했습니다.");
      setHasData(false);
      setPopularReviews([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPopularReviews(getPeriodFromFilter(selectedFilter));
  }, [selectedFilter]);

  const handleFilterChange = (filter: string) => {
    setSelectedFilter(filter);
    fetchPopularReviews(getPeriodFromFilter(filter));
  };

  return (
      <div>
        <SectionHeader
            title="🔥 인기 리뷰"
            description="가장 화제의 리뷰들은 뭐가 있을까?"
            subDescription="(좋아요와 댓글 수는 해당 기간 내 집계된 수치입니다.)"
            selectedFilter={selectedFilter}
            onFilterChange={handleFilterChange}
        />

        {loading ? (
            <div className="flex justify-center py-8">
              <p className="text-body2 text-gray-500">로딩 중...</p>
            </div>
        ) : error ? (
            <div className="flex justify-center py-8">
              <p className="text-body2 text-red-500">{error}</p>
            </div>
        ) : !hasData ? (
            <EmptyState
                title=""
                description="등록된 인기 리뷰가 없습니다."
                iconSrc={getImagePath("/icon/ic_comment-filled.svg")}
                iconAlt="리뷰 아이콘"
            />
        ) : (
            <>
              <div className="flex flex-col gap-[30px] mb-[30px]">
                {popularReviews.map(review => (
                    <ReviewCard key={review.id} review={review}/>
                ))}
              </div>

              <div className="flex justify-center">
                <Link to="/reviews">
                  <Button variant="outline">
                    리뷰 더보기
                    <img
                        src={getImagePath("/icon/ic_chevron-right.svg")}
                        alt="더보기"
                        width={16}
                        height={16}
                    />
                  </Button>
                </Link>
              </div>
            </>
        )}
      </div>
  );
}
