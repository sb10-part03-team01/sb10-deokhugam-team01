import { HTMLAttributes, ReactNode } from "react";

interface LabelProps extends HTMLAttributes<HTMLSpanElement> {
  children: ReactNode;
}

export default function Label({
  children,
  className = "",
  ...props
}: LabelProps) {
  const baseClasses =
    "inline-flex items-center justify-center bg-blue-100 text-body3 font-medium text-blue-500 px-[6px] py-[4px] rounded-[4px]";

  return (
    <span className={`${baseClasses} ${className}`} {...props}>
      {children}
    </span>
  );
}
