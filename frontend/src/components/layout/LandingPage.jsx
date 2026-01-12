import React from 'react'
import './LandingPage.css'
import landingHero from '../../assets/langingPage.jpg'
import Button from '../common/Button'
import { UserLock, MapPin, Mail, Phone, UserRoundPlus, Link, UserRoundCog } from 'lucide-react'
import Workflow from './WorkFlow'
import Statics from './Statics'

export default function LandingPage() {
  return (
    <div className="landing-page">
      <section id="hero" className="hero" style={{ backgroundImage: `url(${landingHero})` }}>
        <div className="hero__overlay" />
        <div className="hero__content">
          <p className="eyebrow">Farm-to-retail</p>
          <h1 className="hero__title">Trade fresh, faster, together</h1>
          <p className="hero__subtitle">
            Farmers list harvests, retailers secure supply, and both track every step with clarity and speed.
          </p>
        </div>
        
      <div className="hero__cta">
        { localStorage.getItem("token") ? (
       <div className='welcome_msg'>
       <UserRoundCog size={30} strokeWidth={3} /> Welcome <br />
        Back
       </div>
    ) : (
        <>
        <Button
          label="Login"
          icon={<UserLock />}
          bgColor="#000309ff"
          hoverBgColor="#111827"
          to="/login"
        />
        <div className='register_btn'>
          <Button
            label='Register as Farmer'
            icon={<UserRoundPlus />}
            bgColor="#083805ff"
            to="/registerFarmer"
          />
          <Button
            label='Register as Retailer'
            icon={<UserRoundPlus />}
            bgColor="#7f3c05ff"
            to="/registerRetailer"
          />
        </div>
        </>
         )} 
      </div>
    
        
      </section>

      <section id="statics">
        <Statics />
      </section>

      <section id="workflow">
        <Workflow />
      </section>
      <section id="contact" className="contact">
        <div className="contact__content">
          <p className='headline'>Get in touch</p>
          <h2>Let’s connect farmers and retailers</h2>
          <p className="contact__lead">
            Reach out for onboarding, partnerships, or support. We’ll help you go from harvest to shelf smoothly.
          </p>
          <div className="contact__grid">
            <div className='contact__grid_div'>
              <h4><Mail /></h4>
              <a href="mailto:hello@freshchain.com" className="contact__link">hello@freshchain.com</a>
            </div>
            <div className='contact__grid_div'>
              <h4><Phone /></h4>
              <a href="tel:+1234567890" className="contact__link">+1 (234) 567-890</a>
            </div>
            <div className='contact__grid_div'>
              <h4><MapPin /></h4>
              <p className="contact__text">Main Market Road, Pune, MH</p>
            </div>
          </div>
          
        </div>
        
      </section>
    </div>
  )
}
