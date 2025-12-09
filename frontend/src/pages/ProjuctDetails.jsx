import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../utils/api'; // Axios helper

const ProductDetails = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);

  const fetchProduct = async () => {
    try {
      const res = await api.get(`/public/${id}`);
      setProduct(res); // adjust if wrapped in res.data
    } catch (err) {
      console.error('Error fetching product:', err.message);
    }
  };

  useEffect(() => {
    fetchProduct();
  }, [id]);

  if (!product) return <div className="text-center mt-10 text-lg">Loading...</div>;

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="flex flex-col md:flex-row bg-white rounded-xl shadow-lg overflow-hidden">
        
        {/* Left: Image */}
        <div className="md:w-1/2 bg-gray-100 flex items-center justify-center p-4">
          <img
            src={product.imageUrl || "https://img.daisyui.com/images/stock/photo-1606107557195-0e29a4b5b4aa.webp"}
            alt={product.productName}
            className="object-contain max-h-[400px] w-full rounded-md"
          />
        </div>

        {/* Right: Info */}
        <div className="md:w-1/2 p-6 flex flex-col justify-center">
          <h2 className="text-3xl font-semibold mb-4 text-gray-800">{product.productName}</h2>
          <p className="text-xl text-green-600 font-bold mb-2">₹{product.price}</p>
          <p className="text-gray-700 mb-3">{product.description || "No description available."}</p>
          <p className="text-sm text-gray-500">Owner: <span className="font-medium text-gray-700">{product.ownerEmail}</span></p>
          <p>Email the owner to buy the item.</p>
        </div>
      </div>
    </div>
  );
};

export default ProductDetails;
