'use client';

import { useState, useEffect } from 'react';
import { 
  Zap, 
  Wifi, 
  Tv, 
  Phone, 
  Droplets, 
  ShieldCheck, 
  CreditCard, 
  Smartphone,
  Search
} from 'lucide-react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import ProviderModal from '@/components/ProviderModal'
import PurchaseModal from '@/components/PurchaseModal'
import "./Bills.css"
import { filters, services, providers } from '../lib/BillsData';
import LoadingScreen from '@/components/loader/Loadingscreen';

interface Provider {
  id: string;
  name: string;
  logo: string;
}

const Bills = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [isProviderModalOpen, setIsProviderModalOpen] = useState(false);
    const [isPurchaseModalOpen, setIsPurchaseModalOpen] = useState(false);
    const [selectedService, setSelectedService] = useState<string>('');
    const [selectedProvider, setSelectedProvider] = useState<Provider | null>(null);
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
      // Handle page loading
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

    const getServiceIcon = (name: string) => {
        const lowerName = name.toLowerCase();
        if (lowerName.includes('electricity')) return <Zap strokeWidth={1.5} />;
        if (lowerName.includes('internet') || lowerName.includes('data')) return <Wifi strokeWidth={1.5} />;
        if (lowerName.includes('tv') || lowerName.includes('cable')) return <Tv strokeWidth={1.5} />;
        if (lowerName.includes('airtime')) return <Smartphone strokeWidth={1.5} />;
        if (lowerName.includes('water')) return <Droplets strokeWidth={1.5} />;
        if (lowerName.includes('insurance')) return <ShieldCheck strokeWidth={1.5} />;
        if (lowerName.includes('betting')) return <CreditCard strokeWidth={1.5} />;
        return <Phone strokeWidth={1.5} />;
    };

    const handleServiceClick = (serviceName: string) => {
      setSelectedService(serviceName);
      setIsProviderModalOpen(true);
    };

    const handleProviderSelect = (provider: Provider) => {
      setSelectedProvider(provider);
      setIsProviderModalOpen(false);
      setIsPurchaseModalOpen(true);
    };

    const handleBackToProviders = () => {
      setIsPurchaseModalOpen(false);
      setIsProviderModalOpen(true);
    };

    const handleCloseModals = () => {
      setIsProviderModalOpen(false);
      setIsPurchaseModalOpen(false);
      setSelectedService('');
      setSelectedProvider(null);
    };

    if (isPageLoading) {
        return <LoadingScreen />;
    }

  return (
    <>
     <div className={`dashboard-container`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
           <div className={`bills-page ${theme === "dark" ? "bg-light" : "bg-dark"}`}>
              <div className="bills-content">
                <div className="page-header">
                  <h1 className="page-title">Pay Bills</h1>
                  <p className="page-description">
                    No more stress over bill payment! ePay got you covered. With just a tap, take care of all your bills in a jiffy. 
                    Chillax and let ePay do the boring part for you. More time for the fun things in life!
                  </p>
                </div>
                
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

                <div className="services-container">
                  {filteredServices.map((service) => (
                    <div 
                      key={service.id} 
                      className={`service-card ${service.color}`}
                      onClick={() => handleServiceClick(service.name)}
                    >
                      <h3 className="service-title">{service.name}</h3>
                      <div className="service-icon-wrapper">
                        {getServiceIcon(service.name)}
                      </div>
                    </div>
                  ))}
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

      <ProviderModal
        isOpen={isProviderModalOpen}
        onClose={handleCloseModals}
        serviceName={selectedService}
        providers={providers}
        onSelectProvider={handleProviderSelect}
        theme={theme}
      />

      <PurchaseModal
        isOpen={isPurchaseModalOpen}
        onClose={handleCloseModals}
        onBack={handleBackToProviders}
        serviceName={selectedService.toLowerCase()}
        provider={selectedProvider}
        theme={theme}
      />
    </div>
    </>
  )
}

export default Bills