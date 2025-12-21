import { describe, it, expect, vi, afterEach, beforeEach } from "vitest";
import api from "../api";

const mockFetch = (payload) => {
  global.fetch = vi.fn().mockResolvedValue({
    ok: payload.ok ?? true,
    statusText: payload.statusText ?? "OK",
    text: vi.fn().mockResolvedValue(payload.text ?? ""),
  });
};

describe("api util", () => {
  const token = "abc123";

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("performs GET with bearer token and parses JSON", async () => {
    mockFetch({ text: JSON.stringify({ hello: "world" }) });

    const result = await api.get("/foo", token);

    expect(result).toEqual({ hello: "world" });
    expect(global.fetch).toHaveBeenCalledWith("http://localhost:8081/foo", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
  });

  it("throws with message from error payload", async () => {
    mockFetch({ ok: false, text: JSON.stringify({ message: "boom" }) });

    await expect(api.post("/err", { a: 1 }, token)).rejects.toThrow("boom");
  });

  it("returns empty object for empty body", async () => {
    mockFetch({ text: "" });

    const result = await api.patch("/empty", { a: 1 }, token);
    expect(result).toEqual({});
  });

  it("passes FormData without content-type for upload and returns raw text", async () => {
    mockFetch({ text: "plain-text" });
    const formData = new FormData();
    formData.append("file", new Blob(["data"], { type: "text/plain" }), "a.txt");

    const result = await api.upload("/upload", formData, token);

    expect(result).toBe("plain-text");
    expect(global.fetch).toHaveBeenCalledWith("http://localhost:8081/upload", expect.objectContaining({
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    }));
  });

  it("falls back to statusText when error payload has no message", async () => {
    mockFetch({ ok: false, text: "failure", statusText: "Bad Request" });

    await expect(api.delete("/del", token)).rejects.toThrow("Bad Request");
  });
});
