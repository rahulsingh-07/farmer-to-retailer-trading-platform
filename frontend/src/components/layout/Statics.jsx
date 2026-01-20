import React, { useEffect, useState } from "react";
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
} from "recharts";
import "./Statics.css";
import { landingPageStats } from "../../services/authService";

const COLORS = ["#2563eb", "#16a34a", "#f59e0b"];

// number formatter for labels & tooltip
const formatNumber = (num) => num.toLocaleString("en-IN") + "+";

export default function Statics() {
  const [statsData, setStatsData] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await landingPageStats();
        setStatsData(data);
      } catch (error) {
        console.error("Failed to fetch stats", error);
      }
    };

    fetchStats();
  }, []);

  if (!statsData) return <p>Loading...</p>;

  const pieData = [
    { id: 1, name: "Total Farmers", value: statsData.totalFarmer },
    { id: 2, name: "Total Retailers", value: statsData.totalRetailer },
    { id: 3, name: "Trades Completed", value: statsData.totalTrades },
  ];

  return (
    <section className="stats-section">
      {/* LEFT CONTENT */}
      <div className="stats-copy">
        <h1>What We Do</h1>
        <p>
          We build a digital agricultural marketplace that directly connects
          farmers and retailers, eliminating intermediaries and ensuring fair
          pricing, transparency, and faster transactions.
        </p>
        <p>
          Our platform empowers farmers to sell at competitive market prices
          while enabling retailers to source fresh crops directly from verified
          producers. Secure payments and real-time data make the entire trading
          process efficient and trustworthy.
        </p>
      </div>

      {/* RIGHT CHART */}
      <div className="stats-chart">
        <ResponsiveContainer width="100%" height={320}>
          <PieChart>
            <Pie
              data={pieData}
              dataKey="value"
              nameKey="name"
              outerRadius={110}
              innerRadius={55}
              paddingAngle={3}
              label={({ value }) => formatNumber(value)}
            >
              {pieData.map((_, index) => (
                <Cell
                  key={index}
                  fill={COLORS[index % COLORS.length]}
                />
              ))}
            </Pie>
            <Tooltip formatter={(value) => formatNumber(value)} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </section>
  );
}
