import { useEffect, useState } from "react";
import axios from "axios";

function DashboardPage() {
  const [status, setStatus] = useState("Initializing...");

  useEffect(() => {
    async function fetchStatus() {
      try {
        // Check if gateway is responding by calling a public endpoint
        const response = await axios.get("/api/masterdata/workstations");
        setStatus(`✅ API gateway is operational (${response.data?.length || 0} workstations available)`);
      } catch (error) {
        setStatus("⚠️ Gateway not fully reachable. Check service status.");
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
