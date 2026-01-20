import { useState } from 'react';
import Carousel from 'react-bootstrap/Carousel';
function AppCarousel({ slides, height = '400px', showCaptions = true }) {
  const [index, setIndex] = useState(0);

  const handleSelect = (selectedIndex) => {
    setIndex(selectedIndex);
  };

  return (
    <Carousel activeIndex={index} onSelect={handleSelect}>
      {slides.map((slide, i) => (
        <Carousel.Item key={i} style={{ height: height }}>
          <div style={{ height: '100%' ,width: '100%',overflow: 'hidden'}}>
            <img
            src={slide.image}
            alt={slide.title}
            style={{height: '100%', width: '100%', objectFit: 'cover' }}
          />
          </div>

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
