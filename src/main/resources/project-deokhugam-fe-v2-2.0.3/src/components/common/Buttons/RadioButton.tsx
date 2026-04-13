import { ButtonHTMLAttributes, ReactNode } from "react";

interface RadioButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "selected" | "unselected";
  children: ReactNode;
}

export default function RadioButton({
  variant = "unselected",
  children,
  className = "",
  ...props
}: RadioButtonProps) {
  const baseClasses = "inline-flex items-center justify-center";

  const variantClasses = {
    // 1. Selected 버튼
    selected: "bg-gray-900 text-body3 font-semibold text-gray-0",
    // 2. Unselected 버튼
    unselected:
      "bg-gray-0 text-body3 font-medium text-gray-600 border border-gray-300 shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)]"
  };

  const sizeClasses = "px-[14px] py-[8.5px] rounded-[100px]";

  return (
    <button
      className={`
        ${baseClasses} 
        ${sizeClasses}
        ${variantClasses[variant]}
        ${className}
      `}
      {...props}
    >
      {children}
    </button>
  );
}
