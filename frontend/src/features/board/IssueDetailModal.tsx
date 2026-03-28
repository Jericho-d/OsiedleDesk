import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import styles from './IssueDetailModal.module.css';
import {
    type AttachmentResponse,
    type IssueResponse,
    Priority,
    Status,
    STATUS_LABELS,
    STATUS_TRANSITIONS,
} from '@/types';
import {attachmentApi, issueApi} from '@/services/api';

function isImageAttachment(att: AttachmentResponse): boolean {
  return att.contentType.startsWith('image/');
}

interface LightboxProps {
  images: AttachmentResponse[];
  currentIndex: number;
  onClose: () => void;
  onNavigate: (index: number) => void;
}

function Lightbox({ images, currentIndex, onClose, onNavigate }: LightboxProps) {
  const current = images[currentIndex];
  if (!current) return null;
  const hasMultiple = images.length > 1;

  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowLeft' && currentIndex > 0) onNavigate(currentIndex - 1);
      if (e.key === 'ArrowRight' && currentIndex < images.length - 1) onNavigate(currentIndex + 1);
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [currentIndex, images.length, onClose, onNavigate]);

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  return createPortal(
    <div className={styles.lightboxBackdrop} onClick={handleBackdropClick}>
      <button className={styles.lightboxClose} onClick={onClose} aria-label="Close image viewer">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>

      {hasMultiple && currentIndex > 0 && (
        <button
          className={`${styles.lightboxNav} ${styles.lightboxNavLeft}`}
          onClick={() => onNavigate(currentIndex - 1)}
          aria-label="Previous image"
        >
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 18L9 12L15 6" />
          </svg>
        </button>
      )}

      <img
        src={attachmentApi.downloadUrl(current.id)}
        alt={current.filename}
        className={styles.lightboxImage}
      />

      {hasMultiple && currentIndex < images.length - 1 && (
        <button
          className={`${styles.lightboxNav} ${styles.lightboxNavRight}`}
          onClick={() => onNavigate(currentIndex + 1)}
          aria-label="Next image"
        >
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 18L15 12L9 6" />
          </svg>
        </button>
      )}

      {hasMultiple && (
        <div className={styles.lightboxCounter}>
          {currentIndex + 1} / {images.length}
        </div>
      )}
    </div>,
    document.body,
  );
}

interface IssueDetailModalProps {
  issueId: number;
  onClose: () => void;
  onIssueChanged: () => void;
}

export function IssueDetailModal({
  issueId,
  onClose,
  onIssueChanged,
}: IssueDetailModalProps) {
  const [issue, setIssue] = useState<IssueResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lightboxIndex, setLightboxIndex] = useState<number | null>(null);
  const modalRef = useRef<HTMLDivElement>(null);

  const fetchIssue = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await issueApi.get(issueId);
      setIssue(data);
    } catch (err) {
      setError('Failed to load issue details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [issueId]);

  useEffect(() => {
    fetchIssue();
  }, [fetchIssue]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && lightboxIndex === null) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose, lightboxIndex]);

  const imageAttachments = useMemo(
    () => issue?.attachments?.filter(isImageAttachment) ?? [],
    [issue],
  );

  const handleAttachmentClick = (att: AttachmentResponse, e: React.MouseEvent) => {
    e.preventDefault();
    if (isImageAttachment(att)) {
      const idx = imageAttachments.findIndex(a => a.id === att.id);
      setLightboxIndex(idx >= 0 ? idx : 0);
    } else {
      window.open(attachmentApi.downloadUrl(att.id), '_blank', 'noopener,noreferrer');
    }
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handleStatusChange = async (target: Status) => {
    if (!issue) return;
    try {
      await issueApi.changeStatus(issue.id, target);
      await fetchIssue();
      onIssueChanged();
    } catch (err) {
      console.error('Failed to change status', err);
    }
  };

  const handleSend = async () => {
    if (!issue) return;
    try {
      await issueApi.send(issue.id);
      await fetchIssue();
      onIssueChanged();
    } catch (err) {
      console.error('Failed to send email', err);
    }
  };

  const formatDate = (dateString: string) => {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(dateString));
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getPriorityStyle = (priority: Priority) => {
    switch (priority) {
      case Priority.LOW:
        return styles.priorityLow;
      case Priority.MEDIUM:
        return styles.priorityMedium;
      case Priority.HIGH:
        return styles.priorityHigh;
      case Priority.CRITICAL:
        return styles.priorityCritical;
      default:
        return '';
    }
  };

  const getAttachmentIcon = (att: AttachmentResponse) => {
    if (isImageAttachment(att)) {
      return (
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--ds-text-secondary)' }}>
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21 15 16 10 5 21" />
        </svg>
      );
    }
    return (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--ds-text-secondary)' }}>
        <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" />
        <polyline points="13 2 13 9 20 9" />
      </svg>
    );
  };

  const renderContent = () => {
    if (loading) {
      return <div className={styles.loading}>Loading issue details...</div>;
    }

    if (error || !issue) {
      return <div className={styles.error}>{error || 'Issue not found'}</div>;
    }

    const availableTransitions = STATUS_TRANSITIONS[issue.status] || [];
    const showSendButton = issue.status === Status.PREPARED;

    return (
      <>
        <div className={styles.header}>
          <div className={styles.titleContainer}>
            <h2 className={styles.title}>{issue.title}</h2>
            <p className={styles.subtitle}>
              AT-{issue.id} · Created {formatDate(issue.createdAt)}
            </p>
          </div>
          <button
            className={styles.closeBtn}
            onClick={onClose}
            aria-label="Close modal"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className={styles.content}>
          <div className={styles.metaGrid}>
            <div className={styles.metaItem}>
              <span className={styles.label}>Status</span>
              <span className={`${styles.badge} ${styles.statusBadge}`}>
                {STATUS_LABELS[issue.status]}
              </span>
            </div>
            <div className={styles.metaItem}>
              <span className={styles.label}>Priority</span>
              <span
                className={`${styles.badge} ${getPriorityStyle(
                  issue.priority as Priority,
                )}`}
              >
                {issue.priority}
              </span>
            </div>
            <div className={styles.metaItem}>
              <span className={styles.label}>Assignee</span>
              <span className={styles.metaValue}>
                {issue.assignee || 'Unassigned'}
              </span>
            </div>
            <div className={styles.metaItem}>
              <span className={styles.label}>Updated</span>
              <span className={styles.metaValue}>
                {formatDate(issue.updatedAt)}
              </span>
            </div>
          </div>

          <div className={styles.section}>
            <span className={styles.label}>Description</span>
            <div className={styles.description}>{issue.description}</div>
            {showSendButton && (
              <button
                className={`${styles.actionBtn} ${styles.btnSend} ${styles.sendInline}`}
                onClick={handleSend}
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="22" y1="2" x2="11" y2="13" />
                  <polygon points="22 2 15 22 11 13 2 9 22 2" />
                </svg>
                Send to Administrative Company
              </button>
            )}
          </div>

          {issue.attachments && issue.attachments.length > 0 && (
            <div className={styles.section}>
              <span className={styles.label}>Attachments</span>
              <div className={styles.attachmentsList}>
                {issue.attachments.map((att) => (
                  <div key={att.id} className={styles.attachmentItem}>
                    {getAttachmentIcon(att)}
                    <a
                      href={attachmentApi.downloadUrl(att.id)}
                      onClick={(e) => handleAttachmentClick(att, e)}
                      className={styles.attachmentLink}
                    >
                      {att.filename}
                    </a>
                    <span className={styles.attachmentSize}>
                      {formatFileSize(att.size)}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className={styles.actions}>
            {availableTransitions.map((targetStatus) => {
              let btnClass = styles.actionBtn;
              if (targetStatus === Status.RESOLVED) {
                btnClass = `${styles.actionBtn} ${styles.btnResolve}`;
              } else if (targetStatus === Status.WONT_DO) {
                btnClass = `${styles.actionBtn} ${styles.btnReject}`;
              }

              return (
                <button
                  key={targetStatus}
                  className={btnClass}
                  onClick={() => handleStatusChange(targetStatus)}
                >
                  Move to {STATUS_LABELS[targetStatus]}
                </button>
              );
            })}
          </div>
        </div>
      </>
    );
  };

  return (
    <>
      {createPortal(
        <div className={styles.backdrop} onClick={handleBackdropClick}>
          <div className={styles.panel} ref={modalRef}>
            {renderContent()}
          </div>
        </div>,
        document.body,
      )}
      {lightboxIndex !== null && imageAttachments.length > 0 && (
        <Lightbox
          images={imageAttachments}
          currentIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
          onNavigate={setLightboxIndex}
        />
      )}
    </>
  );
}
