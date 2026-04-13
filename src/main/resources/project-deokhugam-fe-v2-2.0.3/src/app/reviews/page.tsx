import {useState, useCallback, useEffect} from "react";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import ReviewSearchSection from "./components/ReviewSearchSection.tsx";
import {getReviews} from "@/api/reviews.ts";
import {useAuthGuard} from "@/hooks/auth/useAuthRedirect.ts";
import {useInfiniteScroll} from "@/hooks/common/useInfiniteScroll.ts";
import EmptyList from "@/components/common/EmptyList.tsx";
import ReviewList from "./components/ReviewList.tsx";
import type {Review, ReviewsParams} from "@/types/reviews.ts";

export default function ReviewsPage() {
  const [orderBy, setOrderBy] = useState<"createdAt" | "rating">("createdAt");
  const [direction, setDirection] = useState<"ASC" | "DESC">("DESC");
  const [reviews, setReviews] = useState<Review[]>([]);
  const [searchKeyword, setSearchKeyword] = useState<string>("");

  const {shouldShowContent} = useAuthGuard();

  const fetcher = async (params: Record<string, unknown>) => {
    const res = await getReviews(undefined, params as ReviewsParams);
    return {
      content: res.content,
      nextCursor: res.nextCursor ?? "",
      nextAfter: res.nextAfter ?? "",
      hasNext: res.hasNext
    };
  };

  const {isLoading, setCursor, setAfter, setIsLoading, resetInfiniteScroll} =
      useInfiniteScroll<Review, Record<string, unknown>>({
        initialParams: {
          orderBy,
          direction,
          search: searchKeyword || undefined,
          limit: 20
        },
        fetcher,
        setData: setReviews
      });

  useEffect(() => {
    const fetchInitialData = async () => {
      setIsLoading(true);

      try {
        const response = await getReviews(undefined, {
          orderBy,
          direction,
          search: searchKeyword || undefined,
          limit: 20
        });
        setReviews(response.content);
        setCursor(response.nextCursor || undefined);
        setAfter(response.nextAfter || undefined);
      } catch (err) {
        console.error("리뷰 조회 실패:", err);
      } finally {
        setIsLoading(false);
      }
    };

    resetInfiniteScroll();
    setReviews([]);
    fetchInitialData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderBy, direction, searchKeyword]);

  const handleSearch = useCallback((value: string) => {
    setSearchKeyword(value);
  }, []);

  const handleOrderByChange = useCallback(
      (newOrderBy: "createdAt" | "rating") => {
        setOrderBy(newOrderBy);
      },
      []
  );

  const handleDirectionChange = useCallback((newDirection: "ASC" | "DESC") => {
    setDirection(newDirection);
  }, []);

  if (!shouldShowContent) {
    return <LoadingScreen/>;
  }

  return (
      <div className="pt-[50px] pb-[80px] h-[inherit] min-h-[inherit] flex flex-col">
        <div className="flex mb-5 py-[6px] text-header1 font-bold text-[#111827]">
          리뷰 리스트
        </div>

        <ReviewSearchSection
            orderBy={orderBy}
            direction={direction}
            onSearch={handleSearch}
            onOrderByChange={handleOrderByChange}
            onDirectionChange={handleDirectionChange}
        />

        {reviews.length === 0 && !isLoading ? (
            <EmptyList keyword={searchKeyword}/>
        ) : (
            <ReviewList reviews={reviews} isLoading={isLoading}/>
        )}
      </div>
  );
}
