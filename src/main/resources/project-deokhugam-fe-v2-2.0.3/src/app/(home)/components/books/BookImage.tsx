import getImagePath from "@/constants/images.ts";

interface BookImageProps {
  thumbnailUrl?: string;
  title?: string;
  rank: number;
  isEmpty?: boolean;
}

export default function BookImage({
  thumbnailUrl,
  title,
  rank,
  isEmpty
}: BookImageProps) {
  if (isEmpty) {
    return (
      <div className="w-[209px] h-[314px] rounded-[6px] mb-[12px] relative">
        <img
          src={getImagePath("/books/imgError.png")}
          alt="기본 도서 이미지"
          width={209}
          height={314}
          className="w-full h-full object-cover rounded-[6px]"
        />
        <div
          className="absolute inset-0 rounded-[6px]"
          style={{
            background:
              "linear-gradient(180deg, rgba(0, 0, 0, 0) 33%, rgba(0, 0, 0, 0.25) 83.58%, rgba(0, 0, 0, 0.5) 177.5%)"
          }}
        />
      </div>
    );
  }

  return (
    <div
      className="w-[209px] h-[314px] rounded-[6px] mb-[12px] relative cursor-pointer hover:opacity-90 transition-opacity"
      style={{
        background:
          "linear-gradient(180deg, rgba(0, 0, 0, 0) 33%, rgba(0, 0, 0, 0.25) 83.58%, rgba(0, 0, 0, 0.5) 177.5%)"
      }}
    >
      <img
        src={thumbnailUrl || getImagePath("/books/imgError.png")}
        alt={title || "기본 도서 이미지"}
        width={209}
        height={314}
        className="w-full h-full rounded-[6px]"
      />
      {/* 그라데이션 오버레이 */}
      <div
        className="absolute inset-0 rounded-[6px] border"
        style={{
          background:
            "linear-gradient(180deg, rgba(0, 0, 0, 0) 33%, rgba(0, 0, 0, 0.25) 83.58%, rgba(0, 0, 0, 0.5) 177.5%)"
        }}
      />
      {/* 순위 표시 */}
      <div
        className="absolute text-gray-0 font-bold flex items-center justify-center"
        style={{
          fontFamily: "Pretendard",
          fontSize: "52px",
          lineHeight: "100%",
          letterSpacing: "-2%",
          left: "20px",
          bottom: "11px"
        }}
      >
        {rank}
      </div>
    </div>
  );
}
