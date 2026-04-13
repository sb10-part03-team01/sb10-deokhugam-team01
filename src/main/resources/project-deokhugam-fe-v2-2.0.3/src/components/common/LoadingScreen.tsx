import getImagePath from "@/constants/images.ts";

export default function LoadingScreen() {
  return (
    <div className="fixed top-0 left-0 right-0 bottom-0 z-[100] bg-white flex items-center justify-center">
      <div className="text-center">
        <img
          src={getImagePath("/common/pageLoader.gif")}
          alt="Loading..."
          width={100}
          height={100}
        />
        <p className="text-body2 text-gray-500">Loading...</p>
      </div>
    </div>
  );
}
