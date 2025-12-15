import React from "react";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, waitFor, within } from "@testing-library/react";

vi.mock("../../../context/AuthContext", () => ({
  __esModule: true,
  useAuth: () => ({ user: { username: "adminuser" } }),
}));

vi.mock("../../../pages/admin/PendingUsersTable", () => ({
  __esModule: true,
  default: () => <div data-testid="pending-table-mock" />,
}));

import AdminDashboard from "../AdminDashboard";

describe("AdminDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("fetches and displays stats", async () => {
    fetch.mockResolvedValue({
      ok: true,
      json: async () => ({
        totalPending: 2,
        totalUsers: 10,
        totalAdmin: 3,
      }),
    });

    render(<AdminDashboard />);

    await waitFor(() => expect(fetch).toHaveBeenCalled());
    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8081/admin/totalUsers",
      expect.objectContaining({
        method: "GET",
        headers: expect.objectContaining({ Authorization: expect.any(String) }),
      })
    );

    const getStatCardByLabel = (label) => {
      const labelNode = screen
        .getAllByText(label)
        .find((el) => el.classList.contains("stat-label"));
      return labelNode?.closest(".stat-card");
    };

    const pendingCard = getStatCardByLabel("Pending Users");
    const totalUsersCard = getStatCardByLabel("Total Users");
    const adminsCard = getStatCardByLabel("Admins");

    expect(within(pendingCard).getByText("2")).toBeInTheDocument();
    expect(within(totalUsersCard).getByText("10")).toBeInTheDocument();
    expect(within(adminsCard).getByText("3")).toBeInTheDocument();
    expect(screen.getByText("adminuser")).toBeInTheDocument();
    expect(screen.getByTestId("pending-table-mock")).toBeInTheDocument();
  });
});
