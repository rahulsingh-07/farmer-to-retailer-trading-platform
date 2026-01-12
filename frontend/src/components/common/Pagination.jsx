// src/components/common/Pagination.jsx
import React from 'react';
import './Pagination.css';

const Pagination = ({ 
  currentPage, 
  totalPages, 
  onPageChange, 
  totalElements,
  pageSize,
  loading = false 
}) => {
  if (totalPages <= 1) return null;

  const getVisiblePages = () => {
    const delta = 2;
    const range = [];
    const rangeWithDots = [];
    let l;

    range.push(0);
    for (let i = Math.max(2, currentPage - delta); i <= Math.min(totalPages - 2, currentPage + delta); i++) {
      range.push(i);
    }
    if (currentPage - delta > 2) {
      range.push(-1);
    }
    range.push(totalPages - 1);
    
    range.forEach((i) => {
      if (i === -1) {
        rangeWithDots.push('...');
      } else {
        rangeWithDots.push(i);
      }
    });

    return rangeWithDots;
  };

  const handlePageClick = (page) => {
    if (page !== '...' && page !== currentPage) {
      onPageChange(page);
    }
  };

  const showingFrom = currentPage * pageSize + 1;
  const showingTo = Math.min((currentPage + 1) * pageSize, totalElements);

  return (
    <div className={`pagination-container ${loading ? 'loading' : ''}`}>
      <div className="pagination-stats">
        Showing {showingFrom}-{showingTo} of {totalElements.toLocaleString()} results
      </div>
      
      <div className="pagination-nav">
        <button
          className="page-btn prev"
          onClick={() => handlePageClick(currentPage - 1)}
          disabled={currentPage === 0 || loading}
        >
          ← Previous
        </button>

        {getVisiblePages().map((page, index) => (
          <button
            key={index}
            className={`page-btn page-num ${page === currentPage ? 'active' : ''} ${page === '...' ? 'dots' : ''}`}
            onClick={() => handlePageClick(page)}
            disabled={page === '...' || loading}
          >
            {page}
          </button>
        ))}

        <button
          className="page-btn next"
          onClick={() => handlePageClick(currentPage + 1)}
          disabled={currentPage === totalPages - 1 || loading}
        >
          Next →
        </button>
      </div>

      <div className="pagination-info">
        Page {currentPage + 1} of {totalPages}
      </div>
    </div>
  );
};

export default Pagination;
