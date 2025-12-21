import React from "react";
import { describe, it, expect, vi, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "../App";

vi.mock("../components/Navbar", () => ({ __esModule: true, default: () => <div data-testid="navbar" /> }));
vi.mock("../pages/LandingPage", () => ({ __esModule: true, default: () => <div data-testid="landing" /> }));
vi.mock("../auth/Login", () => ({ __esModule: true, default: () => <div data-testid="login" /> }));
vi.mock("../components/PrivateRoute", () => ({
  __esModule: true,
  default: ({ children }) => <div data-testid="private-route">{children}</div>,
}));
vi.mock("../components/Sidebar", () => ({ __esModule: true, default: () => <div data-testid="sidebar" /> }));
vi.mock("../auth/RoleDashboard", () => ({ __esModule: true, default: () => <div data-testid="role-dashboard" /> }));

describe("App routing", () => {
  afterEach(() => {
    cleanup();
  });

  const renderApp = (initialPath) =>
    render(
      <MemoryRouter initialEntries={[initialPath]}>
        <App />
      </MemoryRouter>
    );

  it("renders landing page with navbar on root path", () => {
    renderApp("/");

    expect(screen.getByTestId("navbar")).toBeInTheDocument();
    expect(screen.getByTestId("landing")).toBeInTheDocument();
  });

  it("renders login page route", () => {
    renderApp("/login");

    expect(screen.getByTestId("navbar")).toBeInTheDocument();
    expect(screen.getByTestId("login")).toBeInTheDocument();
  });

  it("renders dashboard inside private route with sidebar", () => {
    renderApp("/dashboard");

    expect(screen.getByTestId("private-route")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar")).toBeInTheDocument();
    expect(screen.getByTestId("role-dashboard")).toBeInTheDocument();
  });
});
