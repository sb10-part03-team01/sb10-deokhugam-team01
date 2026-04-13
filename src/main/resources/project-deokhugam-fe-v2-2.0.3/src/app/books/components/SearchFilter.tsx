import Selectbox from "@/components/ui/Selectbox.tsx";
import {BOOKS_ORDERBY, SORT_DIRECTION} from "@/constants/selectOptions.ts";
import clsx from "clsx";
import {Dispatch, SetStateAction, useRef} from "react";
import getImagePath from "@/constants/images.ts";

export default function SearchFilter({
                                       orderBy,
                                       direction,
                                       keyword,
                                       setOrderBy,
                                       setDirection,
                                       setKeyword
                                     }: {
  orderBy: string;
  direction: string;
  keyword: string;
  setOrderBy: Dispatch<
      SetStateAction<"title" | "publishedDate" | "rating" | "reviewCount">
  >;
  setDirection: Dispatch<SetStateAction<"ASC" | "DESC">>;
  setKeyword: Dispatch<SetStateAction<string>>;
}) {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      setKeyword(e.currentTarget.value);
    }
  };
  const searchRef = useRef<HTMLInputElement | null>(null);

  return (
      <div className="flex items-center justify-between mb-[30px]">
        <div
            className="flex items-center bg-gray-100 h-[46px] p-5 gap-2 rounded-full w-[calc(100vw_*_(300/1920))] min-w-[310px]">
          <img
              src={getImagePath("/icon/ic_search.svg")}
              alt="Search"
              width={20}
              height={20}
          />
          <input
              ref={searchRef}
              type="text"
              placeholder="내가 찾는 책 이름을 검색해보세요"
              onKeyDown={handleKeyDown}
              maxLength={40}
              className={clsx(
                  "bg-gray-100 w-full font-medium",
                  "placeholder:font-medium"
              )}
          />
          {keyword && (
              <img
                  src={getImagePath("/icon/ic_xbox.svg")}
                  alt="X"
                  width={20}
                  height={20}
                  className="cursor-pointer"
                  onClick={() => {
                    setKeyword("");
                    if (searchRef.current) searchRef.current.value = "";
                  }}
              />
          )}
        </div>
        <div className="flex items-center gap-2">
          <Selectbox
              options={BOOKS_ORDERBY}
              value={orderBy}
              onChange={v =>
                  setOrderBy(
                      v as "title" | "publishedDate" | "rating" | "reviewCount"
                  )
              }
          />
          <Selectbox
              options={SORT_DIRECTION}
              value={direction}
              onChange={v => setDirection(v as "ASC" | "DESC")}
          />
        </div>
      </div>
  );
}
