import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { calculateProfileCompletion } from '@/app/api';

interface KycCheckProgressProps {
  progress?: number;
  linkText?: string;
  linkUrl?: string;
  autoCalculate?: boolean;
}

const KycCheckProgress: React.FC<KycCheckProgressProps> = ({
  progress,
  linkText = "Complete your KYC",
  linkUrl = "#",
  autoCalculate = true
}) => {
  const [calculatedProgress, setCalculatedProgress] = useState<number>(0);

  useEffect(() => {
    if (autoCalculate) {
      const percentage = calculateProfileCompletion();
      setCalculatedProgress(percentage);
    }
  }, [autoCalculate]);

  // Use provided progress or calculated progress
  const displayProgress = autoCalculate ? calculatedProgress : (progress || 0);
  const clampedProgress = Math.max(0, Math.min(100, displayProgress));

  return (
    <div className="kyc-progress-container">
      <Link href={"/user"} className="kyc-progress-header-link">
        <h2 className="kyc-progress-header">{linkText}</h2>
        <span className="kyc-progress-arrow">→</span>
      </Link>
      <div className="kyc-progress-bar-wrapper">
        <div className="kyc-progress-bar">
          <div
            className="kyc-progress-fill"
            style={{ width: `${clampedProgress}%` }}
          ></div>
        </div>
        <span 
          className="kyc-progress-percentage" 
          style={{ color: clampedProgress === 100 ? '#4CAF50' : 'inherit' }}
        >
          {clampedProgress}%
        </span>
      </div>
    </div>
  );
};

export default KycCheckProgress;