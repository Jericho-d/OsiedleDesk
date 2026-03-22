import {type ReactNode, useState} from 'react';
import Sidenav from '@/components/Sidenav';
import TopBar from '@/components/TopBar';
import styles from './AppShell.module.css';

interface AppShellProps {
  children: ReactNode;
  onCreateIssue?: () => void;
}

export default function AppShell({ children, onCreateIssue }: AppShellProps) {
  const [sidenavCollapsed, setSidenavCollapsed] = useState(() => {
    const saved = localStorage.getItem('sidenav-collapsed');
    return saved === 'true';
  });

  const toggleSidenav = () => {
    setSidenavCollapsed(prev => {
      const newState = !prev;
      localStorage.setItem('sidenav-collapsed', String(newState));
      return newState;
    });
  };

  return (
    <div className={styles.shell}>
      <Sidenav collapsed={sidenavCollapsed} onToggle={toggleSidenav} />
      <div className={styles.main}>
        <TopBar onCreateIssue={onCreateIssue} onToggleSidenav={toggleSidenav} />
        <div className={styles.content}>{children}</div>
      </div>
    </div>
  );
}
