export enum Status {
  PREPARED = 'PREPARED',
  IN_PROGRESS = 'IN_PROGRESS',
  ACKNOWLEDGED = 'ACKNOWLEDGED',
  RESOLVED = 'RESOLVED',
  WONT_DO = 'WONT_DO',
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

export interface AttachmentResponse {
  id: number;
  issueId: number;
  filename: string;
  contentType: string;
  size: number;
}

export interface IssueResponse {
  id: number;
  title: string;
  description: string;
  status: Status;
  priority: Priority;
  assignee: string;
  sent: boolean;
  creatorId: number;
  createdAt: string;
  updatedAt: string;
  attachments: AttachmentResponse[];
}

export interface IssueCreateRequest {
  title: string;
  description: string;
}

export interface UserResponse {
  id: number;
  username: string;
  role: string;
  createdAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface ApiError {
  message: string;
  status: number;
}

export const STATUS_LABELS: Record<Status, string> = {
  [Status.PREPARED]: 'Prepared',
  [Status.IN_PROGRESS]: 'In Progress',
  [Status.ACKNOWLEDGED]: 'Acknowledged',
  [Status.RESOLVED]: 'Resolved',
  [Status.WONT_DO]: "Won't Do",
};

export const BOARD_COLUMNS: Status[] = [
  Status.PREPARED,
  Status.IN_PROGRESS,
  Status.ACKNOWLEDGED,
  Status.RESOLVED,
  Status.WONT_DO,
];

export const STATUS_TRANSITIONS: Record<Status, Status[]> = {
  [Status.PREPARED]: [],
  [Status.IN_PROGRESS]: [Status.ACKNOWLEDGED, Status.RESOLVED, Status.WONT_DO],
  [Status.ACKNOWLEDGED]: [Status.IN_PROGRESS, Status.RESOLVED, Status.WONT_DO],
  [Status.RESOLVED]: [Status.IN_PROGRESS, Status.ACKNOWLEDGED, Status.WONT_DO],
  [Status.WONT_DO]: [Status.IN_PROGRESS, Status.ACKNOWLEDGED, Status.RESOLVED],
};
