import axios from 'axios';
import { BACKEND_INTERNAL_URL } from './config';

export interface WatchedUrl {
  id: string;
  url: string;
  label: string;
  selector: string | null;
  checkIntervalMinutes: number;
}

export async function getDueUrls(): Promise<WatchedUrl[]> {
  const response = await axios.get<WatchedUrl[]>(`${BACKEND_INTERNAL_URL}/internal/watched-urls/due`);
  return response.data;
}

export async function submitSnapshot(watchedUrlId: string, contentHash: string, content: string): Promise<void> {
  await axios.post(`${BACKEND_INTERNAL_URL}/internal/snapshots`, {
    watchedUrlId,
    contentHash,
    content,
  });
}
