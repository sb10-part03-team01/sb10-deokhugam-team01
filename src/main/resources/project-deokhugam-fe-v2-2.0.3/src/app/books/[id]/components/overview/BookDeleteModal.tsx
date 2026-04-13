import {BookResponse, deleteBook} from "@/api/books.ts";
import Modal from "@/components/ui/Modal.tsx";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import {useNavigate} from "react-router-dom";
import {useState} from "react";

export default function BookDeleteModal({
                                          isOpen,
                                          close,
                                          data
                                        }: {
  isOpen: boolean;
  close: () => void;
  data: BookResponse | null;
}) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const {showTooltip} = useTooltipStore();

  const handleDeleteBook = async () => {
    setIsSubmitting(true);
    try {
      await deleteBook(String(data?.id));

      close();
      navigate("/books");
      showTooltip("도서를 정상적으로 삭제하였습니다!");
    } catch (error) {
      console.error("도서 삭제 실패:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <Modal
          isDelete
          isOpen={isOpen}
          onClose={close}
          disabled={isSubmitting}
          buttonText="삭제"
          action={handleDeleteBook}
      >
        <h2 className="text-lg font-semibold mb-5">도서 삭제</h2>
        <p className="font-medium">
          정말 &apos;
          <span className="inline-block max-w-[300px] align-bottom truncate">
          {data?.title}
        </span>
          &apos;를 삭제하시겠습니까?
        </p>
      </Modal>
  );
}
