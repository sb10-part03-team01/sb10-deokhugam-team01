import clsx from "clsx";

export const labelStyle = "text-gray-500 font-semibold mb-[10px]";

export const fieldWrap = "flex flex-col";

export const inputContainer =
  "bg-gray-100 h-[54px] px-5 rounded-full align-center content-center";

export const inputStyle = clsx("!text-gray-600 border-[1.5px] border-gray-100");

const inputFocusStyle = clsx(
  "!outline-none focus:!outline-none focus:!ring-0 focus:!ring-offset-0 focus:!shadow-none transition-all duration-200",
  "focus:bg-gray-0 focus:border-gray-400 focus:text-gray-800 focus:shadow-[0px_4px_8px_0px_rgba(24,24,24,0.05)]"
);

export const dateStyle = clsx(
  "!text-gray-600 bg-gray-100 border-[1.5px] resize-none p-5 rounded-full font-medium w-full h-[54px]",
  "placeholder:text-gray-400",
  inputFocusStyle
);

export const textareaStyle = clsx(
  "!text-gray-600 bg-gray-100 border-[1.5px] resize-none p-5 rounded-xl font-medium h-[calc(100vh_*_(120/1080))]",
  "placeholder:text-gray-400",
  inputFocusStyle
);

export const errorTextStyle = clsx(
  "text-red-500 text-sm pl-5 font-medium mt-1"
);
