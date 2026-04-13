import { useMediaQuery } from "react-responsive";

export default function useResponsiveLimit(type: "bookList" | "popularBook") {
  const isDesktop1200 = useMediaQuery({ minWidth: 1200 });
  const isTabletPC769 = useMediaQuery({ minWidth: 769 });
  const isDesktop941 = useMediaQuery({ minWidth: 941 });
  const isTabletPC707 = useMediaQuery({ minWidth: 707 });
  const isTablet474 = useMediaQuery({ minWidth: 474 });

  if (!type) return;

  if (type === "bookList") {
    if (isDesktop1200) return 20;
    if (isTabletPC769) return 16;
    return 12;
  }

  if (type === "popularBook") {
    if (isDesktop941) return 4;
    if (isTabletPC707) return 3;
    if (isTablet474) return 2;
    return 1;
  }
}
