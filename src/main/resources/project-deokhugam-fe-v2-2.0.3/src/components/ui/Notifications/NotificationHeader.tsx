interface NotificationHeaderProps {
  isMarkingAllRead: boolean;
  onMarkAllAsRead: () => void;
}

export default function NotificationHeader({
  isMarkingAllRead,
  onMarkAllAsRead
}: NotificationHeaderProps) {
  return (
    <div className="relative z-10 flex justify-between items-center mb-[14px]">
      <h2 className="text-title1 font-bold text-gray-800">알림</h2>
      <button
        className="text-body3 font-medium text-gray-500 underline decoration-solid underline-offset-0 decoration-0 decoration-skip-ink-auto my-[3.5px] disabled:opacity-50"
        onClick={onMarkAllAsRead}
        disabled={isMarkingAllRead}
      >
        {isMarkingAllRead ? (
          <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-gray-500 mx-auto" />
        ) : (
          "모두 읽음"
        )}
      </button>
    </div>
  );
}
