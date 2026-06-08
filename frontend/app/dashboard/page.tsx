'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import Navbar from '@/components/Navbar';

interface WatchedUrl {
  id: string;
  url: string;
  label: string;
  selector: string | null;
  checkIntervalMinutes: number;
  active: boolean;
  createdAt: string;
  lastCheckedAt: string | null;
}

export default function DashboardPage() {
  const router = useRouter();
  const [urls, setUrls] = useState<WatchedUrl[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ url: '', label: '', selector: '', checkIntervalMinutes: 60 });

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      router.push('/login');
      return;
    }
    fetchUrls();
  }, []);

  const fetchUrls = async () => {
    try {
      const res = await api.get('/api/watched-urls');
      setUrls(res.data);
    } catch {
      router.push('/login');
    }
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.post('/api/watched-urls', {
      url: form.url,
      label: form.label,
      selector: form.selector || null,
      checkIntervalMinutes: form.checkIntervalMinutes,
    });
    setForm({ url: '', label: '', selector: '', checkIntervalMinutes: 60 });
    setShowForm(false);
    fetchUrls();
  };

  const handleDelete = async (id: string) => {
    await api.delete(`/api/watched-urls/${id}`);
    fetchUrls();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-4xl mx-auto p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Watched URLs</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            + Add URL
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleAdd} className="bg-white rounded-lg shadow p-4 mb-6 space-y-3">
            <input
              placeholder="URL (https://...)"
              value={form.url}
              onChange={(e) => setForm({ ...form, url: e.target.value })}
              className="w-full border rounded px-3 py-2"
              required
            />
            <input
              placeholder="Label"
              value={form.label}
              onChange={(e) => setForm({ ...form, label: e.target.value })}
              className="w-full border rounded px-3 py-2"
              required
            />
            <input
              placeholder="CSS Selector (optional)"
              value={form.selector}
              onChange={(e) => setForm({ ...form, selector: e.target.value })}
              className="w-full border rounded px-3 py-2"
            />
            <input
              type="number"
              placeholder="Check interval (minutes)"
              value={form.checkIntervalMinutes}
              onChange={(e) => setForm({ ...form, checkIntervalMinutes: parseInt(e.target.value) })}
              className="w-full border rounded px-3 py-2"
              min={1}
            />
            <div className="flex gap-2">
              <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Save</button>
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 rounded border hover:bg-gray-100">Cancel</button>
            </div>
          </form>
        )}

        <div className="space-y-3">
          {urls.length === 0 && <p className="text-gray-500">No URLs watched yet.</p>}
          {urls.map((w) => (
            <div key={w.id} className="bg-white rounded-lg shadow p-4 flex justify-between items-start">
              <div>
                <p className="font-semibold">{w.label}</p>
                <p className="text-sm text-blue-600">{w.url}</p>
                <p className="text-xs text-gray-400 mt-1">
                  Every {w.checkIntervalMinutes} min · {w.active ? '🟢 Active' : '🔴 Inactive'} ·{' '}
                  Last checked: {w.lastCheckedAt ? new Date(w.lastCheckedAt).toLocaleString() : 'Never'}
                </p>
              </div>
              <button
                onClick={() => handleDelete(w.id)}
                className="text-red-500 hover:text-red-700 text-sm ml-4"
              >
                Delete
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
