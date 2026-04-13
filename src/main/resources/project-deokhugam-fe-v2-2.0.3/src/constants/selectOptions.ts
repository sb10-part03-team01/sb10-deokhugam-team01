export const BOOKS_ORDERBY = [
  { value: "title", label: "제목순" },
  { value: "publishedDate", label: "출판일순" },
  { value: "rating", label: "평점순" },
  { value: "reviewCount", label: "리뷰순" }
] as const;

export const REVEWS_ORDERBY = [
  { value: "createdAt", label: "시간순" },
  { value: "rating", label: "평점순" }
] as const;

export const SORT_DIRECTION = [
  { value: "DESC", label: "내림차순" },
  { value: "ASC", label: "오름차순" }
] as const;
