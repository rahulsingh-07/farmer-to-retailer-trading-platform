import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import api from '../utils/api';

const SearchResults = () => {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q');
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchSearchResults = async () => {
    try {
      const data = await api.get(`/public/search?q=${query}&page=${page}&size=6`);
      const items = data._embedded?.productResponseDTOList || [];
      setProducts(items);
      setTotalPages(data.page?.totalPages || 1);
    } catch (err) {
      console.error('Search failed:', err);
    }
  };

  useEffect(() => {
    if (query) {
      fetchSearchResults();
    }
  }, [query, page]);

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Search Results for "{query}"</h2>
      {products.length === 0 ? (
        <p>No products found.</p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {products.map((product, index) => (
              <div key={index} className="card bg-base-100 shadow-md">
                <figure>
                  <img src={product.imageUrl} className="w-full h-48 object-cover" alt={product.productName} />
                </figure>
                <div className="card-body">
                  <h2 className="card-title">{product.productName}</h2>
                  <p>₹{product.price}</p>
                  <div className="card-actions justify-end">
                    <Link to={`/productdetails/${product.productId}`} className="btn btn-primary">View</Link>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="join flex justify-center mt-6">
            <button className="join-item btn" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>«</button>
            <button className="join-item btn">Page {page + 1}</button>
            <button className="join-item btn" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page + 1 >= totalPages}>»</button>
          </div>
        </>
      )}
    </div>
  );
};

export default SearchResults;
