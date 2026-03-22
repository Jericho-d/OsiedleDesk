import {useDroppable} from '@dnd-kit/core';
import {SortableContext, verticalListSortingStrategy} from '@dnd-kit/sortable';
import {IssueResponse, Status, STATUS_LABELS, STATUS_TRANSITIONS} from '@/types';
import {IssueCard} from './IssueCard';
import {SkeletonCard} from './SkeletonCard';
import styles from './BoardColumn.module.css';

interface BoardColumnProps {
  status: Status;
  issues: IssueResponse[];
  loading: boolean;
  onIssueClick: (id: number) => void;
  activeIssueStatus?: Status;
}

export function BoardColumn({ status, issues, loading, onIssueClick, activeIssueStatus }: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({
    id: status,
    data: { type: 'COLUMN', status },
  });

  const isPrepared = status === Status.PREPARED;
  
  // Highlight if dragging over, not prepared, and transition is valid (if active status known)
  const isValidDrop = !isPrepared && 
    (!activeIssueStatus || STATUS_TRANSITIONS[activeIssueStatus].includes(status));

  const showHighlight = isOver && isValidDrop;
  
  return (
    <div
      ref={setNodeRef}
      className={`${styles.column} ${isPrepared ? styles.prepared : ''} ${showHighlight ? styles.isOver : ''}`}
    >
      <div className={styles.header}>
        <div className={styles.title}>
          {STATUS_LABELS[status]}
          {isPrepared && (
             <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
               <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
               <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
             </svg>
          )}
        </div>
        <div className={styles.count}>{loading ? '-' : issues.length}</div>
      </div>
      
      <div className={styles.list}>
        {loading ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <SortableContext items={issues.map(i => i.id)} strategy={verticalListSortingStrategy}>
            {issues.length > 0 ? (
              issues.map((issue) => (
                <IssueCard
                  key={issue.id}
                  issue={issue}
                  onIssueClick={onIssueClick}
                />
              ))
            ) : (
              <div className={styles.empty}>
                 <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                   <circle cx="12" cy="12" r="10"></circle>
                   <line x1="8" y1="12" x2="16" y2="12"></line>
                 </svg>
                <span className={styles.emptyText}>No issues</span>
              </div>
            )}
          </SortableContext>
        )}
      </div>
    </div>
  );
}
