import { useState, useEffect } from 'react';
import './ErrorNotification.css';

/**
 * Notification/Toast component for displaying error, warning, info, and success messages.
 * Used for displaying API error responses and user feedback.
 */
export function ErrorNotification({ 
  message, 
  type = 'error', // 'error', 'warning', 'info', 'success'
  duration = 5000,
  onClose 
}) {
  const [isVisible, setIsVisible] = useState(!!message);

  useEffect(() => {
    if (message) {
      setIsVisible(true);
      
      // Auto-hide after duration (don't auto-hide for errors - require manual close)
      if (type !== 'error') {
        const timer = setTimeout(() => {
          handleClose();
        }, duration);
        
        return () => clearTimeout(timer);
      }
    }
  }, [message, duration, type]);

  const handleClose = () => {
    setIsVisible(false);
    if (onClose) {
      onClose();
    }
  };

  if (!isVisible || !message) {
    return null;
  }

  return (
    <div className={`notification notification-${type}`}>
      <div className="notification-content">
        <div className="notification-icon">
          {type === 'error' && '✕'}
          {type === 'warning' && '⚠'}
          {type === 'info' && 'ℹ'}
          {type === 'success' && '✓'}
        </div>
        <div className="notification-message">
          {message}
        </div>
        <button 
          className="notification-close"
          onClick={handleClose}
          aria-label="Close notification"
        >
          ×
        </button>
      </div>
    </div>
  );
}

/**
 * Notification container component to manage multiple notifications
 */
export function NotificationContainer({ notifications = [] }) {
  return (
    <div className="notification-container">
      {notifications.map((notif) => (
        <ErrorNotification
          key={notif.id}
          message={notif.message}
          type={notif.type || 'info'}
          duration={notif.duration}
          onClose={() => notif.onClose && notif.onClose(notif.id)}
        />
      ))}
    </div>
  );
}

export default ErrorNotification;
