import React from 'react'
import PropTypes from 'prop-types'
import './Button.css'
import { useNavigate } from "react-router-dom";

const defaultIcon = (
  <svg className="svgIcon" viewBox="0 0 576 512" aria-hidden="true" focusable="false">
    <path d="M512 80c8.8 0 16 7.2 16 16v32H48V96c0-8.8 7.2-16 16-16H512zm16 144V416c0 8.8-7.2 16-16 16H64c-8.8 0-16-7.2-16-16V224H528zM64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H512c35.3 0 64-28.7 64-64V96c0-35.3-28.7-64-64-64H64zm56 304c-13.3 0-24 10.7-24 24s10.7 24 24 24h48c13.3 0 24-10.7 24-24s-10.7-24-24-24H120zm128 0c-13.3 0-24 10.7-24 24s10.7 24 24 24H360c13.3 0 24-10.7 24-24s-10.7-24-24-24H248z"></path>
  </svg>
)


export default function Button({
  label = 'Pay',
  icon,
  type = 'button',
  bgColor,
  hoverBgColor,
  style,
  to,              
  onClick,
  ...props
}) {
  const navigate = useNavigate(); 

  const styleVars = {
    ...(bgColor ? { '--btn-bg': bgColor } : {}),
    ...(hoverBgColor ? { '--btn-bg-hover': hoverBgColor } : {}),
  }
  const handleClick = (e) => {
    if (onClick) onClick(e);     // allow custom logic
    if (to) navigate(to);        // handle navigation
  };

  const resolvedIcon = React.isValidElement(icon)
    ? React.cloneElement(icon, {
        className: [icon.props.className, 'svgIcon'].filter(Boolean).join(' '),
      })
    : defaultIcon

  return (
    <button type={type} className="Btn" style={{ ...styleVars, ...style }} onClick={handleClick} {...props}>
      {label}
      {resolvedIcon}
    </button>
  )
}


Button.propTypes = {
  label: PropTypes.node,
  icon: PropTypes.node,
  type: PropTypes.string,
  bgColor: PropTypes.string,
  hoverBgColor: PropTypes.string,
  style: PropTypes.object,
  style: PropTypes.object,
  to: PropTypes.string,        
  onClick: PropTypes.func,

}
