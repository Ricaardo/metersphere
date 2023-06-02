export interface LoginData {
  username: string;
  password: string;
  authenticate: string;
}

export interface LoginRes {
  sessionId: string;
  csrfToken: string;
}
