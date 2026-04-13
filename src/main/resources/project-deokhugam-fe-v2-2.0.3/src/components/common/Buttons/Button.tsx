import { ButtonHTMLAttributes, ReactNode } from "react";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "icon";
  children: ReactNode;
}

export default function Button({
  variant = "primary",
  children,
  className = "",
  disabled = false,
  ...props
}: ButtonProps) {
  const baseClasses =
    "inline-flex items-center justify-center font-medium transition-colors gap-1";

  const variantClasses = {
    // 1. Primary 버튼 (회색 배경)
    primary: {
      normal: "bg-gray-900 text-gray-0 text-body2 font-medium",
      hover: "hover:bg-gray-700 hover:text-gray-0",
      disabled:
        "disabled:bg-gray-500 disabled:border-gray-500 disabled:text-white"
    },
    // 2. Secondary 버튼 (흰색 배경 + border + shadow)
    secondary: {
      normal:
        "bg-gray-0 text-gray-600 border border-gray-300 shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)] text-body2 font-medium",
      hover: "hover:bg-gray-100 hover:text-gray-600",
      disabled: "disabled:bg-gray-0 disabled:text-gray-300"
    },
    // 3. Outline 버튼 (흰색 배경)
    outline: {
      normal:
        "bg-gray-0 text-gray-600 text-body2 font-medium border border-gray-300",
      hover: "hover:bg-gray-100 hover:text-gray-600",
      disabled: "disabled:bg-gray-0 disabled:text-gray-300"
    },
    // 4. Icon 버튼 (원형)
    icon: {
      normal: "bg-gray-a-800",
      hover: "hover:bg-gray-a-500",
      disabled: "disabled:bg-gray-a-800"
    }
  };

  const sizeClasses = {
    primary: "px-[18px] py-[14px] rounded-[100px]",
    secondary: "px-[18px] py-[14px] rounded-[100px]",
    outline: "px-[18px] py-[14px] rounded-[100px]",
    icon: "p-[14px] rounded-[100px] border border-white-a-10"
  };

  const currentVariant = variantClasses[variant];
  const currentSize = sizeClasses[variant];

  return (
    <button
      className={`
        ${baseClasses} 
        ${currentSize}
        ${currentVariant.normal} 
        ${currentVariant.hover} 
        ${currentVariant.disabled}
        ${className}
      `}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
}
