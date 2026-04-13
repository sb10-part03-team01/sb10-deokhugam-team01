interface CommentHeaderProps {
  commentCount?: number;
}

export default function CommentHeader({ commentCount }: CommentHeaderProps) {
  return (
    <div className="flex items-center gap-[4px]">
      <h2 className="text-body1 font-semibold text-gray-900">댓글</h2>
      {commentCount !== undefined && commentCount > 0 && (
        <span className="text-body1 font-semibold text-gray-500">
          {commentCount}
        </span>
      )}
    </div>
  );
}
