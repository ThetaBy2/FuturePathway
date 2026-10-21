import { useState } from 'react';
import { api, contentOf, emptyToUndef, errMsg, fmtDate, numOrUndef } from '../api.js';
import {
  Area, Badge, Card, DelButton, Empty, Err, Field, Layout, Pager, Sel, Tabs,
} from '../components.jsx';
import { InterviewsPanel, VerifyPanel, useCatalog, useLoad } from '../panels.jsx';

const TABS = [
  { id: 'jobs', label: '💼 My jobs' },
  { id: 'apps', label: '📥 Applications' },
  { id: 'interviews', label: '🗓 Interviews' },
  { id: 'verify', label: '✅ Verify' },
];

const APP_STATUSES = ['APPLIED', 'SHORTLISTED', 'INTERVIEWED', 'SELECTED', 'REJECTED'];

export default function Company() {
  const [tab, setTab] = useState('jobs');
  const { data } = useLoad(async () => (await api.get('/jobs/my')).data, []);
  const myJobs = data ?? [];
  return (
    <Layout title="Company dashboard" subtitle="Post jobs, review candidates, and run interviews.">
      <Tabs tabs={TABS} active={tab} onChange={setTab} />
      {tab === 'jobs' && <JobsTab />}
      {tab === 'apps' && <AppsTab jobs={myJobs} />}
      {tab === 'interviews' && <InterviewsPanel jobs={myJobs} />}
      {tab === 'verify' && <VerifyPanel />}
    </Layout>
  );
}

/* ── jobs CRUD ─────────────────────────────────────────────────────── */
const blankJob = {
  title: '', description: '', jobDomain: '', minCgpa: '', maxBacklogs: '',
  eligibleBranches: '', packageLpa: '', deadline: '', active: true,
};

function JobsTab() {
  const catalog = useCatalog();
  const { data, error, reload } = useLoad(async () => (await api.get('/jobs/my')).data, []);
  const [form, setForm] = useState(blankJob);
  const [skills, setSkills] = useState([]);
  const [pickSkill, setPickSkill] = useState('');
  const [pickLevel, setPickLevel] = useState(3);
  const [editingId, setEditingId] = useState(null);
  const [msg, setMsg] = useState('');

  const jobs = data ?? [];

  const addSkill = () => {
    if (!pickSkill) return;
    const id = Number(pickSkill);
    if (skills.some((s) => s.skillId === id)) return;
    setSkills([...skills, { skillId: id, requiredProficiency: Number(pickLevel) }]);
    setPickSkill('');
  };

  const startEdit = (j) => {
    setEditingId(j.id);
    setForm({
      title: j.title, description: j.description ?? '', jobDomain: j.jobDomain ?? '',
      minCgpa: j.minCgpa ?? '', maxBacklogs: j.maxBacklogs ?? '',
      eligibleBranches: (j.eligibleBranches ?? []).join(', '),
      packageLpa: j.packageLpa ?? '', deadline: j.deadline ?? '', active: j.active,
    });
    setSkills((j.skills ?? []).map((s) => ({ skillId: s.skillId, requiredProficiency: s.requiredProficiency })));
    setMsg('');
  };

  const reset = () => {
    setEditingId(null);
    setForm(blankJob);
    setSkills([]);
  };

  const submit = async (e) => {
    e.preventDefault();
    setMsg('');
    const body = {
      title: form.title,
      description: emptyToUndef(form.description),
      jobDomain: emptyToUndef(form.jobDomain),
      minCgpa: numOrUndef(form.minCgpa),
      maxBacklogs: form.maxBacklogs === '' ? undefined : Number(form.maxBacklogs),
      eligibleBranches: form.eligibleBranches.split(',').map((s) => s.trim()).filter(Boolean),
      packageLpa: numOrUndef(form.packageLpa),
      deadline: emptyToUndef(form.deadline) || null,
      active: form.active,
      skills,
    };
    try {
      if (editingId) await api.put(`/jobs/${editingId}`, body);
      else await api.post('/jobs', body);
      reset();
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  const remove = async (id) => {
    try {
      await api.delete(`/jobs/${id}`);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  const skillName = (id) => catalog.find((c) => c.id === id)?.name ?? `#${id}`;

  return (
    <>
      <Card title={editingId ? `Edit job #${editingId}` : 'Post a job'}>
        <Err error={msg} />
        <form className="form" onSubmit={submit}>
          <Field label="Title *" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <Area label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <div className="form-row">
            <Field label="Domain" value={form.jobDomain} onChange={(e) => setForm({ ...form, jobDomain: e.target.value })} placeholder="Engineering" />
            <Field label="Min CGPA" type="number" step="0.01" value={form.minCgpa} onChange={(e) => setForm({ ...form, minCgpa: e.target.value })} />
            <Field label="Max backlogs" type="number" value={form.maxBacklogs} onChange={(e) => setForm({ ...form, maxBacklogs: e.target.value })} />
          </div>
          <div className="form-row">
            <Field label="Eligible branches (csv, blank = all)" value={form.eligibleBranches} onChange={(e) => setForm({ ...form, eligibleBranches: e.target.value })} placeholder="CSE, IT" />
            <Field label="Package (LPA)" type="number" step="0.5" value={form.packageLpa} onChange={(e) => setForm({ ...form, packageLpa: e.target.value })} />
            <Field label="Deadline" type="date" value={form.deadline} onChange={(e) => setForm({ ...form, deadline: e.target.value })} />
          </div>
          <label className="field">
            <span><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> Accepting applications</span>
          </label>
          <div>
            <b>Required skills</b>
            {skills.length === 0 ? <p className="muted">None yet.</p> : (
              <div>{skills.map((s) => (
                <span className="tag" key={s.skillId}>
                  {skillName(s.skillId)} ≥{s.requiredProficiency}
                  {' '}<button className="link" onClick={() => setSkills(skills.filter((x) => x.skillId !== s.skillId))} type="button">✕</button>
                </span>
              ))}</div>
            )}
            <div className="skill-pick">
              <Sel label="Skill" value={pickSkill} onChange={(e) => setPickSkill(e.target.value)} allowEmpty emptyLabel="— choose —" options={catalog.map((c) => ({ value: c.id, label: c.name }))} />
              <Sel label="Min level" value={pickLevel} onChange={(e) => setPickLevel(e.target.value)} options={[1, 2, 3, 4, 5]} />
              <button className="btn ghost" onClick={addSkill} type="button">Add</button>
            </div>
          </div>
          <div className="btn-row">
            <button className="btn" type="submit">{editingId ? 'Save' : 'Post job'}</button>
            {editingId && <button className="btn ghost" onClick={reset} type="button">Cancel</button>}
          </div>
        </form>
      </Card>
      <Card title="My postings">
        <Err error={error} />
        {jobs.length === 0 ? <Empty text="No jobs posted yet." /> : (
          <div className="table-wrap"><table>
            <thead><tr><th>Title</th><th>Domain</th><th>Package</th><th>Deadline</th><th>Status</th><th /></tr></thead>
            <tbody>
              {jobs.map((j) => (
                <tr key={j.id}>
                  <td><b>#{j.id} {j.title}</b><br /><span className="muted">{(j.skills ?? []).map((s) => s.skillName).join(', ')}</span></td>
                  <td>{j.jobDomain || '—'}</td>
                  <td>{j.packageLpa ?? '—'}</td>
                  <td>{j.deadline || '—'}</td>
                  <td><Badge value={j.active ? 'ACTIVE' : 'INACTIVE'} text={j.active ? 'Open' : 'Closed'} /></td>
                  <td>
                    <span className="btn-row">
                      <button className="btn ghost sm" onClick={() => startEdit(j)} type="button">Edit</button>
                      <DelButton onDelete={() => remove(j.id)} />
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table></div>
        )}
      </Card>
    </>
  );
}

/* ── applications ──────────────────────────────────────────────────── */
function AppsTab({ jobs }) {
  const [jobId, setJobId] = useState('');
  const [page, setPage] = useState(null);
  const [pageNum, setPageNum] = useState(0);
  const [error, setError] = useState('');
  const [fits, setFits] = useState({});

  const load = async (jid, p = 0) => {
    if (!jid) return;
    setError('');
    try {
      const { data } = await api.get(`/jobs/${jid}/applications`, { params: { page: p, size: 10 } });
      setPage(data);
      setPageNum(p);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const changeStatus = async (appId, status) => {
    setError('');
    try {
      await api.put(`/applications/${appId}/status`, { status });
      load(jobId, pageNum);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const checkFit = async (a) => {
    setError('');
    try {
      const { data } = await api.get(`/ai/students/${a.studentId}/match/${a.jobId}`);
      setFits((f) => ({ ...f, [a.id]: data }));
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <Card title="Applications">
      <Err error={error} />
      {jobs.length === 0 ? <Empty text="Post a job first." /> : (
        <>
          <Sel label="Job" value={jobId} onChange={(e) => { setJobId(e.target.value); setFits({}); load(e.target.value, 0); }} allowEmpty emptyLabel="— select a job —" options={jobs.map((j) => ({ value: j.id, label: `#${j.id} ${j.title}` }))} />
          {page && (
            <>
              {contentOf(page).length === 0 ? <Empty text="No applications yet." /> : (
                <div className="table-wrap"><table>
                  <thead><tr><th>Candidate</th><th>Branch / CGPA</th><th>Match</th><th>Status</th><th /></tr></thead>
                  <tbody>
                    {contentOf(page).map((a) => (
                      <tr key={a.id}>
                        <td><b>{a.studentName}</b><br /><span className="muted">{a.rollNo}</span></td>
                        <td>{a.branch} · {a.cgpa ?? '—'}</td>
                        <td>
                          {a.matchScore != null ? <b>{a.matchScore}%</b> : '—'}
                          {fits[a.id] && (
                            <div className="muted">
                              Fresh AI fit: <b>{fits[a.id].matchScore}%</b> <Badge value={fits[a.id].source} />
                              <br />{fits[a.id].reasoning}
                              {(fits[a.id].missingSkills ?? []).length > 0 && (
                                <><br />Gaps: {fits[a.id].missingSkills.join('; ')}</>
                              )}
                            </div>
                          )}
                        </td>
                        <td>
                          <select value={a.status} onChange={(e) => changeStatus(a.id, e.target.value)}>
                            {APP_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
                          </select>
                        </td>
                        <td>
                          <button className="btn ghost sm" onClick={() => checkFit(a)} type="button">AI fit</button>
                          <div className="muted">applied {fmtDate(a.appliedAt)}</div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table></div>
              )}
              <Pager page={page} onPage={(p) => load(jobId, p)} />
            </>
          )}
        </>
      )}
    </Card>
  );
}
