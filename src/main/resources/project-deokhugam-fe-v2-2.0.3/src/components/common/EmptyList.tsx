import getImagePath from "@/constants/images.ts";

export default function EmptyList({ keyword }: { keyword: string }) {
  return (
    <div className="flex-[1] w-full h-full flex flex-col items-center justify-center">
      <div className="relative mx-auto w-[480px] min-h-[480px]">
        <img
          src={getImagePath("/common/empty_background_pattern.png")}
          alt="pattern"
        />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 border border-gray-200 h-12 w-12 justify-items-center content-center rounded-lg shadow-[0_1px_2px_0_#0A0D120D]">
          <div className="relative">
            <img
              src={getImagePath("/icon/ic_empty_search.svg")}
              alt="search_icon"
              width={24}
              height={24}
            />
          </div>
          <p className="absolute min-w-max bottom-[-40px] font-bold text-lg text-gray-400">
            ‘{keyword}’에 대한 검색 결과가 없습니다.
          </p>
        </div>
      </div>
    </div>
  );
}
