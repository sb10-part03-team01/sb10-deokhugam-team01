import { ReactNode } from "react";
import { motion, AnimatePresence } from "framer-motion";
import clsx from "clsx";
import { createPortal } from "react-dom";

interface ModalProps {
  isDelete?: boolean;
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
  disabled: boolean;
  action: () => void;
  buttonText: string;
}

export default function Modal({
  isDelete,
  isOpen,
  onClose,
  children,
  disabled,
  action,
  buttonText
}: ModalProps) {
  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <motion.div
          className="fixed inset-0 px-5 z-50 flex items-center justify-center bg-black/50"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={onClose}
        >
          <motion.div
            className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-lg"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.9, opacity: 0 }}
            onClick={e => e.stopPropagation()}
          >
            {children}
            <div className="flex justify-end mt-5 gap-3 font-medium">
              <button
                onClick={onClose}
                className="h-[46px] px-[18px] rounded-full border border-solid border-gray-300 box-border"
              >
                취소
              </button>
              <button
                className={clsx(
                  "h-[46px] px-[18px] rounded-full bg-gray-900 text-white",
                  "disabled:bg-gray-500",
                  isDelete && "bg-red-500"
                )}
                disabled={disabled}
                onClick={action}
              >
                {buttonText}
              </button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>,

    document.body
  );
}
