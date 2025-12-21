// CropMarketplace.jsx - Flipkart-style marketplace
import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import FiltersPanel from "./FiltersPanel";
import CropCard from "./CropCard";
import "../../css/CropMarketplace.css";

const CropMarketplace = () => {
  const { user, token } = useAuth();

  const [crops, setCrops] = useState([]);
  const [filters, setFilters] = useState({
    category: "",
    location: "",
    variety: "",
    page: 0,
  });
  const [pageMeta, setPageMeta] = useState({
    totalPages: 0,
    totalElements: 0,
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCrops();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const fetchCrops = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();

      if (filters.category) params.append("category", filters.category);
      if (filters.location) params.append("location", filters.location);
      if (filters.variety) params.append("variety", filters.variety);

      params.append("page", filters.page || 0);
      params.append("size", 12);

      const data = await api.get(`/public/crops?${params.toString()}`); // public endpoint returns JSON directly

      const rawContent = data?.content || [];
      const normalized = rawContent.map((item) => {
        let images = [];
        if (item.images) {
          images = item.images;
        } else if (Array.isArray(item.imageUrl)) {
          images = item.imageUrl.map((img) => img.imageUrl);
        } else if (item.imageUrl) {
          images = [item.imageUrl];
        }

        const status = item.status || item.auctionStatus;

        return {
          ...item,
          images,
          status,
        };
      });

      setCrops(normalized);
      setPageMeta({
        totalPages: data?.totalPages || 0,
        totalElements: data?.totalElements || normalized.length,
      });
    } catch (err) {
      toast.error(err.message || "Failed to load crops");
    } finally {
      setLoading(false);
    }
  };

  const changePage = (nextPage) => {
    setFilters((prev) => ({
      ...prev,
      page: Math.max(0, nextPage),
    }));
  };

  let gridContent;
  if (loading) {
    gridContent = <div className="loading-skeleton">Loading crops...</div>;
  } else if (crops.length === 0) {
    gridContent = <div className="empty-state">No crops found. Adjust filters.</div>;
  } else {
    gridContent = crops.map((crop) => (
      <CropCard key={crop.id} crop={crop} user={user} token={token} />
    ));
  }

  return (
    <div className="marketplace-container">
      <FiltersPanel filters={filters} setFilters={setFilters} />

      <div className="marketplace-grid">{gridContent}</div>

      {pageMeta.totalPages > 1 && (
        <div className="pagination">
          <button
            type="button"
            disabled={filters.page <= 0}
            onClick={() => changePage(0)}
          >
            First
          </button>
          <button
            type="button"
            disabled={filters.page <= 0}
            onClick={() => changePage(filters.page - 1)}
          >
            Prev
          </button>
          <span>
            Page {Number(filters.page || 0) + 1} of {pageMeta.totalPages}
          </span>
          <button
            type="button"
            disabled={filters.page >= pageMeta.totalPages - 1}
            onClick={() => changePage((filters.page || 0) + 1)}
          >
            Next
          </button>
          <button
            type="button"
            disabled={filters.page >= pageMeta.totalPages - 1}
            onClick={() => changePage(pageMeta.totalPages - 1)}
          >
            Last
          </button>
        </div>
      )}
    </div>
  );
};

export default CropMarketplace;
