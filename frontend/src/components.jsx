import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { homeFor, roleLabel, useAuth } from './auth.jsx';

export function RequireAuth({ roles, children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to={homeFor(user.role)} replace />;
  return children;
}

export function Layout({ title, subtitle, children }) {
  const { user, logout } = useAuth();
  const nav = useNavigate();
  return (
    <div className="app">
      <header className="topbar">
        <button className="brand" onClick={() => nav(user ? homeFor(user.role) : '/login')}>
          <span className="brand-mark">FP</span> Future Pathway
        </button>
        {user && (
          <div className="userbox">
            <span className="userchip">
              {user.name} · {roleLabel(user.role)}
            </span>
            <button
              className="btn ghost sm"
              onClick={() => {
                logout();
                nav('/login');
              }}
            >
              Logout
            </button>
          </div>
        )}
      </header>
      <main className="main">
        {title && <h1>{title}</h1>}
        {subtitle && <p className="muted">{subtitle}</p>}
        {children}
      </main>
      <footer className="footer muted">Future Pathway · Spring Boot + PostgreSQL + Gemini</footer>
    </div>
  );
}

export function Tabs({ tabs, active, onChange }) {
  return (
    <div className="tabs">
      {tabs.map((t) => (
        <button
          key={t.id}
          className={active === t.id ? 'tab active' : 'tab'}
          onClick={() => onChange(t.id)}
          type="button"
        >
          {t.label}
        </button>
      ))}
    </div>
  );
}

export function Card({ title, actions, children, className = '' }) {
  return (
    <section className={`card ${className}`}>
      {(title || actions) && (
        <div className="card-head">
          {title && <h2>{title}</h2>}
          {actions && <div className="card-actions">{actions}</div>}
        </div>
      )}
      {children}
    </section>
  );
}

export function Field({ label, ...props }) {
  return (
    <label className="field">
      {label && <span>{label}</span>}
      <input {...props} />
    </label>
  );
}

export function Area({ label, ...props }) {
  return (
    <label className="field">
      {label && <span>{label}</span>}
      <textarea {...props} />
    </label>
  );
}

export function Sel({ label, options, allowEmpty = false, emptyLabel = '— any —', ...props }) {
  return (
    <label className="field">
      {label && <span>{label}</span>}
      <select {...props}>
        {allowEmpty && <option value="">{emptyLabel}</option>}
        {options.map((o) =>
          typeof o === 'string' ? (
            <option key={o} value={o}>
              {o}
            </option>
          ) : (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ),
        )}
      </select>
    </label>
  );
}

const badgeClass = (v = '') => {
  const s = String(v).toUpperCase();
  if (['SELECTED', 'VERIFIED', 'ACTIVE', 'COMPLETED'].includes(s)) return 'badge green';
  if (['SHORTLISTED', 'INTERVIEWED', 'SCHEDULED', 'PENDING'].includes(s)) return 'badge amber';
  if (['REJECTED', 'CANCELLED', 'INACTIVE'].includes(s)) return 'badge red';
  if (['APPLIED', 'GEMINI', 'TECHNICAL'].includes(s)) return 'badge blue';
  if (['HEURISTIC-FALLBACK', 'SELF_DECLARED', 'SOFT'].includes(s)) return 'badge gray';
  return 'badge gray';
};

export function Badge({ value, text }) {
  return <span className={badgeClass(value)}>{text ?? String(value).replace(/_/g, ' ')}</span>;
}

export function Err({ error }) {
  if (!error) return null;
  return <div className="error">{error}</div>;
}

export function Empty({ text = 'Nothing here yet.' }) {
  return <p className="muted">{text}</p>;
}

export function Pager({ page, onPage }) {
  if (!page || (page.totalPages ?? 1) <= 1) return null;
  return (
    <div className="pager">
      <button
        className="btn ghost sm"
        disabled={page.first}
        onClick={() => onPage(page.number - 1)}
        type="button"
      >
        ← Prev
      </button>
      <span className="muted">
        Page {page.number + 1} of {page.totalPages} ({page.totalElements} total)
      </span>
      <button
        className="btn ghost sm"
        disabled={page.last}
        onClick={() => onPage(page.number + 1)}
        type="button"
      >
        Next →
      </button>
    </div>
  );
}

export function DelButton({ onDelete, title = 'Delete' }) {
  const [armed, setArmed] = useState(false);
  if (!armed) {
    return (
      <button className="btn danger-ghost sm" onClick={() => setArmed(true)} type="button">
        {title}
      </button>
    );
  }
  return (
    <span className="del-confirm">
      <button className="btn danger sm" onClick={onDelete} type="button">
        Confirm
      </button>
      <button className="btn ghost sm" onClick={() => setArmed(false)} type="button">
        Cancel
      </button>
    </span>
  );
}
