import React from 'react';
import styles from './LoadingScreen.module.css';

const LoadingScreen: React.FC = () => {
  return (
    <div className={styles.loadingContainer}>
      {/* Animated background elements */}
      <div className={styles.bgCircle1}></div>
      <div className={styles.bgCircle2}></div>
      <div className={styles.bgCircle3}></div>
      
      <div className={styles.content}>
        

        {/* Brand Name */}
        <h1 className={styles.brandName}>ePay</h1>

        {/* Tagline */}
        <p className={styles.tagline}>Business Banking</p>
        
        {/* Status text */}
        <p className={styles.statusText}>Initializing secure connection...</p>
        
        {/* Modern progress indicator */}
        <div className={styles.progressContainer}>
          <div className={styles.progressBar}></div>
          <div className={styles.progressGlow}></div>
        </div>

        {/* Animated dots */}
        <div className={styles.dots}>
          <span className={styles.dot}></span>
          <span className={styles.dot}></span>
          <span className={styles.dot}></span>
        </div>
      </div>
    </div>
  );
};

export default LoadingScreen;