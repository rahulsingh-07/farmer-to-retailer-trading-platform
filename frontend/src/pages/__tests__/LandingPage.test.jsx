import React from "react";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { MemoryRouter } from "react-router-dom";
import { render, screen, fireEvent, cleanup, waitFor } from "@testing-library/react";
import FarmerLandingPage from "../LandingPage";

let originalFetch;

const renderLanding = () =>
  render(
    <MemoryRouter>
      <FarmerLandingPage />
    </MemoryRouter>
  );

describe("LandingPage", () => {
  beforeEach(() => {
    originalFetch = globalThis.fetch;
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
    globalThis.fetch = originalFetch;
  });

  it("renders hero content and CTA buttons", () => {
    renderLanding();

    expect(
      screen.getByRole("heading", { level: 1, name: /Connect\s+.*Farmers.*Directly.*Retailers/i })
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /join as farmer/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /join as retailer/i })).toBeInTheDocument();
  });

  it("loads stats after scrolling and shows fetched totals", async () => {
    const mockResponse = { totalFarmer: 12, totalRetailer: 34 };
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve(mockResponse),
      })
    );

    renderLanding();

    const zeroStats = screen.getAllByText("0");
    expect(zeroStats.length).toBeGreaterThanOrEqual(2);

    // Trigger scroll past threshold to reveal stats and kick off fetch
    Object.defineProperty(globalThis, "scrollY", { value: 400, writable: true });
    fireEvent.scroll(globalThis);

    await waitFor(() => expect(globalThis.fetch).toHaveBeenCalled());
    await waitFor(() => {
      expect(screen.getByText("12")).toBeInTheDocument();
      expect(screen.getByText("34")).toBeInTheDocument();
    });
  });
});
