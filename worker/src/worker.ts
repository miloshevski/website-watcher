import { getDueUrls, submitSnapshot } from './api';
import { fetchContent } from './fetcher';
import { hashContent } from './hasher';
import { WORKER_POLL_INTERVAL_MS } from './config';

export async function runOnce(): Promise<void> {
  const urls = await getDueUrls();
  console.log(`[worker] ${urls.length} URL(s) due for checking`);

  for (const watched of urls) {
    try {
      const content = await fetchContent(watched.url, watched.selector);
      const hash = hashContent(content);
      await submitSnapshot(watched.id, hash, content);
      console.log(`[worker] checked: ${watched.label} (${watched.url})`);
    } catch (err) {
      console.error(`[worker] failed to check ${watched.url}:`, err);
    }
  }
}

export function startPolling(): void {
  console.log(`[worker] polling every ${WORKER_POLL_INTERVAL_MS}ms`);

  const poll = async () => {
    try {
      await runOnce();
    } catch (err) {
      console.error('[worker] poll error:', err);
    } finally {
      setTimeout(poll, WORKER_POLL_INTERVAL_MS);
    }
  };

  poll();
}
