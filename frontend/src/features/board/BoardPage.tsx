import {useCallback, useEffect, useMemo, useState} from 'react';
import {
    defaultDropAnimationSideEffects,
    DndContext,
    DragEndEvent,
    DragOverlay,
    DragStartEvent,
    DropAnimation,
    PointerSensor,
    useSensor,
    useSensors,
} from '@dnd-kit/core';

import {BOARD_COLUMNS, IssueResponse, Status, STATUS_TRANSITIONS} from '@/types';
import {issueApi} from '@/services/api';
import {BoardColumn} from './BoardColumn';
import {IssueCard} from './IssueCard';
import styles from './BoardPage.module.css';

interface BoardPageProps {
  onIssueClick?: (id: number) => void;
  refreshKey?: number;
}

const dropAnimation: DropAnimation = {
  sideEffects: defaultDropAnimationSideEffects({
    styles: {
      active: {
        opacity: '0.5',
      },
    },
  }),
};

export default function BoardPage({ onIssueClick, refreshKey }: BoardPageProps) {
  const [issues, setIssues] = useState<IssueResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeId, setActiveId] = useState<number | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  const fetchIssues = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await issueApi.list();
      setIssues(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load issues');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchIssues();
  }, [fetchIssues, refreshKey]);

  const columns = useMemo(() => {
    const cols: Record<Status, IssueResponse[]> = {
      [Status.PREPARED]: [],
      [Status.IN_PROGRESS]: [],
      [Status.ACKNOWLEDGED]: [],
      [Status.RESOLVED]: [],
      [Status.WONT_DO]: [],
    };
    
    BOARD_COLUMNS.forEach(status => {
        if (!cols[status]) cols[status] = [];
    });

    issues.forEach((issue) => {
      if (cols[issue.status]) {
        cols[issue.status].push(issue);
      }
    });
    return cols;
  }, [issues]);

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as number);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveId(null);

    if (!over) return;

    const activeId = active.id as number;
    const overId = over.id;
    
    const issue = issues.find((i) => i.id === activeId);
    if (!issue) return;

    let targetStatus: Status | undefined;
    
    if (Object.values(Status).includes(overId as Status)) {
        targetStatus = overId as Status;
    } else {
        const overIssue = issues.find(i => i.id === overId);
        if (overIssue) {
            targetStatus = overIssue.status;
        }
    }

    if (!targetStatus || targetStatus === issue.status) {
        return;
    }

    if (targetStatus === Status.PREPARED) {
        return; 
    }

    const allowed = STATUS_TRANSITIONS[issue.status];
    if (!allowed.includes(targetStatus)) {
        return;
    }

    const previousIssues = [...issues];
    const newIssues = issues.map(i => 
        i.id === activeId ? { ...i, status: targetStatus! } : i
    );
    
    setIssues(newIssues);

    try {
        await issueApi.changeStatus(activeId, targetStatus);
    } catch (err) {
        console.error("Failed to update status", err);
        setIssues(previousIssues);
    }
  };

  const activeIssue = useMemo(
    () => (activeId ? issues.find((i) => i.id === activeId) : null),
    [activeId, issues]
  );

  if (error) {
    return (
      <div className={styles.error}>
        <p>Error: {error}</p>
        <button className={styles.retryButton} onClick={fetchIssues}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <DndContext
      sensors={sensors}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className={styles.board}>
        {BOARD_COLUMNS.map((status) => (
          <BoardColumn
            key={status}
            status={status}
            issues={columns[status]}
            loading={loading}
            onIssueClick={onIssueClick || (() => {})}
            activeIssueStatus={activeIssue?.status}
          />
        ))}
      </div>
      <DragOverlay dropAnimation={dropAnimation}>
        {activeIssue ? (
           <IssueCard issue={activeIssue} onIssueClick={() => {}} />
        ) : null}
      </DragOverlay>
    </DndContext>
  );
}
