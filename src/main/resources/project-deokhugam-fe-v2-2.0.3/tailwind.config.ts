import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}"
  ],
  theme: {
    extend: {
      screens: {
        s: "320px",
        sm400: "400px",
        sm: "480px",
        xs650: "650px",
        lg900: "900px",
        lg1050: "1050px",
        lg: "1199px",
        xlg: "1920px"
      },
      fontFamily: {
        sans: [
          "Pretendard",
          "-apple-system",
          "BlinkMacSystemFont",
          "system-ui",
          "Roboto",
          "Helvetica Neue",
          "Segoe UI",
          "Apple SD Gothic Neo",
          "Noto Sans KR",
          "Malgun Gothic",
          "Apple Color Emoji",
          "Segoe UI Emoji",
          "Segoe UI Symbol",
          "sans-serif"
        ],
        mono: ["Pretendard", "monospace"]
      },
      colors: {
        // Gray 컬러 팔레트
        gray: {
          0: "#FFFFFF",
          50: "#FAFAFA",
          100: "#F5F5F5",
          200: "#E9E9EC",
          300: "#D7D7DB",
          400: "#AEAEB7",
          500: "#858593",
          600: "#54545E",
          700: "#3E3E48",
          800: "#232329",
          900: "#1B1B23",
          950: "#111117",
          1000: "#000000"
        },
        // Gray_A 컬러 팔레트 (투명도)
        "gray-a": {
          500: "#1B1B2366",
          800: "#1B1B23cc"
        },
        // White_A 컬러 팔레트 (투명도)
        "white-a": {
          10: "#FFFFFF1a"
        },
        // Yellow 컬러 팔레트
        yellow: {
          400: "#FEC84B"
        },
        // Red 컬러 팔레트
        red: {
          500: "#F53A3E"
        },
        // Blue 컬러 팔레트
        blue: {
          100: "#ECF0FA",
          500: "#3A73EF"
        }
      },
      fontSize: {
        header1: ["24px", "auto"],
        "header1-160": ["24px", "160%"],
        header2: ["22px", "140%"],
        title1: ["20px", "auto"],
        "title1-140": ["20px", "140%"],
        body1: ["18px", "auto"],
        body2: ["16px", "auto"],
        "body2-140": ["16px", "140%"],
        "body2-160": ["16px", "160%"],
        body3: ["14px", "auto"],
        "body3-150": ["14px", "150%"],
        body4: ["13px", "auto"],
        caption1: ["12px", "auto"],
        caption2: ["11px", "auto"]
      },
      fontWeight: {
        medium: "500",
        semibold: "600",
        bold: "700"
      }
    }
  },
  plugins: []
};

export default config;
