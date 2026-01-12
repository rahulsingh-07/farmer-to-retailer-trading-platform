import React from 'react'
import PropTypes from 'prop-types'
import './LandingBtn.css'

const defaultIcon = (
  <svg
    className="landing-btn__icon"
    viewBox="0 0 16 19"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
    focusable="false"
  >
    <path
      className="landing-btn__icon-path"
      d="M7 18C7 18.5523 7.44772 19 8 19C8.55228 19 9 18.5523 9 18H7ZM8.70711 0.292893C8.31658 -0.0976311 7.68342 -0.0976311 7.29289 0.292893L0.928932 6.65685C0.538408 7.04738 0.538408 7.68054 0.928932 8.07107C1.31946 8.46159 1.95262 8.46159 2.34315 8.07107L8 2.41421L13.6569 8.07107C14.0474 8.46159 14.6805 8.46159 15.0711 8.07107C15.4616 7.68054 15.4616 7.04738 15.0711 6.65685L8.70711 0.292893ZM9 18L9 1H7L7 18H9Z"
    />
  </svg>
)

export default function LandingBtn({ label = 'Explore', icon, type = 'button', className = '', ...props }) {
  const resolvedIcon = React.isValidElement(icon)
    ? React.cloneElement(icon, {
        className: [icon.props.className, 'landing-btn__icon'].filter(Boolean).join(' '),
      })
    : defaultIcon

  return (
    <button type={type} className={[ 'landing-btn', className ].filter(Boolean).join(' ')} {...props}>
      <span className="landing-btn__label">{label}</span>
      {resolvedIcon}
    </button>
  )
}

LandingBtn.propTypes = {
  label: PropTypes.node,
  icon: PropTypes.node,
  type: PropTypes.string,
  className: PropTypes.string,
}

