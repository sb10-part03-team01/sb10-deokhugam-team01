import getImagePath from "@/constants/images.ts";

interface ReviewImageProps {
  bookThumbnailUrl?: string;
  bookTitle?: string;
  isEmpty?: boolean;
}

export default function ReviewImage({
  bookThumbnailUrl,
  bookTitle,
  isEmpty = false
}: ReviewImageProps) {
  if (isEmpty) {
    return (
      <div className="flex-shrink-0">
        <img
          src={getImagePath("/books/imgError.png")}
          alt="기본 도서 이미지"
          width={96.5}
          height={144.75}
          className="w-[96.5px] h-[144.75px] object-cover rounded-[6px]"
        />
      </div>
    );
  }

  return (
    <div className="min-w-max">
      <img
        src={bookThumbnailUrl || getImagePath("/books/imgError.png")}
        alt={bookTitle || "기본 도서 이미지"}
        width={96.5}
        height={144.75}
        className="w-[96.5px] h-[144.75px] rounded-[6px] border"
        onError={e => {
          const target = e.target as HTMLImageElement;
          target.src = getImagePath("/books/imgError.png");
        }}
      />
    </div>
  );
}
