'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import "./Bills.css"
import { filters, services } from '../lib/BillsData';
import LoadingScreen from '@/components/loader/Loadingscreen';

const Bills = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [selectedFilter, setSelectedFilter] = useState<string>('all');
    const [searchQuery, setSearchQuery] = useState<string>('');
    const [isPageLoading, setIsPageLoading] = useState(true);
    
    const filteredServices = services.filter(service => {
      const matchesFilter = selectedFilter === 'all' || service.category === selectedFilter;
      const matchesSearch = service.name.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesFilter && matchesSearch;
    });
    
    useEffect(() => {
      const loadingTimer = setTimeout(() => {
        setIsPageLoading(false);
      }, 2000);
      return () => clearTimeout(loadingTimer);
    }, []);

    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.classList.toggle('dark', newTheme === 'dark');
        document.body.classList.toggle('dark-theme', newTheme === 'dark');
    };

  const getServiceImage = (name: string) => {
    const lowerName = name.toLowerCase();
    if (lowerName.includes('electricity')) return { src: '../../assets/images/electricity_bill.png', alt: 'Electricity' };
    if (lowerName.includes('internet') || lowerName.includes('data')) return { src: '../../assets/images/wifi.png', alt: 'Data' };
    if (lowerName.includes('tv') || lowerName.includes('cable')) return { src: '../../assets/images/cabletv-banner.png', alt: 'Cable TV' };
    if (lowerName.includes('airtime')) return { src: '../../assets/images/airtime-banner.png', alt: 'Airtime' };
    if (lowerName.includes('betting')) return { src: '../../assets/images/betting-background.png', alt: 'Betting' };
    return { src: '../../assets/images/shopping-banner.png', alt: 'Service' };
  };

    if (isPageLoading) {
        return <LoadingScreen />;
    }

  return (
    <div className={`dashboard-container`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className={`bills-page`}>
            <div className="bills-content">

              {/* ---- Hero Banner ---- */}
              <div className="page-header">
                <div className="page-header-text">
                  <h1 className="page-title">Pay Bills</h1>
                  <p className="page-description">
                    Make bill payments easier than ever with ePay. With just a few taps,
                    handle all your bills swiftly and hassle-free. Say goodbye to stress
                    and hello to convenience!
                  </p>
                </div>
                <div className="page-header-illustration">
                  <Image
                    src="../../assets/images/bills-hero.png"
                    alt="Pay Bills Illustration"
                    width={420}
                    height={240}
                    style={{ objectFit: 'contain', maxHeight: '220px' }}
                  />
                </div>
              </div>

              {/* ---- Filter Bar ---- */}
              <div className="filter-container">
                {/* Dropdown for mobile/tablet */}
                <select 
                  className="filter-select"
                  value={selectedFilter}
                  onChange={(e) => setSelectedFilter(e.target.value)}
                >
                  {filters.map((filter) => (
                    <option key={filter.id} value={filter.id}>
                      {filter.label}
                    </option>
                  ))}
                </select>

                {/* Buttons for desktop */}
                <div className="filter-buttons">
                  {filters.map((filter) => (
                    <button
                      key={filter.id}
                      onClick={() => setSelectedFilter(filter.id)}
                      className={`filter-button ${selectedFilter === filter.id ? 'active' : ''}`}
                    >
                      {filter.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* ---- Service Cards Grid ---- */}
              <div className="services-container">
                {filteredServices.map((service) => {
                  const img = getServiceImage(service.name);
                  return (
                    <Link
                      key={service.id}
                      href={service.href}
                      className={`service-card ${service.color}`}
                    >
                      {/* Card title at top */}
                      <h3 className="service-title">{service.name}</h3>

                      {/* Center illustration */}
                      <div className="service-icon-wrapper">
                        <Image
                          src={img.src}
                          alt={img.alt}
                          width={200}
                          height={160}
                          style={{ objectFit: 'contain', maxHeight: '160px', width: '100%' }}
                        />
                      </div>

                      {/* Pay button at bottom */}
                      <div className="service-pay-btn">
                        Pay <span className="arrow">›</span>
                      </div>
                    </Link>
                  );
                })}
              </div>

              {filteredServices.length === 0 && (
                <div className="no-results">
                  <p>No services found matching "{searchQuery}"</p>
                </div>
              )}

            </div>
          </div>
          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
      
      <DepositModal 
        isOpen={isDepositOpen} 
        onClose={() => setIsDepositOpen(false)} 
        theme={theme} 
      />
    </div>
  );
};

export default Bills;