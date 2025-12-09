
// components/Rating.jsx
const Rating = ({ rating, setRating }) => {
  return (
    <div className="rating">
      {[1, 2, 3, 4, 5].map((num) => (
        <input
          key={num}
          type="radio"
          name="rating-2"
          className="mask mask-star-2 bg-orange-400"
          aria-label={`${num} star`}
          checked={rating === num}
          onChange={() => setRating(num)}
        />
      ))}
    </div>
  );
};

export default Rating;
