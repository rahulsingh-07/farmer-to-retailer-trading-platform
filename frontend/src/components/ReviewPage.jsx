import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import api from '../utils/api';
import { toast } from 'react-toastify';


const ReviewPage = () => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
   const [pageInfo, setPageInfo] = useState({
    currentPage: 0,
    totalPages: 1,
    first: true,
    last: false,
  });

  const fetchReviews = async (page) => {
    try {
      const res = await api.get(`/public/reviews?page=${page}&size=5`);
        const data = res;
        setReviews(data.content);
        setPageInfo({
          currentPage: data.pageable.pageNumber,
          totalPages: data.totalPages,
          first: data.first,
          last: data.last,
        }); 
    } catch (err) {
      
      toast.error("something went wrong");
    } finally {
      setLoading(false);
    }
  };

 
useEffect(() => {
   window.scrollTo({ top: 0, behavior: 'smooth' });
    fetchReviews(pageInfo.currentPage);
  }, [pageInfo.currentPage]);

  const renderStars = (rating) => {
    return [...Array(5)].map((_, i) => (
      <span key={i}>{i < rating ? "★" : "☆"}</span>
    ));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-100 to-blue-100 py-10 px-4">
      <div className="max-w-3xl mx-auto text-center mb-10">
        <h1 className="text-3xl font-bold text-gray-800 mb-2">Your Feedback Matters ❤️</h1>
        <p className="text-gray-600">We value your reviews and constantly strive to improve.</p>
      </div>

      {loading ? (
        <div className="text-center mt-20">
          <div className="w-12 h-12 border-4 border-purple-300 border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p className="mt-4 text-purple-600">Loading reviews...</p>
        </div>
      ) : (
        <div className="grid gap-6 max-w-4xl mx-auto px-4 py-8">
          {reviews.length === 0 ? (
            <div className="text-center text-gray-500 text-lg">
              No reviews yet. Be the first one!
            </div>

          ) : (

            reviews.map((r, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.1 }}
                className="bg-white rounded-2xl shadow-md hover:shadow-xl transition-shadow duration-300 p-6"
              >
                <div className="flex items-center gap-1 text-yellow-400 mb-3">
                  {renderStars(r.rating)}
                </div>
                <p className="text-gray-800 italic mb-3">“{r.comment}”</p>
                <p className="text-sm text-gray-500 text-right">– {r.username}</p>
              </motion.div>
            ))
          )}
        </div>
      )}
      <div className="join flex justify-center mt-6">
        <button className="join-item btn"  onClick={() =>
      setPageInfo((prev) => ({
        ...prev,
        currentPage: prev.currentPage - 1,
      }))
    }
          disabled={pageInfo.first}>«</button>
        <button className="join-item btn">Page {pageInfo.currentPage + 1}</button>
        <button className="join-item btn"  onClick={() =>
      setPageInfo((prev) => ({
        ...prev,
        currentPage: prev.currentPage + 1,
      }))
    }
          disabled={pageInfo.last}>»</button>
      </div>
    </div>
  );
};



export default ReviewPage;