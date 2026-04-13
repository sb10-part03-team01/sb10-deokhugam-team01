import getImagePath from "@/constants/images.ts";

export default function NotificationEmptyState() {
  return (
    <div className="absolute inset-0 w-full h-full rounded-[16px] overflow-hidden flex items-center justify-center">
      <img
        src={getImagePath('/notification/decorative_Background_pattern.png')}
        alt="배경 패턴"
        width={336}
        height={336}
        className="object-cover"
      />
    </div>
  );
}
