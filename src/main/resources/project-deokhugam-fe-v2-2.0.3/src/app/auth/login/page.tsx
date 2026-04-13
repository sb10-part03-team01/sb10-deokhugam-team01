import {useState, useEffect} from "react";
import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {loginSchema, type LoginFormData} from "@/schemas/authSchema.ts";
import {useAuthStore} from "@/store/authStore.ts";
import {useTooltipStore} from "@/store/tooltipStore.ts";
import Input from "@/components/ui/Input.tsx";
import {Link} from "react-router-dom";
import {useNavigate} from "react-router-dom";
import Button from "@/components/common/Buttons/Button.tsx";
import getImagePath from "@/constants/images.ts";

export default function LoginPage() {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const {login, isLoading, user, clearError} = useAuthStore();
  const {showTooltip} = useTooltipStore();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: {errors, isSubmitting, isValid},
    watch
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: "onChange"
  });

  const emailValue = watch("email") || "";
  const passwordValue = watch("password") || "";

  const togglePasswordVisibility = () => {
    setIsPasswordVisible(!isPasswordVisible);
  };

  useEffect(() => {
    if (user?.id) {
      navigate("/");
    }
  }, [user?.id, navigate]);

  useEffect(() => {
    clearError();
  }, [clearError]);

  const onSubmit = async (data: LoginFormData) => {
    try {
      await login(data.email, data.password);
    } catch (error) {
      console.error("로그인 실패:", error);

      if (error instanceof Error) {
        if (error.message.includes("이메일 또는 비밀번호가 불일치")) {
          showTooltip(
              "이메일 또는 비밀번호가 불일치합니다.",
              getImagePath("/icon/ic_exclamation-circle.svg")
          );
        } else if (error.message.includes("이메일 또는 비밀번호를 확인")) {
          showTooltip(
              "이메일 또는 비밀번호를 확인해주세요.",
              getImagePath("/icon/ic_exclamation-circle.svg")
          );
        } else if (error.message.includes("서버 오류")) {
          showTooltip(
              "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
              getImagePath("/icon/ic_exclamation-circle.svg")
          );
        } else {
          showTooltip(error.message, getImagePath("/icon/ic_exclamation-circle.svg"));
        }
      } else {
        showTooltip(
            "로그인에 실패했습니다.",
            getImagePath("/icon/ic_exclamation-circle.svg")
        );
      }
    }
  };

  return (
      <div className="min-h-screen bg-gray-0 flex items-center justify-center p-4">
        <div className="w-full max-w-[400px]">
          <div className="mb-[14px] text-center">
            <img
                src={getImagePath("/logo/logo_symbol.png")}
                alt="로고"
                width={136}
                height={98}
                className="mx-auto"
            />
          </div>

          <div className="mb-8 text-center">
            <h1 className="text-header1 font-bold text-[#181D27] mb-[10px]">
              다시 만나서 반갑습니다!
            </h1>
            <p className="text-body2 font-medium text-gray-500">
              덕후감에 로그인해주세요
            </p>
          </div>

          <form
              onSubmit={handleSubmit(onSubmit)}
              className="flex flex-col gap-5 mb-6"
          >
            <div>
              <Input
                  type="email"
                  label="이메일"
                  placeholder="이메일을 입력해주세요"
                  value={emailValue}
                  onChange={e => {
                    register("email").onChange(e);
                  }}
                  onBlur={e => {
                    register("email").onBlur(e);
                  }}
                  name={register("email").name}
                  ref={register("email").ref}
                  error={errors.email?.message}
              />
            </div>

            <div>
              <Input
                  type={isPasswordVisible ? "text" : "password"}
                  label="비밀번호"
                  placeholder="비밀번호를 입력해주세요"
                  showPasswordToggle={true}
                  onTogglePassword={togglePasswordVisibility}
                  isPasswordVisible={isPasswordVisible}
                  value={passwordValue}
                  onChange={e => {
                    register("password").onChange(e);
                  }}
                  onBlur={e => {
                    register("password").onBlur(e);
                  }}
                  name={register("password").name}
                  ref={register("password").ref}
                  error={errors.password?.message}
              />
            </div>

            <Button
                type="submit"
                variant="primary"
                className="w-full mt-1"
                disabled={!isValid || isSubmitting || isLoading}
            >
              {isSubmitting || isLoading ? "로그인 중..." : "로그인"}
            </Button>
          </form>

          <div className="flex items-center justify-center gap-1">
          <span className="text-body3 font-medium text-gray-500">
            계정이 없으신가요?
          </span>
            <Link
                to="/signup"
                className="text-body3 font-semibold text-gray-700 underline decoration-solid"
            >
              회원가입
            </Link>
          </div>
        </div>
      </div>
  );
}
