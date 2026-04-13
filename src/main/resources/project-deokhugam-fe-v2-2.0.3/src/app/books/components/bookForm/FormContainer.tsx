import {BookFormValues, bookFormSchema} from "@/schemas/bookFormSchema.ts";
import {zodResolver} from "@hookform/resolvers/zod";
import {FormProvider, useForm} from "react-hook-form";

export default function FormContainer({
                                        children,
                                        defaultValues,
                                      }: {
  children: React.ReactNode;
  defaultValues?: BookFormValues;
}) {
  const methods = useForm<BookFormValues>({
    mode: "onChange",
    resolver: zodResolver(bookFormSchema),
    defaultValues: defaultValues || {
      isbn: "",
      title: "",
      author: "",
      description: "",
      publisher: "",
      publishedDate: "",
    },
  });

  return <FormProvider {...methods}>{children}</FormProvider>;
}
