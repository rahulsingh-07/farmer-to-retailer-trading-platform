import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import RetailerDashboard from "../RetailerDashboard";

const apiMock = vi.hoisted(() => ({ get: vi.fn() }));
vi.mock("../../../utils/api", () => ({ __esModule: true, default: apiMock }));

vi.mock("../../../context/AuthContext", () => ({
  useAuth: () => ({ user: { username: "Rita" }, token: "retailer-token" }),
}));

describe("RetailerDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  const renderDashboard = () =>
    render(
      <MemoryRouter>
        <RetailerDashboard />
      </MemoryRouter>
    );

  it("renders dashboard stats and notifications from API", async () => {
    apiMock.get
      .mockResolvedValueOnce({
        totalCrops: 0,
        totalActiveOrders: 0,
      })
      .mockResolvedValueOnce({
        confirmed: 4,
        shipped: 3,
        needConfirmation: 2,
        notifications: 5,
      })
      .mockResolvedValueOnce({ content: [] });

    renderDashboard();

    await waitFor(() => expect(apiMock.get).toHaveBeenCalledWith("/user/retailer/dashboard", "retailer-token"));

    expect(await screen.findByText(/9 orders/)).toBeInTheDocument();
    expect(screen.getByText(/2 need confirmation/)).toBeInTheDocument();
    expect(screen.getByText(/Unread alerts/).parentElement?.querySelector(".rd-card-value")?.textContent).toBe("5");
  });

  it("requests supplier signals and renders the market grid", async () => {
    apiMock.get.mockImplementation((path) => {
      if (path === "/public/crops") {
        return Promise.resolve({
          content: [
            { category: "Potato", pricePerUnit: 40 },
            { category: "Potato", pricePerUnit: 60 },
            { category: "Onion", pricePerUnit: 50 },
          ],
        });
      }
      if (path === "/user/retailer/dashboard") {
        return Promise.resolve({
          confirmed: 0,
          shipped: 0,
          needConfirmation: 0,
          notifications: 0,
        });
      }
      return Promise.resolve({});
    });

    renderDashboard();

    await waitFor(() => expect(apiMock.get).toHaveBeenCalledWith("/public/crops"));
    expect(await screen.findByText(/Supplier pulse/i)).toBeInTheDocument();
  });

  it("shows loading placeholder when no signals", async () => {
    apiMock.get
      .mockResolvedValueOnce({ totalCrops: 0 })
      .mockResolvedValueOnce({ confirmed: 0, shipped: 0, needConfirmation: 0, notifications: 0 })
      .mockResolvedValueOnce({ content: [] });

    renderDashboard();

    const loadingSignals = await screen.findAllByText(/Loading/);
    expect(loadingSignals.length).toBeGreaterThan(0);
  });
});
