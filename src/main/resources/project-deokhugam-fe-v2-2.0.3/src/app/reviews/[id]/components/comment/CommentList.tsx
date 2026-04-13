import CommentItem from "./CommentItem.tsx";
import type {Comment} from "@/types/reviews.ts";

interface CommentListProps {
  comments: Comment[];
  reviewId: string;
  setData: React.Dispatch<React.SetStateAction<Comment[]>>;
  onCommentUpdate?: (updatedComment: Comment) => void;
  onCommentCountChange?: (count: number) => void;
}

export default function CommentList({
                                      comments,
                                      reviewId,
                                      setData,
                                      onCommentUpdate,
                                      onCommentCountChange
                                    }: CommentListProps) {
  if (comments.length === 0) {
    return (
        <div>
          <p className="text-body2 font-medium text-gray-400 text-center">
            등록된 댓글이 없습니다.
          </p>
        </div>
    );
  }

  return (
      <div>
        {comments.map(comment => (
            <CommentItem
                key={comment.id}
                comment={comment}
                data={comments}
                setData={setData}
                reviewId={reviewId}
                onCommentUpdate={onCommentUpdate}
                onCommentCountChange={onCommentCountChange}
            />
        ))}
      </div>
  );
}
