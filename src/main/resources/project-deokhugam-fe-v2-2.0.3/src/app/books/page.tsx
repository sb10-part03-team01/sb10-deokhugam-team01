import {useAuthGuard} from "@/hooks/auth/useAuthRedirect.ts";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import PageHead from "./components/PageHead.tsx";
import BookSearchSection from "./components/BookSearchSection.tsx";
import {useEffect, useState} from "react";
import ContentsList from "./components/ContentsList.tsx";
import {Book, BooksParams, getBooks} from "@/api/books.ts";
import {useInfiniteScroll} from "@/hooks/common/useInfiniteScroll.ts";
import EmptyList from "@/components/common/EmptyList.tsx";
import useResponsiveLimit from "@/hooks/book/useResponsiveLimit.ts";

export default function BooksPage() {
  const [orderBy, setOrderBy] = useState<
      "title" | "publishedDate" | "rating" | "reviewCount"
  >("title");
  const [direction, setDirection] = useState<"ASC" | "DESC">("DESC");
  const [keyword, setKeyword] = useState("");
  const [booksData, setBooksData] = useState<Book[]>([]);
  const limit = useResponsiveLimit("bookList");

  const {isLoading, setCursor, setAfter, setIsLoading, resetInfiniteScroll} =
      useInfiniteScroll<Book, BooksParams>({
        initialParams: {orderBy, direction, keyword, limit},
        fetcher: getBooks,
        setData: setBooksData
      });

  useEffect(() => {
    const fetchBook = async () => {
      setIsLoading(true);

      try {
        const response = await getBooks({orderBy, direction, keyword, limit});
        setBooksData(response.content);
        setCursor(response.nextCursor);
        setAfter(response.nextAfter);
      } catch (err) {
        console.error("도서 조회 실패:", err);
      } finally {
        setIsLoading(false);
      }
    };

    resetInfiniteScroll();
    setBooksData([]);
    fetchBook();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderBy, direction, keyword]);

  const {shouldShowContent} = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen/>;
  }

  return (
      <div className="pt-[50px] pb-[80px] h-[inherit] min-h-[inherit] flex flex-col">
        <PageHead/>
        <BookSearchSection
            orderBy={orderBy}
            direction={direction}
            onSearch={setKeyword}
            onOrderByChange={setOrderBy}
            onDirectionChange={setDirection}
        />
        {booksData.length === 0 && !isLoading ? (
            <EmptyList keyword={keyword}/>
        ) : (
            <ContentsList booksData={booksData} isLoading={isLoading}/>
        )}
      </div>
  );
}
