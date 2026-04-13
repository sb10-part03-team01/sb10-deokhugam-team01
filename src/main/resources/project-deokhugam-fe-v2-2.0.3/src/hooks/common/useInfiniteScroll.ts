import { useCallback, useEffect, useState } from "react";

export interface CursorItem {
  id: string;
  createdAt: string;
}

export interface UseInfiniteScrollOptions<T, P> {
  initialParams?: P;
  fetcher: (params: P) => Promise<{
    content: T[];
    nextCursor: string;
    nextAfter: string;
    hasNext: boolean;
  }>;
  setData: React.Dispatch<React.SetStateAction<T[]>>;
}

export const useInfiniteScroll = <
  T extends CursorItem,
  P extends Record<string, unknown>
>({
  initialParams,
  fetcher,
  setData
}: UseInfiniteScrollOptions<T, P>) => {
  const [cursor, setCursor] = useState<string>();
  const [after, setAfter] = useState<string>();
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const fetchMore = useCallback(async () => {
    if (isLoading || !hasMore) return;

    setIsLoading(true);
    try {
      const params = {
        ...(initialParams || {}),
        ...(cursor ? { cursor } : {}),
        ...(after ? { after } : {})
      } as P;

      const response = await fetcher(params);

      if (response.content.length === 0) {
        setHasMore(false);
        return;
      }

      if (response.hasNext) {
        setHasMore(true);
      } else {
        setHasMore(false);
      }

      setData(prev => {
        const combined = [...(prev || []), ...response.content];
        const unique = Array.from(
          new Map(combined.map(item => [item.id, item])).values()
        );
        return unique;
      });

      setCursor(response.nextCursor);
      setAfter(response.nextAfter);
    } catch (err) {
      console.error("Infinite scroll fetch error:", err);
    } finally {
      setIsLoading(false);
    }
  }, [cursor, after, hasMore, isLoading, fetcher, setData, initialParams]);

  const resetInfiniteScroll = () => {
    setCursor(undefined);
    setAfter(undefined);
    setHasMore(true);
  };

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const windowHeight = window.innerHeight;
      const docHeight = Math.max(
        document.body.scrollHeight,
        document.documentElement.scrollHeight
      );

      if (scrollTop + windowHeight >= docHeight - 100) {
        fetchMore();
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [fetchMore]);

  return {
    cursor,
    after,
    hasMore,
    isLoading,
    setCursor,
    setAfter,
    setHasMore,
    setIsLoading,
    resetInfiniteScroll,
    fetchMore
  };
};
