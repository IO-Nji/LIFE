import { useState, useEffect } from 'react';
import api from '../api/api';
import './ProductsPage.css';

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [modules, setModules] = useState([]);
  const [parts, setParts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [productsRes, modulesRes, partsRes] = await Promise.all([
        api.get('/api/masterdata/product-variants'),
        api.get('/api/masterdata/modules'),
        api.get('/api/masterdata/parts')
      ]);
      setProducts(productsRes.data);
      setModules(modulesRes.data);
      setParts(partsRes.data);
    } catch (err) {
      console.error('Error fetching data:', err);
      setError(err.response?.data?.message || 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const toggleModuleExpand = (moduleId) => {
    setExpandedModules(prev => ({
      ...prev,
      [moduleId]: !prev[moduleId]
    }));
  };

  const getModulePartsForProduct = (product) => {
    // In a real scenario, these would come from a junction table
    // For now, we'll show all modules as available for this product
    return modules;
  };

  const getPartsForModule = (module) => {
    // In a real scenario, these would come from a junction table
    // For now, we'll show all parts as available for this module
    return parts;
  };

  if (loading) {
    return (
      <div style={{ padding: '20px', textAlign: 'center' }}>
        <p>Loading products...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '20px', color: 'red' }}>
        <p>Error: {error}</p>
        <button onClick={fetchAllData}>Retry</button>
      </div>
    );
  }

  return (
    <div className="products-container">
      <div className="products-header">
        <h1>Products Catalog</h1>
        <p className="products-subtitle">LEGO Product Variants: {products.length} | Modules: {modules.length} | Parts: {parts.length}</p>
      </div>

      {selectedProduct ? (
        <div className="product-details">
          <button className="btn-back" onClick={() => setSelectedProduct(null)}>← Back to Products</button>
          
          <div className="product-info">
            <h2>{selectedProduct.name}</h2>
            <p className="product-desc">{selectedProduct.description}</p>
            <div className="product-specs">
              <div className="spec-item">
                <strong>Price:</strong> <span className="price">${selectedProduct.price.toFixed(2)}</span>
              </div>
              <div className="spec-item">
                <strong>Est. Time:</strong> <span>{selectedProduct.estimatedTimeMinutes} minutes</span>
              </div>
            </div>
          </div>

          <div className="components-section">
            <h3>Product Components</h3>
            
            <div className="modules-list">
              {getModulePartsForProduct(selectedProduct).length === 0 ? (
                <p className="empty-message">No components available for this product</p>
              ) : (
                getModulePartsForProduct(selectedProduct).map(module => (
                  <div key={module.id} className="module-card">
                    <div 
                      className="module-header"
                      onClick={() => toggleModuleExpand(module.id)}
                    >
                      <span className="expand-icon">
                        {expandedModules[module.id] ? '▼' : '▶'}
                      </span>
                      <div className="module-info">
                        <h4>{module.name}</h4>
                        <p className="module-type">{module.type || 'COMPONENT'}</p>
                      </div>
                    </div>

                    {expandedModules[module.id] && (
                      <div className="module-content">
                        <p className="module-description">{module.description}</p>
                        
                        <div className="parts-sublist">
                          <h5>Parts Required:</h5>
                          {getPartsForModule(module).length === 0 ? (
                            <p className="empty-message">No parts specified</p>
                          ) : (
                            <ul>
                              {getPartsForModule(module).map(part => (
                                <li key={part.id} className="part-item">
                                  <span className="part-name">{part.name}</span>
                                  <span className="part-category">{part.category}</span>
                                  <span className="part-cost">${part.unitCost?.toFixed(2) || '0.00'}</span>
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="products-grid">
          {products.length === 0 ? (
            <p className="empty-message">No products available</p>
          ) : (
            products.map(product => (
              <div 
                key={product.id} 
                className="product-card"
                onClick={() => setSelectedProduct(product)}
              >
                <div className="product-card-content">
                  <h3>{product.name}</h3>
                  <p className="card-description">{product.description}</p>
                  <div className="card-specs">
                    <span className="price-badge">${product.price.toFixed(2)}</span>
                    <span className="time-badge">{product.estimatedTimeMinutes}m</span>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
