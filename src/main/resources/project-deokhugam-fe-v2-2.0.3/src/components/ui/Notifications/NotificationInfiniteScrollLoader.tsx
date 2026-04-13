import getImagePath from "@/constants/images.ts";

export default function NotificationInfiniteScrollLoader() {
  return (
    <div className="flex items-center justify-center py-4">
      <div className="text-center">
        <img
          src={getImagePath("/notification/scroll_Loading.gif")}
          alt="알림 로딩 중..."
          width={100}
          height={100}
        />
      </div>
    </div>
  );
}
