import { Link } from "react-router-dom";

function HomePage() {
  return (
    <section>
      <h2>Welcome to the LEGO Factory Prototype</h2>
      <p>
        This frontend will coordinate authentication, dashboards, and workflow
        screens across the microservices in the LEGO Sample Factory control system.
      </p>
      <p>
        Use the navigation to explore the app. As services come online we can add
        routes that call backend APIs with <code>axios</code>.
      </p>
      <Link className="primary-link" to="/dashboard">
        Go to dashboard
      </Link>
    </section>
  );
}

export default HomePage;
