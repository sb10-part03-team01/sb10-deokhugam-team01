import clsx from "clsx";
import {useNavigate} from "react-router-dom";

export default function ButtonContainer({
  id,
  isSubmitDisabled: disabled,
  isSubmitting,
  isEdit
}: {
  id?: string;
  isSubmitDisabled: boolean;
  isSubmitting: boolean;
  isEdit: boolean;
}) {
  const navigate = useNavigate();

  return (
    <div className="flex items-center justify-end gap-3">
      <button
        type="button"
        className="border border-gray-300 bg-white h-[46px] px-[18px] rounded-full text-gray-600 font-medium"
        onClick={() =>
          isEdit ? navigate(`/books/${id}`) : navigate("/books")
        }
      >
        취소
      </button>
      <button
        className={clsx(
          "border h-[46px] px-[18px] rounded-full text-white font-medium min-w-[65.66px]",
          disabled
            ? "bg-gray-500 border-gray-500"
            : "border-gray-900 bg-gray-900"
        )}
        disabled={disabled}
      >
        {isSubmitting ? (
          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-50 mx-auto" />
        ) : isEdit ? (
          "수정"
        ) : (
          "등록"
        )}
      </button>
    </div>
  );
}
