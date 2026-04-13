import { SetStateAction, useState } from "react";
import getImagePath from "@/constants/images.ts";

interface Props {
  totalStars: number;
  rating: number;
  setRating: React.Dispatch<SetStateAction<number>>;
}

export default function ReviewRating({ totalStars, rating, setRating }: Props) {
  const [isInteracting, setIsInteracting] = useState(false);

  const handleSetRating = (index: number) => {
    setRating(index + 1);
  };

  const handleMouseDown = (index: number) => {
    setIsInteracting(true);
    handleSetRating(index);
  };

  const handleMouseMove = (index: number) => {
    if (isInteracting) handleSetRating(index);
  };

  const handleMouseUp = () => {
    setIsInteracting(false);
  };

  const handleTouchStart = (event: React.TouchEvent) => {
    setIsInteracting(true);
    handleTouchMove(event);
  };

  const handleTouchMove = (event: React.TouchEvent) => {
    if (!isInteracting) return;

    const touchX = event.touches[0].clientX;
    const starElements = document.querySelectorAll("");
    starElements.forEach((star, index) => {
      const { left, right } = star.getBoundingClientRect();
      if (touchX >= left && touchX <= right) {
        handleSetRating(index);
      }
    });
  };

  const handleTouchEnd = () => {
    setIsInteracting(false);
  };

  return (
    <div
      className="flex gap-[.5px]"
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
      onTouchEnd={handleTouchEnd}
    >
      {[...Array(totalStars)].map((_, index) => {
        return (
          <div
            key={index}
            className="relative w-[30px] h-[30px] cursor-pointer"
          >
            <img
              draggable={false}
              src={
                index < rating
                  ? getImagePath("/icon/ic_star.svg")
                  : getImagePath("/icon/ic_star_failled.svg")
              }
              alt={index < rating ? "filled star" : "empty star"}
              onMouseDown={() => handleMouseDown(index)}
              onMouseMove={() => handleMouseMove(index)}
              onTouchStart={handleTouchStart}
              onTouchMove={handleTouchMove}
            />
          </div>
        );
      })}
    </div>
  );
}
