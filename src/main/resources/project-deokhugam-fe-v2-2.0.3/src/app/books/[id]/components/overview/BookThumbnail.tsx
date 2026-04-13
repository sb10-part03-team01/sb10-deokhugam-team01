import {BookResponse} from "@/api/books.ts";
import clsx from "clsx";
import {useState} from "react";
import getImagePath from "@/constants/images.ts";

export default function BookThumbnail({data}: { data: BookResponse | null }) {
  const [imgErrors, setImgErrors] = useState<Record<string, boolean>>({});

  return (
      <div
          className={clsx(
              "relative min-w-[400px] min-h-[600px] max-h-[600px] w-[calc(100vw_*_(400/1920)) h-[calc(100vh_*_(600/1344))]] border rounded-xl overflow-hidden",
              "max-lg900:min-w-[300px] max-lg900:min-h-[500px] max-lg900:max-h-[500px]",
              "max-xs650:min-w-[200px] max-xs650:min-h-[300px] max-xs650:max-h-[300px]",
              "max-sm:min-w-full max-sm:min-h-[calc(100vh_*_(600/1344))]"
          )}
      >
        {data?.thumbnailUrl && !imgErrors[data.id] && (
            <img
                src={data.thumbnailUrl}
                alt={data.title || "thumbnail"}
                onError={() => setImgErrors(prev => ({...prev, [data.id]: true}))}
                className={"w-full h-full "}
            />
        )}
        {(!data?.thumbnailUrl || imgErrors[data.id]) && (
            <img src={getImagePath("/books/imgError.png")} alt="이미지 없음"/>
        )}
      </div>
  );
}
