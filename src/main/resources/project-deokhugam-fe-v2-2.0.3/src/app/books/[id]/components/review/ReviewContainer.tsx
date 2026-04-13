import {getReviews} from "@/api/reviews.ts";
import {useInfiniteScroll} from "@/hooks/common/useInfiniteScroll.ts";
import {Review, ReviewsParams} from "@/types/reviews.ts";
import {
  Dispatch,
  ReactNode,
  SetStateAction,
  useEffect,
  useState
} from "react";

type Props = {
  id: string;
  children: (props: {
    data: Review[];
    setData: Dispatch<SetStateAction<Review[]>>;
    isLoading: boolean;
    totalElements: number;
    setTotalElements: Dispatch<SetStateAction<number>>;
  }) => ReactNode;
};

export default function ReviewContainer({id, children}: Props) {
  const [data, setData] = useState<Review[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const limit = 20;

  const {isLoading, setCursor, setAfter, setIsLoading, resetInfiniteScroll} =
      useInfiniteScroll<Review, ReviewsParams>({
        initialParams: {limit},
        fetcher: async params => {
          const res = await getReviews(id, {...params});

          return {
            content: res.content,
            nextCursor: res.nextCursor ?? "",
            nextAfter: res.nextAfter ?? "",
            hasNext: res.hasNext
          };
        },
        setData
      });

  useEffect(() => {
    const fetchReviewList = async () => {
      setIsLoading(true);

      try {
        const response = await getReviews(id);
        setData(response.content);
        setTotalElements(response.totalElements);
        setCursor(response.nextCursor ?? undefined);
        setAfter(response.nextAfter ?? undefined);
      } catch (err) {
        console.error("리뷰 조회 실패:", err);
      } finally {
        setIsLoading(false);
      }
    };

    resetInfiniteScroll();
    setData([]);
    fetchReviewList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  return (
      <>
        {" "}
        {children({data, setData, isLoading, totalElements, setTotalElements})}
      </>
  );
}
