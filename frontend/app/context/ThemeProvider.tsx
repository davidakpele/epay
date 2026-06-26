'use client';

import { createContext, useEffect, useState, ReactNode } from "react";

interface ThemeContextType {
  theme: string;
  toggleTheme: () => void;
  themeLoaded: boolean;
}

export const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export default function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<string>("light"); 
  const [themeLoaded, setThemeLoaded] = useState(false);

  useEffect(() => {
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) {
      setTheme(savedTheme);
    } else {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      const initial = prefersDark ? "dark" : "light";
      setTheme(initial);
      localStorage.setItem("theme", initial);
    }
    setThemeLoaded(true);
  }, []);

  useEffect(() => {
    if (themeLoaded) {
      document.documentElement.classList.toggle("dark", theme === "dark");
      localStorage.setItem("theme", theme);
    }
  }, [theme, themeLoaded]);

  const toggleTheme = () => {
    console.log('Theme toggling from:', theme); // Debug log
    setTheme((prev) => {
      const newTheme = prev === "dark" ? "light" : "dark";
      console.log('New theme:', newTheme); // Debug log
      return newTheme;
    });
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, themeLoaded }}>
      {/* Show children immediately but apply opacity transition */}
      <div style={{ 
        opacity: themeLoaded ? 1 : 0,
        transition: 'opacity 0.2s ease-in-out'
      }}>
        {children}
      </div>
    </ThemeContext.Provider>
  );
}