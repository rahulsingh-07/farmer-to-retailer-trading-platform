import { useState } from 'react';
import Carousel from 'react-bootstrap/Carousel';
function AppCarousel({ slides }) {
  const [index, setIndex] = useState(0);

  const handleSelect = (selectedIndex) => {
    setIndex(selectedIndex);
  };

  return (
    <Carousel activeIndex={index} onSelect={handleSelect}>
      {slides.map((slide, i) => (
        <Carousel.Item key={i}>
          <img
            className="d-block w-100"
            src={slide.image}
            alt={slide.title}
            style={{ height: '400px', objectFit: 'cover' }}
          />

          <Carousel.Caption>
            <h3>{slide.title}</h3>
            <p>{slide.description}</p>
          </Carousel.Caption>
        </Carousel.Item>
      ))}
    </Carousel>
  );
}

export default AppCarousel;
