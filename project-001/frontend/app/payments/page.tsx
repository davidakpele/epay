'use client';

import { useState, FormEvent, ChangeEvent, useEffect } from 'react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import "./Payments.css"

const Payments = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');

    // useEffect(() => {
    //     const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | null;
    //     const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    //     const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');
        
    //     setTheme(initialTheme);
    //     document.documentElement.classList.toggle('dark', initialTheme === 'dark');
    //     document.body.classList.toggle('dark-theme', initialTheme === 'dark');
    // }, []);
    
    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.classList.toggle('dark', newTheme === 'dark');
        document.body.classList.toggle('dark-theme', newTheme === 'dark');
    };

  return (
    <>
     <div className={`dashboard-container`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">

         </div>
        <Footer theme={theme} />
      </main>
      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal 
        isOpen={isDepositOpen} 
        onClose={() => setIsDepositOpen(false)} 
        theme={theme} 
        />
    </div>
    </>
  )
}

export default Payments;