import getImagePath from "@/constants/images.ts";

interface BookInfoProps {
  title: string;
  author?: string;
  rating: number;
  reviewCount: number;
  isEmpty?: boolean;
}

export default function BookInfo({
  title,
  author,
  rating,
  reviewCount,
  isEmpty = false
}: BookInfoProps) {
  return (
    <>
      <div className="mb-[8px]">
        <h3 className="text-body2 font-semibold text-gray-950 mb-[6px] hover:text-gray-700 transition-colors line-clamp-1 overflow-hidden text-ellipsis">
          {isEmpty ? "" : title}
        </h3>
        <p className="text-body3 font-medium text-gray-500">
          {isEmpty ? "" : author || "저자 정보 없음"}
        </p>
      </div>

      <div className="flex items-center gap-[4px]">
        <div className="flex">
          {isEmpty
            ? [...Array(5)].map((_, index) => (
                <img
                  key={index}
                  src={getImagePath("/icon/ic_star_failled.svg")}
                  alt="빈별점"
                  width={16}
                  height={16}
                />
              ))
            : [...Array(5)].map((_, index) => {
                const starIndex = index + 1;

                if (starIndex <= Math.floor(rating)) {
                  return (
                    <img
                      key={index}
                      src={getImagePath("/icon/ic_star.svg")}
                      alt="별점"
                      width={16}
                      height={16}
                    />
                  );
                } else if (
                  starIndex === Math.ceil(rating) &&
                  rating % 1 >= 0.5
                ) {
                  return (
                    <img
                      key={index}
                      src={getImagePath("/icon/ic_star_half.svg")}
                      alt="반별점"
                      width={16}
                      height={16}
                    />
                  );
                } else {
                  return (
                    <img
                      key={index}
                      src={getImagePath("/icon/ic_star_failled.svg")}
                      alt="빈별점"
                      width={16}
                      height={16}
                    />
                  );
                }
              })}
        </div>
        <span className="text-body4 font-medium text-gray-500">
          {isEmpty ? "" : `(${reviewCount})`}
        </span>
      </div>
    </>
  );
}
