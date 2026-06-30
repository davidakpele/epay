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
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import LoadingScreen from '@/components/loader/Loadingscreen';
import { useRouter } from 'next/navigation';
import { Toast } from '@/app/types/auth';
import { KYCDocument, LoginHistory, MetaMapErrors, UserData, UserSettings } from '@/app/types/utils';
import { userService, getUserId, updateHasSeenMetaMap, updateCompleteProfileDetails, updateNotificationContainer, capitalizeFirstLetter, getUserEmail, getUserFirstName, getUserLastName, getHasSeenMetaMap, getToken } from '@/app/api/index';
import KYCSuccessModal from '@/components/KYCSuccessModal';
import { City, Country, State } from 'country-state-city';
import SupportChatBot from '@/components/SupportChatBot';

const UserProfile = () => {
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);
  const [showResetPasswordModal, setShowResetPasswordModal] = useState(false);
  const [resetLinkLoading, setResetLinkLoading] = useState(false);
  const [resetLinkSent, setResetLinkSent] = useState(false);
  const [show2FAModal, setShow2FAModal] = useState(false);
  const [showMetaMapModal, setShowMetaMapModal] = useState(false);
  const [metaMapStep, setMetaMapStep] = useState(1);
  const [showMetaMapExit, setShowMetaMapExit] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [metaMapData, setMetaMapData] = useState({
    bvn: '',
    firstName: '',
    lastName: '',
    dob: '',
    gender: '',
    country: '',
    countryCode: '',   // e.g. 'NG'
    state: '',
    stateCode: '',     // e.g. 'OY'
    city: '',
  });
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [isEditUserProfileDetails, setIsEditUserProfileDetails] = useState(false);
  const [isShowSuspendAccountModal, setShowSuspendAccountModal] = useState(false);
  const [suspendLoading, setSuspendLoading] = useState(false);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);
  const [uploadingDocId, setUploadingDocId] = useState<string | null>(null);
  const passportRef    = React.useRef<HTMLInputElement | null>(null);
  const utilityBillRef = React.useRef<HTMLInputElement | null>(null);
  const docUploadRefs: Record<string, React.RefObject<HTMLInputElement | null>> = {
    passport:     passportRef,
    utility_bill: utilityBillRef,
  };
  const [userData, setUserData] = useState<UserData | null>(null);
  const [userProfile, setUserProfile] = useState<any>(null);
  const [profileImage, setProfileImage] = useState('/assets/images/user-profile.jpg');
  const [metaMapErrors, setMetaMapErrors] = useState<MetaMapErrors>({});
  const [showKYCSuccess, setShowKYCSuccess] = useState(false);
  
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

    const firstName = getUserFirstName() || '';
    const lastName = getUserLastName() || '';
    setMetaMapData(prev => ({ ...prev, firstName, lastName }));
  }, []);

  const fetchUserProfile = async () => {
    try {
      const userId = getUserId();
      const response = await userService.getById(userId);

      setUserProfile(response);
      const userRecord = response.records?.[0] || {};

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

      const isProfileIncomplete = !userRecord.telephone
        || !userRecord.gender
        || !userRecord.dateofBirth
        || !userRecord.country
        || !userRecord.city;

      const hasSeenMetaMap = getHasSeenMetaMap();

      if (isProfileIncomplete && !hasSeenMetaMap) {
        setTimeout(() => setShowMetaMapModal(true), 1000);
      }

      const firstName = userRecord.firstName || getUserFirstName() || '';
      const lastName = userRecord.lastName || getUserLastName() || '';
      setMetaMapData(prev => ({ ...prev, firstName, lastName }));

    } catch (error) {
      console.error('Error fetching user profile:', error);
      showToast('Failed to load user profile');
    } finally {
      setTimeout(() => setIsPageLoading(false), 2000);
    }
  };

  useEffect(() => {
    document.title = 'User Profile - ePay Online Business Banking';
  }, []);

  useEffect(() => {
      try {
          const storedData = localStorage.getItem('data');
          if (storedData) {
              const parsedData = JSON.parse(storedData);
              const userPhoto = parsedData?.user?.photo;
              if (userPhoto && userPhoto !== '/assets/images/user-profile.jpg') {
                  setProfileImage(userPhoto);
              }
          }
      } catch (error) {
          console.error('Error loading profile image:', error);
      }
  }, []);
  
  useEffect(() => {
    const handleStorageChange = () => {
        try {
            const storedData = localStorage.getItem('data');
            if (storedData) {
                const parsedData = JSON.parse(storedData);
                const userPhoto = parsedData?.user?.photo;
                if (userPhoto && userPhoto !== '/assets/images/user-profile.jpg') {
                    setProfileImage(userPhoto);
                } else {
                    setProfileImage('/assets/images/user-profile.jpg');
                }
            }
        } catch (error) {
            console.error('Error refreshing profile image:', error);
        }
    };
    window.addEventListener('profileImageUpdated', handleStorageChange);
    return () => window.removeEventListener('profileImageUpdated', handleStorageChange);
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
    setResetLinkSent(false);
    setShowResetPasswordModal(true);
  };

  const handleSendResetLink = async () => {
    const userId = getUserId();
    if (!userId) return;
    setResetLinkLoading(true);
    try {
      const response = await userService.sendResetPasswordLink(userId);
      setResetLinkSent(true);
      showToast('Password reset link sent to your email!', 'success');
    } catch (err: any) {
      showToast(err?.message || 'Failed to send reset link. Please try again.');
    } finally {
      setResetLinkLoading(false);
    }
  };

  const handleEnable2FA = () => {
    setShow2FAModal(true);
  };

  const handleSuspendAccount = () => {
    setShowSuspendAccountModal(true);
  };

  const handleConfirmSuspend = async () => {
    const userId = getUserId();
    if (!userId) return;
    setSuspendLoading(true);
    try {
      const response = await userService.suspendAccount(userId);
      if (response?.status === 'success') {
        showToast('Your account has been suspended. A confirmation email has been sent.', 'success');
        setShowSuspendAccountModal(false);
        // Force logout after a short delay so user sees the toast
        setTimeout(() => {
          router.push('/auth/logout');
        }, 2500);
      } else {
        showToast(response?.message || 'Failed to suspend account. Please try again.');
      }
    } catch (err: any) {
      showToast(err?.message || 'Failed to suspend account. Please try again.');
    } finally {
      setSuspendLoading(false);
    }
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

  const handleCountryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = Country.getAllCountries().find(c => c.isoCode === e.target.value);
    setMetaMapData(prev => ({
      ...prev,
      country: selected?.name || '',
      countryCode: e.target.value,
      state: '', stateCode: '', 
      city: '',
    }));
  };

  const handleStateChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = State.getStatesOfCountry(metaMapData.countryCode)
      .find(s => s.isoCode === e.target.value);
    setMetaMapData(prev => ({
      ...prev,
      state: selected?.name || '',
      stateCode: e.target.value,
      city: '',
    }));
  };

  const handleCityChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setMetaMapData(prev => ({ ...prev, city: e.target.value }));
  };

  const handleMetaMapNext = async () => {
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
  
    if (!metaMapData.gender.trim()) {
      errors.gender = 'Gender is required';
    }
  
    if (!metaMapData.countryCode.trim()) {
      errors.country = 'Country is required';
    }
  
    if (!metaMapData.stateCode.trim()) {
      errors.state = 'State is required';
    }
  
    if (!metaMapData.city.trim()) {
      errors.city = 'City is required';
    }
  
    if (Object.keys(errors).length > 0) {
      setMetaMapErrors(errors);
      return;
    }
    const userId = getUserId();
    const userEmail = getUserEmail();

    const finalData = {
      bvn: metaMapData.bvn,
      firstName: metaMapData.firstName,
      lastName: metaMapData.lastName,
      email: userEmail,
      address: '',
      telephone: '',
      gender: metaMapData.gender,
      dob: metaMapData.dob,
      country: metaMapData.country,
      state: metaMapData.state || '',
      city: metaMapData.city || '',
    };

    const response = await userService.updateProfile(finalData, userId);
     if (response.status === "success") {
        setMetaMapErrors({});
        updateHasSeenMetaMap(true);
        setShowMetaMapModal(false);
        setMetaMapStep(1);
        setShowKYCSuccess(true);

        updateNotificationContainer({
          type: "profile_update",
          description: "Your profile has been updated successfully",
          date: new Date().toISOString()
        });

        if (typeof window !== 'undefined' && (window as any).refreshNavbarNotifications) {
          (window as any).refreshNavbarNotifications();
        }
        showToast('Verification submitted successfully!', 'success');
        await fetchUserProfile();
     }
 
  };

  const handleMetaMapSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    setMetaMapData(prev => ({ ...prev, [name]: value }));
    setMetaMapErrors(prev => ({ ...prev, [name]: undefined }));
  };
  const handleMetaMapClose = () => {
    setShowMetaMapExit(true);
    updateHasSeenMetaMap(false);
  };

  const handleMetaMapExit = () => {
    setShowMetaMapModal(false);
    setShowMetaMapExit(false);
    updateHasSeenMetaMap(false);
    setMetaMapStep(1);
  };

  const handleMetaMapContinue = () => {
    setShowMetaMapExit(false);
  };

  // ── KYC document handlers ────────────────────────────────────────────────

  const handleUploadKycDocument = async (docType: string, file: File) => {
    const userId = getUserId();
    if (!userId || !file) return;

    setUploadingDocId(docType);
    try {
      const token = getToken();
      // Use the same base URL the rest of the app uses
      const url = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8292/api'}/user/${userId}/kyc/upload?docType=${docType}`;

      // Send the file as raw binary with its real MIME type.
      // The controller accepts both multipart/form-data and octet-stream.
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': file.type || 'application/octet-stream',
        },
        body: file,
      });

      const response = await res.json();

      if (response?.status === 'success') {
        showToast(
          docType === 'passport'
            ? 'Passport uploaded successfully!'
            : 'Utility bill uploaded successfully!',
          'success'
        );
        await fetchUserProfile();
      } else {
        showToast(response?.message || 'Upload failed. Please try again.');
      }
    } catch (err: any) {
      showToast(err?.message || 'Failed to upload document.');
    } finally {
      setUploadingDocId(null);
    }
  };

  const handleViewKycDocument = (docType: string) => {
    const userId = getUserId();
    const token  = getToken();
    if (!userId) return;
    setLoadingDocId(docType);

    // Build the authenticated URL — the backend streams the file inline.
    // We can't open a bearer-auth URL directly in a new tab, so we fetch
    // the blob and create a temporary object URL.
    const url = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8292/api'}/user/${userId}/kyc/document?docType=${docType}`;

    fetch(url, { headers: { 'Authorization': `Bearer ${token}` } })
      .then(res => {
        if (!res.ok) throw new Error('Failed to load document');
        return res.blob();
      })
      .then(blob => {
        const objUrl = URL.createObjectURL(blob);
        const newTab = window.open(objUrl, '_blank');
        if (!newTab) {
          showToast('Unable to open document. Please allow popups for this site.');
        }
        // Release the object URL after a short delay
        setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
      })
      .catch(() => showToast('Failed to load document. Please try again.'))
      .finally(() => setLoadingDocId(null));
  };

  // ────────────────────────────────────────────────────────────────────────

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
                  <img
                    src={profileImage}
                    alt="User profile"
                    width={21}
                    height={21}
                    className="settings-avatar"
                    onError={(e) => {
                      e.currentTarget.src = '/assets/images/user-profile.jpg';
                    }}
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
                            onChange={handleChange}>
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
                        {/* ── Redesigned Personal Information cards ── */}
                        <div className="pi-grid">
                          {[
                            { icon: 'fa-user', label: 'Full Name',      value: capitalizeFirstLetter(userData.fullName),       col: 1 },
                            { icon: 'fa-envelope', label: 'Email Address', value: userData.email,                              col: 1 },
                            { icon: 'fa-phone', label: 'Phone Number',  value: userData.phone || '—',                         col: 1 },
                            { icon: 'fa-at', label: 'Username',         value: userData.username,                              col: 1 },
                            { icon: 'fa-birthday-cake', label: 'Date of Birth', value: userData.dateOfBirth || '—',           col: 1 },
                            { icon: 'fa-venus-mars', label: 'Gender',   value: capitalizeFirstLetter(userData.gender) || '—', col: 1 },
                            { icon: 'fa-map-marker-alt', label: 'Address', value: capitalizeFirstLetter(userData.address) || '—', col: 2 },
                            { icon: 'fa-globe', label: 'Country',       value: capitalizeFirstLetter(userData.country) || '—', col: 1 },
                            { icon: 'fa-city', label: 'City / State',   value: capitalizeFirstLetter(userData.city) || '—',   col: 1 },
                            { icon: 'fa-link', label: 'Referral Name',  value: userData.referralName || '—',                  col: 1 },
                          ].map((item, i) => (
                            <div
                              key={i}
                              className={`pi-card${item.col === 2 ? ' pi-card--wide' : ''}`}
                            >
                              <div className="pi-card-icon">
                                <i className={`fas ${item.icon}`} />
                              </div>
                              <div className="pi-card-body">
                                <span className="pi-card-label">{item.label}</span>
                                <span className="pi-card-value">{item.value}</span>
                              </div>
                            </div>
                          ))}
                        </div>

                        {/* ── Profile completion bar ── */}
                        <div className="pi-completion-bar-wrap">
                          {(() => {
                            const pct = (() => {
                              const fields = [
                                userData.fullName, userData.email, userData.phone,
                                userData.username, userData.dateOfBirth, userData.gender,
                                userData.country, userData.city
                              ];
                              const filled = fields.filter(f => f && f !== '—' && f.trim() !== '').length;
                              return Math.round((filled / fields.length) * 100);
                            })();
                            return (
                              <>
                                <div className="pi-completion-header">
                                  <span className="pi-completion-label">Profile Completeness</span>
                                  <span className="pi-completion-pct" style={{ color: pct === 100 ? '#2b0f56' : '#c1aa01ff' }}>{pct}%</span>
                                </div>
                                <div className="pi-completion-track">
                                  <div className="pi-completion-fill" style={{ width: `${pct}%`, background: pct === 100 ? '#2b0f56' : '#c1aa01ff' }} />
                                </div>
                                {pct < 100 && (
                                  <p className="pi-completion-hint">
                                    <i className="fas fa-info-circle" style={{ marginRight: 5, color: '#2b0f56' }} />
                                    Click <strong>Edit Profile</strong> to complete your information and unlock full account features.
                                  </p>
                                )}
                              </>
                            );
                          })()}
                        </div>
                      </>
                    )}
                  </div>

                  <div className="user-profile-info-section">
                    <div className="user-profile-section-header">
                      <FileText size={20} />
                      <h2>KYC &amp; Identity Verification</h2>
                    </div>

                    {(() => {
                      const fields = [
                        userData.fullName, userData.email, userData.phone,
                        userData.username, userData.dateOfBirth, userData.gender,
                        userData.country, userData.city
                      ];
                      const filled = fields.filter(f => f && f !== '—' && f.trim() !== '').length;
                      const pct    = Math.round((filled / fields.length) * 100);
                      const isDone = pct === 100;

                      return (
                        <>
                          {/* Tier banner */}
                          <div className={`kyc-tier-banner ${isDone ? 'kyc-tier-banner--done' : 'kyc-tier-banner--pending'}`}>
                            <div className="kyc-tier-left">
                              <div className={`kyc-tier-icon ${isDone ? 'kyc-tier-icon--done' : 'kyc-tier-icon--pending'}`}>
                                <i className={`fas ${isDone ? 'fa-crown' : 'fa-user'}`} />
                              </div>
                              <div>
                                <p className="kyc-tier-name">{isDone ? 'Tier 2 — Verified' : 'Tier 1 — Limited'}</p>
                                <p className="kyc-tier-sub">
                                  {isDone
                                    ? 'Full access: transfers, deposits & withdrawals enabled'
                                    : 'Complete KYC to unlock transfers, deposits & withdrawals'}
                                </p>
                              </div>
                            </div>
                            <span className={`kyc-tier-badge ${isDone ? 'kyc-tier-badge--done' : 'kyc-tier-badge--pending'}`}>
                              {isDone ? 'Verified' : 'Pending'}
                            </span>
                          </div>

                          {/* Progress bar */}
                          <div style={{ margin: '20px 0 8px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                              <span style={{ fontSize: 13, fontWeight: 600, color: '#374151' }}>KYC Completion</span>
                              <span style={{ fontSize: 13, fontWeight: 700, color: isDone ? '#2b0f56' : '#c1aa01ff' }}>{pct}%</span>
                            </div>
                            <div style={{ height: 8, background: '#e5e7eb', borderRadius: 999, overflow: 'hidden' }}>
                              <div style={{
                                height: '100%', borderRadius: 999,
                                background: isDone
                                  ? 'linear-gradient(90deg, #2b0f56, #2b0f56)'
                                  : 'linear-gradient(90deg, #d97706, #d97706)',
                                width: `${pct}%`, transition: 'width 0.6s ease'
                              }} />
                            </div>
                          </div>

                          {/* Steps checklist */}
                          <div className="kyc-checklist">
                            {[
                              { label: 'Personal details (name, email)',  done: !!(userData.fullName && userData.email) },
                              { label: 'Phone number',                    done: !!userData.phone },
                              { label: 'Date of birth & gender',          done: !!(userData.dateOfBirth && userData.gender) },
                              { label: 'Country & city',                  done: !!(userData.country && userData.city) },
                            ].map((step, i) => (
                              <div key={i} className={`kyc-checklist-item ${step.done ? 'kyc-checklist-item--done' : ''}`}>
                                <div className={`kyc-check-circle ${step.done ? 'kyc-check-circle--done' : ''}`}>
                                  <i className={`fas ${step.done ? 'fa-check' : 'fa-circle'}`}
                                     style={{ fontSize: step.done ? 10 : 6 }} />
                                </div>
                                <span>{step.label}</span>
                              </div>
                            ))}
                          </div>

                          {/* CTA / done note */}
                          {!isDone ? (
                            <button
                              className="kyc-cta-btn"
                              onClick={() => {
                                if (!getHasSeenMetaMap()) {
                                  setShowMetaMapModal(true);
                                } else {
                                  handleEditProfile();
                                }
                              }}
                            >
                              <i className="fas fa-id-card" style={{ marginRight: 8 }} />
                              Complete KYC Verification
                            </button>
                          ) : (
                            <>
                              <div className="kyc-done-note">
                                <i className="fas fa-check-circle" style={{ color: "#fff", marginRight: 8 }} />
                                Your identity has been verified. You now have full Tier 2 access.
                              </div>
                              {/* Interactive KYC document cards — always shown (not just when done) */}
                            </>
                          )}

                          {/* ── KYC document upload/view cards (always visible) ── */}
                          {(() => {
                            const rec = userProfile?.records?.[0] || {};
                            const docs = [
                              {
                                docType:   'passport',
                                label:     'Government Issued Passport',
                                icon:      'fa-id-card',
                                uploaded:  !!rec.passportDoc,
                                fileUrl:   rec.passportDoc,
                              },
                              {
                                docType:   'utility_bill',
                                label:     'Proof of Address (Utility Bill)',
                                icon:      'fa-file-alt',
                                uploaded:  !!rec.utilityBillDoc,
                                fileUrl:   rec.utilityBillDoc,
                              },
                            ];

                            return (
                              <div className="kyc-doc-list">
                                {docs.map((doc) => (
                                  <div key={doc.docType} className={`kyc-doc-card ${doc.uploaded ? 'kyc-doc-card--uploaded' : ''}`}>
                                    {/* Hidden file input */}
                                    <input
                                      type="file"
                                      accept="image/jpeg,image/png,image/webp,application/pdf"
                                      style={{ display: 'none' }}
                                      ref={docUploadRefs[doc.docType]}
                                      onChange={(e) => {
                                        const file = e.target.files?.[0];
                                        if (file) handleUploadKycDocument(doc.docType, file);
                                        e.target.value = '';
                                      }}
                                    />

                                    {/* Icon */}
                                    <div className={`kyc-doc-icon ${doc.uploaded ? 'kyc-doc-icon--uploaded' : ''}`}>
                                      <i className={`fas ${doc.icon}`} />
                                    </div>

                                    {/* Info */}
                                    <div className="kyc-doc-info">
                                      <p className="kyc-doc-label">{doc.label}</p>
                                      <p className="kyc-doc-status">
                                        {doc.uploaded
                                          ? <><i className="fas fa-check-circle" style={{ color: '#0a9a08d9', marginRight: 4 }} />Uploaded</>
                                          : <><i className="fas fa-exclamation-circle" style={{ color: '#d97706', marginRight: 4 }} />Not uploaded</>
                                        }
                                      </p>
                                    </div>

                                    {/* Actions */}
                                    <div className="kyc-doc-actions">
                                      {/* Upload / Replace button */}
                                      <button
                                        className="kyc-doc-btn kyc-doc-btn--upload"
                                        disabled={uploadingDocId === doc.docType}
                                        onClick={() => docUploadRefs[doc.docType].current?.click()}
                                        title={doc.uploaded ? 'Replace document' : 'Upload document'}
                                      >
                                        {uploadingDocId === doc.docType ? (
                                          <><div className="kyc-doc-spinner" />&nbsp;Uploading…</>
                                        ) : (
                                          <><i className="fas fa-upload" style={{ marginRight: 5 }} />
                                            {doc.uploaded ? 'Replace' : 'Upload'}</>
                                        )}
                                      </button>

                                      {/* View button — only when a doc exists */}
                                      {doc.uploaded && (
                                        <button
                                          className="kyc-doc-btn kyc-doc-btn--view"
                                          disabled={loadingDocId === doc.docType}
                                          onClick={() => handleViewKycDocument(doc.docType)}
                                          title="View document"
                                        >
                                          {loadingDocId === doc.docType ? (
                                            <div className="kyc-doc-spinner" />
                                          ) : (
                                            <><i className="fas fa-eye" style={{ marginRight: 5 }} />View</>
                                          )}
                                        </button>
                                      )}
                                    </div>
                                  </div>
                                ))}
                              </div>
                            );
                          })()}
                        </>
                      );
                    })()}
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
                  <div className="user-profile-modal-overlay" onClick={() => { if (!resetLinkLoading) { setShowResetPasswordModal(false); setResetLinkSent(false); }}} />
                  <div className="user-profile-modal">
                    <div className="user-profile-modal-content">

                      {resetLinkSent ? (
                        /* ── Success state ── */
                        <>
                          <div className="user-profile-modal-icon" style={{ background: '#dcfce7' }}>
                            <i className="fas fa-check-circle" style={{ fontSize: 48, color: '#16a34a' }} />
                          </div>
                          <h3>Reset Link Sent!</h3>
                          <p>
                            A password reset link has been sent to{' '}
                            <strong>{userData?.email || 'your email address'}</strong>.
                            <br /><br />
                            Check your inbox and follow the instructions to reset your password.
                            The link expires in <strong>10 minutes</strong>.
                          </p>
                          <div className="user-profile-modal-actions" style={{ gridTemplateColumns: '1fr' }}>
                            <button
                              className="user-profile-btn-primary"
                              onClick={() => { setShowResetPasswordModal(false); setResetLinkSent(false); }}
                            >
                              Done
                            </button>
                          </div>
                        </>
                      ) : (
                        /* ── Confirm state ── */
                        <>
                          <div className="user-profile-modal-icon primary">
                            <Key size={48} />
                          </div>
                          <h3>Reset Your Password</h3>
                          <p>
                            We'll send a password reset link to{' '}
                            <strong>{userData?.email || 'your registered email address'}</strong>.
                            <br /><br />
                            Click the link in the email to set a new password.
                          </p>
                          <div className="user-profile-modal-actions">
                            <button
                              className="user-profile-btn-secondary"
                              onClick={() => setShowResetPasswordModal(false)}
                              disabled={resetLinkLoading}
                            >
                              Cancel
                            </button>
                            <button
                              className="user-profile-btn-primary"
                              onClick={handleSendResetLink}
                              disabled={resetLinkLoading}
                              style={{ display: 'flex', alignItems: 'center', gap: 8, justifyContent: 'center' }}
                            >
                              {resetLinkLoading ? (
                                <><div className="kyc-doc-spinner" style={{ borderColor: 'rgba(255,255,255,0.3)', borderTopColor: '#fff' }} /> Sending…</>
                              ) : (
                                <><i className="fas fa-paper-plane" />Send Reset Link</>
                              )}
                            </button>
                          </div>
                        </>
                      )}

                    </div>
                  </div>
                </>
              )}

              {isShowSuspendAccountModal && (
                <>
                  <div className="user-profile-modal-overlay" onClick={() => { if (!suspendLoading) setShowSuspendAccountModal(false); }} />
                  <div className="user-profile-modal">
                    <div className="user-profile-modal-content">

                      <div className="user-profile-modal-icon warning">
                        <AlertCircle size={48} />
                      </div>

                      <h3>Suspend Your Account?</h3>

                      <p style={{ textAlign: 'center', color: '#6b7280', fontSize: 14, lineHeight: 1.6 }}>
                        This will <strong style={{ color: '#dc2626' }}>immediately disable</strong> your ability to:
                      </p>

                      {/* What gets locked */}
                      <div style={{
                        background: '#fff7ed', border: '1px solid #fed7aa', borderRadius: 12,
                        padding: '12px 16px', width: '100%', marginTop: 4
                      }}>
                        {[
                          { icon: 'fa-sign-in-alt',  text: 'Log in to your account' },
                          { icon: 'fa-paper-plane',  text: 'Transfer money to others' },
                          { icon: 'fa-arrow-up',     text: 'Withdraw from your wallet' },
                        ].map((item, i) => (
                          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: i < 2 ? 8 : 0 }}>
                            <i className={`fas ${item.icon}`} style={{ color: '#ea580c', fontSize: 13, width: 16 }} />
                            <span style={{ fontSize: 13, color: '#374151' }}>{item.text}</span>
                          </div>
                        ))}
                      </div>

                      <p style={{ textAlign: 'center', color: '#6b7280', fontSize: 13, lineHeight: 1.5, marginTop: 8 }}>
                        A confirmation email will be sent to{' '}
                        <strong style={{ color: '#111827' }}>{userData?.email}</strong>.
                        Contact support to reactivate your account.
                      </p>

                      <div className="user-profile-modal-actions">
                        <button
                          className="user-profile-btn-secondary"
                          onClick={() => setShowSuspendAccountModal(false)}
                          disabled={suspendLoading}
                        >
                          Cancel
                        </button>
                        <button
                          className="user-profile-btn-danger"
                          onClick={handleConfirmSuspend}
                          disabled={suspendLoading}
                          style={{ display: 'flex', alignItems: 'center', gap: 8, justifyContent: 'center' }}
                        >
                          {suspendLoading ? (
                            <><div className="kyc-doc-spinner" style={{ borderColor: 'rgba(255,255,255,0.3)', borderTopColor: '#fff' }} /> Suspending…</>
                          ) : (
                            <><i className="fas fa-ban" />Yes, Suspend Account</>
                          )}
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
                                  <span className="kyc-error-message">{metaMapErrors.bvn}</span>
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
                                <span className="kyc-error-message">{metaMapErrors.firstName}</span>
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
                                <span className="kyc-error-message">{metaMapErrors.lastName}</span>
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
                                {metaMapErrors.dob && (
                                  <span className="kyc-error-message">{metaMapErrors.dob}</span>
                                )}
                            </div>
                            <div className="metamap-form-group">
                              <label>Gender</label>
                              <select 
                                className={`settings-select ${metaMapErrors.gender ? 'error' : ''}`}
                                name='gender' 
                                id="gender" 
                                value={formData.gender}
                                onChange={handleMetaMapSelectChange}>
                                <option value="">--Select--</option>
                                <option value="male">Male</option>
                                <option value="female">Female</option>
                              </select>
                              {metaMapErrors.gender && (
                                <span className="kyc-error-message">{metaMapErrors.gender}</span>
                              )}
                            </div>

                            {/* Country */}
                            <div className="metamap-form-group">
                              <label>Country</label>
                              <select name="country" value={metaMapData.countryCode} onChange={handleCountryChange}>
                                <option value="">-- Select Country --</option>
                                {Country.getAllCountries().map(c => (
                                  <option key={c.isoCode} value={c.isoCode}>{c.name}</option>
                                ))}
                              </select>
                              {metaMapErrors.country && <span className="kyc-error-message">{metaMapErrors.country}</span>}
                            </div>

                            {/* State */}
                            <div className="metamap-form-group">
                              <label>State</label>
                              <select name="state" value={metaMapData.stateCode} onChange={handleStateChange}
                                disabled={!metaMapData.countryCode}>
                                <option value="">-- Select State --</option>
                                {State.getStatesOfCountry(metaMapData.countryCode).map(s => (
                                  <option key={s.isoCode} value={s.isoCode}>{s.name}</option>
                                ))}
                              </select>
                              {metaMapErrors.state && <span className="kyc-error-message">{metaMapErrors.state}</span>}
                            </div>

                            {/* City */}
                            <div className="metamap-form-group">
                              <label>City</label>
                              <select name="city" value={metaMapData.city} onChange={handleCityChange}
                                disabled={!metaMapData.stateCode}>
                                <option value="">-- Select City --</option>
                                {City.getCitiesOfState(metaMapData.countryCode, metaMapData.stateCode).map(c => (
                                  <option key={c.name} value={c.name}>{c.name}</option>
                                ))}
                              </select>
                              {metaMapErrors.city && <span className="kyc-error-message">{metaMapErrors.city}</span>}
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
        <KYCSuccessModal
          isOpen={showKYCSuccess}
          onClose={() => setShowKYCSuccess(false)}
        />
         {!isChatOpen && (
          <button
            className="chat-fab"
            onClick={() => setIsChatOpen(true)}
            aria-label="Open support chat"
          >
            <i className="fa-solid fa-comment-dots"></i>
          </button>
        )}

        <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
    </div>
    </>
  );
};

export default UserProfile;