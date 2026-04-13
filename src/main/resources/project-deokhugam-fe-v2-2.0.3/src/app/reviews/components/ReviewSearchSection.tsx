import {memo} from "react";
import Selectbox from "@/components/ui/Selectbox.tsx";
import SearchBar from "@/components/ui/SearchBar.tsx";
import {REVEWS_ORDERBY, SORT_DIRECTION} from "@/constants/selectOptions.ts";
import clsx from "clsx";

interface ReviewSearchSectionProps {
  orderBy: "createdAt" | "rating";
  direction: "DESC" | "ASC";
  onSearch: (value: string) => void;
  onOrderByChange: (value: "createdAt" | "rating") => void;
  onDirectionChange: (value: "DESC" | "ASC") => void;
}

const ReviewSearchSection = memo(function ReviewSearchSection({
                                                                orderBy,
                                                                direction,
                                                                onSearch,
                                                                onOrderByChange,
                                                                onDirectionChange
                                                              }: ReviewSearchSectionProps) {
  return (
      <div
          className={clsx(
              "flex justify-between items-center mb-[30px]",
              "max-md:flex-col max-md:items-start gap-y-4"
          )}
      >
        <div className="max-md:flex-[1] max-md:w-full">
          <SearchBar onSearch={onSearch} placeholder={'도서명, 리뷰어, 내용을 검색해보세요.'}/>
        </div>

        <div className="flex gap-2">
          <Selectbox
              options={REVEWS_ORDERBY}
              value={orderBy}
              onChange={onOrderByChange}
          />
          <Selectbox
              options={SORT_DIRECTION}
              value={direction}
              onChange={onDirectionChange}
          />
        </div>
      </div>
  );
});

export default ReviewSearchSection;
