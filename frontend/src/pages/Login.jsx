import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { homeFor, useAuth } from '../auth.jsx';
import { errMsg } from '../api.js';
import { Err, Field } from '../components.jsx';

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const u = await login(email.trim(), password);
      nav(homeFor(u.role));
    } catch (err) {
      setError(errMsg(err, 'Login failed'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="app">
      <main className="main auth-wrap">
        <div className="auth-card">
          <h1>🎓 Future Pathway</h1>
          <p className="muted">Campus placements & career readiness — sign in to continue.</p>
          <form className="form" onSubmit={submit}>
            <Field label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            <Field
              label="Password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Err error={error} />
            <button className="btn" disabled={busy} type="submit">
              {busy ? 'Signing in…' : 'Login'}
            </button>
          </form>
          <p className="muted">
            New here? <Link to="/register">Create a student or company account</Link>
          </p>
          <p className="muted">
            Seeded portal admin: <code>admin@futurepathway.local</code> / <code>Admin@123</code>
          </p>
        </div>
      </main>
    </div>
  );
}
