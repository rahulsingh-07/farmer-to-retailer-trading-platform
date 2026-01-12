import React, { Children } from 'react'
import Sidebar from './Sidebar'
import './MainPanel.css'


export default function MainPanel({ children }) {
  return (
    <div className="main-panel">
      <section>
        <Sidebar />
      </section>
      <section className="main-content">
        {children}
      </section>
    </div>
  );
}
