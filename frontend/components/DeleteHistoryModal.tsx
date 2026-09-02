'use client';

import React from 'react';
import { AlertCircle, Trash2, X } from 'lucide-react';
import './DeleteHistoryModal.css';
import { Transaction } from '@/app/types/utils';

interface DeleteHistoryModalProps {
  transaction: Transaction;
  onClose: () => void;
  onDelete: (transactionId: string) => Promise<void>;
  theme?: 'light' | 'dark';
}

const DeleteHistoryModal: React.FC<DeleteHistoryModalProps> = ({
  transaction,
  onClose,
  onDelete,
  theme = 'light'
}) => {
  const [isDeleting, setIsDeleting] = React.useState(false);
  const [error, setError] = React.useState<string>('');

  const handleDelete = async () => {
    try {
      setIsDeleting(true);
      setError('');
      await onDelete(transaction.id);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to delete transaction');
    } finally {
      setIsDeleting(false);
    }
  };

  // Prevent background scroll when modal is open
  React.useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, []);

  return (
    <>
      <div 
        className={`delete-modal-overlay ${theme}`}
        onClick={onClose}
      />
      
      <div className={`delete-modal-container ${theme}`}>
        <div className="delete-modal-content">
          {/* Header */}
          <div className="delete-modal-header">
            <div className="delete-modal-header-left">
              <div className="delete-modal-icon-wrapper">
                <Trash2 size={24} />
              </div>
              <div>
                <h2 className="delete-modal-title">Delete Transaction</h2>
                <p className="delete-modal-subtitle">
                  This action cannot be undone
                </p>
              </div>
            </div>
            <button 
              className="delete-modal-close-btn"
              onClick={onClose}
              disabled={isDeleting}
            >
              <X size={20} />
            </button>
          </div>

          {/* Warning Message */}
          <div className="delete-modal-warning">
            <div className="warning-content">
              <h3 className="warning-title">Are you sure?</h3>
              <p className="warning-text">
                You are about to permanently delete this transaction from your history.
              </p>
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="delete-error-message">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {/* Action Buttons */}
          <div className="delete-modal-actions">
            <button
              className="delete-cancel-btn"
              onClick={onClose}
              disabled={isDeleting}
            >
              Cancel
            </button>
            <button
              className="delete-confirm-btn"
              onClick={handleDelete}
              disabled={isDeleting}
            >
              {isDeleting ? (
                <>
                  <div className="delete-spinner-small"></div>
                  <span>Deleting...</span>
                </>
              ) : (
                <>
                  <Trash2 size={16} />
                  <span>Delete</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default DeleteHistoryModal;