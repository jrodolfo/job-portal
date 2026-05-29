import {setupServer} from 'msw/node';

/**
 * MSW (Mock Service Worker) server instance for node environment (tests).
 * It is used to intercept network requests during tests.
 */
export const server = setupServer();
