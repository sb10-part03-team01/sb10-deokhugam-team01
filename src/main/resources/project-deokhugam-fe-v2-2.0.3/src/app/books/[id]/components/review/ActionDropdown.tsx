import ActionMenu from "@/components/common/ActionMenu.tsx";
import {useClickOutside} from "@/hooks/common/useClickOutside.ts";
import {Dispatch, SetStateAction} from "react";
import getImagePath from "@/constants/images.ts";

export default function ActionDropdown({
                                         showModal,
                                         reviewId,
                                         setReviewId,
                                         setIsEdit
                                       }: {
  showModal: () => void;
  reviewId: string;
  setReviewId: Dispatch<SetStateAction<string>>;
  setIsEdit: Dispatch<SetStateAction<boolean>>;
}) {
  const {open: showDropdown, setOpen, dropdownRef} = useClickOutside();

  const showDeleteModal = () => {
    showModal();
    setReviewId(reviewId);
  };

  const handleEdit = () => {
    setOpen(false);
    setIsEdit(true);
  };

  return (
      <div className="relative" ref={dropdownRef}>
        <button
            type="button"
            className="min-w-6 pt-1"
            onClick={() => setOpen(prev => !prev)}
        >
          <img
              src={getImagePath("/icon/ic_more.svg")}
              alt="더보기"
              width={24}
              height={24}
          />
        </button>
        {showDropdown && (
            <ActionMenu onEdit={handleEdit} onDelete={showDeleteModal}/>
        )}
      </div>
  );
}
