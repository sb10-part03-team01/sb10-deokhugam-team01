import {useAuthGuard} from "@/hooks/auth/useAuthRedirect.ts";
import LoadingScreen from "@/components/common/LoadingScreen.tsx";
import PageHead from "../components/bookForm/PageHead.tsx";
import FormContainer from "../components/bookForm/FormContainer.tsx";
import FormFields from "../components/bookForm/FormFields.tsx";
import clsx from "clsx";

export default function AddBookPage() {
  const {shouldShowContent} = useAuthGuard();

  if (!shouldShowContent) {
    return <LoadingScreen/>;
  }

  return (
      <div className={clsx("pt-[50px]", "max-md:pb-[150px]")}>
        <PageHead mode="add"/>
        <FormContainer>
          <FormFields/>
        </FormContainer>
      </div>
  );
}
