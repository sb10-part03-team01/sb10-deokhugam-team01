import clsx from "clsx";
import {useNavigate, useLocation} from "react-router-dom";
import {useClickOutside} from "@/hooks/common/useClickOutside.ts";
import {useAuthStore} from "@/store/authStore.ts";
import {useEffect, useState} from "react";
import {authApi} from "@/api/auth.ts";
import {getNotifications} from "@/api/notifications.ts";
import Notification from "./Notifications/Notification.tsx";
import NavProfile from "../common/NavProfile.tsx";
import getImagePath from "@/constants/images.ts";

export default function NavBar() {
  const [mounted, setMounted] = useState(false);
  const [userNickname, setUserNickname] = useState("");
  const [hasUnreadNotifications, setHasUnreadNotifications] = useState(false);

  const {open, setOpen, dropdownRef} = useClickOutside();
  const {
    open: isNotificationOpen,
    setOpen: setIsNotificationOpen,
    dropdownRef: notificationRef
  } = useClickOutside();

  const navigate = useNavigate();
  const {pathname} = useLocation();
  const userId = useAuthStore(state => state.user?.id);

  useEffect(() => setMounted(true), []);
  useEffect(() => {
    if (userId) {
      const fetchProfile = async () => {
        const profile = await authApi.getUserProfile(userId);
        setUserNickname(profile.nickname);
      };

      fetchProfile();
    }
  }, [userId]);

  useEffect(() => {
    const checkUnreadNotifications = async () => {
      if (!userId) return;

      try {
        const response = await getNotifications({
          userId,
          direction: "DESC",
          limit: 20
        });

        const hasUnread = response.content.some(
            notification => !notification.confirmed
        );
        setHasUnreadNotifications(hasUnread);
      } catch (error) {
        console.error("알림 상태 확인 실패:", error);
        setHasUnreadNotifications(false);
      }
    };

    if (userId) {
      checkUnreadNotifications();

      const interval = setInterval(checkUnreadNotifications, 10000);

      return () => clearInterval(interval);
    }
  }, [userId]);

  return (
      <div
          className={clsx(
              "fixed left-0 right-0 top-0 border-b border-solid border-gray-100 bg-white py-4 z-[10]",
              "max-[1432px]:px-4"
          )}
      >
        <div className="flex items-center justify-between max-w-[1400px] mx-auto">
          <div
              className={clsx(
                  "flex items-center gap-10",
                  "max-xs650:gap-5",
                  "max-sm400:gap-0"
              )}
          >
            <img
                src={getImagePath("/nav/deokhugam.svg")}
                alt="Deokhugam"
                width={115}
                height={33}
                className="cursor-pointer"
                onClick={() => {
                  navigate("/");
                }}
            />
            <ul className="flex items-center gap-3">
              <li
                  className={clsx(
                      "px-3 cursor-pointer font-bold duration-[.2s]",
                      "hover:text-black",
                      pathname.includes("/books") ? "text-black" : "text-gray-500"
                  )}
                  onClick={() => navigate("/books")}
              >
                도서
              </li>
              <li
                  className={clsx(
                      "px-3 cursor-pointer font-bold duration-[.2s]",
                      "hover:text-black",
                      pathname.includes("/reviews") ? "text-black" : "text-gray-500"
                  )}
                  onClick={() => navigate("/reviews")}
              >
                리뷰
              </li>
            </ul>
          </div>
          {!mounted ? null : userId ? (
              <div className={clsx("flex items-center gap-6", "max-sm400:gap-3")}>
                <div className="relative" ref={notificationRef}>
                  <button
                      className="h-4"
                      onClick={() => setIsNotificationOpen(prev => !prev)}
                  >
                    <img
                        src={getImagePath("/nav/notification.svg")}
                        alt="알림"
                        width={20}
                        height={20}
                    />
                    {hasUnreadNotifications && (
                        <div
                            className="absolute top-0 right-[-5px] w-1.5 h-1.5 bg-red-500 rounded"/>
                    )}
                  </button>

                  {isNotificationOpen && (
                      <div className="absolute top-[58px] right-[-50px] z-20">
                        <Notification
                            onClose={() => setIsNotificationOpen(false)}
                            onNotificationRead={() => {
                              const checkUnreadNotifications = async () => {
                                if (!userId) return;
                                try {
                                  const response = await getNotifications({
                                    userId,
                                    direction: "DESC",
                                    limit: 20
                                  });
                                  const hasUnread = response.content.some(
                                      notification => !notification.confirmed
                                  );
                                  setHasUnreadNotifications(hasUnread);
                                } catch (error) {
                                  console.error("알림 상태 확인 실패:", error);
                                }
                              };
                              checkUnreadNotifications();
                            }}
                        />
                      </div>
                  )}
                </div>
                <div className="relative" ref={dropdownRef}>
                  <button
                      className="flex items-center gap-1 text-gray-600 font-medium"
                      onClick={() => setOpen(prev => !prev)}
                  >
                    {userNickname}
                    <img
                        src={getImagePath("/nav/arrow_down.svg")}
                        alt="arrow"
                        width={18}
                        height={18}
                        className={clsx("duration-100", open && "rotate-180")}
                    />
                  </button>
                  <div
                      className={clsx(
                          "overflow-hidden transition-all duration-300 ease-in-out",
                          open
                              ? "max-h-[170px] opacity-100 pointer-events-auto"
                              : "max-h-0 opacity-0 pointer-events-none"
                      )}
                  >
                    <NavProfile
                        userId={userId}
                        userNickname={userNickname}
                        setUserNickname={setUserNickname}
                        profileMenuController={setOpen}
                    />
                  </div>
                </div>
              </div>
          ) : (
              <button
                  className="border bg-gray-900 text-white rounded-md px-3 py-1.5 text-sm"
                  onClick={() => navigate("/login")}
              >
                로그인
              </button>
          )}
        </div>
      </div>
  );
}
