import clsx from "clsx";
import getImagePath from "@/constants/images.ts";

interface ActionMenuProps {
  onEdit: () => void;
  onDelete: () => void;
}

export default function ActionMenu({ onEdit, onDelete }: ActionMenuProps) {
  return (
    <div className="absolute right-0 top-10 bg-white rounded-xl border overflow-hidden z-10">
      <button
        className={clsx(
          "min-w-max py-[14.5px] px-4 flex items-center gap-[6px] text-gray-600 font-medium duration-[.2s]",
          "hover:bg-gray-50"
        )}
        onClick={onEdit}
      >
        <img
          src={getImagePath("/icon/ic_edit.svg")}
          alt="Edit"
          width={18}
          height={18}
          className="min-w-[18px]"
        />
        수정하기
      </button>
      <button
        className={clsx(
          "min-w-max py-[14.5px] px-4 flex items-center gap-[6px] text-gray-600 font-medium duration-[.2s]",
          "hover:bg-gray-50"
        )}
        onClick={onDelete}
      >
        <img
          src={getImagePath("/icon/ic_trash.svg")}
          alt="Delete"
          width={18}
          height={18}
          className="min-w-[18px]"
        />
        삭제하기
      </button>
    </div>
  );
}
