import getImagePath from "@/constants/images.ts";

export default function InfiniteScrollLoader() {
  return (
    <div className="fixed top-0 left-0 right-0 bottom-0 z-[100] bg-white/30 flex items-center justify-center">
      <div className="text-center">
        <img
          src={getImagePath("/common/dataLoader.gif")}
          alt="Loading..."
          width={100}
          height={100}
        />
      </div>
    </div>
  );
}
