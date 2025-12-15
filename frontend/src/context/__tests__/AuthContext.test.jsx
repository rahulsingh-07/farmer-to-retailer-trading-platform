import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AuthProvider, useAuth } from "../AuthContext";

const apiMock = vi.hoisted(() => ({ post: vi.fn(), delete: vi.fn() }));
vi.mock("../../utils/api", () => ({ __esModule: true, default: apiMock }));

const toastMock = vi.hoisted(() => ({ success: vi.fn(), warn: vi.fn(), error: vi.fn() }));
vi.mock("react-toastify", () => ({ toast: toastMock }));

const makeToken = (payload) => {
  const base64 = Buffer.from(JSON.stringify(payload))
    .toString("base64")
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  return `header.${base64}.sig`;
};

const Consumer = () => {
  const { user, token, login, logout } = useAuth();
  return (
    <div>
      <div data-testid="user">{user?.username || ""}</div>
      <div data-testid="token">{token || ""}</div>
      <button onClick={() => login({ username: "alice", password: "secret" })}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  );
};

const renderWithProvider = () =>
  render(
    <AuthProvider>
      <Consumer />
    </AuthProvider>
  );

describe("AuthContext", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("logs in, stores token, and sets user", async () => {
    const token = makeToken({ sub: "alice", role: "FARMER", exp: Math.floor(Date.now() / 1000) + 3600 });
    apiMock.post.mockResolvedValue({ token });

    renderWithProvider();
    await userEvent.click(screen.getByText(/login/i));

    await waitFor(() => expect(apiMock.post).toHaveBeenCalledWith("/auth/login", { username: "alice", password: "secret" }));
    expect(localStorage.getItem("token")).toBe(token);
    expect(screen.getByTestId("user")).toHaveTextContent("alice");
    expect(screen.getByTestId("token")).toHaveTextContent(token);
    expect(toastMock.success).toHaveBeenCalled();
  });

  it("hydrates user from existing token on mount", async () => {
    const token = makeToken({ sub: "bob", role: "RETAILER", exp: Math.floor(Date.now() / 1000) + 3600 });
    localStorage.setItem("token", token);

    renderWithProvider();

    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("bob"));
    expect(screen.getByTestId("token")).toHaveTextContent(token);
  });

  it("logs out and clears state", async () => {
    const token = makeToken({ sub: "alice", role: "FARMER", exp: Math.floor(Date.now() / 1000) + 3600 });
    apiMock.post.mockResolvedValue({ token });

    renderWithProvider();
    await userEvent.click(screen.getByText(/login/i));
    await waitFor(() => expect(screen.getByTestId("user")).toHaveTextContent("alice"));

    await userEvent.click(screen.getByText(/logout/i));

    expect(localStorage.getItem("token")).toBeNull();
    expect(screen.getByTestId("user")).toHaveTextContent("");
    expect(screen.getByTestId("token")).toHaveTextContent("");
    expect(toastMock.success).toHaveBeenCalled();
  });
});
