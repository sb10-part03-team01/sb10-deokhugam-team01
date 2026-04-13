import {Link} from "react-router-dom";
import BookImage from "./BookImage.tsx";
import BookInfo from "./BookInfo.tsx";
import type {PopularBook} from "@/api/books.ts";
import clsx from "clsx";

interface BookCardProps {
  book: PopularBook;
}

export default function BookCard({book}: BookCardProps) {
  if (book.isEmpty) {
    return (
        <div className={clsx("flex-1", "max-lg:max-w-min")}>
          <BookImage thumbnailUrl="" title="" rank={0} isEmpty={true}/>
          <BookInfo
              title=""
              author=""
              rating={0}
              reviewCount={0}
              isEmpty={true}
          />
        </div>
    );
  }

  return (
      <div className={clsx("flex-1", "max-lg:max-w-min")}>
        <Link to={`/books/${book.bookId}`} className="block">
          <BookImage
              thumbnailUrl={book.thumbnailUrl}
              title={book.title}
              rank={book.rank}
              isEmpty={false}
          />
          <BookInfo
              title={book.title}
              author={book.author}
              rating={book.rating}
              reviewCount={book.reviewCount}
          />
        </Link>
      </div>
  );
}
