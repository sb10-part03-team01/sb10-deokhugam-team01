import {useTooltipStore} from "@/store/tooltipStore.ts";
import clsx from "clsx";

export default function Tooltip() {
  const {isVisible, content, icon} = useTooltipStore();

  return (
      <div
          className={clsx(
              "fixed left-1/2 transform -translate-x-1/2 bg-gray-a-800/80 text-white px-5 py-3 rounded-[16px] text-sm transition-opacity duration-150 font-medium flex items-center gap-2",
              isVisible ? "opacity-100" : "opacity-0 pointer-events-none"
          )}
          style={{
            bottom: `calc(40px + env(safe-area-inset-bottom))`
          }}
      >
        {icon && <img src={icon} alt="Check" width={20} height={20}/>}
        {content}
      </div>
  );
}
