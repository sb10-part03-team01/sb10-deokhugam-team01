import { useEffect, useState } from "react";

interface DelayedLoaderProps {
  isLoading: boolean;
  delay: number;
  children: React.ReactNode;
}

export default function DelayedLoader({
  isLoading,
  delay,
  children
}: DelayedLoaderProps) {
  const [show, setShow] = useState(false);

  useEffect(() => {
    let timer: NodeJS.Timeout;

    if (isLoading) {
      timer = setTimeout(() => setShow(true), delay);
    } else {
      setShow(false);
    }

    return () => clearTimeout(timer);
  }, [isLoading, delay]);

  return <>{show && children}</>;
}
