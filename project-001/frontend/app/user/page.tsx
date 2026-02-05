'use client';

import React, { useState, useEffect } from 'react';
import { 
  ArrowLeft, 
  User, 
  Key,
  Shield,
  AlertCircle,
  Smartphone,
  Monitor,
  FileText,
  CheckCircle
} from 'lucide-react';
import './UserProfile.css';
import { KYCDocument, LoginHistory, MetaMapErrors, UserData, UserSettings } from '@/app/types/utils';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import LoadingScreen from '@/components/loader/Loadingscreen';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { Toast } from '@/app/types/auth';
import { userService, getUserId, updateCompleteProfileDetails, updateNotificationContainer } from '@/app/api/index';

const UserProfile = () => {
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);
  const [showResetPasswordModal, setShowResetPasswordModal] = useState(false);
  const [show2FAModal, setShow2FAModal] = useState(false);
  const [showMetaMapModal, setShowMetaMapModal] = useState(false);
  const [metaMapStep, setMetaMapStep] = useState(1);
  const [showMetaMapExit, setShowMetaMapExit] = useState(false);
  const [metaMapData, setMetaMapData] = useState({
    bvn: '',
    firstName: '',
    lastName: '',
    dob: ''
  });
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [isEditUserProfileDetails, setIsEditUserProfileDetails] = useState(false);
  const [isShowSuspendAccountModal, setShowSuspendAccountModal] = useState(false);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);
  const [userData, setUserData] = useState<UserData | null>(null);
  const [userProfile, setUserProfile] = useState<any>(null);
  const [metaMapErrors, setMetaMapErrors] = useState<MetaMapErrors>({});

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    telephone: '',
    gender: '',
    dob: '',
    address: '',
    country: '',
    state: '',
    city: ''
  });

  const [formErrors, setFormErrors] = useState({
    firstName: '',
    lastName: '',
    email: '',
    telephone: '',
    gender: '',
    dob: '',
    address: '',
    country: '',
    state: '',
    city: ''
  });

  const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
      setToasts((prev) => {
        if (prev.length >= 5) return prev;
  
        const id = Date.now();
        const newToast: Toast = { id, message: msg, type, exiting: false };
  
        setTimeout(() => {
          setToasts((currentToasts) =>
            currentToasts.map((t) => (t.id === id ? { ...t, exiting: true } : t))
          );
  
          setTimeout(() => {
            setToasts((currentToasts) => currentToasts.filter((t) => t.id !== id));
          }, 300);
        }, 3000);
  
        return [...prev, newToast];
      });
  };

  const router = useRouter();

  useEffect(() => {
    fetchUserProfile();
    
    // Check if modal has been shown before
    const hasSeenMetaMap = localStorage.getItem('hasSeenMetaMap');
    if (!hasSeenMetaMap) {
      setTimeout(() => {
        setShowMetaMapModal(true);
      }, 1000);
    }
  }, []);

  const fetchUserProfile = async () => {
    setIsPageLoading(true);
    try {
      const userId = getUserId();
      const response = await userService.getById(userId);
      
      setUserProfile(response);
      const userRecord = response.records?.[0] || {};
      
      // Map API response to UserData structure
      const mappedUserData: UserData = {
        id: response.id || userId,
        fullName: `${userRecord.firstName || ''} ${userRecord.lastName || ''}`.trim(),
        email: response.email || '',
        username: response.username || '',
        phone: userRecord.telephone || '',
        dateOfBirth: userRecord.dateofBirth || '',
        gender: userRecord.gender || '',
        address: userRecord.address || '',
        city: userRecord.city || '',
        country: userRecord.country || '',
        referralName: response.referralName || response.username || '',
        customerId: response.customerId || `#${userId}`,
        status: response.status || 'Active',
        kycLevel: response.kycLevel || 1
      };
      
      setUserData(mappedUserData);
      const loadingTimer = setTimeout(() => {
        setIsPageLoading(false);
      }, 2000);
  
      return () => clearTimeout(loadingTimer);
    } catch (error) {
      console.error('Error fetching user profile:', error);
      showToast('Failed to load user profile');
    } finally {
      const loadingTimer = setTimeout(() => {
        setIsPageLoading(false);
      }, 2000);
  
      return () => clearTimeout(loadingTimer);
    }
  };

  useEffect(() => {
    document.title = 'User Profile - ePay Online Business Banking';
  }, []);

  const kycDocuments: KYCDocument[] = [
    {
      id: '1',
      type: 'Government Issued Passport',
      verifiedOn: 'Oct 14, 2025',
      status: 'verified'
    },
    {
      id: '2',
      type: 'Proof of Address (Utility Bill)',
      verifiedOn: 'Oct 15, 2025',
      status: 'verified'
    }
  ];

  const loginHistory: LoginHistory[] = [
    {
      id: '1',
      device: 'iPhone 15 Pro',
      browser: 'Chrome',
      location: 'Lagos, NG',
      timestamp: '2 mins ago'
    },
    {
      id: '2',
      device: 'MacBook Pro',
      browser: 'Safari',
      location: 'New York, US',
      timestamp: '2 days ago'
    },
    {
      id: '3',
      device: 'iPhone 15 Pro',
      browser: 'App',
      location: 'London, UK',
      timestamp: '1 week ago'
    }
  ];

  const handleBackToUsers = () => {
    router.back();
  };

  const handleEditProfile = () => {
    if (!userData) return;
    
    setFormData({
      firstName: userData.fullName.split(' ')[0] || '',
      lastName: userData.fullName.split(' ').slice(1).join(' ') || '',
      email: userData.email || '',
      telephone: userData.phone || '',
      gender: userData.gender ? userData.gender.toLowerCase() : '',
      dob: userData.dateOfBirth || '',
      address: userData.address || '',
      country: userData.country || '',
      state: '',
      city: userData.city || ''
    });
    setIsEditUserProfileDetails(true);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    if (formErrors[name as keyof typeof formErrors]) {
      setFormErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {
      firstName: '',
      lastName: '',
      email: '',
      telephone: '',
      gender: '',
      dob: '',
      address: '',
      country: '',
      state: '',
      city: ''
    };
    
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }
    
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }
    
    if (!formData.telephone.trim()) {
      newErrors.telephone = 'Phone number is required';
    }
    
    if (!formData.gender) {
      newErrors.gender = 'Gender is required';
    }

    if (!formData.dob) {
      newErrors.dob = 'Date of birth is required';
    }
    
    if (!formData.country.trim()) {
      newErrors.country = 'Country is required';
    }
    
    if (!formData.city.trim()) {
      newErrors.city = 'City is required';
    }
    
    setFormErrors(newErrors);
    return Object.values(newErrors).every(error => error === '');
  };

  const handleRequestDeactivation = () => {
    setShowDeactivateModal(true);
  };

  const handleResetPassword = () => {
    setShowResetPasswordModal(true);
  };

  const handleEnable2FA = () => {
    setShow2FAModal(true);
  };

  const handleSuspendAccount = () => {
    setShowSuspendAccountModal(true);
  };

  const handleViewDocument = (docId: string) => {
    setLoadingDocId(docId);
    console.log('View document:', docId);

    setTimeout(() => {
      setLoadingDocId(null);
    }, 2000);
  };

  const handleProcessFAModal = () => {
    setShow2FAModal(false);
    showToast('Successfully Enabled 2Factor authentication.!', 'success');
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
    document.body.classList.toggle('dark-theme', newTheme === 'dark');
  };
  
  const handleSaveProfile = async () => {
    if (!validateForm()) {
      showToast('Please fix the errors in the form');
      return;
    }

    setIsLoading(true);
    
    try {
      const userId = getUserId();
      
      const finalData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        gender: formData.gender,
        address: formData.address,
        dob: formData.dob,
        telephone: formData.telephone,
        country: formData.country,
        state: formData.state || '',
        city: formData.city
      };

      const response = await userService.updateProfile(finalData, userId);
      
      if (response.status === "success") {
        updateCompleteProfileDetails(
          formData.firstName, 
          formData.lastName, 
          formData.gender, 
          formData.telephone, 
          formData.dob,
          formData.email, 
          formData.country, 
          formData.state || '',
          formData.city, 
          response.is_profile_complete
        );

        updateNotificationContainer({
          type: "profile_update",
          description: "Your profile has been updated successfully",
          date: new Date().toISOString()
        });

        if (typeof window !== 'undefined' && (window as any).refreshNavbarNotifications) {
          (window as any).refreshNavbarNotifications();
        }

        showToast('Profile updated successfully!', 'success');
        setIsEditUserProfileDetails(false);
        
        await fetchUserProfile();
      }
      
    } catch (error: any) {
      console.error('Error updating profile:', error);
      const errorMessage = error.response?.data?.message || 'Failed to update profile';
      showToast(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // MetaMap Modal Handlers
  const handleMetaMapChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setMetaMapData(prev => ({
      ...prev,
      [name]: value
    }));

    setMetaMapErrors(prev => ({
      ...prev,
      [name]: undefined
    }));
  };


  const handleMetaMapAgree = () => {
    setMetaMapStep(2);
  };

  const handleMetaMapNext = () => {
    const errors: MetaMapErrors = {};

    if (!metaMapData.bvn.trim()) {
      errors.bvn = 'BVN is required';
    }

    if (!metaMapData.firstName.trim()) {
      errors.firstName = 'First name is required';
    }

    if (!metaMapData.lastName.trim()) {
      errors.lastName = 'Last name is required';
    }

    if (!metaMapData.dob) {
      errors.dob = 'Date of birth is required';
    }

    if (Object.keys(errors).length > 0) {
      setMetaMapErrors(errors);
      return;
    }

    setMetaMapErrors({});
    localStorage.setItem('hasSeenMetaMap', 'true');
    setShowMetaMapModal(false);
    setMetaMapStep(1);
    showToast('Verification submitted successfully!', 'success');
  };


  const handleMetaMapClose = () => {
    setShowMetaMapExit(true);
  };

  const handleMetaMapExit = () => {
    localStorage.setItem('hasSeenMetaMap', 'true');
    setShowMetaMapModal(false);
    setShowMetaMapExit(false);
    setMetaMapStep(1);
  };

  const handleMetaMapContinue = () => {
    setShowMetaMapExit(false);
  };

  if (isPageLoading || !userData) {
    return <LoadingScreen />;
  }

  return (
    <>
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : 'light'}`}>
      <Sidebar />

      <main className={`main-content`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        
        <div className="scrollable-content">
            <div className={`user-profile-container ${theme == "dark" ? "bg-light" : "bg-dark"}`}>
              <div className="toastrs">
                {toasts.map((toast) => (
                  <div
                    key={toast.id}
                    className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}
                  >
                    <div className="toast-icon">
                      <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true"></i>
                    </div>
                    <div className="toast-message">{toast.message}</div>
                  </div>
                ))}
              </div>
              
              <div className="user-profile-header">
                <button className={`user-profile-back-btn ${theme === "dark" ? "color-light" : "color-dark"}`} onClick={handleBackToUsers}>
                  <ArrowLeft size={20} />
                  <span>Back to Users</span>
                </button>
              </div>

              <div className="user-profile-info-card">
                <div className="user-profile-info-left">
                  <div className="user-profile-avatar">
                  <Image
                    src="/assets/images/user-profile.jpg"
                    alt={userData?.fullName ?? 'User profile'}
                    width={48}
                    height={48}
                  />
                  </div>
                  <div className="user-profile-basic-info">
                    <div className="user-profile-name-row">
                      <h1 className="user-profile-name">{userData.fullName.split(' ')[0]} {userData.fullName.split(' ')[userData.fullName.split(' ').length - 1]}</h1>
                      <span className={`user-profile-status-badge ${userData.status.toLowerCase()}`}>
                        {userData.status}
                      </span>
                      <span className="user-profile-kyc-badge">
                        <CheckCircle size={14} />
                        KYC Level {userData.kycLevel}
                      </span>
                    </div>
                    <p className="user-profile-customer-id">Customer ID: {userData.customerId}</p>
                  </div>
                </div>
                <div className="user-profile-info-actions">
                  <button className="user-profile-btn-secondary" onClick={handleRequestDeactivation}>
                    Request Deactivation
                  </button>
                  {!isEditUserProfileDetails && 
                  ( <button className="user-profile-btn-primary" onClick={handleEditProfile}>
                    Edit Profile
                  </button>)
                  }
                 
                </div>
              </div>

              <div className="user-profile-content">
                <div className="user-profile-main">
                  <div className="user-profile-info-section">
                    <div className="user-profile-section-header">
                      <User size={20} />
                      <h2>{isEditUserProfileDetails? 'Edit Personal Information' : 'Personal Information'}</h2>
                    </div>
                    {isEditUserProfileDetails ? (
                      <>
                      <div className="settings-form-grid">
                        <div className="settings-form-group">
                          <label>First Name</label>
                          <input 
                            type="text"
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleChange}
                            className={formErrors.firstName ? 'error' : ''}
                          />
                          {formErrors.firstName && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.firstName}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Last Name</label>
                          <input 
                            type="text"
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleChange}
                            className={formErrors.lastName ? 'error' : ''}
                          />
                          {formErrors.lastName && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.lastName}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Email Address</label>
                          <input 
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className={formErrors.email ? 'error' : ''}
                          />
                          {formErrors.email && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.email}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Phone Number</label>
                          <input 
                            type="tel"
                            name="telephone"
                            value={formData.telephone}
                            onChange={handleChange}
                            className={formErrors.telephone ? 'error' : ''}
                          />
                          {formErrors.telephone && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.telephone}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Gender</label>
                          <select 
                            className={`settings-select ${formErrors.gender ? 'error' : ''}`}
                            name='gender' 
                            id="gender" 
                            value={formData.gender}
                            onChange={handleChange}
                          >
                            <option value="">--Select--</option>
                            <option value="male">Male</option>
                            <option value="female">Female</option>
                            <option value="other">Other</option>
                            <option value="prefer-not-to-say">Prefer not to say</option>
                          </select>
                          {formErrors.gender && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.gender}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Date of Birth</label>
                          <input 
                            type="date"
                            name="dob"
                            value={formData.dob}
                            onChange={handleChange}
                            className={formErrors.dob ? 'error' : ''}
                          />
                          {formErrors.dob && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.dob}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Address (Optional)</label>
                          <input 
                            type="text" 
                            name='address'
                            value={formData.address}
                            onChange={handleChange}
                            className={formErrors.address ? 'error' : ''}
                          />
                          {formErrors.address && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.address}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>Country</label>
                          <input 
                            type="text" 
                            name='country'
                            value={formData.country}
                            onChange={handleChange}
                            className={formErrors.country ? 'error' : ''}
                          />
                          {formErrors.country && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.country}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>State (Optional)</label>
                          <input 
                            type="text" 
                            name='state'
                            value={formData.state}
                            onChange={handleChange}
                            className={formErrors.state ? 'error' : ''}
                          />
                          {formErrors.state && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.state}
                            </span>
                          )}
                        </div>
                        <div className="settings-form-group">
                          <label>City</label>
                          <input 
                            type="text" 
                            name='city'
                            value={formData.city}
                            onChange={handleChange}
                            className={formErrors.city ? 'error' : ''}
                          />
                          {formErrors.city && (
                            <span className="error-message" style={{color: '#ef4444', fontSize: '0.875rem', marginTop: '0.25rem', display: 'block'}}>
                              {formErrors.city}
                            </span>
                          )}
                        </div>
                      </div>

                      <div className="settings-actions">
                        <button className="settings-btn-secondary" onClick={()=>setIsEditUserProfileDetails(false)}>Cancel</button>
                        <button className="settings-btn-primary" onClick={handleSaveProfile} disabled={isLoading}>
                          {isLoading ? 'Saving...' : 'Save Changes'}
                        </button>
                      </div>
                      </>
                    ):(
                      <>
                       <div className="user-profile-info-grid">
                          <div className="user-profile-info-item">
                            <label>FULL NAME</label>
                            <p>{userData.fullName}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>EMAIL ADDRESS</label>
                            <p>{userData.email}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>PHONE NUMBER</label>
                            <p>{userData.phone}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>USERNAME</label>
                            <p>{userData.username}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>DATE OF BIRTH</label>
                            <p>{userData.dateOfBirth}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>GENDER</label>
                            <p>{userData.gender}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>ADDRESS</label>
                            <p>{userData.address}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>COUNTRY</label>
                            <p>{userData.country}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>CITY/STATE</label>
                            <p>{userData.city}</p>
                          </div>
                          <div className="user-profile-info-item">
                            <label>REFERRAL NAME</label>
                            <p>{userData.referralName}</p>
                          </div>
                        </div>
                      </>
                    )}
                  </div>

                  <div className="user-profile-info-section">
                    <div className="user-profile-section-header">
                      <FileText size={20} />
                      <h2>KYC & Verification Documents</h2>
                    </div>
                    <div className="user-profile-documents-list">
                      {kycDocuments.map((doc) => (
                        <div key={doc.id} className="user-profile-document-item">
                          <div className="user-profile-document-icon">
                            <FileText size={20} />
                          </div>
                          <div className="user-profile-document-info">
                            <h3>{doc.type}</h3>
                            <p>Verified on {doc.verifiedOn}</p>
                          </div>
                           {loadingDocId === doc.id ? (
                            <>
                              <div className={`setting-notif-loader-container"}`}>
                                <div className="settings-notif-spinner"></div>
                              </div>
                            </>
                           ) : (
                            <>
                              <button className="user-profile-btn-view" onClick={() => handleViewDocument(doc.id)} disabled={loadingDocId === doc.id}>View</button>
                            </>
                            )}
                          
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="user-profile-sidebar">
                  <div className="user-profile-sidebar-section">
                    <h3 className="user-profile-sidebar-title">Security Actions</h3>
                    <div className="user-profile-security-actions">
                      <button className="user-profile-security-action-btn" onClick={handleResetPassword}>
                        <Key size={18} />
                        <span>Reset Password</span>
                      </button>
                      <button className="user-profile-security-action-btn" onClick={handleEnable2FA}>
                        <Shield size={18} />
                        <span>Enable 2FA (Force)</span>
                      </button>
                      <button className="user-profile-security-action-btn danger" onClick={handleSuspendAccount}>
                        <AlertCircle size={18} />
                        <span>Suspend Account</span>
                      </button>
                    </div>
                  </div>

                  <div className="user-profile-sidebar-section">
                    <h3 className="user-profile-sidebar-title">Login History</h3>
                    <div className="user-profile-login-history-list">
                      {loginHistory.map((login) => (
                        <div key={login.id} className="user-profile-login-history-item">
                          <div className="user-profile-login-icon">
                            {login.device.includes('iPhone') || login.device.includes('App') ? 
                              <Smartphone size={20} /> : 
                              <Monitor size={20} />
                            }
                          </div>
                          <div className="user-profile-login-info">
                            <h4>{login.device} • {login.browser}</h4>
                            <p>{login.location} • {login.timestamp}</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {showDeactivateModal && (
                <>
                  <div className="user-profile-modal-overlay" onClick={() => setShowDeactivateModal(false)} />
                  <div className="user-profile-modal">
                    <div className="user-profile-modal-content">
                      <div className="user-profile-modal-icon warning">
                        <AlertCircle size={48} />
                      </div>
                      <h3>Request Account Deactivation</h3>
                      <p>Are you sure you want to request deactivation for this account? This action requires admin approval.</p>
                      <div className="user-profile-modal-actions">
                        <button className="user-profile-btn-secondary" onClick={() => setShowDeactivateModal(false)}>
                          Cancel
                        </button>
                        <button className="user-profile-btn-danger" onClick={() => {
                          console.log('Deactivation requested');
                          setShowDeactivateModal(false);
                        }}>
                          Request Deactivation
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {showResetPasswordModal && (
                <>
                  <div className="user-profile-modal-overlay" onClick={() => setShowResetPasswordModal(false)} />
                  <div className="user-profile-modal">
                    <div className="user-profile-modal-content">
                      <div className="user-profile-modal-icon primary">
                        <Key size={48} />
                      </div>
                      <h3>Reset User Password</h3>
                      <p>A password reset link will be sent to the user's email address.</p>
                      <div className="user-profile-modal-actions">
                        <button className="user-profile-btn-secondary" onClick={() => setShowResetPasswordModal(false)}>
                          Cancel
                        </button>
                        <button className="user-profile-btn-primary" onClick={() => {
                          console.log('Password reset sent');
                          setShowResetPasswordModal(false);
                        }}>
                          Send Reset Link
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {isShowSuspendAccountModal && (
                <>
                  <div className="user-profile-modal-overlay" onClick={() => setShowSuspendAccountModal(false)} />
                  <div className="user-profile-modal">
                    <div className="user-profile-modal-content">
                      <div className="user-profile-modal-icon warning">
                        <AlertCircle size={48} />
                      </div>
                      <h3>Request Account Suspension</h3>
                      <p>Are you sure you want to request suspend your account? This action requires admin approval.</p>
                      <div className="user-profile-modal-actions">
                        <button className="user-profile-btn-secondary" onClick={() => setShowSuspendAccountModal(false)}>
                          Cancel
                        </button>
                        <button className="user-profile-btn-danger" onClick={() => {
                          setShowSuspendAccountModal(false);
                        }}>
                          Request Suspension
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {show2FAModal && (
                  <>
                    <div className="user-profile-modal-overlay" onClick={() => setShow2FAModal(false)} />
                      <div className="user-profile-modal">
                        <div className="user-profile-modal-content">
                          <div className="user-profile-modal-icon primary">
                            <Shield size={48} />
                          </div>
                          <h3>Enable 2FA (Force)</h3>
                          <p>This is another nice step to secure your account, on every time someone tries to login - you receive private clearance code and until you give that to that person only then will he gain access to you account</p>
                          <div className="user-profile-modal-actions">
                            <button className="user-profile-btn-secondary" onClick={() => setShow2FAModal(false)}>
                              Cancel
                            </button>
                            <button className="user-profile-btn-primary" onClick={handleProcessFAModal}>
                              Enable 2FA
                            </button>
                          </div>
                        </div>
                      </div>
                  </>
              )}

            {showMetaMapModal && (
              <>
                <div className="metamap-modal-overlay" />
                <div className="metamap-modal">
                  <div className="metamap-modal-header">
                    <div className="metamap-logo">
                      {metaMapStep === 2 && <button className="metamap-back-btn" onClick={() => setMetaMapStep(1)}>←</button>}
                      <span style={{fontWeight: '600', fontSize: '18px', color:"#3f444b"}}>KYC Form</span>
                    </div>
                    <button className="metamap-close-btn" onClick={handleMetaMapClose}>×</button>
                  </div>

                  {/* Show Exit Modal */}
                  {showMetaMapExit ? (
                    <div className="metamap-exit-modal">
                      <h3>Are you sure you want to leave?</h3>
                      <p>You will have to restart the verification</p>
                      
                      <div className="metamap-exit-illustration">
                        {/* You can replace this with an actual image */}
                        <div className="metamap-exit-icon">👋</div>
                      </div>
                      
                    
                      <div className="metamap-exit-actions">
                        <button className="metamap-exit-btn" onClick={handleMetaMapExit}>Exit</button>
                        <button className="metamap-continue-btn" onClick={handleMetaMapContinue}>Continue verification</button>
                      </div>
                    </div>
                  ) : (
                    <>
                      {/* Step 1: Agreement */}
                      {metaMapStep === 1 && (
                        <div className="metamap-step">
                          <h2 className="metamap-title">Let's verify your identity</h2>
                          <p className="metamap-subtitle">To get verified, you will need to:</p>

                          <div className="metamap-steps-list">
                            <div className="metamap-step-item">
                              <div className="metamap-step-icon-svg">
                                <img 
                                  src="/assets/icon.svg" 
                                  alt="Enter details icon"
                                  width={32}
                                  height={32}
                                />
                              </div>
                              <span style={{color:"#232939"}}>Enter your details</span>
                            </div>
                            <div className="metamap-step-item">
                              <div className="metamap-step-icon-svg">
                                <img 
                                  src="/assets/selfie.svg" 
                                  alt="Take a selfie icon"
                                  width={32}
                                  height={32}
                                />
                              </div>
                              <span style={{color:"#232939"}}>Take a selfie</span>
                            </div>
                          </div>

                          <div className="metamap-terms">
                            <p>By clicking "Agree and Continue" I consent to Company and its service provider, MetaMap, obtaining and disclosing a scan of my face geometry and barcode of my ID for the purpose of verifying my identity pursuant to Company and MetaMap's Privacy Policies and for improving and updating MetaMap products or services (including its algorithm). Company and MetaMap shall store the biometric data for no longer than 3 years (or as determined by your local regulation).</p>
                            <p>I can exercise my privacy rights, including withdrawal of my consent, by contacting privacy@metamap.com.</p>
                        
                            <p>I have read and agreed to MetaMap <a href="#">Privacy Policy</a>.</p>
                          </div>

                          <button className="metamap-btn-primary" onClick={handleMetaMapAgree}>
                            Agree and Continue
                          </button>
                        </div>
                      )}

                      {/* Step 2: Form */}
                      {metaMapStep === 2 && (
                        <div className="metamap-step">
                          <div className="metamap-form">
                            <div className="metamap-form-group">
                              <label>BVN</label>
                              <input 
                                type="text"
                                name="bvn"
                                placeholder="Enter your BVN"
                                value={metaMapData.bvn}
                                onChange={handleMetaMapChange}
                              />
                                {metaMapErrors.bvn && (
                                  <span className="error-message">{metaMapErrors.bvn}</span>
                                )}
                            </div>

                            <div className="metamap-form-group">
                              <label>First name</label>
                              <input 
                                type="text"
                                name="firstName"
                                placeholder="Enter your first name"
                                value={metaMapData.firstName}
                                onChange={handleMetaMapChange}
                              />
                              {metaMapErrors.firstName && (
                                <span className="error-message">{metaMapErrors.firstName}</span>
                              )}
                            </div>

                            <div className="metamap-form-group">
                              <label>Last name</label>
                              <input 
                                type="text"
                                name="lastName"
                                placeholder="Enter your last name"
                                value={metaMapData.lastName}
                                onChange={handleMetaMapChange}
                              />
                              {metaMapErrors.lastName && (
                                <span className="error-message">{metaMapErrors.lastName}</span>
                              )}
                            </div>

                            <div className="metamap-form-group">
                              <label>Date of Birth</label>
                              <input 
                                type="date"
                                name="dob"
                                placeholder="Date of Birth"
                                value={metaMapData.dob}
                                onChange={handleMetaMapChange}
                              />
                              <small>Requested format: dd/mm/yyyy</small>
                                {metaMapErrors.dob && (
                                  <span className="error-message">{metaMapErrors.dob}</span>
                                )}
                            </div>
                          </div>

                          <button className="metamap-btn-primary" onClick={handleMetaMapNext}>
                            Next
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              </>
            )}
          </div>
          <Footer theme={theme} />
        </div>
      </main>
       <MobileNav activeTab="profile" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal
        isOpen={isDepositOpen} 
        onClose={() => setIsDepositOpen(false)} 
        theme={theme} 
      />
    </div>
    </>
  );
};

export default UserProfile;