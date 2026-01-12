import Navbar from "./Navbar";
import { Outlet } from "react-router-dom";
import ScrollToSection from "../../utils/ScrollToSection";

const PublicLayout = () => {
  return (
    <>
      <ScrollToSection />
      <Navbar />
      <Outlet />
    </>
  );
};

export default PublicLayout;
