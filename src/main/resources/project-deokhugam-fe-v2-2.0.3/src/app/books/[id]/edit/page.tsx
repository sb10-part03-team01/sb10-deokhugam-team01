import {useEffect, useState} from "react";
import FormContainer from "../../components/bookForm/FormContainer.tsx";
import PageHead from "../../components/bookForm/PageHead.tsx";
import {BookResponse, getBookDetail} from "@/api/books.ts";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import {useParams} from "react-router-dom";
import FormFields from "../../components/bookForm/FormFields.tsx";
import clsx from "clsx";

export default function EditBookPage() {
  const {id: paramsId} = useParams();
  const id = String(paramsId);

  const [data, setData] = useState<BookResponse | null>(null);

  useEffect(() => {
    const fetchBookDetail = async () => {
      try {
        const response = await getBookDetail(id);
        setData(response);
      } catch (err) {
        console.error("도서 상세 조회 실패:", err);
      }
    };

    fetchBookDetail();
  }, [id]);

  if (!data) return <LoadingScreen/>;

  return (
      <div className={clsx("pt-[50px]", "max-md:pb-[150px]")}>
        <PageHead mode="edit"/>
        <FormContainer defaultValues={data}>
          <FormFields id={id} data={data} isEdit/>
        </FormContainer>
      </div>
  );
}
