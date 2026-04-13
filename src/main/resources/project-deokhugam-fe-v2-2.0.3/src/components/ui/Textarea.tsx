import {
  forwardRef,
  TextareaHTMLAttributes,
  memo,
  useRef,
  useEffect,
  useState
} from "react";
import clsx from "clsx";

interface TextareaProps
  extends Omit<
    TextareaHTMLAttributes<HTMLTextAreaElement>,
    "onChange" | "value"
  > {
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
}

const TextareaBase = forwardRef<HTMLTextAreaElement, TextareaProps>(
  function TextareaBase(
    {
      placeholder = "내용을 입력해주세요",
      value,
      onChange,
      className = "",
      ...props
    },
    ref
  ) {
    const [isFocused, setIsFocused] = useState(false);

    const onChangeRef = useRef(onChange);
    const isControlledRef = useRef(value !== undefined);
    useEffect(() => {
      onChangeRef.current = onChange;
      isControlledRef.current = value !== undefined;
    }, [onChange, value]);

    const internalValueRef = useRef("");

    const hasValue =
      (isControlledRef.current
        ? (value ?? "")
        : (internalValueRef.current ?? "")
      ).length > 0;

    const handleFocus = () => setIsFocused(true);
    const handleBlur = () => setIsFocused(false);

    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      const newValue = e.target.value;
      if (!isControlledRef.current) {
        internalValueRef.current = newValue;
      }
      onChangeRef.current?.(newValue);
    };

    const containerStyles = clsx(
      "w-full py-[20px] px-[20px] rounded-[14px] text-body2 font-medium resize-none !outline-none transition-all duration-200",
      isFocused
        ? "bg-white border-[1.5px] border-gray-400 text-gray-800"
        : hasValue
          ? "bg-gray-100 border-[1.5px] border-transparent text-gray-600"
          : "bg-gray-100 border-[1.5px] border-transparent text-gray-400",
      className
    );

    return (
      <textarea
        ref={ref}
        defaultValue={
          !isControlledRef.current ? internalValueRef.current : undefined
        }
        value={isControlledRef.current ? value : undefined}
        placeholder={isFocused ? "" : placeholder}
        onFocus={handleFocus}
        onBlur={handleBlur}
        onChange={handleChange}
        className={containerStyles}
        style={{
          scrollbarWidth: "thin",
          scrollbarColor: "#D7D7DB transparent"
        }}
        {...props}
      />
    );
  }
);

const Textarea = memo(TextareaBase, (prev, next) => {
  if (prev.value !== next.value) return false;
  if (prev.placeholder !== next.placeholder) return false;
  if (prev.className !== next.className) return false;
  if (prev.disabled !== next.disabled) return false;
  if (prev.readOnly !== next.readOnly) return false;
  return true;
});

Textarea.displayName = "Textarea";

export default Textarea;
