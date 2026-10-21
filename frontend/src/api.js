import axios from 'axios';

// In dev, Vite proxies /api → http://localhost:8080 (see vite.config.js).
// For a production build served elsewhere, set VITE_API_URL, e.g.
//   VITE_API_URL=http://localhost:8080/api npm run build
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 90000, // AI calls can take a while
});

export function setAuthToken(token) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
}

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401 && !window.location.pathname.startsWith('/login')) {
      localStorage.removeItem('fp-auth');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  },
);

/** Unwrap Spring Data Page<T> → content array (passes plain arrays through). */
export const contentOf = (data) => (Array.isArray(data) ? data : data?.content ?? []);

/** Human-readable message from our ErrorResponse shape. */
export function errMsg(err, fallback = 'Something went wrong') {
  const d = err?.response?.data;
  if (!d) return err?.message || fallback;
  let m = d.message || fallback;
  if (d.fieldErrors) {
    m += ': ' + Object.entries(d.fieldErrors).map(([k, v]) => `${k} ${v}`).join('; ');
  }
  return m;
}

/** Convert '' → undefined so optional fields are omitted from JSON. */
export const emptyToUndef = (v) => (v === '' || v === null ? undefined : v);
export const numOrUndef = (v) => (v === '' || v === null || v === undefined ? undefined : Number(v));

export const fmtDate = (iso) => (iso ? new Date(iso).toLocaleString() : '—');
export const toDatetimeLocal = (d) => {
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};
