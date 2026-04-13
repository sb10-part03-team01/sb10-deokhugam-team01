import {useClickOutside} from "@/hooks/common/useClickOutside.ts";
import clsx from "clsx";
import {useState, useEffect} from "react";
import getImagePath from "@/constants/images.ts";

interface SelectOption<T> {
  value: T;
  label: string;
}

interface SelectBoxProps<T> {
  options: readonly SelectOption<T>[];
  value: T;
  onChange: (value: T) => void;
}

export default function Selectbox<T extends string>({
                                                      options,
                                                      value,
                                                      onChange
                                                    }: SelectBoxProps<T>) {
  const [buttonValue, setButtonValue] = useState(
      options.find(o => o.value === value)?.label || options[0].label
  );

  const {open, setOpen, dropdownRef} = useClickOutside();

  useEffect(() => {
    const selected = options.find(o => o.value === value);
    if (selected) setButtonValue(selected.label);
  }, [value, options]);

  return (
      <div className="relative" ref={dropdownRef}>
        <div
            onClick={() => setOpen(prev => !prev)}
            className="px-[18px] h-[46px] border border-gray-300 rounded-full flex items-center gap-1 text-gray-600 font-medium cursor-pointer"
        >
          {buttonValue}
          <img
              src={getImagePath("/icon/ic_chevron-down.svg")}
              alt="보기"
              width={18}
              height={18}
          />
        </div>

        {open && (
            <ul className="absolute left-0 mt-2 w-full bg-white border border-gray-200 rounded-2xl shadow-lg z-10 overflow-y-auto text-center text-gray-600 font-medium">
              {options.map(opt => (
                  <li
                      key={opt.value}
                      onClick={() => {
                        onChange(opt.value);
                        setButtonValue(opt.label);
                        setOpen(false);
                      }}
                      className={clsx(
                          "px-3 py-4 cursor-pointer duration-[.2s]",
                          "hover:bg-gray-100"
                      )}
                  >
                    {opt.label}
                  </li>
              ))}
            </ul>
        )}
      </div>
  );
}
