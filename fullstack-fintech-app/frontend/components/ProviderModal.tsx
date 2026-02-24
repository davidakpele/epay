import React from 'react';
import { X, ArrowLeft } from 'lucide-react';
import './ProviderModal.css';

interface Provider {
  id: string;
  name: string;
  logo: string;
}

interface ProviderModalProps {
  isOpen: boolean;
  onClose: () => void;
  serviceName: string;
  providers: Provider[];
  onSelectProvider: (provider: Provider) => void;
  theme: 'light' | 'dark';
}

const ProviderModal: React.FC<ProviderModalProps> = ({
  isOpen,
  onClose,
  serviceName,
  providers,
  onSelectProvider,
  theme
}) => {
  if (!isOpen) return null;

  return (
    <div className="provider-modal-overlay" onClick={onClose}>
      <div 
        className={`provider-modal-content ${theme}`} 
        onClick={(e) => e.stopPropagation()}
      >
        <div className="provider-modal-header">
          <button className="back-button" onClick={onClose}>
            <ArrowLeft size={20} />
          </button>
          <h2 className="provider-modal-title">Select Provider</h2>
          <button className="close-button" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="providers-grid">
          {providers.map((provider) => (
            <button
              key={provider.id}
              className="provider-item"
              onClick={() => onSelectProvider(provider)}
            >
              <img 
                src={provider.logo} 
                alt={provider.name}
                className="provider-logo"
              />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ProviderModal;