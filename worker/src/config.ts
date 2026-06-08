import dotenv from 'dotenv';
dotenv.config();

export const BACKEND_INTERNAL_URL = process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';
export const WORKER_POLL_INTERVAL_MS = parseInt(process.env.WORKER_POLL_INTERVAL_MS || '60000', 10);
