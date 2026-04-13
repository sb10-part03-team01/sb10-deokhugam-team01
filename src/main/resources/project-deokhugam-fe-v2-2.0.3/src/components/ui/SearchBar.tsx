import { useState, forwardRef, InputHTMLAttributes, memo, useRef } from "react";
import clsx from "clsx";
import getImagePath from "@/constants/images.ts";

interface SearchBarProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> {
  placeholder?: string;
  onSearch?: (value: string) => void;
  onClear?: () => void;
}

const SearchBar = memo(
  forwardRef<HTMLInputElement, SearchBarProps>(
    (
      {
        placeholder = "내가 찾는 책 이름을 검색해보세요",
        onSearch,
        onClear,
        className = "",
        ...props
      },
      ref
    ) => {
      const [isFocused, setIsFocused] = useState(false);
      const inputRef = useRef<HTMLInputElement>(null);

      // 상태 결정
      const getSearchState = () => {
        if (isFocused) return "typing";
        if (inputRef.current?.value && inputRef.current.value.length > 0)
          return "completed";
        return "default";
      };

      const searchState = getSearchState();

      // 포커스 핸들러
      const handleFocus = () => {
        setIsFocused(true);
      };

      // 블러 핸들러
      const handleBlur = () => {
        setIsFocused(false);
      };

      // 검색 핸들러
      const handleSearch = () => {
        const value = inputRef.current?.value?.trim() || "";
        onSearch?.(value);
      };

      // 지우기 핸들러
      const handleClear = () => {
        if (inputRef.current) {
          inputRef.current.value = "";
        }
        onClear?.();
        onSearch?.("");
      };

      // 엔터키 핸들러
      const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
          handleSearch();
        }
      };

      // 상태별 컨테이너 스타일
      const getContainerStyles = () => {
        switch (searchState) {
          case "typing":
            return "bg-white border-[1.5px] border-gray-400 shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)]";
          case "completed":
            return "bg-gray-100 border-[1.5px] border-transparent";
          default:
            return "bg-gray-100 border-[1.5px] border-transparent";
        }
      };

      // 상태별 텍스트 스타일
      const getTextStyles = () => {
        switch (searchState) {
          case "typing":
            return "text-gray-800";
          case "completed":
            return "text-gray-600";
          default:
            return "text-gray-400";
        }
      };

      return (
        <div
          className={clsx(
            `relative min-w-[320px] ${className}`,
            "max-xs650:max-w-full"
          )}
        >
          <div className="absolute left-[22px] top-1/2 transform -translate-y-1/2 z-10">
            <img
              src={getImagePath("/icon/ic_search.svg")}
              alt="검색"
              width={20}
              height={20}
            />
          </div>

          <input
            ref={node => {
              inputRef.current = node;
              if (typeof ref === "function") {
                ref(node);
              } else if (ref) {
                ref.current = node;
              }
            }}
            type="text"
            placeholder={searchState === "typing" ? "" : placeholder}
            onFocus={handleFocus}
            onBlur={handleBlur}
            onKeyDown={handleKeyDown}
            className={`w-full py-[13px] pl-[47px] pr-[30px] rounded-full text-body2 font-medium placeholder:text-gray-400 !outline-none focus:!outline-none focus:!ring-0 focus:!ring-offset-0 focus:!shadow-none transition-all duration-200 ${getContainerStyles()} ${getTextStyles()}`}
            {...props}
          />

          {(searchState === "typing" ||
            (searchState === "completed" &&
              inputRef.current?.value &&
              inputRef.current.value.length > 0)) && (
            <button
              type="button"
              onClick={handleClear}
              className="absolute right-[22px] top-1/2 transform -translate-y-1/2 w-[20px] h-[20px] flex items-center justify-center hover:opacity-70 transition-opacity z-10"
            >
              <img
                src={getImagePath("/icon/ic_xbox.svg")}
                alt="지우기"
                width={20}
                height={20}
              />
            </button>
          )}
        </div>
      );
    }
  ),
  () => {
    // 항상 리렌더링하지 않음 (강제로 메모이제이션)
    return true;
  }
);

SearchBar.displayName = "SearchBar";

export default SearchBar;
