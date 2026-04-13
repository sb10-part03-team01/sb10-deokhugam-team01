import { z } from "zod";

// 로그인 스키마
export const loginSchema = z.object({
  email: z
    .string()
    .min(1, "이메일을 입력해주세요")
    .pipe(z.email("유효하지 않은 이메일입니다")),
  password: z
    .string()
    .min(1, "비밀번호를 입력해주세요")
    .min(8, "비밀번호는 8자 이상 입력해주세요")
});

// 회원가입 스키마
export const signupSchema = z
  .object({
    email: z
      .string()
      .min(1, "이메일을 입력해주세요")
      .pipe(z.email("유효하지 않은 이메일입니다")),
    nickname: z
      .string()
      .min(1, "닉네임을 입력해주세요")
      .min(2, "닉네임은 2자 이상 입력해주세요")
      .max(10, "닉네임은 10자 이하로 입력해주세요"),
    password: z
      .string()
      .min(1, "비밀번호를 입력해주세요")
      .min(8, "비밀번호는 8자 이상 입력해주세요")
      .regex(
        /^(?=.*[a-zA-Z])(?=.*\d)/,
        "비밀번호는 영문과 숫자를 포함해야 합니다"
      ),
    confirmPassword: z.string().min(1, "비밀번호 확인을 입력해주세요")
  })
  .refine(data => data.password === data.confirmPassword, {
    message: "비밀번호가 일치하지 않습니다",
    path: ["confirmPassword"]
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type SignupFormData = z.infer<typeof signupSchema>;
