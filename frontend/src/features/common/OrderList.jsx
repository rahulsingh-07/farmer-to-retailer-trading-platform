import { useState, useEffect, useCallback, useRef } from "react";
import BackBtn from "../../components/common/BackBtn";
import FilterButton from "../../components/common/FilterButton";
import OrderCard from "./OrderCard";
import Pagination from "../../components/common/Pagination";
import { getAllRetailerOrders } from "../retailer/retailerService";
import { getAllFarmerOrders } from "../farmer/farmerService";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import "./OrderList.css";

export default function OrderList() {
  const { user } = useAuth();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  const [filters, setFilters] = useState({
    status: "",
    page: 0,
    size: 12,
  });

  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // used to prevent race-condition updates
  const requestIdRef = useRef(0);

  /* ---------------- FILTER OPTIONS ---------------- */
  const filterOptions = {
    status: [
      { value: "", label: "All Orders" },
      { value: "PENDING", label: "Pending" },
      { value: "CONFIRMED", label: "Confirmed" },
      { value: "PAID", label: "Paid" },
      { value: "SHIPPED", label: "Shipped" },
      { value: "DELIVERED", label: "Delivered" },
    ],
  };

  /* ---------------- FETCH ORDERS ---------------- */
  const fetchOrders = useCallback(async () => {
    if (!user) return;

    const requestId = ++requestIdRef.current;
    setLoading(true);

    try {
      let data;

      if (user.role === "RETAILER" || user.username?.startsWith("RETL")) {
        data = await getAllRetailerOrders(filters);
      } else {
        data = await getAllFarmerOrders(filters);
      }

      // Ignore outdated responses
      if (requestId !== requestIdRef.current) return;

      setOrders(Array.isArray(data?.content) ? data.content : []);
      setTotalPages(data?.totalPages ?? 0);
      setTotalElements(data?.totalElements ?? 0);
    } catch (err) {
      toast.error(err.message || "Failed to load orders");
    } finally {
      if (requestId === requestIdRef.current) {
        setLoading(false);
      }
    }
  }, [filters, user]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);
  
  /* ---------------- HANDLERS ---------------- */
  const handleFilterChange = useCallback((newFilters) => {
    setFilters((prev) => {
      const next = { ...prev, ...newFilters, page: 0 };
      return JSON.stringify(prev) === JSON.stringify(next) ? prev : next;
    });
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilters({ status: "", page: 0, size: 12 });
  }, []);

  const handlePageChange = useCallback((page) => {
    setFilters((prev) => ({ ...prev, page }));
  }, []);

  /* ---------------- FIRST LOAD SKELETON ---------------- */
  if (loading && orders.length === 0) {
    return (
      <div className="order-loading">
        <div className="skeleton-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="order-card-skeleton" />
          ))}
        </div>
      </div>
    );
  }

  /* ---------------- RENDER ---------------- */
  return (
    <>
      <nav className="nav-bar">
        <BackBtn />

        <FilterButton
          filters={filterOptions}
          activeFilters={{ status: filters.status }}
          onFilterChange={handleFilterChange}
          onClearFilters={handleClearFilters}
        />
      </nav>

      <div className={`order-grid ${loading ? "loading-overlay" : ""}`}>
        {orders.length > 0 ? (
          orders.map((order) => (
            <OrderCard key={order.orderId} order={order} />
          ))
        ) : (
          <div className="no-orders">
            <p>
              No orders{" "}
              {filters.status ? `with status "${filters.status}"` : ""} found
            </p>
            <button
              onClick={handleClearFilters}
              className="clear-filters-btn"
            >
              Clear Filters
            </button>
          </div>
        )}
      </div>

      <Pagination
        currentPage={filters.page}
        totalPages={totalPages}
        totalElements={totalElements}
        pageSize={filters.size}
        onPageChange={handlePageChange}
        loading={loading}
      />
    </>
  );
}
