import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { toast } from 'react-toastify';
import CropCard from './CropCard';
import BackBtn from '../../../components/common/BackBtn';
import FilterButton from '../../../components/common/FilterButton';
import Pagination from '../../../components/common/Pagination';
import { getFilteredCrops } from '../../retailer/retailerService';
import { getMyCrops } from '../../farmer/farmerService';
import { useAuth } from '../../../context/AuthContext';
import './CropList.css';

export default function CropList() {
  const { user } = useAuth()
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    cropType: '',
    category: '',
    variety: '',
    page: 0,
    size: 12
  });
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Backend filter options (you can fetch these dynamically)
  const filterOptions = useMemo(() => ({
    cropType:[
      {value:'AUCTION',label:'Bid'},
      {value:'FIXED',label:'Fixed Price'}
    ],
    category: [
      { value: 'Cereals', label: 'Cereals' },
      { value: 'Pulses', label: 'Pulses'},
      { value: 'Vegetables', label: 'Vegetables'},
      { value: 'Fruits', label: 'Fruits' },
      { value: 'Oilseeds', label: 'Oilseeds' },
      { value: 'Spices', label: 'Spices' },
      { value: 'Others', label: 'Others' }
    ],
    variety: [
      { value: 'Sharbati', label: 'Sharbati' },
      { value: 'Pusa 1121', label: 'Pusa 1121' },
      { value: 'Desi', label: 'Desi Variety' }
    ]
  }), []);


  const fetchCrops = useCallback(async () => {
  if (!user) {
    setLoading(false);
    return;
  }

  setLoading(true);
  try {
    const data =
      user.role === 'RETAILER' || user.username?.startsWith('RETL')
        ? await getFilteredCrops(filters)
        : await getMyCrops(filters);
    setCrops(Array.isArray(data.content) ? data.content : []);
    setTotalPages(data.totalPages ?? 0);
    setTotalElements(data.totalElements ?? 0);

  } catch (err) {
    toast.error(err?.message || 'Failed to load crops');
  } finally {
    setLoading(false);
  }
}, [filters, user]);

  useEffect(() => {
    fetchCrops();
  }, [fetchCrops]);

  // Filter handlers
  const handleFilterChange = useCallback((newFilters) => {
    setFilters(prev => ({ ...prev, ...newFilters, page: 0 }));
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilters({ cropType:'',category: '', variety: '', page: 0, size: 12 });
  }, []);


  // Pagination handlers
  const handlePageChange = useCallback((page) => {
    setFilters(prev => ({ ...prev, page }));
  }, []);

  

  if (loading && crops.length === 0) {
    return (
      <div className="crop-loading">
        <div className="skeleton-grid">
          {[1,2,3,4,5,6].map(i => (
            <div key={i} className="crop-card-skeleton" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <>
      <nav className="nav-bar">
        <div>
          <BackBtn />
        </div>
        {/* <SearchButton onSearch={handleSearch} /> */}
        <FilterButton 
          filters={filterOptions}
          activeFilters={{
            cropType: filters.cropType,
            category: filters.category,
            variety: filters.variety
          }}
          onFilterChange={handleFilterChange}
          onClearFilters={handleClearFilters}
        />
      </nav>

      <div className="crop-grid">
        {crops.length > 0 ? (
          crops.map((crop) => (
            <CropCard 
              key={crop.id} 
              crop={{
                ...crop,
                image: crop.images && crop.images.length > 0 ? crop.images[0] : null
              }} 
            />
          ))
        ) : (
          <div className="no-crops">
            <p>No crops found matching your filters</p>
            <button onClick={handleClearFilters} className="clear-filters-btn">
              Clear All Filters
            </button>
          </div>
        )}
      </div>

      {/* ✅ Universal Pagination */}
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
