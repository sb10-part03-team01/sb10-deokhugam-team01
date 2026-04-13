import {useAuthGuard} from "@/hooks/auth/useAuthRedirect.ts";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import {useParams} from "react-router-dom";
import BookThumbnail from "./components/overview/BookThumbnail.tsx";
import BookInfo from "./components/overview/BookInfo.tsx";
import OverviewContainer from "./components/overview/OverviewContainer.tsx";
import ReviewContainer from "./components/review/ReviewContainer.tsx";
import ReviewForm from "./components/review/ReviewForm.tsx";
import ReviewList from "./components/review/ReviewList.tsx";
import clsx from "clsx";

export default function BookDetailPage() {
  const {id: paramsId} = useParams()
  const id = String(paramsId);

  const {shouldShowContent} = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen/>;
  }

  return (
      <div
          className={clsx(
              "pt-[50px] pb-[150px] h-[inherit] min-h-[inherit] flex flex-col gap-[40px]",
              "max-lg900:pt-0"
          )}
      >
        <OverviewContainer id={id}>
          {({data}) => (
              <>
                <BookThumbnail data={data}/>
                <BookInfo id={id} data={data}/>
              </>
          )}
        </OverviewContainer>
        <ReviewContainer id={id}>
          {({data, setData, isLoading, totalElements, setTotalElements}) => (
              <>
                <ReviewForm
                    data={data}
                    setData={setData}
                    totalElements={totalElements}
                    setTotalElements={setTotalElements}
                    bookId={id}
                />
                <ReviewList
                    data={data}
                    setData={setData}
                    isLoading={isLoading}
                    setTotalElements={setTotalElements}
                    bookId={id}
                />
              </>
          )}
        </ReviewContainer>
      </div>
  );
}
