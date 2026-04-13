import NavBar from "@/components/ui/NavBar.tsx";
import Tooltip from "@/components/ui/Tooltip.tsx";
import Footer from "@/components/ui/Footer.tsx";

import clsx from "clsx";
import {Outlet, useLocation} from "react-router-dom";


export default function ClientLayout({}) {
  const {pathname} = useLocation();
  console.log({pathname})
  const hideNavigation =
      pathname.includes("/login") || pathname.includes("/signup");

  const hideFooter =
      pathname.includes("/login") ||
      pathname.includes("/signup") ||
      pathname.includes("/books/add") ||
      pathname.includes("/books/") ||
      pathname.includes("/reviews/");

  return (
      <>
        {!hideNavigation && <NavBar/>}
        <div
            className={clsx(
                !hideNavigation &&
                "min-h-[calc(100vh-139px)] mt-[67px] px-4 max-w-[1200px] mx-auto"
            )}
        >
          <Outlet/>
        </div>
        {!hideFooter && <Footer/>}
        <Tooltip/>
      </>
  );
}
