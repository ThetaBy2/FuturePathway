import { useState } from 'react';
import { api, contentOf, emptyToUndef, errMsg } from '../api.js';
import {
  Badge, Card, Empty, Err, Field, Layout, Pager, Sel, Tabs,
} from '../components.jsx';
import { InterviewsPanel, StatsPanel, VerifyPanel, useLoad } from '../panels.jsx';

const TABS = [
  { id: 'dashboard', label: '📊 Dashboard' },
  { id: 'students', label: '🎓 Students' },
  { id: 'eligible', label: '✅ Eligible' },
  { id: 'jobs', label: '💼 Jobs' },
  { id: 'interviews', label: '🗓 Interviews' },
  { id: 'verify', label: '🔍 Verify' },
  { id: 'profile', label: '🏫 Profile' },
];

export default function College() {
  const [tab, setTab] = useState('dashboard');
  const { data } = useLoad(async () => (await api.get('/jobs', { params: { size: 100 } })).data, []);
  const allJobs = contentOf(data);
  return (
    <Layout title="College dashboard (TPO)" subtitle="Students, eligibility, interviews, and placements at a glance.">
      <Tabs tabs={TABS} active={tab} onChange={setTab} />
      {tab === 'dashboard' && <StatsPanel url="/college/dashboard" />}
      {tab === 'students' && <StudentsTab />}
      {tab === 'eligible' && <EligibleTab jobs={allJobs} />}
      {tab === 'jobs' && <JobsTab jobs={allJobs} />}
      {tab === 'interviews' && <InterviewsPanel jobs={allJobs} />}
      {tab === 'verify' && <VerifyPanel />}
      {tab === 'profile' && <ProfileTab />}
    </Layout>
  );
}

function StudentsTab() {
  const [branch, setBranch] = useState('');
  const [page, setPage] = useState(null);
  const [error, setError] = useState('');

  const load = async (p = 0) => {
    setError('');
    try {
      const { data } = await api.get('/college/students', { params: { branch: emptyToUndef(branch), page: p, size: 15 } });
      setPage(data);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <Card title="Students">
      <Err error={error} />
      <div className="skill-pick">
        <Field label="Branch filter" value={branch} onChange={(e) => setBranch(e.target.value)} placeholder="CSE" />
        <button className="btn" onClick={() => load(0)} type="button">Load</button>
      </div>
      {page && (
        <>
          {contentOf(page).length === 0 ? <Empty /> : (
            <div className="table-wrap"><table>
              <thead><tr><th>Name</th><th>Roll no</th><th>Branch</th><th>Batch</th><th>CGPA</th><th>Backlogs</th></tr></thead>
              <tbody>
                {contentOf(page).map((s) => (
                  <tr key={s.id}>
                    <td><b>{s.name}</b><br /><span className="muted">{s.email}</span></td>
                    <td>{s.rollNo}</td>
                    <td>{s.branch}</td>
                    <td>{s.batchYear}</td>
                    <td>{s.cgpa ?? '—'}</td>
                    <td>{s.backlogCount}</td>
                  </tr>
                ))}
              </tbody>
            </table></div>
          )}
          <Pager page={page} onPage={load} />
        </>
      )}
    </Card>
  );
}

function EligibleTab({ jobs }) {
  const [jobId, setJobId] = useState('');
  const [page, setPage] = useState(null);
  const [error, setError] = useState('');

  const load = async (jid, p = 0) => {
    if (!jid) return;
    setError('');
    try {
      const { data } = await api.get(`/college/jobs/${jid}/eligible`, { params: { page: p, size: 15 } });
      setPage(data);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <Card title="Auto-filter eligible students for a job">
      <Err error={error} />
      <Sel
        label="Job (job criteria + interview selection criteria are merged)"
        value={jobId}
        onChange={(e) => { setJobId(e.target.value); load(e.target.value, 0); }}
        allowEmpty
        emptyLabel="— select a job —"
        options={jobs.map((j) => ({ value: j.id, label: `#${j.id} ${j.title} (${j.companyName})` }))}
      />
      {page && (
        <>
          <p className="muted">{page.totalElements} eligible student(s)</p>
          {contentOf(page).length === 0 ? <Empty /> : (
            <div className="table-wrap"><table>
              <thead><tr><th>Name</th><th>Roll no</th><th>Branch</th><th>CGPA</th><th>Backlogs</th></tr></thead>
              <tbody>
                {contentOf(page).map((s) => (
                  <tr key={s.id}>
                    <td><b>{s.name}</b></td>
                    <td>{s.rollNo}</td>
                    <td>{s.branch}</td>
                    <td>{s.cgpa ?? '—'}</td>
                    <td>{s.backlogCount}</td>
                  </tr>
                ))}
              </tbody>
            </table></div>
          )}
          <Pager page={page} onPage={(p) => load(jobId, p)} />
        </>
      )}
    </Card>
  );
}

function JobsTab({ jobs }) {
  return (
    <Card title="All job postings (reference)">
      {jobs.length === 0 ? <Empty /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Job</th><th>Company</th><th>Domain</th><th>Package</th><th>Status</th></tr></thead>
          <tbody>
            {jobs.map((j) => (
              <tr key={j.id}>
                <td><b>#{j.id} {j.title}</b></td>
                <td>{j.companyName}</td>
                <td>{j.jobDomain || '—'}</td>
                <td>{j.packageLpa ?? '—'}</td>
                <td><Badge value={j.active ? 'ACTIVE' : 'INACTIVE'} text={j.active ? 'Open' : 'Closed'} /></td>
              </tr>
            ))}
          </tbody>
        </table></div>
      )}
    </Card>
  );
}

function ProfileTab() {
  const { data: me, error, reload } = useLoad(async () => (await api.get('/college/me')).data, []);
  const [form, setForm] = useState(null);
  const [msg, setMsg] = useState('');

  if (error) return <Card title="College profile"><Err error={error} /></Card>;
  if (!me) return <Card title="College profile"><p className="muted">Loading…</p></Card>;
  const f = form ?? { name: me.name, address: me.address ?? '', tpoContactEmail: me.tpoContactEmail ?? '' };

  const save = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      await api.put('/college/me', f);
      setForm(null);
      setMsg('Profile saved.');
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  return (
    <Card title="College profile">
      {msg && <div className={msg === 'Profile saved.' ? 'success' : 'error'}>{msg}</div>}
      <form className="form" onSubmit={save}>
        <Field label="Name *" required value={f.name} onChange={(e) => setForm({ ...f, name: e.target.value })} />
        <Field label="Address" value={f.address} onChange={(e) => setForm({ ...f, address: e.target.value })} />
        <Field label="TPO contact email" type="email" value={f.tpoContactEmail} onChange={(e) => setForm({ ...f, tpoContactEmail: e.target.value })} />
        <div><button className="btn" type="submit">Save</button></div>
      </form>
    </Card>
  );
}
