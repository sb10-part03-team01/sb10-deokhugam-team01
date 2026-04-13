import {
  Control,
  FieldValues,
  FormState,
  UseFormRegister,
  UseFormSetError,
  UseFormSetValue,
  UseFormWatch
} from "react-hook-form";
import {
  errorTextStyle,
  fieldWrap,
  inputStyle,
  labelStyle,
  textareaStyle
} from "../../styles.ts";
import clsx from "clsx";
import CalendarForm from "./CalendarForm.tsx";
import Input from "@/components/ui/Input.tsx";
import {Dispatch, SetStateAction, useRef} from "react";
import {getOcr, getIsbn} from "@/api/books.ts";
import {BookFormValues} from "@/schemas/bookFormSchema.ts";
import getImagePath from "@/constants/images.ts";

type FormMethodsSubset<T extends FieldValues> = {
  register: UseFormRegister<T>;
  control: Control<T>;
  setValue: UseFormSetValue<T>;
  setError: UseFormSetError<T>;
  formState: FormState<T>;
  watch: UseFormWatch<T>;
};

export default function FormInputsContainer({
                                              isEdit,
                                              formMethods,
                                              isFocusDisabled,
                                              setIsFetchIsbnLoading,
                                              isSubmitting,
                                              setImageFile
                                            }: {
  isEdit: boolean;
  formMethods: FormMethodsSubset<BookFormValues>;
  isFocusDisabled: boolean;
  setIsFetchIsbnLoading: Dispatch<SetStateAction<boolean>>;
  isSubmitting: boolean;
  setImageFile: Dispatch<SetStateAction<File | null>>;
}) {
  const {register, control, setValue, setError, formState, watch} =
      formMethods;

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const {errors} = formState;
  const isbnValue = watch("isbn");

  const isbnDisalbed = isFocusDisabled || isSubmitting || isEdit;

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleFetchISBN = async () => {
    const file = fileInputRef.current?.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("image", file);

    setIsFetchIsbnLoading(true);
    try {
      const response = await getOcr(formData);

      setValue("isbn", String(response), {
        shouldValidate: true,
        shouldDirty: true
      });
    } catch (error) {
      setError("isbn", {
        type: "manual",
        message:
            "ISBN 인식에 실패했습니다. 올바른 바코드 이미지를 선택해주세요."
      });

      setValue("isbn", "");
      console.error("ISBN 인식 에러:", error);
    } finally {
      setIsFetchIsbnLoading(false);
    }
  };

  const handleFetchBookInfo = async () => {
    if (!isbnValue) return;

    setIsFetchIsbnLoading(true);
    try {
      const response = await getIsbn(isbnValue);

      setValue("title", response.title, {
        shouldValidate: true,
        shouldDirty: true
      });
      setValue("author", response.author, {
        shouldValidate: true,
        shouldDirty: true
      });
      setValue("publisher", response.publisher, {
        shouldValidate: true,
        shouldDirty: true
      });
      setValue("publishedDate", response.publishedDate, {
        shouldValidate: true,
        shouldDirty: true
      });
      setValue("description", response.description, {
        shouldValidate: true,
        shouldDirty: true
      });

      // 썸네일 이미지가 있는 경우 File 객체로 변환
      if (response.thumbnailImage) {
        // Base64 이미지 데이터를 URL로 변환
        const imageUrl = `data:image/jpeg;base64,${response.thumbnailImage}`;

        // URL을 File 객체로 변환
        try {
          const imageResponse = await fetch(imageUrl);
          const blob = await imageResponse.blob();
          const file = new File([blob], "book-cover.jpg", {
            type: "image/jpeg"
          });
          setImageFile(file);
        } catch (error) {
          console.error("이미지 변환 중 오류가 발생했습니다:", error);
          setImageFile(null);
        }
      } else {
        setImageFile(null);
      }
    } catch (error) {
      setError("isbn", {
        type: "manual",
        message: "도서 정보를 불러오는데 실패했습니다. ISBN을 확인해주세요."
      });
      console.error("도서 정보 조회 에러:", error);
    } finally {
      setIsFetchIsbnLoading(false);
    }
  };

  return (
      <div className="flex-[2] flex flex-col gap-y-[30px]">
        <div className={clsx(fieldWrap)}>
          <label {...(!isEdit && {htmlFor: "ISBN"})} className={labelStyle}>
            ISBN
          </label>
          <div className="flex items-center">
            <Input
                id="ISBN-input"
                type="text"
                {...register("isbn")}
                placeholder="ISBN를 입력해주세요"
                disabled={isbnDisalbed}
                className={clsx(
                    "max-w-[440px] font-medium flex-[1]",
                    inputStyle,
                    errors.isbn && "border-red-500"
                )}
            />
            <Input
                ref={fileInputRef}
                id="ISBN"
                type="file"
                className="hidden"
                accept=".jpg, .jpeg, .png"
                onChange={handleFetchISBN}
            />
            {!isEdit && (
                <>
                  <button
                      type="button"
                      onClick={handleClick}
                      disabled={isbnDisalbed}
                      className={clsx(
                          "ml-3 px-5 rounded-full border border-gray-300 h-[54px] font-medium text-gray-600 min-w-[128px] duration-[.2s] flex items-center justify-center gap-2",
                          !isbnDisalbed && "hover:bg-gray-50"
                      )}
                  >
                    <img
                        src={getImagePath("/icon/ic_photo.svg")}
                        alt="Photo"
                        width={18}
                        height={18}
                    />
                    이미지로 ISBN 입력하기
                  </button>
                  <button
                      type="button"
                      onClick={handleFetchBookInfo}
                      disabled={isbnDisalbed || !isbnValue}
                      className={clsx(
                          "ml-3 mr-[18px] px-5 rounded-full border h-[54px] font-medium min-w-[128px] duration-[.2s]",
                          isbnDisalbed || !isbnValue
                              ? "border-gray-200 text-gray-400 cursor-not-allowed"
                              : "border-gray-300 text-gray-600 hover:bg-gray-50"
                      )}
                  >
                    {isFocusDisabled ? (
                        <div
                            className="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-500 mx-auto"/>
                    ) : (
                        "정보 불러오기"
                    )}
                  </button>
                </>
            )}
          </div>
          {errors.isbn && <p className={errorTextStyle}>{errors.isbn.message}</p>}
        </div>
        <div className={fieldWrap}>
          <label htmlFor="title" className={labelStyle}>
            제목
          </label>
          <Input
              id="title"
              type="text"
              maxLength={150}
              {...register("title")}
              placeholder="책 제목을 입력해주세요"
              className={clsx(inputStyle, errors.title && "border-red-500")}
          />
          {errors.title && (
              <p className={errorTextStyle}>{errors.title.message}</p>
          )}
        </div>
        <div className={fieldWrap}>
          <label htmlFor="author" className={labelStyle}>
            저자
          </label>
          <Input
              id="author"
              type="text"
              maxLength={50}
              {...register("author")}
              placeholder="지은이를 입력해주세요"
              className={clsx(inputStyle, errors.author && "border-red-500")}
          />
          {errors.author && (
              <p className={errorTextStyle}>{errors.author.message}</p>
          )}
        </div>
        <div className="flex gap-5 w-full">
          <div className="flex-[1] flex flex-col">
            <label htmlFor="publisher" className={labelStyle}>
              출판사
            </label>
            <Input
                id="publisher"
                type="text"
                maxLength={50}
                {...register("publisher")}
                placeholder="출판사를 입력해주세요"
                className={clsx(inputStyle, errors.publisher && "border-red-500")}
            />
            {errors.publisher && (
                <p className={errorTextStyle}>{errors.publisher.message}</p>
            )}
          </div>
          <div className="flex-[1] flex flex-col">
            <label htmlFor="publishedDate" className={labelStyle}>
              출판년도
            </label>
            <CalendarForm control={control} errors={errors}/>
          </div>
        </div>
        <div className={clsx(fieldWrap, "mb-[30px]")}>
          <label htmlFor="description" className={labelStyle}>
            설명
          </label>
          <textarea
              id="description"
              placeholder="책에 대한 설명을 입력해주세요"
              maxLength={1000}
              {...register("description")}
              className={clsx(
                  textareaStyle,
                  errors.description
                      ? "focus:border-red-500 border-red-500"
                      : "border-gray-100"
              )}
          />
          {errors.description && (
              <p className={errorTextStyle}>{errors.description.message}</p>
          )}
        </div>
      </div>
  );
}
