import z from "zod";

export const bookFormSchema = z.object({
  isbn: z.string().min(1, "ISBN 정보를 불러와주세요."),
  title: z.string().min(1, "제목을 입력해주세요.").max(150, "글자수를 150자 이하로 입력해주세요."),
  author: z.string().min(1, "저자를 입력해주세요.").max(50, "글자수를 50자 이하로 입력해주세요."),
  publisher: z.string().min(1, "출판사를 입력해주세요.").max(50, "글자수를 50자 이하로 입력해주세요."),
  publishedDate: z.string().min(1, "출판일을 선택해주세요."),
  description: z.string().min(1, "설명을 입력해주세요.").max(1000, "글자수를 1000자 이하로 입력해주세요."),
  thumbnailImage: z.any().optional(),
});

export type BookFormValues = z.infer<typeof bookFormSchema>;
