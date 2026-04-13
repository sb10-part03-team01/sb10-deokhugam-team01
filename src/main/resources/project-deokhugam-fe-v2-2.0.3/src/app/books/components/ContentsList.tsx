import {Book} from "@/api/books.ts";
import StarRating from "@/components/common/StarRating.tsx";
import DelayedLoader from "@/components/common/DelayedLoader.tsx";
import InfiniteScrollLoader from "@/components/common/InfiniteScrollLoader.tsx";
import {useNavigate} from "react-router-dom";
import {useState} from "react";
import clsx from "clsx";
import getImagePath from "@/constants/images.ts";

export default function ContentsList({
                                       booksData,
                                       isLoading
                                     }: {
  booksData: Book[];
  isLoading: boolean;
}) {
  const [imgErrors, setImgErrors] = useState<Record<string, boolean>>({});

  const navigate = useNavigate();

  return (
      <>
        <DelayedLoader isLoading={isLoading} delay={1000}>
          <InfiniteScrollLoader/>
        </DelayedLoader>
        <div
            className={clsx(
                "grid w-full gap-x-[2%] gap-y-[60px] grid-cols-5 max-lg1050:grid-cols-4 max-md:grid-cols-3 max-xs650:grid-cols-2 max-sm400:grid-cols-1"
            )}
        >
          {booksData.map(book => {
            return (
                <div
                    key={book.id}
                    onClick={() => navigate(`/books/${book.id}`)}
                    className="cursor-pointer"
                >
                  <div
                      className="relative h-[calc(100vw_*_(325/1920))] min-h-[325px] rounded overflow-hidden border">
                    {book.thumbnailUrl && !imgErrors[book.id] && (
                        <img
                            src={book.thumbnailUrl}
                            alt={book.title || "thumbnail"}
                            onError={() =>
                                setImgErrors(prev => ({...prev, [book.id]: true}))
                            }
                            className={"w-full h-full"}
                        />
                    )}
                    {(!book.thumbnailUrl || imgErrors[book.id]) && (
                        <img src={getImagePath("/books/imgError.png")} alt="이미지 없음"/>
                    )}
                  </div>
                  <p className="mt-4 font-bold text-gray-950 line-clamp-2">
                    {book.title}
                  </p>
                  <p className="text-gray-500 font-medium line-clamp-1 mb-3">
                    {book.author}
                  </p>
                  <div className="flex items-center gap-1">
                    <StarRating rating={book.rating}/>
                    <span className="text-body4 font-medium text-gray-500">
                  ({book.reviewCount})
                </span>
                  </div>
                </div>
            );
          })}
        </div>
      </>
  );
}
