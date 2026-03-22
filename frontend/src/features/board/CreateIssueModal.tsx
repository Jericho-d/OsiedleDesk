import React, {useCallback, useEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import styles from './CreateIssueModal.module.css';
import type {IssueCreateRequest} from '@/types';
import {issueApi} from '@/services/api';

interface CreateIssueModalProps {
  onClose: () => void;
  onIssueCreated: () => void;
}

const TITLE_MAX = 200;
const DESCRIPTION_MAX = 2000;
const TITLE_WARN_THRESHOLD = TITLE_MAX * 0.9;
const DESCRIPTION_WARN_THRESHOLD = DESCRIPTION_MAX * 0.9;

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function charCountClass(current: number, max: number, warnThreshold: number): string {
  if (current >= max) return `${styles.charCount} ${styles.charCountError ?? ''}`;
  if (current >= warnThreshold) return `${styles.charCount} ${styles.charCountWarn ?? ''}`;
  return styles.charCount ?? '';
}

export function CreateIssueModal({ onClose, onIssueCreated }: CreateIssueModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [files, setFiles] = useState<File[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dragover, setDragover] = useState(false);

  const titleInputRef = useRef<HTMLInputElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    titleInputRef.current?.focus();
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value.length <= TITLE_MAX) setTitle(value);
  };

  const handleDescriptionChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    if (value.length <= DESCRIPTION_MAX) setDescription(value);
  };

  const addFiles = useCallback((incoming: FileList | File[]) => {
    const newFiles = Array.from(incoming);
    setFiles((prev) => [...prev, ...newFiles]);
  }, []);

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      addFiles(e.target.files);
      e.target.value = '';
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragover(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragover(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragover(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      addFiles(e.dataTransfer.files);
    }
  };

  const handleUploadZoneClick = () => {
    fileInputRef.current?.click();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    setSubmitting(true);
    setError(null);

    try {
      const data: IssueCreateRequest = {
        title: title.trim(),
        description: description.trim(),
      };
      await issueApi.create(data, files.length > 0 ? files : undefined);
      onIssueCreated();
      onClose();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create issue';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const uploadZoneClass = dragover
    ? `${styles.fileUpload} ${styles.fileUploadDragover}`
    : styles.fileUpload;

  return createPortal(
    <div className={styles.backdrop} onClick={handleBackdropClick}>
      <div className={styles.panel}>
        <div className={styles.header}>
          <h2 className={styles.title}>Create Issue</h2>
          <button
            className={styles.closeBtn}
            onClick={onClose}
            aria-label="Close modal"
            type="button"
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

        <form className={styles.formCard} onSubmit={handleSubmit}>
          {error && <div className={styles.alert}>{error}</div>}

          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="create-issue-title">
              Title<span className={styles.required}>*</span>
            </label>
            <input
              ref={titleInputRef}
              id="create-issue-title"
              className={styles.input}
              type="text"
              value={title}
              onChange={handleTitleChange}
              placeholder="Enter issue title"
              maxLength={TITLE_MAX}
              required
            />
            <span className={charCountClass(title.length, TITLE_MAX, TITLE_WARN_THRESHOLD)}>
              {title.length} / {TITLE_MAX}
            </span>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="create-issue-description">
              Description
            </label>
            <textarea
              id="create-issue-description"
              className={styles.input}
              value={description}
              onChange={handleDescriptionChange}
              placeholder="Describe the issue in detail"
              maxLength={DESCRIPTION_MAX}
              rows={6}
            />
            <span className={charCountClass(description.length, DESCRIPTION_MAX, DESCRIPTION_WARN_THRESHOLD)}>
              {description.length} / {DESCRIPTION_MAX}
            </span>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Attachments</label>
            <div
              className={uploadZoneClass}
              onClick={handleUploadZoneClick}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') handleUploadZoneClick();
              }}
            >
              <svg
                className={styles.fileUploadIcon}
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <span className={styles.fileUploadText}>Drop files here or click to browse</span>
              <span className={styles.fileUploadHint}>Attach files to this issue</span>
            </div>
            <input
              ref={fileInputRef}
              className={styles.fileInput}
              type="file"
              multiple
              onChange={handleFileInputChange}
              tabIndex={-1}
            />

            {files.length > 0 && (
              <div className={styles.fileList}>
                {files.map((file, index) => (
                  <div key={`${file.name}-${file.size}-${index}`} className={styles.fileItem}>
                    <span className={styles.fileItemName}>{file.name}</span>
                    <span className={styles.fileItemSize}>{formatFileSize(file.size)}</span>
                    <button
                      type="button"
                      className={styles.fileItemRemove}
                      onClick={() => removeFile(index)}
                      aria-label={`Remove ${file.name}`}
                    >
                      <svg
                        width="16"
                        height="16"
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
                ))}
              </div>
            )}
          </div>

          <div className={styles.actions}>
            <button
              type="button"
              className={styles.btnSecondary}
              onClick={onClose}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={styles.btnPrimary}
              disabled={submitting || !title.trim()}
            >
              {submitting ? 'Creating...' : 'Create Issue'}
            </button>
          </div>
        </form>
      </div>
    </div>,
    document.body,
  );
}
