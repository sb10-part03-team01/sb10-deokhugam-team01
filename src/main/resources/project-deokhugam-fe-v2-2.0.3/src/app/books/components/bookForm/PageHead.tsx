export default function PageHead({ mode }: { mode: "add" | "edit" }) {
  return (
    <>
      <p className="text-header1 font-bold">
        도서 {mode === "add" ? "등록하기" : "수정"}
      </p>
    </>
  );
}
