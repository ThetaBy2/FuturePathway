import { useState } from 'react';
import { api, contentOf, emptyToUndef, errMsg, fmtDate } from '../api.js';
import {
  Badge, Card, DelButton, Empty, Err, Field, Layout, Pager, Sel, Tabs,
} from '../components.jsx';
import { StatsPanel, TrendingPanel, VerifyPanel, useLoad } from '../panels.jsx';

const TABS = [
  { id: 'users', label: '👥 Users' },
  { id: 'skills', label: '🧠 Skill catalog' },
  { id: 'trending', label: '📈 Trending' },
  { id: 'queues', label: '🔍 Queues' },
  { id: 'dashboard', label: '📊 Dashboard' },
];

export default function Admin() {
  const [tab, setTab] = useState('users');
  return (
    <Layout title="Portal admin" subtitle="Users, skill catalog, trends, and verification.">
      <Tabs tabs={TABS} active={tab} onChange={setTab} />
      {tab === 'users' && <UsersTab />}
      {tab === 'skills' && <SkillsTab />}
      {tab === 'trending' && <TrendingPanel showRefresh />}
      {tab === 'queues' && <VerifyPanel />}
      {tab === 'dashboard' && <StatsPanel url="/portal/dashboard" />}
    </Layout>
  );
}

function UsersTab() {
  const [pageNum, setPageNum] = useState(0);
  const { data, error } = useLoad(async () => (await api.get('/admin/users', { params: { page: pageNum, size: 15 } })).data, [pageNum]);
  return (
    <Card title="All users">
      <Err error={error} />
      {!data ? <p className="muted">Loading…</p> : (
        <>
          <div className="table-wrap"><table>
            <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th></tr></thead>
            <tbody>
              {contentOf(data).map((u) => (
                <tr key={u.id}>
                  <td><b>{u.name}</b></td>
                  <td>{u.email}</td>
                  <td><Badge value={u.role} /></td>
                  <td>{fmtDate(u.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table></div>
          <Pager page={data} onPage={setPageNum} />
        </>
      )}
      <p className="muted">College & extra admin accounts are provisioned via <code>POST /api/auth/register/college</code> (see Swagger / Postman).</p>
    </Card>
  );
}

const blankSkill = { name: '', category: 'TECHNICAL', description: '' };

function SkillsTab() {
  const [pageNum, setPageNum] = useState(0);
  const { data, error, reload } = useLoad(async () => (await api.get('/skills', { params: { page: pageNum, size: 15 } })).data, [pageNum]);
  const [form, setForm] = useState(blankSkill);
  const [editingId, setEditingId] = useState(null);
  const [msg, setMsg] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setMsg('');
    try {
      const body = { name: form.name, category: form.category, description: emptyToUndef(form.description) };
      if (editingId) await api.put(`/skills/${editingId}`, body);
      else await api.post('/skills', body);
      setForm(blankSkill);
      setEditingId(null);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  const remove = async (id) => {
    setMsg('');
    try {
      await api.delete(`/skills/${id}`);
      reload();
    } catch (err) {
      setMsg(errMsg(err));
    }
  };

  return (
    <Card title="Skill catalog">
      <Err error={msg || error} />
      {!data ? <p className="muted">Loading…</p> : (
        <>
          <div className="table-wrap"><table>
            <thead><tr><th>Name</th><th>Category</th><th>Description</th><th /></tr></thead>
            <tbody>
              {contentOf(data).map((s) => (
                <tr key={s.id}>
                  <td><b>{s.name}</b></td>
                  <td><Badge value={s.category} /></td>
                  <td className="muted">{(s.description || '').slice(0, 80)}</td>
                  <td>
                    <span className="btn-row">
                      <button className="btn ghost sm" onClick={() => { setEditingId(s.id); setForm({ name: s.name, category: s.category, description: s.description ?? '' }); }} type="button">Edit</button>
                      <DelButton onDelete={() => remove(s.id)} />
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table></div>
          <Pager page={data} onPage={setPageNum} />
        </>
      )}
      <h3>{editingId ? 'Edit skill' : 'Add skill'}</h3>
      <form className="form" onSubmit={submit}>
        <div className="form-row">
          <Field label="Name *" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Sel label="Category *" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} options={['TECHNICAL', 'SOFT']} />
        </div>
        <Field label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        <div className="btn-row">
          <button className="btn" type="submit">{editingId ? 'Save' : 'Add'}</button>
          {editingId && <button className="btn ghost" onClick={() => { setEditingId(null); setForm(blankSkill); }} type="button">Cancel</button>}
        </div>
      </form>
    </Card>
  );
}
