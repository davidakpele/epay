import React, { useState } from 'react';
import { X, ArrowLeft } from 'lucide-react';
import './PurchaseModal.css';
import { getFiat } from '@/app/api/utils';

interface Provider {
  id: string;
  name: string;
  logo: string;
}

interface PurchaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onBack: () => void;
  serviceName: string;
  provider: Provider | null;
  theme: 'light' | 'dark';
}

const PurchaseModal: React.FC<PurchaseModalProps> = ({
  isOpen,
  onClose,
  onBack,
  serviceName,
  provider,
  theme
}) => {
  const [recipient, setRecipient] = useState('');
  const [amount, setAmount] = useState('');
  const [saveAsBeneficiary, setSaveAsBeneficiary] = useState(false);
  const [errors, setErrors] = useState<{ recipient?: string; amount?: string }>({});
  const [isLoading, setIsLoading] = useState(false);

  if (!isOpen || !provider) return null;

  const resetForm = () => {
    setRecipient('');
    setAmount('');
    setSaveAsBeneficiary(false);
    setErrors({});
    setIsLoading(false);
  };

  const handleBack = () => {
    resetForm();
    onBack();
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const validateRecipient = (value: string): boolean => {
    if (!value.trim()) {
      setErrors(prev => ({ ...prev, recipient: 'Recipient is required' }));
      return false;
    }
    // Add additional validation if needed (e.g., phone number format, email format)
    setErrors(prev => ({ ...prev, recipient: undefined }));
    return true;
  };

  const validateAmount = (value: string): boolean => {
    if (!value.trim()) {
      setErrors(prev => ({ ...prev, amount: 'Amount is required' }));
      return false;
    }
    
    const numAmount = parseFloat(value);
    if (isNaN(numAmount) || numAmount <= 0) {
      setErrors(prev => ({ ...prev, amount: 'Please enter a valid amount' }));
      return false;
    }
    
    setErrors(prev => ({ ...prev, amount: undefined }));
    return true;
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // Allow only numbers and decimal point
    if (value === '' || /^\d*\.?\d{0,2}$/.test(value)) {
      setAmount(value);
      if (value) validateAmount(value);
    }
  };

  const handleRecipientChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setRecipient(value);
    if (errors.recipient && value.trim()) {
      setErrors(prev => ({ ...prev, recipient: undefined }));
    }
  };

  const handleSubmit = async () => {
    const isRecipientValid = validateRecipient(recipient);
    const isAmountValid = validateAmount(amount);

    if (isRecipientValid && isAmountValid) {
      setIsLoading(true);
      
      try {
        // Simulate API call - replace with your actual API call
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        console.log({ recipient, amount, saveAsBeneficiary, provider });
        
        // Proceed with form submission
        // After successful submission, you might want to:
        // resetForm();
        // onClose();
        // or navigate to next step
        
      } catch (error) {
        console.error('Submission error:', error);
        // Handle error state
      } finally {
        setIsLoading(false);
      }
    }
  };

  const isFormValid = recipient.trim() !== '' && amount.trim() !== '' && parseFloat(amount) > 0;

  return (
    <div className="purchase-modal-overlay" onClick={handleClose}>
      <div 
        className={`purchase-modal-content ${theme}`} 
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-labelledby="purchase-modal-title"
        aria-modal="true"
      >
        <div className="purchase-modal-header">
          <button 
            className="back-button" 
            onClick={handleBack}
            aria-label="Go back"
            disabled={isLoading}
          >
            <ArrowLeft size={20} />
          </button>
          <div className="purchase-modal-title-wrapper">
            <img 
              src={provider.logo} 
              alt={`${provider.name} logo`}
              className="purchase-provider-logo"
            />
            <div>
              <h2 id="purchase-modal-title" className="purchase-modal-title">
                Buy {provider.name} Prepaid Recharge {serviceName}
              </h2>
            </div>
          </div>
          <button 
            className="close-button" 
            onClick={handleClose}
            aria-label="Close modal"
            disabled={isLoading}
          >
            <X size={20} />
          </button>
        </div>

        <div className="purchase-modal-body">
          <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
            <div className="form-group">
              <label className="form-label" htmlFor="recipient-input">
                Recipient *
              </label>
              <input
                id="recipient-input"
                type="text"
                className={`form-input ${errors.recipient ? 'error' : ''}`}
                placeholder="Enter recipient phone number or ID"
                value={recipient}
                onChange={handleRecipientChange}
                onBlur={() => validateRecipient(recipient)}
                aria-required="true"
                aria-invalid={!!errors.recipient}
                aria-describedby={errors.recipient ? "recipient-error" : undefined}
                autoComplete="off"
                disabled={isLoading}
              />
              {errors.recipient && (
                <span id="recipient-error" className="error-message" role="alert">
                  {errors.recipient}
                </span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="amount-input">
                Amount *
              </label>
              <div className="amount-input-wrapper">
                <span className="currency-symbol" aria-hidden="true">
                  {getFiat()}
                </span>
                <input 
                  id="amount-input"
                  type="text" 
                  inputMode="decimal"
                  className={`amount-input ${errors.amount ? 'error' : ''}`}
                  placeholder="0.00" 
                  value={amount}
                  onChange={handleAmountChange}
                  onBlur={() => validateAmount(amount)}
                  aria-required="true"
                  aria-invalid={!!errors.amount}
                  aria-describedby={errors.amount ? "amount-error" : undefined}
                  autoComplete="off"
                  disabled={isLoading}
                />
              </div>
              {errors.amount && (
                <span id="amount-error" className="error-message" role="alert">
                  {errors.amount}
                </span>
              )}
            </div>

            <button 
              type="submit"
              className="next-button"
              disabled={!isFormValid || isLoading}
              aria-label={isFormValid ? 'Proceed to next step' : 'Fill in all required fields to continue'}
            >
              {isLoading ? (
                <span className="button-content">
                  <span className="spinner"></span>
                  Processing...
                </span>
              ) : (
                'Next'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PurchaseModal;