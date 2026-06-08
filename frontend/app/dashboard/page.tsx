'use client';

import { useEffect, useState, useRef, useCallback } from 'react';
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
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState({ url: '', label: '', selector: '', checkIntervalMinutes: 60 });
  const [showPicker, setShowPicker] = useState(false);
  const [pickerTarget, setPickerTarget] = useState<'add' | 'edit'>('add');
  const [pickerLoading, setPickerLoading] = useState(false);
  const iframeRef = useRef<HTMLIFrameElement>(null);

  useEffect(() => {
    if (!localStorage.getItem('token')) {
      router.push('/login');
      return;
    }
    fetchUrls();
  }, []);

  const handlePickerMessage = useCallback((e: MessageEvent) => {
    if (e.data?.type === 'ww-selector') {
      if (pickerTarget === 'edit') {
        setEditForm(f => ({ ...f, selector: e.data.selector }));
      } else {
        setForm(f => ({ ...f, selector: e.data.selector }));
      }
      setShowPicker(false);
    }
  }, [pickerTarget]);

  useEffect(() => {
    window.addEventListener('message', handlePickerMessage);
    return () => window.removeEventListener('message', handlePickerMessage);
  }, [handlePickerMessage]);

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

  const startEdit = (w: WatchedUrl) => {
    setEditingId(w.id);
    setEditForm({ url: w.url, label: w.label, selector: w.selector || '', checkIntervalMinutes: w.checkIntervalMinutes });
  };

  const handleEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.put(`/api/watched-urls/${editingId}`, {
      url: editForm.url,
      label: editForm.label,
      selector: editForm.selector || null,
      checkIntervalMinutes: editForm.checkIntervalMinutes,
    });
    setEditingId(null);
    fetchUrls();
  };

  const openPicker = (target: 'add' | 'edit') => {
    const url = target === 'edit' ? editForm.url : form.url;
    if (!url) return;
    setPickerTarget(target);
    setPickerLoading(true);
    setShowPicker(true);
  };

  const pickerUrl = pickerTarget === 'edit' ? editForm.url : form.url;

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
            <div className="flex gap-2 items-center">
              <input
                placeholder="CSS Selector (optional)"
                value={form.selector}
                onChange={(e) => setForm({ ...form, selector: e.target.value })}
                className="flex-1 border rounded px-3 py-2"
              />
              <button
                type="button"
                onClick={() => openPicker('add')}
                disabled={!form.url}
                title={form.url ? 'Pick element visually' : 'Enter a URL first'}
                className="px-3 py-2 rounded border border-blue-500 text-blue-600 hover:bg-blue-50 disabled:opacity-40 disabled:cursor-not-allowed text-sm whitespace-nowrap"
              >
                Pick element
              </button>
            </div>
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
            <div key={w.id} className="bg-white rounded-lg shadow p-4">
              {editingId === w.id ? (
                <form onSubmit={handleEdit} className="space-y-2">
                  <input
                    value={editForm.url}
                    onChange={(e) => setEditForm({ ...editForm, url: e.target.value })}
                    className="w-full border rounded px-3 py-2 text-sm"
                    placeholder="URL"
                    required
                  />
                  <input
                    value={editForm.label}
                    onChange={(e) => setEditForm({ ...editForm, label: e.target.value })}
                    className="w-full border rounded px-3 py-2 text-sm"
                    placeholder="Label"
                    required
                  />
                  <div className="flex gap-2 items-center">
                    <input
                      value={editForm.selector}
                      onChange={(e) => setEditForm({ ...editForm, selector: e.target.value })}
                      className="flex-1 border rounded px-3 py-2 text-sm"
                      placeholder="CSS Selector (optional)"
                    />
                    <button
                      type="button"
                      onClick={() => openPicker('edit')}
                      disabled={!editForm.url}
                      className="px-3 py-2 rounded border border-blue-500 text-blue-600 hover:bg-blue-50 disabled:opacity-40 disabled:cursor-not-allowed text-sm whitespace-nowrap"
                    >
                      Pick element
                    </button>
                  </div>
                  <input
                    type="number"
                    value={editForm.checkIntervalMinutes}
                    onChange={(e) => setEditForm({ ...editForm, checkIntervalMinutes: parseInt(e.target.value) || 60 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                    placeholder="Check interval (minutes)"
                    min={1}
                  />
                  <div className="flex gap-2">
                    <button type="submit" className="bg-blue-600 text-white px-3 py-1.5 rounded text-sm hover:bg-blue-700">Save</button>
                    <button type="button" onClick={() => setEditingId(null)} className="px-3 py-1.5 rounded border text-sm hover:bg-gray-100">Cancel</button>
                  </div>
                </form>
              ) : (
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-semibold">{w.label}</p>
                    <p className="text-sm text-blue-600">{w.url}</p>
                    {w.selector && (
                      <p className="text-xs text-gray-500 mt-0.5 font-mono">selector: {w.selector}</p>
                    )}
                    <p className="text-xs text-gray-400 mt-1">
                      Every {w.checkIntervalMinutes} min · {w.active ? '🟢 Active' : '🔴 Inactive'} ·{' '}
                      Last checked: {w.lastCheckedAt ? new Date(w.lastCheckedAt).toLocaleString() : 'Never'}
                    </p>
                  </div>
                  <div className="flex gap-2 ml-4">
                    <button onClick={() => startEdit(w)} className="text-blue-500 hover:text-blue-700 text-sm">Edit</button>
                    <button onClick={() => handleDelete(w.id)} className="text-red-500 hover:text-red-700 text-sm">Delete</button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {showPicker && (
        <div className="fixed inset-0 z-50 flex flex-col bg-black bg-opacity-70">
          <div className="bg-white flex items-center justify-between px-4 py-2 shadow">
            <span className="text-sm font-medium text-gray-700">
              Hover over an element and <strong>click</strong> to select it as the watched selector
            </span>
            <button
              onClick={() => setShowPicker(false)}
              className="text-gray-500 hover:text-gray-800 text-xl leading-none ml-4"
            >
              ✕
            </button>
          </div>
          {pickerLoading && (
            <div className="bg-blue-50 text-blue-700 text-xs text-center py-1">Loading page…</div>
          )}
          <iframe
            ref={iframeRef}
            src={`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/internal/proxy?url=${encodeURIComponent(pickerUrl)}`}
            className="flex-1 w-full border-0"
            sandbox="allow-scripts allow-same-origin"
            onLoad={() => setPickerLoading(false)}
          />
        </div>
      )}
    </div>
  );
}
