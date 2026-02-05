'use client';

import React, { useEffect } from 'react';
import { Transaction as TransactionType } from '@/app/types/utils'; // Import your Transaction type

interface Props {
  transaction: TransactionType; // Use your imported Transaction type
}

const formatAmount = (amount: number) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Math.abs(amount));
};

const getSwapDescription = (description: string, transaction: TransactionType) => {
  // If it's a swap transaction, format the description like in your example
  if (transaction.type === 'swap') {
    // Check if description already has the swap format
    if (description.toLowerCase().includes('swap')) {
      return description;
    }
    // Generate a swap description based on currencies
    const fromCurrency = transaction.currency;
    const toCurrency = 'EUR'; // Assuming swap is to EUR based on your example
    const rate = 0.00060695; // Example rate
    return `SWAP ${fromCurrency}/EUR : cut ${rate.toFixed(8)}`;
  }
  return description;
};

const TransactionReceipt: React.FC<Props> = ({ transaction }) => {
  useEffect(() => {
    // Auto-open receipt when component mounts
    openReceiptWindow();
  }, [transaction]);

  const openReceiptWindow = () => {
    const printWindow = window.open('', '_blank', 'width=600,height=800');
    
    if (!printWindow) {
      return;
    }

    // Generate a receipt number if not provided
    const receiptNumber = transaction.transactionId || `NX${Math.random().toString(36).substr(2, 9)}`;
    
    // Generate reference number if not provided
    const referenceNo = transaction.referenceNo || `REF${Math.floor(1000000 + Math.random() * 9000000)}`;
    
    // Generate terminal ID if not provided
    const terminalId = transaction.terminalId || 'ATM-9989';
    
    // Format date
    const formattedDate = new Date(transaction.date).toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric'
    });
    
    const formattedTime = new Date(transaction.date).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    }).replace(':', ':');
    
    const description = getSwapDescription(transaction.description, transaction);
    
    const receiptContent = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Transaction Receipt - ${receiptNumber}</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          /* Modern CSS Reset */
          * { 
            margin: 0; 
            padding: 0; 
            box-sizing: border-box; 
          }
          
          /* Base Styles */
          body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #f5f5f7;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
            padding: 20px;
            line-height: 1.5;
            color: #333;
          }
          
          /* Receipt Container */
          .receipt-container {
            width: 100%;
            max-width: 420px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 20px #c3290b;
            overflow: hidden;
            position: relative;
          }
          
          /* Header Section */
          .receipt-header {
            background: #c3290b;
            color: white;
            padding: 25px 20px;
            text-align: center;
          }
          
          .bank-logo {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 5px;
            color: #fff;
            letter-spacing: 0.5px;
          }
          
          .bank-tagline {
            font-size: 12px;
            opacity: 0.8;
            margin-bottom: 15px;
            font-weight: 400;
            color: #a0a0a0;
          }
          
          .receipt-title {
            font-size: 18px;
            font-weight: 600;
            margin: 15px 0 5px;
          }
          
          .receipt-subtitle {
            font-size: 14px;
            opacity: 0.9;
            font-weight: 400;
            color: #d0d0d0;
            margin-bottom: 15px;
          }
          
          /* Status Badge */
          .status-badge {
            display: inline-block;
            padding: 6px 16px;
            background: ${transaction.status === 'completed' ? '#10b981' : 
                        transaction.status === 'pending' ? '#f59e0b' : 
                        '#ef4444'};
            color: white;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }
          
          /* Content Sections */
          .receipt-content {
            padding: 0;
          }
          
          .section {
            padding: 20px;
            border-bottom: 1px solid #eaeaea;
          }
          
          .section-title {
            font-size: 12px;
            font-weight: 600;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 15px;
          }
          
          /* Info Grid */
          .info-grid {
            display: grid;
            gap: 12px;
          }
          
          .info-row {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            padding: 5px 0;
          }
          
          .info-label {
            font-weight: 500;
            color: #666;
            font-size: 13px;
            flex: 1;
          }
          
          .info-value {
            font-weight: 500;
            color: #333;
            font-size: 13px;
            text-align: right;
            flex: 1;
            word-break: break-word;
          }
          
          /* Amount Highlight */
          .amount-section {
            text-align: center;
            padding: 25px 20px;
            background: #f8f9fa;
            border-bottom: 1px solid #eaeaea;
          }
          
          .amount-main {
            font-size: 28px;
            font-weight: 700;
            color: ${transaction.amount >= 0 ? '#10b981' : '#ef4444'};
            margin-bottom: 5px;
          }
          
          .amount-sub {
            font-size: 13px;
            color: #666;
            font-weight: 500;
            text-transform: uppercase;
          }
          
          /* Balance Summary */
          .balance-summary {
            background: #f8f9fa;
            border: 1px solid #eaeaea;
            border-radius: 6px;
            padding: 15px;
            margin-top: 10px;
          }
          
          /* Security Features */
          .security-features {
            display: flex;
            justify-content: center;
            gap: 20px;
            margin: 20px 0;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 6px;
            border: 1px solid #eaeaea;
          }
          
          .security-icon {
            font-size: 12px;
            color: #666;
            display: flex;
            align-items: center;
            gap: 5px;
          }
          
          /* Footer */
          .receipt-footer {
            padding: 25px 20px;
            background: #fff;
            text-align: center;
          }
          
          .thank-you {
            font-size: 15px;
            font-weight: 600;
            color: #1a1a2e;
            margin-bottom: 15px;
          }
          
          .footer-text {
            font-size: 12px;
            color: #666;
            line-height: 1.5;
            margin-bottom: 8px;
          }
          
          .contact-info {
            font-size: 11px;
            color: #888;
            margin-top: 15px;
            font-family: monospace;
          }
          
          /* QR Code Section */
          .qr-section {
            text-align: center;
            padding: 20px;
            border: 1px solid #eaeaea;
            border-radius: 6px;
            margin: 20px 0;
            background: #fff;
          }
          
          .qr-placeholder {
            width: 100px;
            height: 100px;
            background: #f8f9fa;
            border: 1px dashed #d1d5db;
            border-radius: 4px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: #666;
            font-size: 10px;
            margin-bottom: 10px;
          }
          
          .qr-label {
            font-size: 12px;
            color: #666;
            margin-top: 10px;
          }
          
          /* Action Buttons */
          .action-buttons {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background: white;
            padding: 15px 20px;
            display: flex;
            gap: 10px;
            justify-content: center;
            box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
            z-index: 1000;
          }
          
          .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            font-size: 14px;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            min-width: 120px;
            justify-content: center;
          }
          
          .btn-primary {
            background: #1a1a2e;
            color: white;
          }
          
          .btn-primary:hover {
            background: #2a2a3e;
          }
          
          .btn-secondary {
            background: #6b7280;
            color: white;
          }
          
          .btn-secondary:hover {
            background: #4b5563;
          }
          
          /* Additional Info */
          .additional-info {
            font-size: 11px;
            color: #888;
            margin-top: 5px;
            line-height: 1.4;
          }
          
          /* Print Styles */
          @media print {
            body {
              background: white !important;
              padding: 0 !important;
              font-size: 11px;
            }
            
            .receipt-container {
              box-shadow: none !important;
              border-radius: 0 !important;
              margin: 0 !important;
              max-width: none !important;
              width: 100% !important;
            }
            
            .action-buttons {
              display: none !important;
            }
            
            .receipt-header {
              background: #c3290b !important;
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }
            
            .status-badge {
              -webkit-print-color-adjust: exact;
              print-color-adjust: exact;
            }
            
            .amount-main {
              font-size: 22px !important;
            }
            
            .section {
              padding: 15px !important;
              page-break-inside: avoid;
            }
            
            .qr-section {
              page-break-inside: avoid;
            }
          }
          
          /* Animations */
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
          }
          
          .receipt-container {
            animation: fadeIn 0.4s ease-out;
          }
          
          /* Responsive Design */
          @media (max-width: 480px) {
            body {
              padding: 10px;
            }
            
            .receipt-container {
              max-width: 100%;
            }
            
            .action-buttons {
              flex-direction: column;
              padding: 10px;
            }
            
            .btn {
              width: 100%;
            }
          }
          
          /* Dark mode for QR code area */
          .qr-verification {
            font-size: 11px;
            color: #666;
            margin-top: 5px;
            font-weight: 500;
          }
        </style>
      </head>
      <body>
        <div class="receipt-container">
          <!-- Header -->
          <div class="receipt-header">
            <div class="bank-logo">ePay</div>
            <div class="bank-tagline">Secure Digital Banking</div>
            <div class="receipt-title">Transaction Receipt</div>
            <div class="receipt-subtitle">Official Confirmation</div>
            <div class="status-badge">${transaction.status}</div>
          </div>
          
          <!-- Main Content -->
          <div class="receipt-content">
            <!-- Transaction Details -->
            <div class="section">
              <div class="section-title">Transaction Details</div>
              <div class="info-grid">
                <div class="info-row">
                  <div class="info-label">Receipt Number</div>
                  <div class="info-value">${receiptNumber}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Date & Time</div>
                  <div class="info-value">${formattedDate} at ${formattedTime}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Terminal ID</div>
                  <div class="info-value">${terminalId}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Reference No</div>
                  <div class="info-value">${referenceNo}</div>
                </div>
              </div>
            </div>
            
            <!-- Amount Section -->
            <div class="amount-section">
              <div class="amount-main">
                ${transaction.amount >= 0 ? '+' : ''}${transaction.currency} ${formatAmount(transaction.amount)}
              </div>
              <div class="amount-sub">
                ${transaction.type === 'swap' ? 'Swap' : 
                  transaction.type.charAt(0).toUpperCase() + transaction.type.slice(1)} Transaction
              </div>
            </div>
            
            <!-- Account Information -->
            <div class="section">
              <div class="section-title">Account Information</div>
              <div class="info-grid">
                <div class="info-row">
                  <div class="info-label">Account Holder</div>
                  <div class="info-value">${transaction.accountHolder || 'Electron Wallet User'}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Account Type</div>
                  <div class="info-value">Digital Wallet</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Transaction Type</div>
                  <div class="info-value">${transaction.type === 'swap' ? 'Swap' : 
                    transaction.type.charAt(0).toUpperCase() + transaction.type.slice(1)}</div>
                </div>
              </div>
            </div>
            
            <!-- Balance Summary -->
            <div class="section">
              <div class="section-title">Balance Summary</div>
              <div class="balance-summary">
                <div class="info-grid">
                  <div class="info-row">
                    <div class="info-label">Previous Balance</div>
                    <div class="info-value">${transaction.currency} ${formatAmount(transaction.previousBalance || 0)}</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">Transaction Amount</div>
                    <div class="info-value" style="color: ${transaction.amount >= 0 ? '#10b981' : '#ef4444'}">
                      ${transaction.amount >= 0 ? '+' : ''}${transaction.currency} ${formatAmount(transaction.amount)}
                    </div>
                  </div>
                  <div class="info-row" style="border-top: 1px solid #eaeaea; padding-top: 12px; margin-top: 8px;">
                    <div class="info-label" style="font-weight: 600;">Available Balance</div>
                    <div class="info-value" style="font-weight: 600;">${transaction.currency} ${formatAmount(transaction.availableBalance || 0)}</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Additional Information -->
            <div class="section">
              <div class="section-title">Additional Information</div>
              <div class="info-grid">
                <div class="info-row">
                  <div class="info-label">Description</div>
                  <div class="info-value">${description}</div>
                </div>
                <div class="info-row">
                  <div class="info-label">Transaction Method</div>
                  <div class="info-value">Digital Wallet Transfer</div>
                </div>
              </div>
            </div>
            
            <!-- Security Features -->
            <div class="section">
              <div class="security-features">
                <span class="security-icon">🔒 SSL Secured</span>
                <span class="security-icon">✅ Verified</span>
                <span class="security-icon">📧 E-Receipt</span>
              </div>
            </div>
            
            <!-- QR Code Section -->
            <div class="section">
              <div class="qr-section">
                <div class="qr-placeholder">
                  QR Code
                </div>
                <div class="qr-verification">Scan to verify transaction authenticity</div>
              </div>
            </div>
          </div>
          
          <!-- Footer -->
          <div class="receipt-footer">
            <div class="thank-you">Thank you for banking with us!</div>
            <div class="footer-text">
              This is an official electronic receipt. Please retain for your records.
            </div>
            <div class="footer-text">
              For inquiries, contact customer service at 1-800-ePay-HELP
            </div>
            <div class="contact-info">
              www.epay.com • support@epay.com
            </div>
          </div>
        </div>
        
        <!-- Action Buttons -->
        <div class="action-buttons">
          <button class="btn btn-primary" onclick="window.print()">
            📄 Print Receipt
          </button>
          <button class="btn btn-secondary" onclick="window.close()">
            ✕ Close Window
          </button>
        </div>
        
        <script>
          // Add keyboard shortcuts
          document.addEventListener('keydown', function(e) {
            // Ctrl+P or Cmd+P for print
            if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
              e.preventDefault();
              window.print();
            }
            // Escape key to close
            if (e.key === 'Escape') {
              window.close();
            }
          });
          
          // Auto-focus on print button for accessibility
          window.onload = function() {
            const printBtn = document.querySelector('.btn-primary');
            if (printBtn) printBtn.focus();
          };
        </script>
      </body>
      </html>
    `;
    
    printWindow.document.write(receiptContent);
    printWindow.document.close();
    printWindow.focus();
  };

  // Component returns null since the receipt opens in a new window
  return null;
};

export default TransactionReceipt;