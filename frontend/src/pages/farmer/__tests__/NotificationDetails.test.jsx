import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import NotificationDetails from "../NotificationDetails";

var mockGet;

vi.mock("../../../context/AuthContext", () => ({
  useAuth: () => ({ token: "test-token" }),
}));

vi.mock("../../../utils/api", () => {
  mockGet = vi.fn();
  return {
    default: { get: mockGet },
  };
});

const renderWithRouter = (notification) => {
  return render(
    <MemoryRouter
      initialEntries={[
        {
          pathname: `/farmer/notifications/${notification.notificationId || notification.id}`,
          state: { notification },
        },
      ]}
    >
      <Routes>
        <Route path="/farmer/notifications/:id" element={<NotificationDetails />} />
      </Routes>
    </MemoryRouter>
  );
};

const baseNotification = {
  id: "notif-1",
  notificationId: "notif-1",
  message: "Bid won",
  read: false,
  createdAt: "2024-10-01T10:00:00Z",
  type: "BID_WON",
  status: "CLOSED",
  auctionId: "AU-123",
  cropName: "Test Crop",
  category: "Grain",
  variety: "Premium",
  quantity: 50,
  unit: "kg",
  imageUrl: ["https://example.com/img.jpg"],
  bidderFullName: "John Doe",
  bidderPhoneNumber: "1234567890",
  bidderAddress: "Farm Lane",
};

describe("NotificationDetails", () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  afterEach(() => {
    cleanup();
  });

  it("renders crop and bidder info", async () => {
    mockGet.mockResolvedValue(baseNotification);
    renderWithRouter(baseNotification);

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Test Crop/i })).toBeInTheDocument();
    });
    expect(screen.getByText(/John Doe/i)).toBeInTheDocument();
    expect(screen.getByText(/Bid won/i)).toBeInTheDocument();
  });

  it("enables Sell when status is not ACTIVE", async () => {
    mockGet.mockResolvedValue({ ...baseNotification, status: "CLOSED" });
    renderWithRouter({ ...baseNotification, status: "CLOSED" });

    const sellButton = await screen.findByRole("button", { name: /sell/i });
    expect(sellButton).toBeEnabled();
  });

  it("disables Sell when status is ACTIVE and not BID_WON", async () => {
    mockGet.mockResolvedValue({ ...baseNotification, status: "ACTIVE", type: "BID_PLACED" });
    renderWithRouter({ ...baseNotification, status: "ACTIVE", type: "BID_PLACED" });

    const sellButton = await screen.findByRole("button", { name: /sell/i });
    expect(sellButton).toBeDisabled();
  });
});
