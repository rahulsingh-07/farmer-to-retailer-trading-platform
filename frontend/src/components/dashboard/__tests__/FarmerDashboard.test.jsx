import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import FarmerDashboard from "../FarmerDashboard";

const apiMock = vi.hoisted(() => ({ get: vi.fn() }));
vi.mock("../../../utils/api", () => ({ __esModule: true, default: apiMock }));

vi.mock("../../../context/AuthContext", () => ({
  useAuth: () => ({ user: { username: "Ravi" }, token: "test-token" }),
}));

describe("FarmerDashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  const renderDashboard = () =>
    render(
      <MemoryRouter>
        <FarmerDashboard />
      </MemoryRouter>
    );

  it("renders stats from API response", async () => {
    apiMock.get
      .mockResolvedValueOnce({
        totalCrops: 5,
        totalActiveOrders: 2,
        totalActiveAuction: 1,
        totalPendingOrders: 3,
        totalWaitingPayment: 4,
        totalShippedOrders: 6,
        totalCompletedDelivery: 7,
      })
      .mockResolvedValueOnce({
        content: [
          { category: "Wheat", pricePerUnit: 100 },
          { category: "Corn", pricePerUnit: 120 },
          { category: "Corn", pricePerUnit: 80 },
        ],
      });

    renderDashboard();

    await waitFor(() => {
      expect(apiMock.get).toHaveBeenCalledWith("/farmer/totalCrops", "test-token");
    });

    expect(await screen.findByText("5")).toBeInTheDocument(); // total crops card value
    expect(screen.getByText(/Active auctions/i)).toBeInTheDocument();
    expect(screen.getByText(/Pending Orders/i)).toBeInTheDocument();
    expect(await screen.findByText(/13 deliveries/i)).toBeInTheDocument(); // 6 + 7
  });

  it("shows market signals from public crops", async () => {
    apiMock.get
      .mockResolvedValueOnce({
        totalCrops: 0,
        totalActiveOrders: 0,
        totalActiveAuction: 0,
        totalPendingOrders: 0,
        totalWaitingPayment: 0,
        totalShippedOrders: 0,
        totalCompletedDelivery: 0,
      })
      .mockResolvedValueOnce({
        content: [
          { category: "Barley", pricePerUnit: 50 },
          { category: "Barley", pricePerUnit: 70 },
          { category: "Rice", pricePerUnit: 90 },
        ],
      });

    renderDashboard();

    await waitFor(() => expect(apiMock.get).toHaveBeenCalledTimes(2));
    const barleyTexts = await screen.findAllByText(/Barley/);
    expect(barleyTexts.length).toBeGreaterThan(0);
    expect(screen.getAllByText(/Rice/).length).toBeGreaterThan(0);
  });

  it("falls back to placeholder when no signals", async () => {
    apiMock.get
      .mockResolvedValueOnce({ totalCrops: 0 })
      .mockResolvedValueOnce({ content: [] });

    renderDashboard();

    const loadingTexts = await screen.findAllByText(/Loading/);
    expect(loadingTexts.length).toBeGreaterThan(0);
  });
});
