import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { MemoryRouter } from "react-router-dom";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Login from "../Login";

let mockLogin;
let mockLogout;
let mockUser;
let mockNavigate;

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("react-toastify", () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

vi.mock("../../context/AuthContext", () => ({
  useAuth: () => ({
    login: mockLogin,
    logout: mockLogout,
    user: mockUser,
  }),
}));

const renderLogin = () => render(
  <MemoryRouter>
    <Login />
  </MemoryRouter>
);

describe("Login", () => {
  beforeEach(() => {
    mockLogin = vi.fn();
    mockLogout = vi.fn();
    mockUser = null;
    mockNavigate = vi.fn();
  });

  afterEach(() => {
    cleanup();
  });

  it("shows validation errors when fields are empty", async () => {
    renderLogin();
    await userEvent.click(screen.getByRole("button", { name: /sign in/i }));

    expect(screen.getByText(/Username is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Password is required/i)).toBeInTheDocument();
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it("submits and navigates on successful login", async () => {
    mockLogin.mockResolvedValue({});
    renderLogin();

    const [userInput] = screen.getAllByPlaceholderText(/enter your username/i);
    const [passInput] = screen.getAllByPlaceholderText(/enter your password/i);
    await userEvent.type(userInput, "alice");
    await userEvent.type(passInput, "secret123");
    await userEvent.click(screen.getByRole("button", { name: /sign in/i }));

    expect(mockLogin).toHaveBeenCalledWith({ username: "alice", password: "secret123" });
    expect(mockNavigate).toHaveBeenCalledWith("/dashboard");
  });

  it("renders logged-in view and logs out", async () => {
    mockUser = { username: "alice" };
    renderLogin();

    expect(
      screen.getByRole("heading", { level: 2, name: /Welcome Back,\s*alice/i })
    ).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: /logout/i }));
    expect(mockLogout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith("/");
  });
});
