import {BookFormValues} from "@/schemas/bookFormSchema.ts";
import {Dispatch, SetStateAction, useEffect, useRef, useState} from "react";
import {UseFormSetValue} from "react-hook-form";
import getImagePath from "@/constants/images.ts";

export default function ImgUploadContainer({
                                             imageFile,
                                             setImageFile,
                                             thumbnailValue,
                                             setValue
                                           }: {
  imageFile: File | null;
  setImageFile: Dispatch<SetStateAction<File | null>>;
  thumbnailValue: string;
  setValue: UseFormSetValue<BookFormValues>;
}) {
  const [preview, setPreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImageFile(file);
    setValue("thumbnailImage", file, {shouldDirty: true});
    const url = URL.createObjectURL(file);
    setPreview(url);
  };

  const handleFileRemove = () => {
    setPreview(null);
    setImageFile(null);
    setValue("thumbnailImage", null, {shouldDirty: true});

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  useEffect(() => {
    if (thumbnailValue) {
      setPreview(thumbnailValue);
    }
  }, [thumbnailValue]);

  useEffect(() => {
    if (imageFile) {
      const url = URL.createObjectURL(imageFile);
      setPreview(url);
      setValue("thumbnailImage", imageFile, {shouldDirty: true});

      // Cleanup function to revoke the object URL
      return () => URL.revokeObjectURL(url);
    }
  }, [imageFile, setValue]);

  return (
      <div className="relative">
        {preview ? (
            <div
                className="relative w-[200px] h-[300px] rounded-[6px] overflow-hidden bg-gray-100 border border-gray-200">
              <img
                  src={preview}
                  alt="thumbnail"
                  className="w-full h-full"
              />
              {preview && (
                  <div
                      className="absolute bottom-3 right-2 cursor-pointer bg-gray-a-800/70 p-3 rounded-full"
                      onClick={handleFileRemove}
                  >
                    <img
                        src={getImagePath("/icon/ic_trash.svg")}
                        alt="X"
                        width={24}
                        height={24}
                    />
                  </div>
              )}
            </div>
        ) : (
            <div
                onClick={handleClick}
                className="bg-gray-100 w-[200px] h-[300px] rounded-[6px] flex flex-col items-center justify-center gap-3 border-[3px] border-dotted border-gray-300 cursor-pointer"
            >
              <img
                  src={getImagePath("/icon/ic_photo_plus.svg")}
                  alt="Add Img"
                  width={32}
                  height={32}
              />
              <p className="text-gray-400 font-semibold text-sm">
                사진을 업로드해주세요
              </p>
            </div>
        )}

        <input
            ref={fileInputRef}
            type="file"
            accept=".jpg, .jpeg, .png"
            className="hidden"
            onChange={handleFileChange}
        />
      </div>
  );
}
