export const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString);
    const year = date.getFullYear().toString().slice(-2); // 뒤 2자리만
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const day = date.getDate().toString().padStart(2, "0");
    return `${year}.${month}.${day}`;
  } catch {
    return dateString;
  }
};
