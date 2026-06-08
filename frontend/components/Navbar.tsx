'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import Link from 'next/link';

interface Notification {
  id: string;
  message: string;
  seen: boolean;
}

export default function Navbar() {
  const router = useRouter();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await api.get('/api/notifications?seen=false');
        setNotifications(res.data);
      } catch {
        // ignore
      }
    };
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 15000);
    return () => clearInterval(interval);
  }, []);

  const markSeen = async (id: string) => {
    await api.put(`/api/notifications/${id}/seen`);
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const logout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  return (
    <nav className="bg-white border-b px-6 py-3 flex justify-between items-center">
      <Link href="/dashboard" className="font-bold text-lg text-blue-600">Website Watcher</Link>
      <div className="flex items-center gap-4 relative">
        <button onClick={() => setOpen(!open)} className="relative">
          <span className="text-xl">🔔</span>
          {notifications.length > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
              {notifications.length}
            </span>
          )}
        </button>
        {open && (
          <div className="absolute right-16 top-8 bg-white border rounded shadow-lg w-72 z-10">
            {notifications.length === 0 ? (
              <p className="p-4 text-sm text-gray-500">No new notifications</p>
            ) : (
              notifications.map((n) => (
                <div key={n.id} className="p-3 border-b flex justify-between items-start">
                  <p className="text-sm">{n.message}</p>
                  <button onClick={() => markSeen(n.id)} className="text-xs text-blue-600 ml-2 shrink-0">✓</button>
                </div>
              ))
            )}
          </div>
        )}
        <button onClick={logout} className="text-sm text-gray-600 hover:text-red-600">Logout</button>
      </div>
    </nav>
  );
}
