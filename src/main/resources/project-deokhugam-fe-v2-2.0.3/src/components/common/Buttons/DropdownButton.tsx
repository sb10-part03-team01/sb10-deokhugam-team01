import { ButtonHTMLAttributes, ReactNode } from "react";

interface DropdownButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
}

export default function DropdownButton({
  children,
  className = "",
  ...props
}: DropdownButtonProps) {
  const baseClasses =
    "inline-flex items-center justify-center gap-1 bg-gray-0 text-body2 font-medium text-gray-600 border border-gray-300 shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)] px-[18px] py-[12.5px] rounded-[100px]";

  return (
    <button className={`${baseClasses} ${className}`} {...props}>
      {children}
    </button>
  );
}
