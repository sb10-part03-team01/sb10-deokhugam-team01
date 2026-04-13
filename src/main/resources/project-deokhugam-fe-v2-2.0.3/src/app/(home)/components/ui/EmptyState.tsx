
interface EmptyStateProps {
  title?: string;
  description: string;
  iconSrc: string;
  iconAlt: string;
}

export default function EmptyState({
  title,
  description,
  iconSrc,
  iconAlt
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-[16px] mb-[30px]">
      <div
        className="p-[12px] bg-gray-0 border border-gray-200 rounded-lg"
        style={{
          boxShadow:
            "0px 1px 2px 0px #0A0D120D, 0px -2px 0px 0px #0A0D120D inset, 0px 0px 0px 1px #0A0D122E inset"
        }}
      >
        <img src={iconSrc} alt={iconAlt} width={24} height={24} />
      </div>

      {title && (
        <h3 className="text-h3 font-semibold text-gray-900 text-center">
          {title}
        </h3>
      )}

      <p className="text-body2 font-medium text-gray-500 text-center">
        {description}
      </p>
    </div>
  );
}
