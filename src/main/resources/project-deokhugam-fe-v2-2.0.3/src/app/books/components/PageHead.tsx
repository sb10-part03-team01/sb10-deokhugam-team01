import {useNavigate} from "react-router-dom";
import getImagePath from "@/constants/images.ts";

export default function PageHead() {
  const navigate = useNavigate();

  return (
    <div className="flex justify-between items-center mb-5">
      <p className="text-header1 font-bold">도서 리스트 둘러보기</p>
      <button
        className="flex items-center gap-1 bg-gray-900 text-white rounded-full px-[18px] py-3 font-medium"
        onClick={() => navigate("/books/add")}
      >
        <img src={getImagePath("/icon/ic_plus.svg")} alt="+" width={18} height={18} />{" "}
        도서 등록
      </button>
    </div>
  );
}
