"use client";

import { Toast } from "@/app/types/auth";
import DepositModal from "@/components/DepositModal";
import Footer from "@/components/Footer";
import Header from "@/components/Header";
import LoadingScreen from "@/components/loader/Loadingscreen";
import MobileNav from "@/components/MobileNav";
import Sidebar from "@/components/Sidebar";
import SupportChatBot from "@/components/SupportChatBot";
import {
  AlertCircle,
  Bell,
  Camera,
  Download,
  Globe,
  Key,
  Lock,
  Mail,
  Shield,
  Smartphone,
  Trash2,
  Upload,
  User,
} from "lucide-react";
import { useRouter } from "next/navigation";
import React, { useEffect, useRef, useState } from "react";
import {
  capitalizeFirstLetter,
  configService,
  formatDateToDDMMYYYY,
  getToken,
  getUserDetails,
  getUserId,
  getUserIsSetTransfer,
  getUsername,
  getUserWalletId,
  updateProfileImageInStorage,
  userService,
  walletService,
} from "../../api";
import { UserSettings } from "../../types/utils";
import "./Settings.css";

const Settings = () => {
  const [activeTab, setActiveTab] = useState<
    "profile" | "security" | "notifications" | "preferences" | "pin"
  >("profile");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showRemoveImageModal, setShowRemoveImageModal] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState<boolean>(true);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const [theme, setTheme] = useState<"light" | "dark">("light");
  const [userProfile, setUserProfile] = useState<any>(null);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [userHasPin, setUserHasPin] = useState(false);
  const [pinMode, setPinMode] = useState("create");
  const [pin, setPin] = useState(["", "", "", ""]);
  const [confirmPin, setConfirmPin] = useState(["", "", "", ""]);
  const [currentPin, setCurrentPin] = useState(["", "", "", ""]);
  const [showChangePinForm, setShowChangePinForm] = useState(false);
  const [showCreatePinForm, setShowCreatePinForm] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [profileImage, setProfileImage] = useState(
    "/assets/images/user-profile.jpg",
  );
  const [hasCustomImage, setHasCustomImage] = useState(false);
  const [loading, setLoading] = useState(true);
  const [updating2FA, setUpdating2FA] = useState(false);
  const [updatingBiometric, setUpdatingBiometric] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const user_details = getUserDetails();
  const router = useRouter();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    gender: "",
    telephone: "",
    dob: "",
    email: "",
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [passwordErrors, setPasswordErrors] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  useEffect(() => {
    document.title = "Settings - ePay Online Business Banking";
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);

  useEffect(() => {
    checkPinStatus();
  }, []);

  useEffect(() => {
    const loadProfileImageFromStorage = () => {
      try {
        const storedData = localStorage.getItem("data");
        if (storedData) {
          const parsedData = JSON.parse(storedData);
          const userPhoto = parsedData?.user?.photo;

          if (userPhoto && userPhoto !== "/assets/images/user-profile.jpg") {
            setProfileImage(userPhoto);
            setHasCustomImage(true);
          } else {
            setProfileImage("/assets/images/user-profile.jpg");
            setHasCustomImage(false);
          }
        }
      } catch (error) {
        console.error("Error loading profile image from storage:", error);
        setProfileImage("/assets/images/user-profile.jpg");
        setHasCustomImage(false);
      }
    };

    loadProfileImageFromStorage();
  }, []);

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserSettings = async () => {
    try {
      const userId = getUserId();
      const response = await configService.getUserSettings(userId);

      if (response?.status === "success" && response?.data) {
        const settingsData = response.data;

        // Update settings state with fetched config data
        setSettings((prev) => ({
          ...prev,
          security: {
            ...prev.security,
            biometricEnabled: settingsData.isBiometric || false,
            sessionTimeout: parseInt(settingsData.sessionTimeOut) || 30,
          },
          notifications: {
            email: settingsData.isEmailAlert || false,
            push: prev.notifications.push,
            sms: settingsData.isReceiveSmsMessage || false,
            transactionAlerts: settingsData.isTransactionAlert || false,
            loginAlerts: settingsData.isLoginAlert || false,
            marketingEmails: settingsData.isReceiveMarketingNews || false,
          },
          preferences: {
            ...prev.preferences,
            language: settingsData.preferredLanguage || "English",
            timezone: settingsData.timeZone || "Africa/Lagos",
          },
        }));
      }
    } catch (error) {
      console.error("Error fetching user settings:", error);
      // Don't show toast here as it's a secondary fetch
    }
  };

  const fetchUserProfile = async () => {
    try {
      const userId = getUserId();

      const response = await userService.getById(userId);
      setUserProfile(response);
      const userRecord = response.records?.[0] || {};

      setFormData({
        firstName: userRecord.firstName || "",
        lastName: userRecord.lastName || "",
        gender: userRecord.gender
          ? capitalizeFirstLetter(userRecord.gender)
          : "",
        telephone: userRecord.telephone || "",
        dob: userRecord.dob || user_details?.dob || "",
        email: response.email || "",
      });

      console.log("User records:", response.records);
      console.log("Photo path:", userRecord.photo);

      // Update settings with fetched data
      setSettings((prev) => ({
        ...prev,
        profile: {
          fullName:
            `${userRecord.firstName || ""} ${userRecord.lastName || ""}`.trim(),
          email: response.email || "",
          phone: userRecord.telephone || "",
          username: response.username || "",
          profileImage: userRecord.photo
            ? `http://localhost/api/v1${userRecord.photo}`
            : "/assets/images/user-profile.jpg",
        },
        security: {
          twoFactorEnabled: response.twoFactorAuth || false,
          biometricEnabled: prev.security.biometricEnabled,
          sessionTimeout: prev.security.sessionTimeout,
        },
        preferences: {
          language: userRecord.language || "English",
          currency: userRecord.currency || "NGN",
          theme: prev.preferences.theme,
          timezone: userRecord.timezone || "Africa/Lagos",
        },
      }));

      if (userRecord.photo) {
        setProfileImage(`http://localhost/api/v1${userRecord.photo}`);
      }
      await fetchUserSettings();
    } catch (error) {
      console.error("Error fetching user profile:", error);
      showToast("Failed to load profile data");
    } finally {
      setLoading(false);
    }
  };

  const checkPinStatus = () => {
    const hasPin = getUserIsSetTransfer();
    setUserHasPin(!!hasPin);
    if (hasPin) {
      setPinMode("update");
    } else {
      setPinMode("create");
    }
    // Reset form visibility when rechecking
    setShowChangePinForm(false);
    setShowCreatePinForm(false);
    setPin(["", "", "", ""]);
    setConfirmPin(["", "", "", ""]);
    setCurrentPin(["", "", "", ""]);
  };

  const [settings, setSettings] = useState<UserSettings>({
    profile: {
      fullName: "",
      email: "",
      phone: "",
      username: "",
      profileImage: "/assets/images/user-profile.jpg",
    },
    security: {
      twoFactorEnabled: false,
      biometricEnabled: false,
      sessionTimeout: 30,
    },
    notifications: {
      email: true,
      push: true,
      sms: false,
      transactionAlerts: true,
      loginAlerts: true,
      marketingEmails: false,
    },
    preferences: {
      language: "English",
      currency: "NGN",
      theme: "light",
      timezone: "Africa/Lagos",
    },
  });

  const showToast = (msg: string, type: "warning" | "success" = "warning") => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;

      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };

      setTimeout(() => {
        setToasts((currentToasts) =>
          currentToasts.map((t) => (t.id === id ? { ...t, exiting: true } : t)),
        );

        setTimeout(() => {
          setToasts((currentToasts) =>
            currentToasts.filter((t) => t.id !== id),
          );
        }, 300);
      }, 3000);

      return [...prev, newToast];
    });
  };

  const handleToggle = async (category: keyof UserSettings, key: string) => {
    if (category === "security" && key === "twoFactorEnabled") {
      const newTwoFAStatus = !settings.security.twoFactorEnabled;
      setUpdating2FA(true);

      try {
        const response = await userService.update2FAStatus(getUserId(), {
          enable2FA: newTwoFAStatus,
        });

        setSettings((prev) => ({
          ...prev,
          security: {
            ...prev.security,
            twoFactorEnabled: newTwoFAStatus,
          },
        }));

        if (userProfile) {
          setUserProfile((prev: any) => ({
            ...prev,
            twoFactorAuth: newTwoFAStatus,
          }));
        }

        showToast(
          `Two-factor authentication ${newTwoFAStatus ? "enabled" : "disabled"}`,
          "success",
        );
        await fetchUserProfile();
      } catch (error) {
        console.error("2FA update error:", error);
        showToast("Failed to update 2FA settings");
      } finally {
        setUpdating2FA(false);
      }
    } else if (category === "security" && key === "biometricEnabled") {
      const newBiometricStatus = !settings.security.biometricEnabled;
      setUpdatingBiometric(true);

      try {
        // Call API to update biometric status
        const response = await configService.updateBiometricStatus(
          getUserId(),
          { enableBiometric: newBiometricStatus },
        );

        setSettings((prev) => ({
          ...prev,
          security: {
            ...prev.security,
            biometricEnabled: newBiometricStatus,
          },
        }));

        showToast(
          `Biometric authentication ${newBiometricStatus ? "enabled" : "disabled"}`,
          "success",
        );
        await fetchUserSettings();
      } catch (error) {
        console.error("Biometric update error:", error);
        showToast("Failed to update biometric settings");
      } finally {
        setUpdatingBiometric(false);
      }
    } else if (category === "notifications") {
      // Update notification preferences
      const newValue =
        !settings.notifications[key as keyof typeof settings.notifications];

      try {
        // Call API to update notification settings
        await configService.updateNotificationSettings(getUserId(), {
          [key]: newValue,
        });

        setSettings((prev) => ({
          ...prev,
          notifications: {
            ...prev.notifications,
            [key]: newValue,
          },
        }));

        showToast("Notification preference updated", "success");
        await fetchUserSettings();
      } catch (error) {
        console.error("Notification update error:", error);
        showToast("Failed to update notification settings");
      }
    } else {
      setSettings((prev) => ({
        ...prev,
        [category]: {
          ...prev[category],
          [key]: !prev[category][key as keyof (typeof prev)[typeof category]],
        },
      }));
    }
  };

  const handleSelectChange = async (
    category: keyof UserSettings,
    key: string,
    value: string,
  ) => {
    try {
      if (category === "security" && key === "sessionTimeout") {
        await configService.updateSessionTimeout(getUserId(), {
          sessionTimeout: parseInt(value),
        });
        showToast("Session timeout updated", "success");
      } else if (category === "preferences") {
        await configService.updatePreferences(getUserId(), {
          [key]: value,
        });
        showToast(`${key} updated successfully`, "success");
      }

      setSettings((prev) => ({
        ...prev,
        [category]: {
          ...prev[category],
          [key]: value,
        },
      }));

      // Refetch settings to ensure consistency
      await fetchUserSettings();
    } catch (error) {
      console.error(`Error updating ${key}:`, error);
      showToast(`Failed to update ${key}`);
    }
  };

  const toggleTheme = () => {
    const newTheme = theme === "light" ? "dark" : "light";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme);
    document.documentElement.classList.toggle("dark", newTheme === "dark");
    document.body.classList.toggle("dark-theme", newTheme === "dark");
  };

  const handleChangePassword = () => {
    setShowPasswordModal(true);
  };

  const handlePasswordInputChange = (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setPasswordErrors((prev) => ({
      ...prev,
      [name]: "",
    }));
  };

  const validatePasswordForm = () => {
    let isValid = true;
    const errors = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };

    if (!passwordData.currentPassword.trim()) {
      errors.currentPassword = "Current password is required";
      isValid = false;
    }

    if (!passwordData.newPassword.trim()) {
      errors.newPassword = "New password is required";
      isValid = false;
    } else if (passwordData.newPassword.length < 8) {
      errors.newPassword = "Password must be at least 8 characters";
      isValid = false;
    } else if (
      !/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(passwordData.newPassword)
    ) {
      errors.newPassword =
        "Password must contain uppercase, lowercase, and number";
      isValid = false;
    }

    if (!passwordData.confirmPassword.trim()) {
      errors.confirmPassword = "Please confirm your password";
      isValid = false;
    } else if (passwordData.newPassword !== passwordData.confirmPassword) {
      errors.confirmPassword = "Passwords do not match";
      isValid = false;
    }

    setPasswordErrors(errors);
    return isValid;
  };

  const handlePasswordSubmit = async () => {
    if (!validatePasswordForm()) {
      return;
    }

    try {
      const data = {
        oldPassword: passwordData.currentPassword,
        confirmPassword: passwordData.confirmPassword,
        password: passwordData.newPassword,
      };

      await userService.updateUserPassword(data);

      showToast("Password changed successfully", "success");
      setShowPasswordModal(false);

      // Reset form
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setPasswordErrors({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error: any) {
      console.error("Password change error:", error);
      const errorMessage =
        error.response?.data?.message || "Failed to change password";
      showToast(errorMessage);
    }
  };

  const handleDeleteAccount = () => {
    setShowDeleteModal(true);
  };

  const handleExportData = () => {
    showToast("Exporting user data...");
  };

  const handleImageUpload = () => {
    fileInputRef.current?.click();
  };

  const handleFileSelect = async (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const validTypes = [
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/gif",
      "image/webp",
    ];
    if (!validTypes.includes(file.type)) {
      showToast("Please select a valid image file (JPG, PNG, GIF, WebP)");
      return;
    }

    const maxSize = 2 * 1024 * 1024; // 2MB
    if (file.size > maxSize) {
      showToast("Image size must be less than 2MB");
      return;
    }

    setIsUploadingImage(true);

    // Show local preview immediately
    if (profileImage.startsWith("blob:")) {
      URL.revokeObjectURL(profileImage);
    }
    const previewUrl = URL.createObjectURL(file);
    setProfileImage(previewUrl);

    try {
      const formData = new FormData();
      formData.append("image", file);
      const id = getUserId();

      const data: any = await userService.uploadProfileImage(id, formData);

      if (
        (data.status === "success" || data.success === true) &&
        data.imageUrl
      ) {
        const fullImageUrl = `http://localhost:8029${data.imageUrl}`;
        URL.revokeObjectURL(previewUrl);
        setProfileImage(fullImageUrl);
        updateProfileImageInStorage(fullImageUrl);
        setHasCustomImage(true);
        showToast("Profile image updated successfully", "success");
      } else {
        throw new Error(
          typeof data.message === "string" ? data.message : "Upload failed",
        );
      }
    } catch (error: any) {
      console.error("Error uploading image:", error);
      URL.revokeObjectURL(previewUrl);
      setProfileImage("/assets/images/user-profile.jpg");
      setHasCustomImage(false);
      const msg =
        typeof error === "string"
          ? error
          : error?.message ||
            error?.data?.message ||
            "Failed to upload image. Please try again.";
      showToast(msg);
    } finally {
      setIsUploadingImage(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleRemoveImage = () => {
    if (!hasCustomImage) {
      showToast("No custom profile image to remove");
      return;
    }
    setShowRemoveImageModal(true);
  };

  const confirmRemoveImage = async () => {
    setShowRemoveImageModal(false);
    setIsUploadingImage(true);

    try {
      const userId = getUserId();
      const response = await userService.removeProfileImage(userId);
      if (response.status === "success") {
        if (profileImage.startsWith("blob:")) {
          URL.revokeObjectURL(profileImage);
        }
        const defaultImage = "/assets/images/user-profile.jpg";
        setProfileImage(defaultImage);
        updateProfileImageInStorage(defaultImage);

        setHasCustomImage(false);
        showToast("Profile image removed successfully", "success");
      } else {
        throw new Error(response.message || "Remove failed");
      }
    } catch (error) {
      console.error("Error removing image:", error);
      showToast("Failed to remove image. Please try again.");
    } finally {
      setIsUploadingImage(false);
    }
  };

  const handlePinInput = (
    index: number,
    value: string,
    isConfirm: boolean = false,
  ) => {
    // Only allow numbers
    if (value && !/^\d$/.test(value)) return;

    if (isConfirm) {
      const newConfirmPin = [...confirmPin];
      newConfirmPin[index] = value;
      setConfirmPin(newConfirmPin);
    } else {
      const newPin = [...pin];
      newPin[index] = value;
      setPin(newPin);
    }
  };

  const handleCurrentPinInput = (index: number, value: string) => {
    if (value && !/^\d$/.test(value)) return;
    const updated = [...currentPin];
    updated[index] = value;
    setCurrentPin(updated);
  };

  const handleSetPin = async () => {
    const pinValue = pin.join("");
    const confirmPinValue = confirmPin.join("");

    if (pinValue.length !== 4) {
      showToast("Please enter a 4-digit PIN");
      return;
    }
    if (pinValue !== confirmPinValue) {
      showToast("PINs do not match. Please try again.");
      return;
    }

    const payload = {
      userId: getUserId(),
      walletId: getUserWalletId(),
      username: getUsername(),
      pin: pinValue,
      confirmPin: pinValue,
    };

    const response = await walletService.createTransferPin(payload);
    if (response?.status === "success") {
      setPin(["", "", "", ""]);
      setConfirmPin(["", "", "", ""]);
      setCurrentPin(["", "", "", ""]);
      setShowCreatePinForm(false);
      setShowChangePinForm(false);
      checkPinStatus();
      showToast("PIN Created Successfully!", "success");
    } else {
      showToast(response?.message || "Failed to create PIN. Please try again.");
    }
  };

  const handleChangePin = async () => {
    const currentPinValue = currentPin.join("");
    const newPinValue = pin.join("");
    const confirmPinValue = confirmPin.join("");

    if (currentPinValue.length !== 4) {
      showToast("Please enter your current 4-digit PIN");
      return;
    }
    if (newPinValue.length !== 4) {
      showToast("Please enter a new 4-digit PIN");
      return;
    }
    if (newPinValue !== confirmPinValue) {
      showToast("New PINs do not match. Please try again.");
      return;
    }
    if (currentPinValue === newPinValue) {
      showToast("New PIN must be different from your current PIN.");
      return;
    }

    const payload = {
      userId: getUserId(),
      walletId: getUserWalletId(),
      username: getUsername(),
      currentPin: currentPinValue,
      pin: newPinValue,
      confirmPin: newPinValue,
    };

    const response = await walletService.createTransferPin(payload);
    if (response?.status === "success") {
      setPin(["", "", "", ""]);
      setConfirmPin(["", "", "", ""]);
      setCurrentPin(["", "", "", ""]);
      setShowChangePinForm(false);
      checkPinStatus();
      showToast("PIN changed successfully!", "success");
    } else {
      showToast(
        response?.message ||
          "Failed to change PIN. Please check your current PIN.",
      );
    }
  };

  const handleCurrencyUpdate = async (value: string) => {
    try {
      const payload = {
        userId: getUserId(),
        currency: value,
      };

      await walletService.updateDefaultCurrency(payload);

      setSettings((prev) => ({
        ...prev,
        preferences: {
          ...prev.preferences,
          currency: value,
        },
      }));

      showToast("Default currency updated successfully", "success");
    } catch (error) {
      console.error("Error updating currency:", error);
      showToast("Failed to update default currency");
    }
  };

  const handleDeleteAccountRequest = async () => {
    try {
      const userId = getUserId();
      const request = await userService.deleteAccount(userId);
      if (request.status !== "success") {
        showToast("Failed to delete account");
        setShowDeleteModal(false);
        return;
      } else {
        showToast("Account deletion request has been submitted!", "success");
        setShowDeleteModal(false);
        router.push("/auth/logout");
      }
    } catch (error) {
      showToast("Failed to delete account");
      setShowDeleteModal(false);
    }
  };

  const userRecord = userProfile?.records?.[0] || {};

  if (isPageLoading) {
    return <LoadingScreen />;
  }

  return (
    <>
      <div className={`dashboard-container ${theme === "dark" ? "dark" : ""}`}>
        <Sidebar />
        <main
          className={`main-content ${isDepositOpen ? "dashboard-blur" : ""}`}
        >
          <Header theme={theme} toggleTheme={toggleTheme} />
          <div className="toastrs">
            {toasts.map((toast) => (
              <div
                key={toast.id}
                className={`toastr toastr--${toast.type} ${toast.exiting ? "toast-exit" : ""}`}
              >
                <div className="toast-icon">
                  <i
                    className={`fa ${toast.type === "success" ? "fa-check-circle" : "fa-exclamation-circle"}`}
                    aria-hidden="true"
                  ></i>
                </div>
                <div className="toast-message">{toast.message}</div>
              </div>
            ))}
          </div>
          <div className="scrollable-content">
            <div
              className={`settings-page ${theme === "dark" ? "bg-light" : "bg-dark"}`}
            >
              {/* Header */}
              <div
                className={`settings-header ${theme === "dark" ? "dark" : "light"}`}
              >
                <div>
                  <h1
                    className={`settings-title ${theme === "dark" ? "color-light" : "coolor-dark"}`}
                  >
                    Settings
                  </h1>
                  <p
                    className={`settings-subtitle ${theme === "dark" ? "color-light" : "coolor-dark"}`}
                  >
                    Manage your account settings and preferences
                  </p>
                </div>
              </div>

              {/* Tabs */}
              <div className="settings-tabs">
                <button
                  className={`settings-tab ${activeTab === "profile" ? "active" : ""} ${theme === "dark" ? "color-light" : ""}`}
                  onClick={() => setActiveTab("profile")}
                >
                  <User size={20} />
                  <span>Profile</span>
                </button>
                <button
                  className={`settings-tab ${activeTab === "security" ? "active" : ""} ${theme === "dark" ? "color-light" : ""}`}
                  onClick={() => setActiveTab("security")}
                >
                  <Shield size={20} />
                  <span>Security</span>
                </button>
                <button
                  className={`settings-tab ${activeTab === "notifications" ? "active" : ""} ${theme === "dark" ? "color-light" : ""}`}
                  onClick={() => setActiveTab("notifications")}
                >
                  <Bell size={20} />
                  <span>Notifications</span>
                </button>
                <button
                  className={`settings-tab ${activeTab === "pin" ? "active" : ""} ${theme === "dark" ? "color-light" : ""}`}
                  onClick={() => setActiveTab("pin")}
                >
                  <Key size={20} />
                  <span>Withdrawal PIN</span>
                </button>
                <button
                  className={`settings-tab ${activeTab === "preferences" ? "active" : ""} ${theme === "dark" ? "color-light" : ""}`}
                  onClick={() => setActiveTab("preferences")}
                >
                  <Globe size={20} />
                  <span>Preferences</span>
                </button>
              </div>

              {/* Content */}
              <div className="settings-content">
                {/* Profile Tab */}
                {activeTab === "profile" && (
                  <div className="settings-section">
                    <div className="settings-section-header">
                      <h2>Profile Information</h2>
                      <p>
                        Update your personal information and profile picture
                      </p>
                    </div>

                    <div className="settings-profile-picture">
                      <div className="settings-avatar-wrapper">
                        {isUploadingImage ? (
                          <div className="settings-avatar-loader">
                            <div className="settings-spinner"></div>
                          </div>
                        ) : (
                          <img
                            src={profileImage}
                            alt="User profile"
                            width={100}
                            height={100}
                            className="settings-avatar"
                            onError={(e) => {
                              e.currentTarget.src =
                                "/assets/images/user-profile.jpg";
                            }}
                          />
                        )}

                        <button
                          className="settings-avatar-upload"
                          onClick={handleImageUpload}
                          disabled={isUploadingImage}
                          title="Change profile picture"
                        >
                          <Camera size={18} />
                        </button>
                      </div>
                      <div>
                        <h3>Profile Picture</h3>
                        <p className="settings-helper-text">
                          JPG, PNG or GIF. Max size 2MB
                        </p>
                        <div className="settings-button-group">
                          <button
                            className="settings-btn-secondary"
                            onClick={handleImageUpload}
                            disabled={isUploadingImage}
                          >
                            <Upload size={16} />
                            {isUploadingImage ? "Uploading..." : "Upload"}
                          </button>
                          <button
                            className="settings-btn-text"
                            onClick={handleRemoveImage}
                            disabled={isUploadingImage || !hasCustomImage}
                          >
                            Remove
                          </button>
                        </div>
                      </div>
                    </div>

                    {/* Profile Information Display */}
                    {!loading && userProfile && (
                      <div className="settings-profile-info">
                        <div className="profile-info-header">
                          <h3 className="account-title">Account Information</h3>
                          <span className="profile-info-badge">
                            <i
                              className="fas fa-check-circle"
                              style={{ marginRight: "5px" }}
                            />
                            Verified
                          </span>
                        </div>

                        <div className="profile-info-cards">
                          {/* Full Name */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-user" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Full Name
                              </span>
                              <span className="profile-info-card-value">
                                {userRecord.firstName || userRecord.lastName ? (
                                  `${userRecord.firstName || ""} ${userRecord.lastName || ""}`.trim()
                                ) : (
                                  <span className="profile-info-empty">
                                    Not provided
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>

                          {/* Email */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-envelope" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Email Address
                              </span>
                              <span className="profile-info-card-value">
                                {userProfile.email || (
                                  <span className="profile-info-empty">
                                    Not provided
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>

                          {/* Gender */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-venus-mars" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Gender
                              </span>
                              <span className="profile-info-card-value">
                                {userRecord.gender ? (
                                  capitalizeFirstLetter(userRecord.gender)
                                ) : (
                                  <span className="profile-info-empty">
                                    Not specified
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>

                          {/* Mobile */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-phone" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Mobile
                              </span>
                              <span className="profile-info-card-value">
                                {userRecord.telephone || (
                                  <span className="profile-info-empty">
                                    Not provided
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>

                          {/* Date of Birth */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-birthday-cake" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Date of Birth
                              </span>
                              <span className="profile-info-card-value">
                                {userRecord.dob ? (
                                  formatDateToDDMMYYYY(userRecord.dob)
                                ) : user_details?.dob ? (
                                  formatDateToDDMMYYYY(user_details.dob)
                                ) : (
                                  <span className="profile-info-empty">
                                    Not provided
                                  </span>
                                )}
                              </span>
                            </div>
                          </div>

                          {/* Username */}
                          <div className="profile-info-card">
                            <div className="profile-info-card-icon">
                              <i className="fas fa-at" />
                            </div>
                            <div className="profile-info-card-body">
                              <span className="profile-info-card-label">
                                Username
                              </span>
                              <span className="profile-info-card-value profile-info-username">
                                @{userProfile.username || "username"}
                              </span>
                            </div>
                          </div>
                        </div>

                        <p className="profile-info-footer-note">
                          <i
                            className="fas fa-info-circle"
                            style={{ marginRight: "6px" }}
                          />
                          To update your details, visit the{" "}
                          <a
                            href="/settings/profile"
                            className="profile-info-link"
                          >
                            Profile page
                          </a>
                          .
                        </p>
                      </div>
                    )}

                    {/* Hidden file input */}
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                      onChange={handleFileSelect}
                      style={{ display: "none" }}
                      aria-label="Upload profile picture"
                    />
                  </div>
                )}

                {/* Security Tab */}
                {activeTab === "security" && (
                  <div className="settings-section">
                    <div className="settings-section-header">
                      <h2>Security Settings</h2>
                      <p>
                        Manage your account security and authentication methods
                      </p>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Lock size={20} />
                        <h3>Password</h3>
                      </div>
                      <p className="settings-card-desc">
                        Change your password regularly to keep your account
                        secure
                      </p>
                      <button
                        className="settings-btn-outline"
                        onClick={handleChangePassword}
                      >
                        Change Password
                      </button>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Shield size={20} />
                        <h3>Two-Factor Authentication</h3>
                      </div>
                      <p className="settings-card-desc">
                        Add an extra layer of security to your account
                      </p>
                      <div className="settings-toggle-row">
                        <div>
                          <div className="settings-toggle-label">
                            Enable 2FA
                          </div>
                          <div className="settings-toggle-desc">
                            Require a code from your phone to login
                          </div>
                        </div>
                        <label className="settings-toggle">
                          <input
                            type="checkbox"
                            checked={settings.security.twoFactorEnabled}
                            disabled={updating2FA}
                            onChange={() =>
                              handleToggle("security", "twoFactorEnabled")
                            }
                          />
                          <span className="settings-toggle-slider"></span>
                        </label>
                      </div>
                      {updating2FA && (
                        <div
                          style={{
                            marginTop: "0.5rem",
                            fontSize: "0.875rem",
                            color: "#6b7280",
                          }}
                        >
                          Updating 2FA settings...
                        </div>
                      )}
                      {!loading && userProfile && userProfile.twoFactorAuth && (
                        <div style={{ marginTop: "0.5rem" }}>
                          <span className="twofa-badge status-active">
                            Protected
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Smartphone size={20} />
                        <h3>Biometric Authentication</h3>
                      </div>
                      <p className="settings-card-desc">
                        Use fingerprint or face recognition to login
                      </p>
                      <div className="settings-toggle-row">
                        <div>
                          <div className="settings-toggle-label">
                            Enable Biometric
                          </div>
                          <div className="settings-toggle-desc">
                            Login with Touch ID or Face ID
                          </div>
                        </div>
                        <label className="settings-toggle">
                          <input
                            type="checkbox"
                            checked={settings.security.biometricEnabled}
                            disabled={updatingBiometric}
                            onChange={() =>
                              handleToggle("security", "biometricEnabled")
                            }
                          />
                          <span className="settings-toggle-slider"></span>
                        </label>
                      </div>
                      {updatingBiometric && (
                        <div
                          style={{
                            marginTop: "0.5rem",
                            fontSize: "0.875rem",
                            color: "#6b7280",
                          }}
                        >
                          Updating biometric settings...
                        </div>
                      )}
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <AlertCircle size={20} />
                        <h3>Session Timeout</h3>
                      </div>
                      <p className="settings-card-desc">
                        Automatically log out after period of inactivity
                      </p>
                      <select
                        className="settings-select"
                        value={settings.security.sessionTimeout}
                        onChange={(e) =>
                          handleSelectChange(
                            "security",
                            "sessionTimeout",
                            e.target.value,
                          )
                        }
                      >
                        <option value={15}>15 minutes</option>
                        <option value={30}>30 minutes</option>
                        <option value={60}>1 hour</option>
                        <option value={120}>2 hours</option>
                      </select>
                    </div>
                  </div>
                )}

                {/* Notifications Tab */}
                {activeTab === "notifications" && (
                  <div className="settings-section">
                    <div className="settings-section-header">
                      <h2>Notification Preferences</h2>
                      <p>Choose how you want to receive notifications</p>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Mail size={20} />
                        <h3>Email Notifications</h3>
                      </div>
                      <div className="settings-toggle-list">
                        <div className="settings-toggle-row">
                          <div>
                            <div className="settings-toggle-label">
                              Email Notifications
                            </div>
                            <div className="settings-toggle-desc">
                              Receive notifications via email
                            </div>
                          </div>
                          <label className="settings-toggle">
                            <input
                              type="checkbox"
                              checked={settings.notifications.email}
                              onChange={() =>
                                handleToggle("notifications", "email")
                              }
                            />
                            <span className="settings-toggle-slider"></span>
                          </label>
                        </div>
                        <div className="settings-toggle-row">
                          <div>
                            <div className="settings-toggle-label">
                              Transaction Alerts
                            </div>
                            <div className="settings-toggle-desc">
                              Get notified about transactions
                            </div>
                          </div>
                          <label className="settings-toggle">
                            <input
                              type="checkbox"
                              checked={settings.notifications.transactionAlerts}
                              onChange={() =>
                                handleToggle(
                                  "notifications",
                                  "transactionAlerts",
                                )
                              }
                            />
                            <span className="settings-toggle-slider"></span>
                          </label>
                        </div>
                        <div className="settings-toggle-row">
                          <div>
                            <div className="settings-toggle-label">
                              Login Alerts
                            </div>
                            <div className="settings-toggle-desc">
                              Get notified of new login attempts
                            </div>
                          </div>
                          <label className="settings-toggle">
                            <input
                              type="checkbox"
                              checked={settings.notifications.loginAlerts}
                              onChange={() =>
                                handleToggle("notifications", "loginAlerts")
                              }
                            />
                            <span className="settings-toggle-slider"></span>
                          </label>
                        </div>
                        <div className="settings-toggle-row">
                          <div>
                            <div className="settings-toggle-label">
                              Marketing Emails
                            </div>
                            <div className="settings-toggle-desc">
                              Receive updates and promotional emails
                            </div>
                          </div>
                          <label className="settings-toggle">
                            <input
                              type="checkbox"
                              checked={settings.notifications.marketingEmails}
                              onChange={() =>
                                handleToggle("notifications", "marketingEmails")
                              }
                            />
                            <span className="settings-toggle-slider"></span>
                          </label>
                        </div>
                      </div>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Bell size={20} />
                        <h3>Push Notifications</h3>
                      </div>
                      <div className="settings-toggle-row">
                        <div>
                          <div className="settings-toggle-label">
                            Push Notifications
                          </div>
                          <div className="settings-toggle-desc">
                            Receive push notifications on your device
                          </div>
                        </div>
                        <label className="settings-toggle">
                          <input
                            type="checkbox"
                            checked={settings.notifications.push}
                            onChange={() =>
                              handleToggle("notifications", "push")
                            }
                          />
                          <span className="settings-toggle-slider"></span>
                        </label>
                      </div>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Smartphone size={20} />
                        <h3>SMS Notifications</h3>
                      </div>
                      <div className="settings-toggle-row">
                        <div>
                          <div className="settings-toggle-label">
                            SMS Notifications
                          </div>
                          <div className="settings-toggle-desc">
                            Receive text messages for important updates
                          </div>
                        </div>
                        <label className="settings-toggle">
                          <input
                            type="checkbox"
                            checked={settings.notifications.sms}
                            onChange={() =>
                              handleToggle("notifications", "sms")
                            }
                          />
                          <span className="settings-toggle-slider"></span>
                        </label>
                      </div>
                    </div>
                  </div>
                )}

                {/* Preferences Tab */}
                {activeTab === "preferences" && (
                  <div className="settings-section">
                    <div className="settings-section-header">
                      <h2>App Preferences</h2>
                      <p>Customize your app experience</p>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Globe size={20} />
                        <h3>Language</h3>
                      </div>
                      <p className="settings-card-desc">
                        Choose your preferred language
                      </p>
                      <select
                        className="settings-select"
                        value={settings.preferences.language}
                        onChange={(e) =>
                          handleSelectChange(
                            "preferences",
                            "language",
                            e.target.value,
                          )
                        }
                      >
                        <option value="English">English</option>
                        <option value="French">French</option>
                        <option value="Spanish">Spanish</option>
                        <option value="German">German</option>
                      </select>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Globe size={20} />
                        <h3>Currency</h3>
                      </div>
                      <p className="settings-card-desc">
                        Set your default currency
                      </p>
                      <select
                        className="settings-select"
                        value={settings.preferences.currency}
                        onChange={(e) => handleCurrencyUpdate(e.target.value)}
                      >
                        <option value="NGN">Nigerian Naira (NGN)</option>
                        <option value="USD">US Dollar (USD)</option>
                        <option value="EUR">Euro (EUR)</option>
                        <option value="GBP">British Pound (GBP)</option>
                      </select>
                    </div>

                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Globe size={20} />
                        <h3>Timezone</h3>
                      </div>
                      <p className="settings-card-desc">Set your timezone</p>
                      <select
                        className="settings-select"
                        value={settings.preferences.timezone}
                        onChange={(e) =>
                          handleSelectChange(
                            "preferences",
                            "timezone",
                            e.target.value,
                          )
                        }
                      >
                        <option value="Africa/Lagos">
                          West Africa Time (Lagos)
                        </option>
                        <option value="America/New_York">
                          Eastern Time (New York)
                        </option>
                        <option value="Europe/London">
                          Greenwich Mean Time (London)
                        </option>
                        <option value="Asia/Tokyo">Japan Time (Tokyo)</option>
                      </select>
                    </div>
                  </div>
                )}

                {/* PIN Tab */}
                {activeTab === "pin" && (
                  <>
                    <div className="settings-card">
                      <div className="settings-card-header">
                        <Key size={20} />
                        <h3>Withdrawal PIN</h3>
                      </div>

                      {userHasPin ? (
                        /* ── PIN already set ── */
                        <>
                          {/* Status banner */}
                          <div className="pin-already-set-banner">
                            <div className="pin-banner-left">
                              <i className="fas fa-shield-alt pin-banner-icon" />
                              <div>
                                <p className="pin-banner-title">
                                  Withdrawal PIN is active
                                </p>
                                <p className="pin-banner-desc">
                                  Your 4-digit PIN is set and protecting your
                                  transactions. Keep it confidential.
                                </p>
                              </div>
                            </div>
                            <span className="pin-set-badge">
                              <i
                                className="fas fa-check-circle"
                                style={{ marginRight: "5px" }}
                              />
                              PIN Set
                            </span>
                          </div>

                          {!showChangePinForm ? (
                            <button
                              className="settings-btn-change-pin"
                              onClick={() => setShowChangePinForm(true)}
                            >
                              <i
                                className="fas fa-key"
                                style={{ marginRight: "8px" }}
                              />
                              Change Withdrawal PIN
                            </button>
                          ) : (
                            <div className="settings-pin-form-wrapper">
                              <div className="pin-form-section-title">
                                Change Withdrawal PIN
                              </div>

                              {/* Current PIN */}
                              <label className="settings-pin-label">
                                Current PIN
                              </label>
                              <div className="settings-pin-inputs">
                                {currentPin.map((digit, index) => (
                                  <input
                                    key={`current-${index}`}
                                    id={`current-pin-${index}`}
                                    type="password"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    className="settings-pin-input"
                                    onChange={(e) =>
                                      handleCurrentPinInput(
                                        index,
                                        e.target.value,
                                      )
                                    }
                                    onInput={(e) => {
                                      if (e.currentTarget.value && index < 3) {
                                        document
                                          .getElementById(
                                            `current-pin-${index + 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                    onKeyDown={(e) => {
                                      if (
                                        e.key === "Backspace" &&
                                        !digit &&
                                        index > 0
                                      ) {
                                        document
                                          .getElementById(
                                            `current-pin-${index - 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                  />
                                ))}
                              </div>

                              {/* New PIN */}
                              <label className="settings-pin-label">
                                New PIN
                              </label>
                              <div className="settings-pin-inputs">
                                {pin.map((digit, index) => (
                                  <input
                                    key={`new-pin-${index}`}
                                    id={`new-pin-${index}`}
                                    type="password"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    className="settings-pin-input"
                                    onChange={(e) =>
                                      handlePinInput(
                                        index,
                                        e.target.value,
                                        false,
                                      )
                                    }
                                    onInput={(e) => {
                                      if (e.currentTarget.value && index < 3) {
                                        document
                                          .getElementById(
                                            `new-pin-${index + 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                    onKeyDown={(e) => {
                                      if (
                                        e.key === "Backspace" &&
                                        !digit &&
                                        index > 0
                                      ) {
                                        document
                                          .getElementById(
                                            `new-pin-${index - 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                  />
                                ))}
                              </div>

                              {/* Confirm New PIN */}
                              <label className="settings-pin-label">
                                Confirm New PIN
                              </label>
                              <div className="settings-pin-inputs">
                                {confirmPin.map((digit, index) => (
                                  <input
                                    key={`confirm-pin-${index}`}
                                    id={`confirm-pin-${index}`}
                                    type="password"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    className="settings-pin-input"
                                    onChange={(e) =>
                                      handlePinInput(
                                        index,
                                        e.target.value,
                                        true,
                                      )
                                    }
                                    onInput={(e) => {
                                      if (e.currentTarget.value && index < 3) {
                                        document
                                          .getElementById(
                                            `confirm-pin-${index + 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                    onKeyDown={(e) => {
                                      if (
                                        e.key === "Backspace" &&
                                        !digit &&
                                        index > 0
                                      ) {
                                        document
                                          .getElementById(
                                            `confirm-pin-${index - 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                  />
                                ))}
                              </div>

                              <div className="pin-form-actions">
                                <button
                                  className="settings-btn-secondary"
                                  onClick={() => {
                                    setShowChangePinForm(false);
                                    setPin(["", "", "", ""]);
                                    setConfirmPin(["", "", "", ""]);
                                    setCurrentPin(["", "", "", ""]);
                                  }}
                                >
                                  Cancel
                                </button>
                                <button
                                  className="settings-btn-primary"
                                  style={{
                                    width: "auto",
                                    padding: "12px 28px",
                                  }}
                                  onClick={handleChangePin}
                                >
                                  Update PIN
                                </button>
                              </div>
                            </div>
                          )}
                        </>
                      ) : (
                        /* ── PIN not set yet ── */
                        <>
                          <div className="pin-not-set-banner">
                            <i className="fas fa-exclamation-circle pin-banner-icon-warn" />
                            <div>
                              <p className="pin-banner-title">
                                No Withdrawal PIN set
                              </p>
                              <p className="pin-banner-desc">
                                Create a 4-digit PIN to authorize withdrawals
                                and protect your account.
                              </p>
                            </div>
                          </div>

                          {!showCreatePinForm ? (
                            <button
                              className="settings-btn-create-pin"
                              onClick={() => setShowCreatePinForm(true)}
                            >
                              <i
                                className="fas fa-plus-circle"
                                style={{ marginRight: "8px" }}
                              />
                              Create Withdrawal PIN
                            </button>
                          ) : (
                            <div className="settings-pin-form-wrapper">
                              <div className="pin-form-section-title">
                                Create Withdrawal PIN
                              </div>

                              {/* New PIN */}
                              <label className="settings-pin-label">
                                New 4-Digit PIN
                              </label>
                              <div className="settings-pin-inputs">
                                {pin.map((digit, index) => (
                                  <input
                                    key={`create-pin-${index}`}
                                    id={`create-pin-${index}`}
                                    type="password"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    className="settings-pin-input"
                                    onChange={(e) =>
                                      handlePinInput(
                                        index,
                                        e.target.value,
                                        false,
                                      )
                                    }
                                    onInput={(e) => {
                                      if (e.currentTarget.value && index < 3) {
                                        document
                                          .getElementById(
                                            `create-pin-${index + 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                    onKeyDown={(e) => {
                                      if (
                                        e.key === "Backspace" &&
                                        !digit &&
                                        index > 0
                                      ) {
                                        document
                                          .getElementById(
                                            `create-pin-${index - 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                  />
                                ))}
                              </div>

                              {/* Confirm PIN */}
                              <label className="settings-pin-label">
                                Confirm PIN
                              </label>
                              <div className="settings-pin-inputs">
                                {confirmPin.map((digit, index) => (
                                  <input
                                    key={`create-confirm-${index}`}
                                    id={`create-confirm-${index}`}
                                    type="password"
                                    inputMode="numeric"
                                    maxLength={1}
                                    value={digit}
                                    className="settings-pin-input"
                                    onChange={(e) =>
                                      handlePinInput(
                                        index,
                                        e.target.value,
                                        true,
                                      )
                                    }
                                    onInput={(e) => {
                                      if (e.currentTarget.value && index < 3) {
                                        document
                                          .getElementById(
                                            `create-confirm-${index + 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                    onKeyDown={(e) => {
                                      if (
                                        e.key === "Backspace" &&
                                        !digit &&
                                        index > 0
                                      ) {
                                        document
                                          .getElementById(
                                            `create-confirm-${index - 1}`,
                                          )
                                          ?.focus();
                                      }
                                    }}
                                  />
                                ))}
                              </div>

                              <div className="pin-form-actions">
                                <button
                                  className="settings-btn-secondary"
                                  onClick={() => {
                                    setShowCreatePinForm(false);
                                    setPin(["", "", "", ""]);
                                    setConfirmPin(["", "", "", ""]);
                                  }}
                                >
                                  Cancel
                                </button>
                                <button
                                  className="settings-btn-primary"
                                  style={{
                                    width: "auto",
                                    padding: "12px 28px",
                                  }}
                                  onClick={handleSetPin}
                                >
                                  Create PIN
                                </button>
                              </div>
                            </div>
                          )}
                        </>
                      )}
                    </div>
                  </>
                )}

                {/* Danger Zone */}
                <div className="settings-danger-zone">
                  <h3>Danger Zone</h3>
                  <div className="settings-danger-actions">
                    <div className="settings-danger-item">
                      <div>
                        <h4>Export Data</h4>
                        <p>Download all your account data</p>
                      </div>
                      <button
                        className="settings-btn-outline"
                        onClick={handleExportData}
                      >
                        <Download size={16} />
                        Export
                      </button>
                    </div>
                    <div className="settings-danger-item">
                      <div>
                        <h4>Delete Account</h4>
                        <p>Permanently delete your account and all data</p>
                      </div>
                      <button
                        className="settings-btn-danger"
                        onClick={handleDeleteAccount}
                        style={{ display: "flex" }}
                      >
                        <Trash2 size={16} />
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              {/* Delete Account Modal */}
              {showDeleteModal && (
                <>
                  <div
                    className="settings-modal-overlay"
                    onClick={() => setShowDeleteModal(false)}
                  />
                  <div className="settings-modal">
                    <div className="settings-modal-content">
                      <div className="settings-modal-icon danger">
                        <AlertCircle size={48} />
                      </div>
                      <h3>Delete Account</h3>
                      <p>
                        Are you sure you want to delete your account? This
                        action cannot be undone and all your data will be
                        permanently removed.
                      </p>
                      <div className="settings-modal-actions">
                        <button
                          className="settings-btn-secondary"
                          onClick={() => setShowDeleteModal(false)}
                        >
                          Cancel
                        </button>
                        <button
                          className="settings-btn-danger"
                          onClick={handleDeleteAccountRequest}
                        >
                          Delete Account
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {/* Remove Image Confirmation Modal */}
              {showRemoveImageModal && (
                <>
                  <div
                    className="settings-modal-overlay"
                    onClick={() => setShowRemoveImageModal(false)}
                  />
                  <div className="settings-modal">
                    <div className="settings-modal-content">
                      <div className="settings-modal-icon danger">
                        <Trash2 size={48} />
                      </div>
                      <h3>Remove Profile Picture</h3>
                      <p>
                        Are you sure you want to remove your profile picture?
                        Your profile will display the default avatar.
                      </p>
                      <div className="settings-modal-actions">
                        <button
                          className="settings-btn-secondary"
                          onClick={() => setShowRemoveImageModal(false)}
                        >
                          Cancel
                        </button>
                        <button
                          className="settings-btn-danger"
                          onClick={confirmRemoveImage}
                        >
                          Remove Picture
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {/* Change Password Modal */}
              {showPasswordModal && (
                <>
                  <div
                    className="settings-modal-overlay"
                    onClick={() => setShowPasswordModal(false)}
                  />
                  <div className="settings-modal">
                    <div className="settings-modal-content">
                      <div className="settings-modal-icon primary">
                        <Key size={48} />
                      </div>
                      <h3>Change Password</h3>
                      <div className="settings-form-group">
                        <label>Current Password</label>
                        <input
                          type="password"
                          name="currentPassword"
                          placeholder="Enter current password"
                          value={passwordData.currentPassword}
                          onChange={handlePasswordInputChange}
                          className={
                            passwordErrors.currentPassword ? "error" : ""
                          }
                        />
                        {passwordErrors.currentPassword && (
                          <span
                            className="error-message"
                            style={{
                              color: "var(--bg-main)",
                              fontSize: "0.875rem",
                              marginTop: "0.25rem",
                              display: "block",
                            }}
                          >
                            {passwordErrors.currentPassword}
                          </span>
                        )}
                      </div>
                      <div className="settings-form-group">
                        <label>New Password</label>
                        <input
                          type="password"
                          name="newPassword"
                          placeholder="Enter new password"
                          value={passwordData.newPassword}
                          onChange={handlePasswordInputChange}
                          className={passwordErrors.newPassword ? "error" : ""}
                        />
                        {passwordErrors.newPassword && (
                          <span
                            className="error-message"
                            style={{
                              color: "var(--bg-main)",
                              fontSize: "0.875rem",
                              marginTop: "0.25rem",
                              display: "block",
                            }}
                          >
                            {passwordErrors.newPassword}
                          </span>
                        )}
                      </div>
                      <div className="settings-form-group">
                        <label>Confirm Password</label>
                        <input
                          type="password"
                          name="confirmPassword"
                          placeholder="Confirm new password"
                          value={passwordData.confirmPassword}
                          onChange={handlePasswordInputChange}
                          className={
                            passwordErrors.confirmPassword ? "error" : ""
                          }
                        />
                        {passwordErrors.confirmPassword && (
                          <span
                            className="error-message"
                            style={{
                              color: "var(--bg-main)",
                              fontSize: "0.875rem",
                              marginTop: "0.25rem",
                              display: "block",
                            }}
                          >
                            {passwordErrors.confirmPassword}
                          </span>
                        )}
                      </div>
                      <div className="settings-modal-actions">
                        <button
                          className="settings-btn-secondary"
                          onClick={() => {
                            setShowPasswordModal(false);
                            setPasswordData({
                              currentPassword: "",
                              newPassword: "",
                              confirmPassword: "",
                            });
                            setPasswordErrors({
                              currentPassword: "",
                              newPassword: "",
                              confirmPassword: "",
                            });
                          }}
                        >
                          Cancel
                        </button>
                        <button
                          className="settings-btn-primary"
                          onClick={handlePasswordSubmit}
                        >
                          Change Password
                        </button>
                      </div>
                    </div>
                  </div>
                </>
              )}
            </div>
            <Footer theme={theme} />
          </div>
        </main>
        <MobileNav
          activeTab="none"
          onPlusClick={() => setIsDepositOpen(true)}
        />
        <DepositModal
          isOpen={isDepositOpen}
          onClose={() => setIsDepositOpen(false)}
          theme={theme}
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

        <SupportChatBot
          isOpen={isChatOpen}
          onClose={() => setIsChatOpen(false)}
        />
      </div>
    </>
  );
};

export default Settings;
