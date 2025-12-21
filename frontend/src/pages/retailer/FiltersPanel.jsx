import React from "react";
import PropTypes from "prop-types";
// FiltersPanel.jsx
const FiltersPanel = ({ filters, setFilters }) => {
  const categories = ["Cereals", "Pulses", "Vegetables", "Fruits", "Oilseeds", "Spices"];

  const updateField = (key, value) => setFilters((prev) => ({ ...prev, [key]: value, page: 0 }));

  const clearAll = () => setFilters({ category: "", location: "", variety: "", page: 0 });

  return (
    <div className="filters-panel">
      <h2>🥬 Find Fresh Crops</h2>
      <div className="filter-group">
        <select
          value={filters.category || ""}
          onChange={(e) => updateField("category", e.target.value)}
          className="filter-select bg-amber-300"
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>

        <input
          placeholder="Location"
          value={filters.location || ""}
          onChange={(e) => updateField("location", e.target.value)}
          className="filter-select"
        />

        <input
          placeholder="Variety"
          value={filters.variety || ""}
          onChange={(e) => updateField("variety", e.target.value)}
          className="filter-select"
        />

        <button className="clear-filters" type="button" onClick={clearAll}>
          Clear All
        </button>
      </div>
    </div>
  );
};

export default FiltersPanel;

FiltersPanel.propTypes = {
  filters: PropTypes.shape({
    category: PropTypes.string,
    location: PropTypes.string,
    variety: PropTypes.string,
    page: PropTypes.number,
  }).isRequired,
  setFilters: PropTypes.func.isRequired,
};