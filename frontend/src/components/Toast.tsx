import React from 'react';
import {createPortal} from 'react-dom';
import styles from './Toast.module.css';

export interface ToastMessage {
  id: string;
  type: 'success' | 'error';
  title: string;
  message?: string;
  exiting?: boolean;
}

interface ToastContainerProps {
  toasts: ToastMessage[];
}

export const ToastContainer: React.FC<ToastContainerProps> = ({ toasts }) => {
  return createPortal(
    <div className={styles['md-toast-container']}>
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`${styles['md-toast']} ${
            toast.type === 'success' ? styles['md-toast--success'] : styles['md-toast--error']
          }`}
          data-exiting={toast.exiting}
        >
          <div className={styles['md-toast__icon']}>
            {toast.type === 'success' ? (
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M20 6L9 17L4 12"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            ) : (
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M18 6L6 18M6 6L18 18"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            )}
          </div>
          <div className={styles['md-toast__content']}>
            <div className={styles['md-toast__headline']}>{toast.title}</div>
            {toast.message && <div className={styles['md-toast__body']}>{toast.message}</div>}
          </div>
        </div>
      ))}
    </div>,
    document.body
  );
};
