import { useEffect, useRef, useState } from 'react';
import { api, contentOf, emptyToUndef, errMsg, fmtDate, numOrUndef } from '../api.js';
import {
  Area, Badge, Card, DelButton, Empty, Err, Field, Layout, Pager, Sel, Tabs,
} from '../components.jsx';
import { TrendingPanel, useCatalog, useLoad } from '../panels.jsx';

const TABS = [
  { id: 'overview', label: '👤 Overview' },
  { id: 'skills', label: '🧠 Skills' },
  { id: 'projects', label: '🛠 Projects' },
  { id: 'certs', label: '📜 Certificates' },
  { id: 'jobs', label: '💼 Jobs' },
  { id: 'ai', label: '✨ AI Coach' },
  { id: 'slots', label: '🗓 Interviews' },
  { id: 'trending', label: '📈 Trending' },
];

export default function Student() {
  const [tab, setTab] = useState('overview');
  return (
    <Layout title="Student dashboard" subtitle="Manage your profile, apply to jobs, and grow with AI guidance.">
      <Tabs tabs={TABS} active={tab} onChange={setTab} />
      {tab === 'overview' && <Overview />}
      {tab === 'skills' && <SkillsTab />}
      {tab === 'projects' && <ProjectsTab />}
      {tab === 'certs' && <CertsTab />}
      {tab === 'jobs' && <JobsTab />}
      {tab === 'ai' && <AiTab />}
      {tab === 'slots' && <SlotsTab />}
      {tab === 'trending' && <TrendingPanel />}
    </Layout>
  );
}

/* ── overview ──────────────────────────────────────────────────────── */
function Overview() {
  const { data: me, error, loading, reload } = useLoad(async () => (await api.get('/students/me')).data, []);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saveError, setSaveError] = useState('');

  if (loading) return <Card><p className="muted">Loading…</p></Card>;
  if (error) return <Card><Err error={error} /></Card>;

  const startEdit = () => {
    setForm({
      name: me.name ?? '', branch: me.branch ?? '', batchYear: me.batchYear ?? '',
      cgpa: me.cgpa ?? '', backlogCount: me.backlogCount ?? 0,
      cvUrl: me.cvUrl ?? '', profilePhotoUrl: me.profilePhotoUrl ?? '', resumeText: me.resumeText ?? '',
    });
    setEditing(true);
    setSaveError('');
  };

  const save = async (e) => {
    e.preventDefault();
    setSaveError('');
    try {
      await api.put('/students/me', {
        name: emptyToUndef(form.name), branch: emptyToUndef(form.branch),
        batchYear: numOrUndef(form.batchYear), cgpa: numOrUndef(form.cgpa),
        backlogCount: numOrUndef(form.backlogCount), cvUrl: emptyToUndef(form.cvUrl),
        profilePhotoUrl: emptyToUndef(form.profilePhotoUrl), resumeText: emptyToUndef(form.resumeText),
      });
      setEditing(false);
      reload();
    } catch (err) {
      setSaveError(errMsg(err));
    }
  };

  return (
    <Card title={`${me.name} · ${me.rollNo}`} actions={!editing && <button className="btn ghost sm" onClick={startEdit} type="button">Edit</button>}>
      {!editing ? (
        <dl className="kv">
          <dt>Email</dt><dd>{me.email}</dd>
          <dt>Branch / batch</dt><dd>{me.branch} · {me.batchYear}</dd>
          <dt>CGPA</dt><dd>{me.cgpa ?? '—'}</dd>
          <dt>Backlogs</dt><dd>{me.backlogCount}</dd>
          <dt>College</dt><dd>{me.collegeName ?? '—'}</dd>
          <dt>CV</dt><dd>{me.cvUrl ? <a href={me.cvUrl} target="_blank" rel="noreferrer">open</a> : '—'}</dd>
        </dl>
      ) : (
        <form className="form" onSubmit={save}>
          <div className="form-row">
            <Field label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <Field label="Branch" value={form.branch} onChange={(e) => setForm({ ...form, branch: e.target.value })} />
          </div>
          <div className="form-row">
            <Field label="Batch year" type="number" value={form.batchYear} onChange={(e) => setForm({ ...form, batchYear: e.target.value })} />
            <Field label="CGPA" type="number" step="0.01" value={form.cgpa} onChange={(e) => setForm({ ...form, cgpa: e.target.value })} />
            <Field label="Backlogs" type="number" value={form.backlogCount} onChange={(e) => setForm({ ...form, backlogCount: e.target.value })} />
          </div>
          <div className="form-row">
            <Field label="CV URL" value={form.cvUrl} onChange={(e) => setForm({ ...form, cvUrl: e.target.value })} />
            <Field label="Photo URL" value={form.profilePhotoUrl} onChange={(e) => setForm({ ...form, profilePhotoUrl: e.target.value })} />
          </div>
          <Area label="CV text (feeds the AI analysis)" value={form.resumeText} onChange={(e) => setForm({ ...form, resumeText: e.target.value })} />
          <Err error={saveError} />
          <div className="btn-row">
            <button className="btn" type="submit">Save</button>
            <button className="btn ghost" onClick={() => setEditing(false)} type="button">Cancel</button>
          </div>
        </form>
      )}
    </Card>
  );
}

/* ── skills ────────────────────────────────────────────────────────── */
function SkillsTab() {
  const catalog = useCatalog();
  const { data: mine, error, reload } = useLoad(async () => (await api.get('/students/me/skills')).data, []);
  const [skillId, setSkillId] = useState('');
  const [level, setLevel] = useState(3);
  const [msg, setMsg] = useState('');
  const mySkills = mine ?? [];

  const add = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      await api.post('/students/me/skills', { skillId: Number(skillId), proficiencyLevel: Number(level) });
      setSkillId('');
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  const changeLevel = async (entry, newLevel) => {
    setMsg('');
    try {
      await api.put(`/students/me/skills/${entry.id}`, { skillId: entry.skillId, proficiencyLevel: Number(newLevel) });
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  const remove = async (id) => {
    setMsg('');
    try {
      await api.delete(`/students/me/skills/${id}`);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  return (
    <Card title="My skills">
      <Err error={msg || error} />
      {mySkills.length === 0 ? <Empty text="No skills yet — add your first one below." /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Skill</th><th>Category</th><th>Level</th><th>Source</th><th /></tr></thead>
          <tbody>
            {mySkills.map((s) => (
              <tr key={s.id}>
                <td><b>{s.skillName}</b></td>
                <td><Badge value={s.category} /></td>
                <td>
                  <select value={s.proficiencyLevel} onChange={(e) => changeLevel(s, e.target.value)}>
                    {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n}/5</option>)}
                  </select>
                </td>
                <td><Badge value={s.source} /></td>
                <td><DelButton onDelete={() => remove(s.id)} /></td>
              </tr>
            ))}
          </tbody>
        </table></div>
      )}
      <h3>Add a skill</h3>
      <form onSubmit={add}>
        <div className="skill-pick">
          <Sel label="Skill" required value={skillId} onChange={(e) => setSkillId(e.target.value)} allowEmpty emptyLabel="— choose —" options={catalog.map((c) => ({ value: c.id, label: `${c.name} (${c.category})` }))} />
          <Sel label="Level" value={level} onChange={(e) => setLevel(e.target.value)} options={[1, 2, 3, 4, 5]} />
          <button className="btn" type="submit">Add</button>
        </div>
      </form>
    </Card>
  );
}

/* ── projects ──────────────────────────────────────────────────────── */
const blankProject = { title: '', description: '', techStack: '', repoLink: '', duration: '' };

function ProjectsTab() {
  const { data, error, reload } = useLoad(async () => (await api.get('/students/me/projects')).data, []);
  const [form, setForm] = useState(blankProject);
  const [editingId, setEditingId] = useState(null);
  const [msg, setMsg] = useState('');
  const list = data ?? [];

  const submit = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      if (editingId) await api.put(`/students/me/projects/${editingId}`, form);
      else await api.post('/students/me/projects', form);
      setForm(blankProject);
      setEditingId(null);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  return (
    <Card title="My projects">
      <Err error={msg || error} />
      {list.length === 0 ? <Empty /> : list.map((p) => (
        <div className="milestone" key={p.id}>
          <h4>{p.title}</h4>
          <p className="muted">{p.description || 'No description.'}</p>
          <div>
            {p.techStack && <span className="tag">🛠 {p.techStack}</span>}
            {p.duration && <span className="tag">⏱ {p.duration}</span>}
            {p.repoLink && <a className="tag" href={p.repoLink} target="_blank" rel="noreferrer">🔗 repo</a>}
          </div>
          <div className="btn-row" style={{ marginTop: 8 }}>
            <button className="btn ghost sm" onClick={() => { setEditingId(p.id); setForm({ title: p.title, description: p.description ?? '', techStack: p.techStack ?? '', repoLink: p.repoLink ?? '', duration: p.duration ?? '' }); }} type="button">Edit</button>
            <DelButton onDelete={async () => { await api.delete(`/students/me/projects/${p.id}`); reload(); }} />
          </div>
        </div>
      ))}
      <h3>{editingId ? 'Edit project' : 'Add project'}</h3>
      <form className="form" onSubmit={submit}>
        <Field label="Title *" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        <Area label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        <div className="form-row">
          <Field label="Tech stack" value={form.techStack} onChange={(e) => setForm({ ...form, techStack: e.target.value })} />
          <Field label="Repo link" value={form.repoLink} onChange={(e) => setForm({ ...form, repoLink: e.target.value })} />
          <Field label="Duration" value={form.duration} onChange={(e) => setForm({ ...form, duration: e.target.value })} placeholder="3 months" />
        </div>
        <div className="btn-row">
          <button className="btn" type="submit">{editingId ? 'Save' : 'Add'}</button>
          {editingId && <button className="btn ghost" onClick={() => { setEditingId(null); setForm(blankProject); }} type="button">Cancel</button>}
        </div>
      </form>
    </Card>
  );
}

/* ── certificates ──────────────────────────────────────────────────── */
const blankCert = { title: '', issuingOrg: '', issueDate: '', credentialUrl: '' };

function CertsTab() {
  const { data, error, reload } = useLoad(async () => (await api.get('/students/me/certificates')).data, []);
  const [form, setForm] = useState(blankCert);
  const [editingId, setEditingId] = useState(null);
  const [msg, setMsg] = useState('');
  const list = data ?? [];

  const submit = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      const body = { ...form, issueDate: emptyToUndef(form.issueDate) || null };
      if (editingId) await api.put(`/students/me/certificates/${editingId}`, body);
      else await api.post('/students/me/certificates', body);
      setForm(blankCert);
      setEditingId(null);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  return (
    <Card title="My certificates">
      <Err error={msg || error} />
      {list.length === 0 ? <Empty /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Title</th><th>Issuer</th><th>Issued</th><th>Status</th><th /></tr></thead>
          <tbody>
            {list.map((c) => (
              <tr key={c.id}>
                <td><b>{c.title}</b>{c.credentialUrl && <> (<a href={c.credentialUrl} target="_blank" rel="noreferrer">credential</a>)</>}</td>
                <td>{c.issuingOrg || '—'}</td>
                <td>{c.issueDate || '—'}</td>
                <td><Badge value={c.verificationStatus} /></td>
                <td>
                  <span className="btn-row">
                    <button className="btn ghost sm" onClick={() => { setEditingId(c.id); setForm({ title: c.title, issuingOrg: c.issuingOrg ?? '', issueDate: c.issueDate ?? '', credentialUrl: c.credentialUrl ?? '' }); }} type="button">Edit</button>
                    <DelButton onDelete={async () => { await api.delete(`/students/me/certificates/${c.id}`); reload(); }} />
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table></div>
      )}
      <h3>{editingId ? 'Edit certificate' : 'Add certificate'}</h3>
      <form className="form" onSubmit={submit}>
        <div className="form-row">
          <Field label="Title *" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <Field label="Issuing org" value={form.issuingOrg} onChange={(e) => setForm({ ...form, issuingOrg: e.target.value })} />
        </div>
        <div className="form-row">
          <Field label="Issue date" type="date" value={form.issueDate} onChange={(e) => setForm({ ...form, issueDate: e.target.value })} />
          <Field label="Credential URL" value={form.credentialUrl} onChange={(e) => setForm({ ...form, credentialUrl: e.target.value })} />
        </div>
        <div className="btn-row">
          <button className="btn" type="submit">{editingId ? 'Save' : 'Add'}</button>
          {editingId && <button className="btn ghost" onClick={() => { setEditingId(null); setForm(blankCert); }} type="button">Cancel</button>}
        </div>
      </form>
    </Card>
  );
}

/* ── jobs + applications ───────────────────────────────────────────── */
function JobsTab() {
  const [domain, setDomain] = useState('');
  const [active, setActive] = useState('true');
  const [minPackage, setMinPackage] = useState('');
  const [page, setPage] = useState(0);
  const [jobs, setJobs] = useState(null);
  const [error, setError] = useState('');
  const [fits, setFits] = useState({});
  const [notice, setNotice] = useState('');
  const apps = useLoad(async () => (await api.get('/students/me/applications')).data, []);

  const search = async (p = 0) => {
    setError('');
    try {
      const { data } = await api.get('/jobs', {
        params: {
          domain: emptyToUndef(domain), active: active === '' ? undefined : active === 'true',
          minPackage: numOrUndef(minPackage), page: p, size: 10,
        },
      });
      setJobs(data);
      setPage(p);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  useFirstMount(search); // initial load

  const apply = async (id) => {
    setNotice('');
    try {
      const { data } = await api.post(`/jobs/${id}/apply`);
      setNotice(`Applied! Match score: ${data.matchScore}%`);
      apps.reload();
    } catch (err) {
      setNotice(errMsg(err));
    }
  };

  const checkFit = async (id) => {
    setNotice('');
    try {
      const { data } = await api.get(`/ai/match/${id}`);
      setFits((f) => ({ ...f, [id]: data }));
    } catch (err) {
      setNotice(errMsg(err));
    }
  };

  const appliedIds = new Set((apps.data ?? []).map((a) => a.jobId));

  return (
    <>
      <Card title="Find jobs">
        <Err error={error} />
        {notice && <div className="success">{notice}</div>}
        <div className="form-row">
          <Field label="Domain" value={domain} onChange={(e) => setDomain(e.target.value)} placeholder="Engineering" />
          <Sel label="Status" value={active} onChange={(e) => setActive(e.target.value)} options={[{ value: '', label: 'All' }, { value: 'true', label: 'Active' }, { value: 'false', label: 'Closed' }]} />
          <Field label="Min package (LPA)" type="number" step="0.5" value={minPackage} onChange={(e) => setMinPackage(e.target.value)} />
        </div>
        <div style={{ marginTop: 10 }}><button className="btn" onClick={() => search(0)} type="button">Search</button></div>
        {jobs && (
          <>
            {contentOf(jobs).length === 0 ? <Empty text="No jobs match." /> : contentOf(jobs).map((j) => (
              <div className="milestone" key={j.id}>
                <h4>{j.title} <span className="muted">· {j.companyName}</span></h4>
                <div>
                  {j.jobDomain && <span className="tag">{j.jobDomain}</span>}
                  {j.packageLpa != null && <span className="tag">💰 {j.packageLpa} LPA</span>}
                  {j.minCgpa != null && <span className="tag">CGPA ≥ {j.minCgpa}</span>}
                  {j.deadline && <span className="tag">⏰ {j.deadline}</span>}
                  <Badge value={j.active ? 'ACTIVE' : 'INACTIVE'} text={j.active ? 'Open' : 'Closed'} />
                </div>
                <p className="muted">{(j.description || '').slice(0, 220)}</p>
                <div>{(j.skills ?? []).map((s) => <span className="tag" key={s.skillId}>{s.skillName} ≥{s.requiredProficiency}</span>)}</div>
                <div className="btn-row" style={{ marginTop: 8 }}>
                  {appliedIds.has(j.id)
                    ? <Badge value="APPLIED" text="Applied ✓" />
                    : <button className="btn sm" disabled={!j.active} onClick={() => apply(j.id)} type="button">Apply</button>}
                  <button className="btn ghost sm" onClick={() => checkFit(j.id)} type="button">Check my fit</button>
                </div>
                {fits[j.id] && (
                  <p><b>Fit: {fits[j.id].matchScore}%</b> <Badge value={fits[j.id].source} /> <span className="muted">{fits[j.id].reasoning}</span></p>
                )}
              </div>
            ))}
            <Pager page={jobs} onPage={search} />
          </>
        )}
      </Card>
      <Card title="My applications">
        {(apps.data ?? []).length === 0 ? <Empty text="You haven't applied anywhere yet." /> : (
          <div className="table-wrap"><table>
            <thead><tr><th>Job</th><th>Company</th><th>Status</th><th>Match</th><th>Applied</th></tr></thead>
            <tbody>
              {(apps.data ?? []).map((a) => (
                <tr key={a.id}>
                  <td><b>{a.jobTitle}</b></td>
                  <td>{a.companyName}</td>
                  <td><Badge value={a.status} /></td>
                  <td>{a.matchScore != null ? `${a.matchScore}%` : '—'}</td>
                  <td>{fmtDate(a.appliedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table></div>
        )}
      </Card>
    </>
  );
}

// tiny "run once" helper
function useFirstMount(fn) {
  const ran = useRef(false);
  useEffect(() => {
    if (!ran.current) {
      ran.current = true;
      fn(0);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}

/* ── AI coach ──────────────────────────────────────────────────────── */
function AiTab() {
  const [analysis, setAnalysis] = useState(null);
  const [roadmaps, setRoadmaps] = useState([]);
  const [history, setHistory] = useState([]);
  const [targetRole, setTargetRole] = useState('');
  const [busy, setBusy] = useState('');
  const [msg, setMsg] = useState('');

  const loadLists = async () => {
    try {
      const [h, r] = await Promise.all([
        api.get('/ai/analysis/me/history'),
        api.get('/ai/roadmaps/me'),
      ]);
      setHistory(h.data);
      setRoadmaps(r.data);
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  useFirstMount(loadLists);

  const analyze = async () => {
    setBusy('analyze'); setMsg('');
    try {
      const { data } = await api.post('/ai/analyze-me');
      setAnalysis(data);
      loadLists();
    } catch (err) {
      setMsg(errMsg(err));
    } finally {
      setBusy('');
    }
  };

  const roadmap = async (e) => {
    e.preventDefault();
    setBusy('roadmap'); setMsg('');
    try {
      const { data } = await api.post('/ai/roadmap', { targetRole });
      setRoadmaps((r) => [data, ...r]);
      setTargetRole('');
    } catch (err) {
      setMsg(errMsg(err));
    } finally {
      setBusy('');
    }
  };

  const show = analysis ?? history[0];

  return (
    <>
      <Err error={msg} />
      <Card title="🔍 Analyse my profile" actions={<button className="btn sm" disabled={busy === 'analyze'} onClick={analyze} type="button">{busy === 'analyze' ? 'Analysing…' : 'Analyse now'}</button>}>
        {!show ? <Empty text="No analysis yet — click “Analyse now”. (Takes ~10–60s with Gemini.)" /> : (
          <>
            <p><Badge value={show.source} /> <span className="muted">{fmtDate(show.generatedAt)}</span></p>
            <p>{show.summaryText}</p>
            <div className="grid2">
              <div>
                <h4>💪 Strong skills</h4>
                {(show.strongSkills ?? []).length === 0 ? <Empty /> : (
                  <div className="table-wrap"><table>
                    <thead><tr><th>Skill</th><th>Level</th><th>Evidence</th></tr></thead>
                    <tbody>
                      {show.strongSkills.map((s, i) => (
                        <tr key={i}><td><b>{s.skill}</b></td><td>{s.level}/5</td><td className="muted">{s.evidence}</td></tr>
                      ))}
                    </tbody>
                  </table></div>
                )}
              </div>
              <div>
                <h4>🌱 Weak skills</h4>
                {(show.weakSkills ?? []).length === 0 ? <Empty /> : (
                  <div className="table-wrap"><table>
                    <thead><tr><th>Skill</th><th>Gap</th><th>Suggestion</th></tr></thead>
                    <tbody>
                      {show.weakSkills.map((s, i) => (
                        <tr key={i}><td><b>{s.skill}</b></td><td className="muted">{s.gap}</td><td className="muted">{s.suggestion}</td></tr>
                      ))}
                    </tbody>
                  </table></div>
                )}
              </div>
            </div>
            {history.length > 1 && <p className="muted">🕘 {history.length} analyses so far — latest shown. Re-analyse anytime to track progress.</p>}
          </>
        )}
      </Card>
      <Card title="🗺 Learning roadmaps">
        <form onSubmit={roadmap}>
          <div className="skill-pick">
            <Field label="Target role" required value={targetRole} onChange={(e) => setTargetRole(e.target.value)} placeholder="Backend Developer" />
            <button className="btn" disabled={busy === 'roadmap'} type="submit">{busy === 'roadmap' ? 'Generating…' : 'Generate'}</button>
          </div>
        </form>
        {roadmaps.length === 0 ? <Empty text="No roadmaps yet." /> : roadmaps.map((r) => (
          <div className="milestone" key={r.id}>
            <h4>🎯 {r.targetRole} <Badge value={r.source} /> <span className="muted">{fmtDate(r.generatedAt)}</span></h4>
            {(r.milestones ?? []).map((m, i) => (
              <div className="milestone" key={i}>
                <h4>Week {m.week}: {m.title}</h4>
                <ul>{(m.tasks ?? []).map((t, j) => <li key={j}>{t}</li>)}</ul>
                {(m.resources ?? []).length > 0 && <p className="muted">📚 {(m.resources ?? []).join(' · ')}</p>}
              </div>
            ))}
          </div>
        ))}
      </Card>
    </>
  );
}

/* ── interview slots ───────────────────────────────────────────────── */
function SlotsTab() {
  const { data, error } = useLoad(async () => (await api.get('/students/me/slots')).data, []);
  const list = data ?? [];
  return (
    <Card title="My interview slots">
      <Err error={error} />
      {list.length === 0 ? <Empty text="No interviews scheduled yet." /> : (
        <div className="table-wrap"><table>
          <thead><tr><th>Job</th><th>Round</th><th>Scheduled at</th><th>Venue / link</th><th>Status</th></tr></thead>
          <tbody>
            {list.map((s) => (
              <tr key={s.id}>
                <td><b>{s.jobTitle}</b></td>
                <td>{s.roundName}</td>
                <td>{fmtDate(s.scheduledAt)}</td>
                <td className="muted">{s.venueOrLink || '—'}</td>
                <td><Badge value={s.status} /></td>
              </tr>
            ))}
          </tbody>
        </table></div>
      )}
    </Card>
  );
}
