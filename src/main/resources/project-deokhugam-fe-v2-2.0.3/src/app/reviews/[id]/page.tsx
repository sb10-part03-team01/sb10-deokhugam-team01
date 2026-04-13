import {useState} from "react";
import {useParams} from "react-router-dom";
import {useAuthGuard} from "@/hooks/auth/useAuthRedirect.ts";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import ReviewHeader from "./components/ReviewHeader.tsx";
import CommentSection from "./components/CommentSection.tsx";

export default function ReviewDetailPage() {
  const {shouldShowContent} = useAuthGuard();
  const {id} = useParams();
  const reviewId = String(id);
  const [commentCount, setCommentCount] = useState<number | undefined>(
      undefined
  );

  if (!shouldShowContent) {
    return <LoadingScreen/>;
  }

  return (
      <div className="pt-[50px] pb-[80px] h-[inherit] min-h-[inherit] flex flex-col gap-[40px]">
        <ReviewHeader reviewId={reviewId} commentCount={commentCount}/>
        <CommentSection
            reviewId={reviewId}
            onCommentCountChange={setCommentCount}
        />
      </div>
  );
}
