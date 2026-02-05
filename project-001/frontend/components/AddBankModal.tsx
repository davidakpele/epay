"use client";

import { useState, useRef, useEffect } from "react";

interface Props {
  onClose: () => void;
  onSuccess: () => void;
}

const AddBankModal = ({ onClose, onSuccess }: Props) => {
  const [accountNumber, setAccountNumber] = useState("");
  const [bank, setBank] = useState("");
  const [isValid, setIsValid] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(false);
  const modalRef = useRef<HTMLDivElement>(null);

  const handleSearch = () => {
    setLoading(true);

    setTimeout(() => {
      if (accountNumber.length === 10 && bank) {
        setIsValid(true);
      } else {
        setIsValid(false);
      }
      setLoading(false);
    }, 1000);
  };

  const handleSave = () => {
    onSuccess();
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (modalRef.current && !modalRef.current.contains(event.target as Node)) {
        onClose();
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [onClose]);

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div ref={modalRef} className="bg-white w-full max-w-md rounded-2xl p-6 relative mx-4">
        {/* Back */}
        <button
          onClick={onClose}
          className="text-sm text-black flex items-center gap-1"
        >
          ← Back
        </button>

        <h2 className="text-xl font-semibold text-center mt-4">
          Add New Bank Account
        </h2>

        <p className="text-sm text-center mt-3 text-gray-600">
          <span className="font-semibold">NOTE:</span> Accounts added here are
          solely for deposits and not for withdrawals. Make sure it is your own
          account and the account name matches Alan Bola
        </p>

        {/* Select Bank */}
        <div className="mt-6">
          <label className="text-sm font-medium text-gray-600">Select Your Bank</label>
          <select
            className="mt-2 w-full border rounded-lg px-3 py-2 text-gray-600"
            value={bank}
            onChange={(e) => setBank(e.target.value)}
          >
            <option value="">Select...</option>
            <option value="gtb">GTBank</option>
            <option value="uba">UBA</option>
            <option value="zenith">Zenith Bank</option>
          </select>
        </div>

        {/* Account Number */}
        <div className="mt-4">
          <label className="text-sm font-medium text-gray-600">Account Number</label>
          <input
            type="text"
            className="mt-2 w-full border rounded-lg px-3 py-2 text-gray-600"
            placeholder="Enter account number"
            value={accountNumber}
            onChange={(e) => setAccountNumber(e.target.value)}
          />
        </div>

        {/* Error */}
        {isValid === false && (
          <p className="mt-3 text-sm text-red-600">
            Invalid bank details provided.
          </p>
        )}

        {/* Action Button */}
        <button
          onClick={isValid ? handleSave : handleSearch}
          disabled={loading}
          className="mt-8 w-full bg-[#5B5E80] text-white py-3 rounded-lg
                     hover:opacity-90 transition"
        >
          {loading
            ? "Checking..."
            : isValid
            ? "Add Bank Details"
            : "Search"}
        </button>
      </div>
    </div>
  );
};

export default AddBankModal;