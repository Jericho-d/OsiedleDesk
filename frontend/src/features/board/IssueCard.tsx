import React, {useEffect, useState} from 'react';
import {useSortable} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';
import {IssueResponse, Priority, Status} from '@/types';
import {issueApi} from '@/services/api';
import {useAuth} from '@/hooks/useAuth';
import styles from './IssueCard.module.css';

interface IssueCardProps {
  issue: IssueResponse;
  onIssueClick: (id: number) => void;
}

export function IssueCard({ issue, onIssueClick }: IssueCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: issue.id });

  const { isAdmin } = useAuth();
  const [isSending, setIsSending] = useState(false);
  const [isSent, setIsSent] = useState(issue.sent);

  useEffect(() => {
    setIsSent(issue.sent);
  }, [issue.sent]);

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const priorityClassMap: Record<Priority, string | undefined> = {
    [Priority.LOW]: styles.priorityLow,
    [Priority.MEDIUM]: styles.priorityMedium,
    [Priority.HIGH]: styles.priorityHigh,
    [Priority.CRITICAL]: styles.priorityCritical,
  };
  const priorityClass = priorityClassMap[issue.priority] ?? '';

  const handleSend = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isSending || isSent) return;

    setIsSending(true);
    try {
      await issueApi.send(issue.id);
      setIsSent(true);
    } catch (error) {
      console.error('Failed to send issue', error);
    } finally {
      setIsSending(false);
    }
  };

  const showSendButton =
    isAdmin &&
    issue.status === Status.PREPARED &&
    !isSent;

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`${styles.card} ${priorityClass} ${isDragging ? styles.dragging : ''}`}
      onClick={() => onIssueClick(issue.id)}
      {...attributes}
      {...listeners}
    >
      <div className={styles.title} title={issue.title}>
        {issue.title}
      </div>
      <div className={styles.footer}>
        <div className={styles.meta}>
          <span className={styles.id}>AT-{issue.id}</span>
          <span className={styles.priorityBadge}>{issue.priority}</span>
        </div>
        
        {isSent && <span className={styles.sentBadge}>Sent</span>}
        
        {!isSent && showSendButton && (
           <button
             className={styles.sendButton}
             onClick={handleSend}
             disabled={isSending}
           >
             {isSending ? '...' : 'Send'}
           </button>
        )}
      </div>
    </div>
  );
}
