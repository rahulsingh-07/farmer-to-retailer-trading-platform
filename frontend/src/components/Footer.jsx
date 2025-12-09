// components/Footer.jsx
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Rating from './Rating'; // ⭐ custom rating stars
import { useAuth } from '../context/AuthContext';
import axios from 'axios';
import { toast } from 'react-toastify';
import api from '../utils/api';

const Footer = () => {
  const { user,token } = useAuth();
  const [comment, setComment] = useState('');
  const [rating, setRating] = useState(5);

  const handleSubmit = async (e) => {
  e.preventDefault();
  if (!user || !token) {
    toast.warn("Please login first to submit a review.");
    return;
  }

  try {
    const data = await api.post("/review", { comment, rating }, token);

    setComment('');
    setRating(5);
    toast.success(data.message);
  } catch (err) {
    console.error(err);
    toast.error("Something went wrong while submitting review.");
  }
};


  return (
    <footer className="bg-gray-900 text-white p-6 text-center">
      <div className="max-w-3xl mx-auto">
        <h2 className="text-xl mb-2">We’d love your feedback!</h2>

          <form onSubmit={handleSubmit} className="space-y-3">
            
            <div className="flex justify-center">
              <Rating rating={rating} setRating={setRating} />
            </div>
            <textarea
              value={comment}
              onChange={e => setComment(e.target.value)}
              placeholder="Your comment here..."
              className="w-full p-2 border rounded text-black bg-pink-100"
              required
            />
            <button type="submit" className="bg-blue-500 px-4 py-2 rounded text-white hover:cursor-pointer">
              Submit Feedback
            </button>
          </form>
        <div className="mt-4">
          <Link to="/allreviews" className="text-blue-400 underline">
            View All Reviews
          </Link>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
