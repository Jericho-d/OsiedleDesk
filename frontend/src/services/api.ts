import type {IssueCreateRequest, IssueResponse, LoginRequest, Status, UserResponse,} from '@/types';

class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options?.headers ?? {}),
    },
  });

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = (await response.json()) as { error?: string; message?: string };
      message = body.error ?? body.message ?? message;
    } catch {
      /* empty */
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

function jsonHeaders(): HeadersInit {
  return { 'Content-Type': 'application/json' };
}

export const authApi = {
  login(data: LoginRequest): Promise<UserResponse> {
    return request<UserResponse>('/api/auth/login', {
      method: 'POST',
      headers: jsonHeaders(),
      body: JSON.stringify(data),
    });
  },

  logout(): Promise<void> {
    return request<void>('/api/auth/logout', { method: 'POST' });
  },

  me(): Promise<UserResponse> {
    return request<UserResponse>('/api/auth/me');
  },
};

export const issueApi = {
  list(): Promise<IssueResponse[]> {
    return request<IssueResponse[]>('/api/issues');
  },

  get(id: number): Promise<IssueResponse> {
    return request<IssueResponse>(`/api/issues/${id}`);
  },

  create(data: IssueCreateRequest, files?: File[]): Promise<IssueResponse> {
    if (files && files.length > 0) {
      const formData = new FormData();
      formData.append(
        'issue',
        new Blob([JSON.stringify(data)], { type: 'application/json' }),
      );
      for (const file of files) {
        formData.append('files', file);
      }
      return request<IssueResponse>('/api/issues', {
        method: 'POST',
        body: formData,
      });
    }

    return request<IssueResponse>('/api/issues', {
      method: 'POST',
      headers: jsonHeaders(),
      body: JSON.stringify(data),
    });
  },

  delete(id: number): Promise<void> {
    return request<void>(`/api/issues/${id}`, { method: 'DELETE' });
  },

  send(id: number): Promise<{ message: string; issue: IssueResponse }> {
    return request<{ message: string; issue: IssueResponse }>(
      `/api/issues/${id}/send`,
      { method: 'POST' },
    );
  },

  changeStatus(
    id: number,
    target: Status,
  ): Promise<{ message: string; issue: IssueResponse }> {
    return request<{ message: string; issue: IssueResponse }>(
      `/api/issues/${id}/status?target=${target}`,
      { method: 'POST' },
    );
  },
};

export const attachmentApi = {
  downloadUrl(id: number): string {
    return `/api/attachments/${id}/download`;
  },
};

export { ApiError };
