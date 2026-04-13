import clsx from "clsx";
import { useNavigate } from "react-router-dom";
import getImagePath from "@/constants/images.ts";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div
      className={clsx(
        "flex flex-col text-center items-center px-10 my-[200px]",
        "max-md:my-0 max-md:justify-center"
      )}
    >
      <img
        src={getImagePath("/common/notFound.png")}
        alt="NotFound"
        width={300}
        height={100}
      />
      <p className={clsx("text-[26px] mt-[-50px] font-semibold text-gray-800")}>
        페이지가 없거나 접근할 수 없어요
        <span
          className={clsx(
            "block font-normal text-[18px] text-gray-400 mt-[5px]"
          )}
        >
          경로를 다시 확인해주세요
        </span>
      </p>
      <button
        type="button"
        onClick={() => navigate("/")}
        className={clsx(
          "mt-[40px] h-12 rounded-full px-6 bg-gray100 transition duration-[.2s] font-medium text-[18px] bg-gray-900 text-white",
          "hover:bg-gray-700 hover:text-gray-100"
        )}
      >
        홈으로 돌아가기
      </button>
    </div>
  );
}
