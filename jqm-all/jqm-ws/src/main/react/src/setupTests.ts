import "@testing-library/jest-dom";
import { server } from "./test-utils/server";
import { beforeAll, afterEach, afterAll } from "vitest";

// jsdom does not implement scrollIntoView
window.HTMLElement.prototype.scrollIntoView = () => {};

beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
