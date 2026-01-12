import React from "react";
import { useNavigate } from "react-router-dom";
import Button from "../../components/common/Button";

const Unauthorized = () => {
  const navigate = useNavigate();

  return (
    <div style={styles.container}>
      <h1>403 - Access Denied</h1>
      <p>You do not have permission to access this page.</p>

      <Button label="Go Back" onClick={() => navigate(-1)}>
      </Button>
    </div>
  );
};

const styles = {
  container: {
    height: "100vh",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    gap: "16px",
  },
};

export default Unauthorized;
