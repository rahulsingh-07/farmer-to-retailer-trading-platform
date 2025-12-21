import React from "react";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PendingUsersTable from "../PendingUsersTable";

const toastMock = vi.hoisted(() => ({ success: vi.fn(), error: vi.fn() }));
vi.mock("react-toastify", () => ({ toast: toastMock }));

describe("PendingUsersTable", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("token", "test-token");
    vi.clearAllMocks();
    globalThis.fetch = vi.fn();
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it("fetches and displays pending users", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        content: [
          {
            userId: 1,
            fullName: "Jane Doe",
            email: "jane@example.com",
            phoneNumber: "1234567890",
            status: "PENDING",
          },
        ],
        number: 0,
        totalPages: 1,
        totalElements: 1,
        size: 10,
      }),
    });

    render(<PendingUsersTable />);

    expect(screen.getByText(/loading users/i)).toBeInTheDocument();

    await screen.findByText("Jane Doe");

    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/admin/pendingUsers?page=0&size=10"),
      expect.objectContaining({
        method: "GET",
        headers: expect.objectContaining({ Authorization: "Bearer test-token" }),
      })
    );

    expect(screen.getByText("Jane Doe")).toBeInTheDocument();
    expect(screen.getByText("PENDING")).toBeInTheDocument();
  });

  it("approves a user and refreshes the list", async () => {
    fetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          content: [
            {
              userId: 1,
              fullName: "Jane Doe",
              email: "jane@example.com",
              phoneNumber: "1234567890",
              status: "PENDING",
            },
          ],
          number: 0,
          totalPages: 1,
          totalElements: 1,
          size: 10,
        }),
      })
      .mockResolvedValueOnce({ ok: true, json: async () => ({}) })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          content: [],
          number: 0,
          totalPages: 1,
          totalElements: 0,
          size: 10,
        }),
      });

    render(<PendingUsersTable />);

    await screen.findByText("Jane Doe");

    const [approveBtn] = screen.getAllByRole("button", { name: /approve/i });
    await userEvent.click(approveBtn);

    await waitFor(() => expect(fetch).toHaveBeenCalledTimes(3));
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining("/status?status=ACTIVE"),
      expect.objectContaining({ method: "PATCH" })
    );
    expect(toastMock.success).toHaveBeenCalled();
  });
});
