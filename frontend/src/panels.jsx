import { useCallback, useEffect, useState } from 'react';
import { api, contentOf, errMsg, fmtDate, numOrUndef } from './api.js';
import { Badge, Card, DelButton, Empty, Err, Field, Pager, Sel } from './components.jsx';

/* ── data hook ─────────────────────────────────────────────────────── */
export function useLoad(loader, deps = []) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData(await loader());
    } catch (err) {
      setError(errMsg(err));
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  useEffect(() => {
    reload();
  }, [reload]);
  return { data, setData, error, setError, loading, reload };
}

/* ── trending skills ───────────────────────────────────────────────── */
export function TrendingPanel({ showRefresh = false }) {
  const [limit, setLimit] = useState(10);
  const { data, error, loading, reload } = useLoad(
    async () => (await api.get('/portal/trending', { params: { limit } })).data,
    [limit],
  );
  const [msg, setMsg] = useState('');
  const [busy, setBusy] = useState(false);

  const refresh = async (kind) => {
    setBusy(true);
    setMsg('');
    try {
      const { data: r } = await api.post(`/portal/trending/refresh${kind === 'insight' ? '-insight' : ''}`);
      setMsg(kind === 'insight' ? String(r.insight).slice(0, 300) : `Scores recomputed for ${r.skillsRefreshed} skills.`);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card
      title="📈 Trending skills"
      actions={
        showRefresh && (
          <span className="btn-row">
            <button className="btn ghost sm" disabled={busy} onClick={() => refresh('scores')} type="button">
              Recompute scores
            </button>
            <button className="btn ghost sm" disabled={busy} onClick={() => refresh('insight')} type="button">
              Regenerate AI insight
            </button>
          </span>
        )
      }
    >
      {msg && <div className="success">{msg}</div>}
      <Err error={error} />
      {loading ? (
        <p className="muted">Loading…</p>
      ) : (data?.length ?? 0) === 0 ? (
        <Empty text="No trending data yet — post jobs with skills, then refresh." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>#</th><th>Skill</th><th>Category</th><th>Demand</th><th>Source</th></tr>
            </thead>
            <tbody>
              {data.map((t, i) => (
                <tr key={t.id}>
                  <td>{i + 1}</td>
                  <td><b>{t.skillName}</b></td>
                  <td><Badge value={t.category} /></td>
                  <td>{t.demandScore}</td>
                  <td className="muted">{t.source}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data?.[0]?.insight && (
        <p className="muted">💡 {data[0].insight}</p>
      )}
      <div className="form-row" style={{ maxWidth: 220 }}>
        <Sel label="Show top" value={limit} onChange={(e) => setLimit(Number(e.target.value))} options={[5, 10, 20]} />
      </div>
    </Card>
  );
}

/* ── dashboard stats ───────────────────────────────────────────────── */
export function StatsPanel({ url }) {
  const { data, error, loading } = useLoad(async () => (await api.get(url)).data, [url]);
  if (loading) return <Card title="Dashboard"><p className="muted">Loading…</p></Card>;
  if (error) return <Card title="Dashboard"><Err error={error} /></Card>;
  const s = data.stats;
  return (
    <>
      <div className="stat-grid">
        <div className="stat"><div className="num">{s.totalStudents}</div><div className="lbl">Students</div></div>
        <div className="stat"><div className="num">{s.totalCompanies}</div><div className="lbl">Companies</div></div>
        <div className="stat"><div className="num">{s.totalJobs}</div><div className="lbl">Jobs ({s.activeJobs} active)</div></div>
        <div className="stat"><div className="num">{s.totalApplications}</div><div className="lbl">Applications</div></div>
      </div>
      <Card title="Applications by status">
        {Object.keys(s.byStatus ?? {}).length === 0 ? <Empty /> : (
          <span className="btn-row">
            {Object.entries(s.byStatus).map(([k, v]) => (
              <span key={k}><Badge value={k} /> <b>{v}</b>&nbsp;&nbsp;</span>
            ))}
          </span>
        )}
      </Card>
      <div className="grid2">
        <Card title="Branch-wise">
          {data.byBranch.length === 0 ? <Empty /> : (
            <div className="table-wrap"><table>
              <thead><tr><th>Branch</th><th>Students</th><th>Applications</th><th>Selected</th></tr></thead>
              <tbody>
                {data.byBranch.map((b) => (
                  <tr key={b.branch}><td>{b.branch}</td><td>{b.students}</td><td>{b.applications}</td><td>{b.selected}</td></tr>
                ))}
              </tbody>
            </table></div>
          )}
        </Card>
        <Card title="Company-wise">
          {data.byCompany.length === 0 ? <Empty /> : (
            <div className="table-wrap"><table>
              <thead><tr><th>Company</th><th>Jobs</th><th>Applications</th><th>Selected</th></tr></thead>
              <tbody>
                {data.byCompany.map((c) => (
                  <tr key={c.companyName}><td>{c.companyName}</td><td>{c.jobs}</td><td>{c.applications}</td><td>{c.selected}</td></tr>
                ))}
              </tbody>
            </table></div>
          )}
        </Card>
      </div>
    </>
  );
}

/* ── verification queues ───────────────────────────────────────────── */
function QueueTable({ title, page, onPage, columns, onApprove }) {
  const rows = contentOf(page);
  return (
    <Card title={title}>
      {rows.length === 0 ? <Empty text="Queue is empty. 🎉" /> : (
        <div className="table-wrap"><table>
          <thead><tr>{columns.map((c) => <th key={c}>{c}</th>)}<th>Action</th></tr></thead>
          <tbody>{rows.map((r) => onApprove(r))}</tbody>
        </table></div>
      )}
      <Pager page={page} onPage={onPage} />
    </Card>
  );
}

export function VerifyPanel() {
  const [skillPage, setSkillPage] = useState(0);
  const [certPage, setCertPage] = useState(0);
  const [error, setError] = useState('');
  const skills = useLoad(async () => (await api.get('/skills/verification-queue', { params: { page: skillPage, size: 10 } })).data, [skillPage]);
  const certs = useLoad(async () => (await api.get('/certificates/pending', { params: { page: certPage, size: 10 } })).data, [certPage]);

  const decide = async (kind, id, approved) => {
    setError('');
    try {
      if (kind === 'skill') {
        await api.put(`/skills/student-skills/${id}/verify`, { approved });
        skills.reload();
      } else {
        await api.put(`/certificates/${id}/verify`, { approved });
        certs.reload();
      }
    } catch (err) {
      setError(errMsg(err));
    }
  };

  return (
    <>
      <Err error={error} />
      <QueueTable
        title="Skill verifications"
        page={skills.data}
        onPage={setSkillPage}
        columns={['Student #', 'Skill', 'Level', 'Source']}
        onApprove={(r) => (
          <tr key={r.id}>
            <td>#{r.studentId}</td>
            <td><b>{r.skillName}</b></td>
            <td>{r.proficiencyLevel}/5</td>
            <td><Badge value={r.source} /></td>
            <td>
              <span className="btn-row">
                <button className="btn sm" onClick={() => decide('skill', r.id, true)} type="button">Verify</button>
                <button className="btn danger-ghost sm" onClick={() => decide('skill', r.id, false)} type="button">Reject</button>
              </span>
            </td>
          </tr>
        )}
      />
      <QueueTable
        title="Certificate verifications"
        page={certs.data}
        onPage={setCertPage}
        columns={['Student #', 'Title', 'Issuer', 'Credential']}
        onApprove={(r) => (
          <tr key={r.id}>
            <td>#{r.studentId}</td>
            <td><b>{r.title}</b></td>
            <td>{r.issuingOrg || '—'}</td>
            <td>{r.credentialUrl ? <a href={r.credentialUrl} target="_blank" rel="noreferrer">link</a> : '—'}</td>
            <td>
              <span className="btn-row">
                <button className="btn sm" onClick={() => decide('cert', r.id, true)} type="button">Verify</button>
                <button className="btn danger-ghost sm" onClick={() => decide('cert', r.id, false)} type="button">Reject</button>
              </span>
            </td>
          </tr>
        )}
      />
    </>
  );
}

/* ── interviews: process + slots (shared by company & college) ──────── */
export function InterviewsPanel({ jobs }) {
  const [jobId, setJobId] = useState(jobs[0]?.id ?? '');
  const [process, setProcess] = useState(null);
  const [roundsText, setRoundsText] = useState('');
  const [minCgpa, setMinCgpa] = useState('');
  const [branches, setBranches] = useState('');
  const [maxBacklogs, setMaxBacklogs] = useState('');
  const [slots, setSlots] = useState([]);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const [slotForm, setSlotForm] = useState({ studentId: '', roundName: '', scheduledAt: '', venueOrLink: '', status: 'SCHEDULED' });
  const [editingId, setEditingId] = useState(null);

  const loadAll = useCallback(async (jid) => {
    if (!jid) return;
    setError('');
    setProcess(null);
    setSlots([]);
    try {
      const { data: p } = await api.get(`/jobs/${jid}/process`);
      setProcess(p);
      setRoundsText((p.rounds ?? []).map((r) => r.round).join(', '));
      const c = p.selectionCriteria ?? {};
      setMinCgpa(c.min_cgpa ?? '');
      setBranches((c.branches ?? []).join(', '));
      setMaxBacklogs(c.max_backlogs ?? '');
      const { data: s } = await api.get(`/processes/${p.id}/slots`);
      setSlots(s);
      setSlotForm((f) => ({ ...f, roundName: p.rounds?.[0]?.round ?? '' }));
    } catch (err) {
      if (err?.response?.status !== 404) setError(errMsg(err));
      else {
        setRoundsText('Aptitude, Technical, HR');
        setMinCgpa(''); setBranches(''); setMaxBacklogs('');
      }
    }
  }, []);

  useEffect(() => {
    if (jobs.length > 0 && !jobId) setJobId(jobs[0].id);
  }, [jobs, jobId]);
  useEffect(() => {
    loadAll(jobId);
  }, [jobId, loadAll]);

  const saveProcess = async () => {
    setError(''); setMsg('');
    const rounds = roundsText.split(',').map((s) => s.trim()).filter(Boolean).map((round) => ({ round }));
    const selectionCriteria = {
      ...(minCgpa !== '' ? { min_cgpa: Number(minCgpa) } : {}),
      ...(branches.trim() ? { branches: branches.split(',').map((s) => s.trim()).filter(Boolean) } : {}),
      ...(maxBacklogs !== '' ? { max_backlogs: Number(maxBacklogs) } : {}),
    };
    try {
      await api.put(`/jobs/${jobId}/process`, { rounds, selectionCriteria });
      setMsg('Interview process saved.');
      loadAll(jobId);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const saveSlot = async (e) => {
    e.preventDefault();
    setError(''); setMsg('');
    const body = {
      studentId: Number(slotForm.studentId),
      roundName: slotForm.roundName,
      scheduledAt: new Date(slotForm.scheduledAt).toISOString(),
      venueOrLink: slotForm.venueOrLink || undefined,
      status: slotForm.status,
    };
    try {
      if (editingId) {
        await api.put(`/slots/${editingId}`, body);
        setMsg('Slot rescheduled.');
      } else {
        await api.post(`/processes/${process.id}/slots`, body);
        setMsg('Slot scheduled.');
      }
      setEditingId(null);
      setSlotForm({ studentId: '', roundName: process?.rounds?.[0]?.round ?? '', scheduledAt: '', venueOrLink: '', status: 'SCHEDULED' });
      loadAll(jobId);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  const startEdit = (s) => {
    setEditingId(s.id);
    const d = new Date(s.scheduledAt);
    const pad = (n) => String(n).padStart(2, '0');
    setSlotForm({
      studentId: s.studentId,
      roundName: s.roundName,
      scheduledAt: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`,
      venueOrLink: s.venueOrLink ?? '',
      status: s.status,
    });
  };

  const deleteSlot = async (id) => {
    setError('');
    try {
      await api.delete(`/slots/${id}`);
      setMsg('Slot cancelled.');
      loadAll(jobId);
    } catch (err) {
      setError(errMsg(err));
    }
  };

  if (jobs.length === 0) return <Card title="Interviews"><Empty text="No jobs found — post a job first." /></Card>;

  return (
    <>
      <Err error={error} />
      {msg && <div className="success">{msg}</div>}
      <Card title="1 · Select job & define process (rounds + criteria)">
        <div className="form">
          <Sel label="Job" value={jobId} onChange={(e) => setJobId(e.target.value)} options={jobs.map((j) => ({ value: j.id, label: `#${j.id} ${j.title}` }))} />
          <Field label="Rounds (comma-separated)" value={roundsText} onChange={(e) => setRoundsText(e.target.value)} placeholder="Aptitude, Technical, HR" />
          <div className="form-row">
            <Field label="Criteria: min CGPA" type="number" step="0.01" value={minCgpa} onChange={(e) => setMinCgpa(e.target.value)} />
            <Field label="Criteria: branches (csv)" value={branches} onChange={(e) => setBranches(e.target.value)} placeholder="CSE, IT" />
            <Field label="Criteria: max backlogs" type="number" value={maxBacklogs} onChange={(e) => setMaxBacklogs(e.target.value)} />
          </div>
          <div><button className="btn" onClick={saveProcess} type="button">Save process</button></div>
        </div>
      </Card>
      {process && (
        <Card title={`2 · Slots for “${process.jobTitle}”`}>
          {slots.length === 0 ? <Empty text="No slots scheduled yet." /> : (
            <div className="table-wrap"><table>
              <thead><tr><th>Student</th><th>Round</th><th>Scheduled at</th><th>Venue / link</th><th>Status</th><th /></tr></thead>
              <tbody>
                {slots.map((s) => (
                  <tr key={s.id}>
                    <td>{s.studentName} (#{s.studentId})</td>
                    <td>{s.roundName}</td>
                    <td>{fmtDate(s.scheduledAt)}</td>
                    <td className="muted">{s.venueOrLink || '—'}</td>
                    <td><Badge value={s.status} /></td>
                    <td>
                      <span className="btn-row">
                        <button className="btn ghost sm" onClick={() => startEdit(s)} type="button">Reschedule</button>
                        <DelButton onDelete={() => deleteSlot(s.id)} title="Cancel" />
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table></div>
          )}
          <h3>{editingId ? 'Reschedule slot' : 'Schedule a slot'}</h3>
          <form className="form" onSubmit={saveSlot}>
            <div className="form-row">
              <Field label="Student ID *" type="number" required value={slotForm.studentId} onChange={(e) => setSlotForm({ ...slotForm, studentId: e.target.value })} />
              <Sel label="Round *" required value={slotForm.roundName} onChange={(e) => setSlotForm({ ...slotForm, roundName: e.target.value })} options={(process.rounds ?? []).map((r) => r.round)} />
            </div>
            <div className="form-row">
              <Field label="Scheduled at *" type="datetime-local" required value={slotForm.scheduledAt} onChange={(e) => setSlotForm({ ...slotForm, scheduledAt: e.target.value })} />
              <Field label="Venue / link" value={slotForm.venueOrLink} onChange={(e) => setSlotForm({ ...slotForm, venueOrLink: e.target.value })} />
              <Sel label="Status" value={slotForm.status} onChange={(e) => setSlotForm({ ...slotForm, status: e.target.value })} options={['SCHEDULED', 'COMPLETED', 'CANCELLED']} />
            </div>
            <div className="btn-row">
              <button className="btn" type="submit">{editingId ? 'Save changes' : 'Schedule'}</button>
              {editingId && <button className="btn ghost" onClick={() => { setEditingId(null); setSlotForm({ studentId: '', roundName: process.rounds?.[0]?.round ?? '', scheduledAt: '', venueOrLink: '', status: 'SCHEDULED' }); }} type="button">Cancel edit</button>}
            </div>
          </form>
        </Card>
      )}
    </>
  );
}

/** Catalog fetcher shared by skill pickers. */
export function useCatalog() {
  const { data } = useLoad(async () => (await api.get('/skills', { params: { size: 100 } })).data, []);
  return contentOf(data);
}

export { numOrUndef };
