'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import './kyc.css';

interface KYCFormData {
  // Personal Information
  firstName: string;
  lastName: string;
  middleName: string;
  dateOfBirth: string;
  gender: string;
  nationality: string;
  
  // Contact Information
  email: string;
  phoneNumber: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  
  // Identity Information
  idType: string;
  idNumber: string;
  issueDate: string;
  expiryDate: string;
  
  // Employment Information
  employmentStatus: string;
  occupation: string;
  employer: string;
  annualIncome: string;
  
  // Banking Information
  accountPurpose: string;
  sourceOfFunds: string;
  
  // Documents
  idDocument: File | null;
  proofOfAddress: File | null;
  photograph: File | null;
}

export default function KYCPage() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<KYCFormData>({
    firstName: '',
    lastName: '',
    middleName: '',
    dateOfBirth: '',
    gender: '',
    nationality: '',
    email: '',
    phoneNumber: '',
    address: '',
    city: '',
    state: '',
    zipCode: '',
    country: '',
    idType: '',
    idNumber: '',
    issueDate: '',
    expiryDate: '',
    employmentStatus: '',
    occupation: '',
    employer: '',
    annualIncome: '',
    accountPurpose: '',
    sourceOfFunds: '',
    idDocument: null,
    proofOfAddress: null,
    photograph: null,
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>, fieldName: keyof KYCFormData) => {
    const file = e.target.files?.[0] || null;
    setFormData(prev => ({
      ...prev,
      [fieldName]: file
    }));
  };

  const handleNext = () => {
    if (currentStep < 4) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Create FormData for file uploads
    const submitData = new FormData();
    
    Object.entries(formData).forEach(([key, value]) => {
      if (value instanceof File) {
        submitData.append(key, value);
      } else if (value) {
        submitData.append(key, value);
      }
    });

    try {
      // Your API call here
      // const response = await fetch('/api/kyc/submit', {
      //   method: 'POST',
      //   body: submitData,
      // });
      
      console.log('KYC Data:', formData);
      alert('KYC submitted successfully!');
      router.push('/dashboard');
    } catch (error) {
      console.error('KYC submission failed:', error);
      alert('Submission failed. Please try again.');
    }
  };

  const renderStepIndicator = () => (
    <div className="step-indicator">
      {[1, 2, 3, 4].map((step) => (
        <div key={step} className="step-item">
          <div className={`step-circle ${currentStep >= step ? 'active' : ''}`}>
            {currentStep > step ? '✓' : step}
          </div>
          <div className="step-label">
            {step === 1 && 'Personal'}
            {step === 2 && 'Contact'}
            {step === 3 && 'Identity'}
            {step === 4 && 'Employment'}
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <div className="kyc-container">
      <div className="kyc-card">
        <div className="kyc-header">
          <h1>Complete Your KYC</h1>
          <p>Please provide accurate information for verification</p>
        </div>

        {renderStepIndicator()}

        <form onSubmit={handleSubmit} className="kyc-form">
          {/* Step 1: Personal Information */}
          {currentStep === 1 && (
            <div className="form-step">
              <h2>Personal Information</h2>
              
              <div className="form-row">
                <div className="form-group">
                  <label>First Name *</label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter first name"
                  />
                </div>

                <div className="form-group">
                  <label>Middle Name</label>
                  <input
                    type="text"
                    name="middleName"
                    value={formData.middleName}
                    onChange={handleInputChange}
                    placeholder="Enter middle name"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Last Name *</label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter last name"
                  />
                </div>

                <div className="form-group">
                  <label>Date of Birth *</label>
                  <input
                    type="date"
                    name="dateOfBirth"
                    value={formData.dateOfBirth}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Gender *</label>
                  <select
                    name="gender"
                    value={formData.gender}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select gender</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Nationality *</label>
                  <input
                    type="text"
                    name="nationality"
                    value={formData.nationality}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter nationality"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Step 2: Contact Information */}
          {currentStep === 2 && (
            <div className="form-step">
              <h2>Contact Information</h2>

              <div className="form-row">
                <div className="form-group">
                  <label>Email Address *</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    required
                    placeholder="example@email.com"
                  />
                </div>

                <div className="form-group">
                  <label>Phone Number *</label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleInputChange}
                    required
                    placeholder="+1 234 567 8900"
                  />
                </div>
              </div>

              <div className="form-group full-width">
                <label>Address *</label>
                <textarea
                  name="address"
                  value={formData.address}
                  onChange={handleInputChange}
                  required
                  placeholder="Enter your full address"
                  rows={3}
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>City *</label>
                  <input
                    type="text"
                    name="city"
                    value={formData.city}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter city"
                  />
                </div>

                <div className="form-group">
                  <label>State/Province *</label>
                  <input
                    type="text"
                    name="state"
                    value={formData.state}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter state"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>ZIP/Postal Code *</label>
                  <input
                    type="text"
                    name="zipCode"
                    value={formData.zipCode}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter ZIP code"
                  />
                </div>

                <div className="form-group">
                  <label>Country *</label>
                  <input
                    type="text"
                    name="country"
                    value={formData.country}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter country"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Identity Information */}
          {currentStep === 3 && (
            <div className="form-step">
              <h2>Identity Verification</h2>

              <div className="form-row">
                <div className="form-group">
                  <label>ID Type *</label>
                  <select
                    name="idType"
                    value={formData.idType}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select ID type</option>
                    <option value="passport">Passport</option>
                    <option value="drivers_license">Driver's License</option>
                    <option value="national_id">National ID Card</option>
                    <option value="residence_permit">Residence Permit</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>ID Number *</label>
                  <input
                    type="text"
                    name="idNumber"
                    value={formData.idNumber}
                    onChange={handleInputChange}
                    required
                    placeholder="Enter ID number"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Issue Date *</label>
                  <input
                    type="date"
                    name="issueDate"
                    value={formData.issueDate}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Expiry Date *</label>
                  <input
                    type="date"
                    name="expiryDate"
                    value={formData.expiryDate}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div className="upload-section">
                <h3>Upload Documents</h3>
                
                <div className="form-group upload-group">
                  <label>ID Document *</label>
                  <div className="file-input-wrapper">
                    <input
                      type="file"
                      onChange={(e) => handleFileChange(e, 'idDocument')}
                      accept="image/*,.pdf"
                      required
                    />
                    <div className="file-input-label">
                      {formData.idDocument ? formData.idDocument.name : '📄 Choose file'}
                    </div>
                  </div>
                  <small>Upload clear photo or scan of your ID</small>
                </div>

                <div className="form-group upload-group">
                  <label>Proof of Address *</label>
                  <div className="file-input-wrapper">
                    <input
                      type="file"
                      onChange={(e) => handleFileChange(e, 'proofOfAddress')}
                      accept="image/*,.pdf"
                      required
                    />
                    <div className="file-input-label">
                      {formData.proofOfAddress ? formData.proofOfAddress.name : '📄 Choose file'}
                    </div>
                  </div>
                  <small>Utility bill, bank statement (not older than 3 months)</small>
                </div>

                <div className="form-group upload-group">
                  <label>Photograph *</label>
                  <div className="file-input-wrapper">
                    <input
                      type="file"
                      onChange={(e) => handleFileChange(e, 'photograph')}
                      accept="image/*"
                      required
                    />
                    <div className="file-input-label">
                      {formData.photograph ? formData.photograph.name : '📷 Choose file'}
                    </div>
                  </div>
                  <small>Recent passport-sized photograph</small>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Employment & Banking */}
          {currentStep === 4 && (
            <div className="form-step">
              <h2>Employment & Banking Information</h2>

              <div className="form-row">
                <div className="form-group">
                  <label>Employment Status *</label>
                  <select
                    name="employmentStatus"
                    value={formData.employmentStatus}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select status</option>
                    <option value="employed">Employed</option>
                    <option value="self_employed">Self-Employed</option>
                    <option value="unemployed">Unemployed</option>
                    <option value="retired">Retired</option>
                    <option value="student">Student</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Occupation *</label>
                  <input
                    type="text"
                    name="occupation"
                    value={formData.occupation}
                    onChange={handleInputChange}
                    required
                    placeholder="Your occupation"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Employer</label>
                  <input
                    type="text"
                    name="employer"
                    value={formData.employer}
                    onChange={handleInputChange}
                    placeholder="Company name"
                  />
                </div>

                <div className="form-group">
                  <label>Annual Income *</label>
                  <select
                    name="annualIncome"
                    value={formData.annualIncome}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select range</option>
                    <option value="0-25000">$0 - $25,000</option>
                    <option value="25000-50000">$25,000 - $50,000</option>
                    <option value="50000-100000">$50,000 - $100,000</option>
                    <option value="100000-250000">$100,000 - $250,000</option>
                    <option value="250000+">$250,000+</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Account Purpose *</label>
                  <select
                    name="accountPurpose"
                    value={formData.accountPurpose}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select purpose</option>
                    <option value="personal_savings">Personal Savings</option>
                    <option value="business">Business</option>
                    <option value="investment">Investment</option>
                    <option value="salary">Salary Account</option>
                    <option value="other">Other</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Source of Funds *</label>
                  <select
                    name="sourceOfFunds"
                    value={formData.sourceOfFunds}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">Select source</option>
                    <option value="salary">Salary/Employment</option>
                    <option value="business">Business Income</option>
                    <option value="investment">Investment Returns</option>
                    <option value="inheritance">Inheritance</option>
                    <option value="savings">Savings</option>
                    <option value="other">Other</option>
                  </select>
                </div>
              </div>

              <div className="terms-section">
                <label className="checkbox-label">
                  <input type="checkbox" required />
                  <span>I declare that all information provided is accurate and complete</span>
                </label>
                <label className="checkbox-label">
                  <input type="checkbox" required />
                  <span>I agree to the terms and conditions and privacy policy</span>
                </label>
              </div>
            </div>
          )}

          {/* Navigation Buttons */}
          <div className="form-navigation">
            {currentStep > 1 && (
              <button type="button" onClick={handlePrevious} className="btn-secondary">
                Previous
              </button>
            )}
            
            {currentStep < 4 ? (
              <button type="button" onClick={handleNext} className="btn-primary">
                Next Step
              </button>
            ) : (
              <button type="submit" className="btn-submit">
                Submit KYC
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
}