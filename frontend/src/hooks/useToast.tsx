import React, {createContext, ReactNode, useCallback, useContext, useRef, useState} from 'react';
import {ToastContainer, ToastMessage} from '../components/Toast';

interface ToastContextType {
  showToast: (type: 'success' | 'error', title: string, message?: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

interface ToastProviderProps {
  children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const idCounter = useRef(0);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (type: 'success' | 'error', title: string, message?: string) => {
      const id = `${Date.now()}-${idCounter.current++}`;
      const duration = type === 'success' ? 5000 : 8000;

      setToasts((prev) => {
        const newToast: ToastMessage = { id, type, title, message };
        const updated = [...prev, newToast];
        if (updated.length > 3) {
          return updated.slice(updated.length - 3);
        }
        return updated;
      });

      setTimeout(() => {
        setToasts((prev) => prev.map((t) => (t.id === id ? { ...t, exiting: true } : t)));

        setTimeout(() => {
          removeToast(id);
        }, 300);
      }, duration);
    },
    [removeToast]
  );

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <ToastContainer toasts={toasts} />
    </ToastContext.Provider>
  );
};
