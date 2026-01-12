import MainPanel from "./MainPanel";
import { Outlet } from "react-router-dom";

const MainLayout = () => {
  return (
    <MainPanel>
      <Outlet />
    </MainPanel>
  );
};

export default MainLayout;
