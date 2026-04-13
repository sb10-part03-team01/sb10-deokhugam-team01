import { InputHTMLAttributes, forwardRef, useState, useEffect } from "react";
import getImagePath from "@/constants/images.ts";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  showPasswordToggle?: boolean;
  onTogglePassword?: () => void;
  isPasswordVisible?: boolean;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      error,
      showPasswordToggle = false,
      onTogglePassword,
      isPasswordVisible = false,
      className = "",
      value,
      onFocus,
      onBlur,
      onChange,
      ...props
    },
    ref
  ) => {
    const [isFocused, setIsFocused] = useState(false);
    const [hasValue, setHasValue] = useState(false);

    // value가 변경될 때 hasValue 업데이트
    useEffect(() => {
      const hasValue = !!(
        value &&
        typeof value === "string" &&
        value.length > 0
      );
      setHasValue(hasValue);
    }, [value]);

    // 포커스 핸들러
    const handleFocus = (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(true);
      onFocus?.(e);
    };

    // 블러 핸들러
    const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
      setIsFocused(false);
      onBlur?.(e);
    };

    // 변경 핸들러
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const hasValue = e.target.value.length > 0;
      setHasValue(hasValue);
      onChange?.(e);
    };

    // 상태별 스타일 결정
    const getInputStyles = () => {
      // 에러 상태
      if (error) {
        return "bg-gray-100 border-[1.5px] border-red-500 text-gray-700";
      }

      // 포커스 중이면 타이핑 상태
      if (isFocused) {
        return "bg-gray-0 border-[1.5px] border-gray-400 text-gray-800 shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)]";
      }

      // 값이 있으면 완료 상태
      if (hasValue) {
        return "bg-gray-100 text-gray-600";
      }

      // 기본 상태
      return "bg-gray-100 text-gray-800";
    };

    // 비밀번호 토글이 있을 때 오른쪽 패딩 조정
    const getPaddingStyles = () => {
      return showPasswordToggle ? "px-[20px] pr-[56px]" : "px-[20px]";
    };

    return (
      <div className="flex flex-col">
        {label && (
          <label className="text-body3 font-semibold text-gray-500 mb-[10px]">
            {label}
          </label>
        )}
        <div className="relative">
          <input
            ref={ref}
            className={`w-full ${getPaddingStyles()} py-[13.5px] rounded-[100px] text-body2 font-medium placeholder:text-gray-400 !outline-none focus:!outline-none focus:!ring-0 focus:!ring-offset-0 focus:!shadow-none transition-all duration-200 ${getInputStyles()} ${className}`}
            onFocus={handleFocus}
            onBlur={handleBlur}
            onChange={handleChange}
            value={value}
            {...props}
          />
          {showPasswordToggle && (
            <button
              type="button"
              onClick={onTogglePassword}
              className="absolute right-[20px] top-1/2 transform -translate-y-1/2"
            >
              {isPasswordVisible ? (
                <img
                  src={getImagePath("/icon/ic_eye_open.svg")}
                  alt="비밀번호 보기"
                  width={24}
                  height={24}
                />
              ) : (
                <img
                  src={getImagePath("/icon/ic_eye_close.svg")}
                  alt="비밀번호 가리기"
                  width={24}
                  height={24}
                />
              )}
            </button>
          )}
        </div>
        {error && (
          <p className="text-body3 font-medium text-red-500 px-[8px] mt-[6px]">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = "Input";

export default Input;
