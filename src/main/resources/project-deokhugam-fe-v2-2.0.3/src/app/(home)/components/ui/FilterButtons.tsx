import RadioButton from "@/components/common/Buttons/RadioButton.tsx";

interface FilterButtonsProps {
  selectedFilter: string;
  onFilterChange: (filter: string) => void;
  filterOptions?: string[];
}

export default function FilterButtons({
                                        selectedFilter,
                                        onFilterChange,
                                        filterOptions = ["전체", "월간", "주간", "일간"]
                                      }: FilterButtonsProps) {
  return (
      <div className="flex gap-[8px] mb-[30px] justify-center">
        {filterOptions.map(option => (
            <RadioButton
                key={option}
                variant={selectedFilter === option ? "selected" : "unselected"}
                onClick={() => onFilterChange(option)}
            >
              {option}
            </RadioButton>
        ))}
      </div>
  );
}
