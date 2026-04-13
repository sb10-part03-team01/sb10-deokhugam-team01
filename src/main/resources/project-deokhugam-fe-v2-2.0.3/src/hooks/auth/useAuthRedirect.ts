import {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {useAuthStore} from "@/store/authStore.ts";
import type {User} from "@/types/auth.ts";

export const useAuthRedirect = (
    redirectPath: string = "/login"
): {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
} => {
  const {user, isLoading, isInitialized} = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (isInitialized && !isLoading && !user?.id) {
      navigate(redirectPath);
    }
  }, [user?.id, isLoading, isInitialized, navigate, redirectPath]);

  return {
    user,
    isLoading: !isInitialized || isLoading,
    isAuthenticated: !!user?.id
  };
};

export const useAuthGuard = (
    redirectPath: string = "/login"
): {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  shouldShowContent: boolean;
} => {
  const {user, isLoading, isAuthenticated} = useAuthRedirect(redirectPath);

  return {
    user,
    isLoading,
    isAuthenticated,
    shouldShowContent: !isLoading && isAuthenticated
  };
};
