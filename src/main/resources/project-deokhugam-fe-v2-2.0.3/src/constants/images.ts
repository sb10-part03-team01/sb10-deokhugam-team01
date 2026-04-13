
export default function getImagePath(imageName: string): string {
  const BASE_URL = import.meta.env.VITE_PUBLIC_PATH || '';
  return `${BASE_URL}/images/${imageName}`;
}