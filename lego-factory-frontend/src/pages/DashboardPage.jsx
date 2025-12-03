import { useEffect, useState } from "react";
import axios from "axios";

function DashboardPage() {
  const [status, setStatus] = useState("Fetching service status...");

  useEffect(() => {
    async function fetchStatus() {
      try {
        const response = await axios.get("/api/status");
        setStatus(`API gateway responded with: ${response.status}`);
      } catch (error) {
        setStatus("Gateway not reachable yet. Keep services running.");
      }
    }

    fetchStatus();
  }, []);

  return (
    <section>
      <h2>Factory Dashboard</h2>
      <p>{status}</p>
      <p>
        Replace this placeholder with role-aware dashboards, task queues, and
        production insights as additional backlog items are delivered.
      </p>
    </section>
  );
}

export default DashboardPage;
