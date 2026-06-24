import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  workspaceId: string | null;
  isAuthenticated: boolean;
  userEmail: string | null;
  userRole: string | null;
}

const initialState: AuthState = {
  accessToken: typeof window !== 'undefined' ? localStorage.getItem('access_token') : null,
  refreshToken: typeof window !== 'undefined' ? localStorage.getItem('refresh_token') : null,
  workspaceId: typeof window !== 'undefined' ? localStorage.getItem('workspace_id') : null,
  isAuthenticated: typeof window !== 'undefined' ? !!localStorage.getItem('access_token') : false,
  userEmail: null,
  userRole: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials(
      state,
      action: PayloadAction<{
        accessToken: string;
        refreshToken: string;
        workspaceId: string;
        userEmail?: string;
        userRole?: string;
      }>
    ) {
      const { accessToken, refreshToken, workspaceId, userEmail, userRole } = action.payload;
      state.accessToken = accessToken;
      state.refreshToken = refreshToken;
      state.workspaceId = workspaceId;
      state.isAuthenticated = true;
      if (userEmail) state.userEmail = userEmail;
      if (userRole) state.userRole = userRole;

      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('refresh_token', refreshToken);
      localStorage.setItem('workspace_id', workspaceId);
    },
    clearCredentials(state) {
      state.accessToken = null;
      state.refreshToken = null;
      state.workspaceId = null;
      state.isAuthenticated = false;
      state.userEmail = null;
      state.userRole = null;

      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('workspace_id');
    },
    setWorkspace(state, action: PayloadAction<string>) {
      state.workspaceId = action.payload;
      localStorage.setItem('workspace_id', action.payload);
    },
  },
});

export const { setCredentials, clearCredentials, setWorkspace } = authSlice.actions;
export default authSlice.reducer;
