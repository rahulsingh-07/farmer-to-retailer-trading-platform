import React from "react";
import AppRoutes from "./app/routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import AgriChatbot from "./features/common/AgriChatbot";

const App = () => {
  return (
    <>
      <AppRoutes />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        newestOnTop
        theme="light"
      />
      <AgriChatbot/>
    </>
  );
};

export default App;
