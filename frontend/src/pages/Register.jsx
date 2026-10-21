import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { homeFor, useAuth } from '../auth.jsx';
import { emptyToUndef, errMsg, numOrUndef } from '../api.js';
import { Area, Err, Field, Tabs } from '../components.jsx';

const blankStudent = {
  name: '', email: '', password: '', rollNo: '', branch: '', batchYear: '', cgpa: '', cvUrl: '', resumeText: '',
};
const blankCompany = { contactName: '', email: '', password: '', companyName: '', industryDomain: '', website: '' };

export default function Register() {
  const { register } = useAuth();
  const nav = useNavigate();
  const [tab, setTab] = useState('student');
  const [stu, setStu] = useState(blankStudent);
  const [cmp, setCmp] = useState(blankCompany);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (setter) => (e) => setter((f) => ({ ...f, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      let data;
      if (tab === 'student') {
        data = await register('/auth/register/student', {
          name: stu.name, email: stu.email.trim(), password: stu.password,
          rollNo: stu.rollNo, branch: stu.branch,
          batchYear: numOrUndef(stu.batchYear), cgpa: numOrUndef(stu.cgpa),
          cvUrl: emptyToUndef(stu.cvUrl), resumeText: emptyToUndef(stu.resumeText),
        });
      } else {
        data = await register('/auth/register/company', {
          contactName: cmp.contactName, email: cmp.email.trim(), password: cmp.password,
          companyName: cmp.companyName, industryDomain: emptyToUndef(cmp.industryDomain),
          website: emptyToUndef(cmp.website),
        });
      }
      nav(homeFor(data.role));
    } catch (err) {
      setError(errMsg(err, 'Registration failed'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="app">
      <main className="main auth-wrap">
        <div className="auth-card">
          <h1>Create account</h1>
          <Tabs
            tabs={[{ id: 'student', label: '🎓 Student' }, { id: 'company', label: '🏢 Company' }]}
            active={tab}
            onChange={(t) => { setTab(t); setError(''); }}
          />
          <form className="form" onSubmit={submit}>
            {tab === 'student' ? (
              <>
                <div className="form-row">
                  <Field label="Full name *" name="name" required value={stu.name} onChange={set(setStu)} />
                  <Field label="Email *" name="email" type="email" required value={stu.email} onChange={set(setStu)} />
                </div>
                <Field
                  label="Password * (min 8 chars, letter + digit)"
                  name="password" type="password" required
                  value={stu.password} onChange={set(setStu)}
                />
                <div className="form-row">
                  <Field label="Roll no *" name="rollNo" required value={stu.rollNo} onChange={set(setStu)} />
                  <Field label="Branch * (e.g. CSE)" name="branch" required value={stu.branch} onChange={set(setStu)} />
                </div>
                <div className="form-row">
                  <Field label="Batch year *" name="batchYear" type="number" required value={stu.batchYear} onChange={set(setStu)} />
                  <Field label="CGPA (0–10)" name="cgpa" type="number" step="0.01" value={stu.cgpa} onChange={set(setStu)} />
                </div>
                <Field label="CV URL" name="cvUrl" value={stu.cvUrl} onChange={set(setStu)} />
                <Area label="CV text (used by Gemini AI analysis)" name="resumeText" value={stu.resumeText} onChange={set(setStu)} />
              </>
            ) : (
              <>
                <div className="form-row">
                  <Field label="Your name *" name="contactName" required value={cmp.contactName} onChange={set(setCmp)} />
                  <Field label="Work email *" name="email" type="email" required value={cmp.email} onChange={set(setCmp)} />
                </div>
                <Field
                  label="Password * (min 8 chars, letter + digit)"
                  name="password" type="password" required
                  value={cmp.password} onChange={set(setCmp)}
                />
                <Field label="Company name *" name="companyName" required value={cmp.companyName} onChange={set(setCmp)} />
                <div className="form-row">
                  <Field label="Industry domain" name="industryDomain" value={cmp.industryDomain} onChange={set(setCmp)} />
                  <Field label="Website" name="website" value={cmp.website} onChange={set(setCmp)} />
                </div>
              </>
            )}
            <Err error={error} />
            <button className="btn" disabled={busy} type="submit">
              {busy ? 'Creating…' : 'Create account'}
            </button>
          </form>
          <p className="muted">
            Already registered? <Link to="/login">Login</Link>
          </p>
        </div>
      </main>
    </div>
  );
}
