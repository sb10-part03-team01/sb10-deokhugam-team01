import DatePicker, {registerLocale} from "react-datepicker";
import {Control, Controller, FieldErrors} from "react-hook-form";
import {dateStyle, errorTextStyle} from "../../styles.ts";
import {ko} from "date-fns/locale";
import {BookFormValues} from "@/schemas/bookFormSchema.ts";
import clsx from "clsx";
import getImagePath from "@/constants/images.ts";

export default function CalendarForm({
                                       control,
                                       errors
                                     }: {
  control: Control<BookFormValues>;
  errors: FieldErrors<BookFormValues>;
}) {
  registerLocale("ko", ko);

  return (
      <>
        <div className="relative">
          <Controller
              name="publishedDate"
              control={control}
              render={({field}) => (
                  <DatePicker
                      {...field}
                      selected={field.value ? new Date(field.value) : null}
                      onChange={date =>
                          field.onChange(date ? date.toISOString().split("T")[0] : "")
                      }
                      dateFormat="yyyy-MM-dd"
                      locale="ko"
                      placeholderText="출판일을 입력해주세요"
                      className={clsx(
                          dateStyle,
                          errors.publishedDate
                              ? "focus:border-red-500 border-red-500"
                              : "border-gray-100"
                      )}
                      renderCustomHeader={({date, decreaseMonth, increaseMonth}) => (
                          <div className="flex justify-between items-center px-2 py-1">
                            <button onClick={decreaseMonth}>
                              <img
                                  src={getImagePath("/icon/u_angle-left-b.svg")}
                                  alt="<"
                                  width={24}
                                  height={24}
                              />
                            </button>
                            <div className="font-medium text-base">
                              {date.getFullYear()}년 {date.getMonth() + 1}월
                            </div>
                            <button onClick={increaseMonth}>
                              <img
                                  src={getImagePath("/icon/u_angle-right-b.svg")}
                                  alt=">"
                                  width={24}
                                  height={24}
                              />
                            </button>
                          </div>
                      )}
                  />
              )}
          />

          <img
              src={getImagePath("/icon/ic_calendar.svg")}
              alt="calendar"
              width={24}
              height={24}
              className="absolute right-1 top-1/4 -translate-x-1/2"
          />
        </div>
        {errors.publishedDate && (
            <p className={errorTextStyle}>{errors.publishedDate.message}</p>
        )}
      </>
  );
}
