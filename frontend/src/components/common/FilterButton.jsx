// FilterButton.jsx - Works with CropList, OrderList, UserList, etc.
import React, { useState, useRef, useEffect } from 'react';
import { FiFilter, FiChevronDown, FiX, FiCheck } from 'react-icons/fi';
import './FilterButton.css';

const FilterButton = ({ 
  filters = {}, // ✅ { category: [{value:'wheat', label:'Wheat', count:12}], location: [...] }
  activeFilters = {}, // ✅ { category: 'wheat', location: 'Aligarh' }
  onFilterChange = () => {}, // ✅ Returns { category: 'wheat', location: 'Aligarh' }
  onClearFilters = () => {},
  className = "",
  position = "right"
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const activeCount = Object.keys(activeFilters).length;

  const handleFilterSelect = (sectionKey, filterValue) => {
    onFilterChange({ [sectionKey]: filterValue });
  };

  const handleClearAll = () => {
    onClearFilters();
    setIsOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className={`filter-button-container ${className}`} ref={dropdownRef}>
      <button
        className={`filter-trigger ${activeCount > 0 ? 'active' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        filter <FiFilter size={18} />
        {activeCount > 0 && (
          <span className="active-badge">{activeCount}</span>
        )}
        <FiChevronDown 
          size={14} 
          className={`transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`} 
        />
      </button>

      {isOpen && (
        <div className={`filter-dropdown ${position}`}>
          <div className="filter-header">
            <div className="header-left">
              <FiFilter size={16} className="filter-icon" />
              <span className="header-title">Filters</span>
            </div>
            <div className="header-right">
              {activeCount > 0 && (
                <button onClick={handleClearAll} className="clear-btn">
                  Clear All
                </button>
              )}
              <FiX 
                size={14} 
                className="close-btn"
                onClick={() => setIsOpen(false)}
              />
            </div>
          </div>

          <div className="filter-content">
            {Object.entries(filters).map(([sectionKey, sectionItems]) => (
              <div key={sectionKey} className="filter-group">
                <h5 className="group-title">
                  {sectionKey.charAt(0).toUpperCase() + sectionKey.slice(1)}
                </h5>
                
                <div className="filter-options">
                  {sectionItems.map((item) => (
                    <label key={item.value} className="filter-option">
                      <input
                        type="radio"
                        name={sectionKey}
                        value={item.value}
                        checked={activeFilters[sectionKey] === item.value}
                        onChange={() => handleFilterSelect(sectionKey, item.value)}
                        className="sr-only"
                      />
                      <div className="option-content">
                        <span className="option-label">{item.label}</span>
                        {item.count !== undefined && (
                          <span className="option-count">({item.count})</span>
                        )}
                        {activeFilters[sectionKey] === item.value && (
                          <FiCheck size={16} className="check-icon" />
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </div>
            ))}
          </div>

          <div className="filter-footer">
            <button 
              className="done-btn"
              onClick={() => setIsOpen(false)}
            >
              Done
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FilterButton;
