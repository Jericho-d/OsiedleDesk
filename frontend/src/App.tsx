import {useCallback, useState} from 'react';
import {Navigate, Route, Routes} from 'react-router-dom';
import {useAuth} from '@/hooks/useAuth';
import {useToast} from '@/hooks/useToast';
import LoginPage from '@/features/auth/LoginPage';
import BoardPage from '@/features/board/BoardPage';
import {IssueDetailModal} from '@/features/board/IssueDetailModal';
import {CreateIssueModal} from '@/features/board/CreateIssueModal';
import AppShell from '@/components/AppShell';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

export default function App() {
  const { user, loading } = useAuth();
  const { showToast } = useToast();

  const [selectedIssueId, setSelectedIssueId] = useState<number | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const refreshBoard = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  const handleIssueClick = useCallback((id: number) => {
    setSelectedIssueId(id);
  }, []);

  const handleCloseDetail = useCallback(() => {
    setSelectedIssueId(null);
  }, []);

  const handleIssueChanged = useCallback(() => {
    refreshBoard();
    showToast('success', 'Issue updated');
  }, [refreshBoard, showToast]);

  const handleOpenCreate = useCallback(() => {
    setShowCreateModal(true);
  }, []);

  const handleCloseCreate = useCallback(() => {
    setShowCreateModal(false);
  }, []);

  const handleIssueCreated = useCallback(() => {
    refreshBoard();
    showToast('success', 'Issue created', 'Your new issue has been added to the board.');
  }, [refreshBoard, showToast]);

  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  return (
    <>
      <Routes>
        <Route
          path="/login"
          element={user ? <Navigate to="/" replace /> : <LoginPage />}
        />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <AppShell onCreateIssue={handleOpenCreate}>
                <Routes>
                  <Route
                    path="/"
                    element={
                      <BoardPage
                        onIssueClick={handleIssueClick}
                        refreshKey={refreshKey}
                      />
                    }
                  />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
              </AppShell>
            </ProtectedRoute>
          }
        />
      </Routes>

      {selectedIssueId !== null && (
        <IssueDetailModal
          issueId={selectedIssueId}
          onClose={handleCloseDetail}
          onIssueChanged={handleIssueChanged}
        />
      )}

      {showCreateModal && (
        <CreateIssueModal
          onClose={handleCloseCreate}
          onIssueCreated={handleIssueCreated}
        />
      )}
    </>
  );
}
